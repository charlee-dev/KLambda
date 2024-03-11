import component.common.core.Usecase
import component.common.feature.auth.JwtService
import component.common.feature.auth.model.AuthResult
import component.common.feature.user.UserRepository
import component.common.feature.user.model.EmailInput
import component.common.feature.user.model.NameInput
import component.common.feature.user.model.PasswordInput
import component.common.feature.user.model.User
import component.common.util.CryptoUtils.generatePasswordHash
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.asResultOr
import dev.forkhandles.result4k.get
import dev.forkhandles.result4k.map
import dev.forkhandles.result4k.mapFailure
import dev.forkhandles.values.ofResult4k
import org.http4k.core.HttpHandler
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import java.time.Instant
import java.util.UUID

internal class RegisterUsecase(
    private val userRepository: UserRepository,
    private val jwtService: JwtService,
) : Usecase {
    override fun handler(): HttpHandler = { request ->
        val input = RegisterInput.lens(request)
        register(input.email, input.password, input.name)
            .map {
                Response(Status.CREATED)
                    .with(AuthResult.lens of AuthResult.success(jwtService.createToken(input.password, it)))
            }
            .mapFailure { it.toResponse() }
            .get()
    }

    private fun register(email: String, password: String, name: String): Result4k<User, LambdaError> {
        validateInput(email, password, name).mapFailure { return Failure(it) }
        validateUserWithEmailNotExists(email).mapFailure { return Failure(it) }

        val user = createUser(password, email, name)
        return Success(user)
    }

    private fun createUser(
        password: String,
        email: String,
        name: String
    ): User {
        val id = UUID.randomUUID()
        val encryptedSecret = jwtService.createEncryptedSecret(password)
        val passwordHash = generatePasswordHash(password)
        val user = userRepository.create(
            User(
                id = id,
                email = email,
                passwordHash = passwordHash,
                encryptedSecret = encryptedSecret,
                name = name,
                lastLogin = Instant.now()
            )
        )
        return user
    }

    private fun validateInput(email: String, password: String, name: String): Result4k<Unit, LambdaError> {
        EmailInput.ofResult4k(email).mapFailure { return Failure(InvalidEmail) }
        PasswordInput.ofResult4k(password).asResultOr { return Failure(InvalidPassword) }
        NameInput.ofResult4k(name).asResultOr { return Failure(InvalidName) }
        return Success(Unit)
    }

    private fun validateUserWithEmailNotExists(email: String): Result4k<Unit, LambdaError> {
        userRepository.findByEmail(email)?.let { return Failure(UserAlreadyExists) }
        return Success(Unit)
    }
}


sealed class LambdaError(val message: String)
data object RegisterFailed : LambdaError("Login failed")
data object InvalidEmail : LambdaError("Invalid email")
data object InvalidPassword : LambdaError("Invalid password")
data object InvalidName : LambdaError("Invalid name")
data object UserAlreadyExists : LambdaError("User already exists")

fun LambdaError.toResponse() = when (this) {
    InvalidEmail,
    InvalidPassword,
    InvalidName -> Response(Status.BAD_REQUEST)

    RegisterFailed -> Response(Status.FORBIDDEN)
    UserAlreadyExists -> Response(Status.CONFLICT)
}.with(AuthResult.lens of AuthResult.failure(message))
