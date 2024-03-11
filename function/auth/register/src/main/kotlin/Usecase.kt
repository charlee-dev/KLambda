import component.common.feature.auth.JwtService
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
import dev.forkhandles.result4k.mapFailure
import dev.forkhandles.values.ofResult4k
import java.time.Instant
import java.util.UUID

internal class Usecase(
    private val userRepository: UserRepository,
    private val jwtService: JwtService,
) {
    fun register(email: String, password: String, name: String): Result4k<User, LambdaError> {
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


internal sealed class LambdaError(val message: String)
internal data object InvalidEmail : LambdaError("Invalid email")
internal data object InvalidPassword : LambdaError("Invalid password")
internal data object InvalidName : LambdaError("Invalid name")
internal data object UserAlreadyExists : LambdaError("User already exists")
