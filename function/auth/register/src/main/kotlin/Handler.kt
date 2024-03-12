import Input.Companion.inputLens
import component.common.LambdaHandler
import component.common.feature.auth.JwtService
import component.common.feature.auth.model.AuthResult
import component.common.feature.auth.model.AuthResult.Companion.failure
import component.common.feature.auth.model.AuthResult.Companion.outputLens
import component.common.feature.auth.model.AuthResult.Companion.success
import component.common.feature.user.UserRepository
import component.common.feature.user.model.EmailInput
import component.common.feature.user.model.NameInput
import component.common.feature.user.model.PasswordInput
import component.common.feature.user.model.User
import component.common.util.CryptoUtils
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.asResultOr
import dev.forkhandles.result4k.flatMap
import dev.forkhandles.result4k.get
import dev.forkhandles.result4k.map
import dev.forkhandles.result4k.mapFailure
import org.http4k.contract.ContractRoute
import org.http4k.contract.meta
import org.http4k.core.Method.POST
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import java.time.Instant
import java.util.UUID

// Access point for AWS Lambda
@Suppress("unused")
class Handler : LambdaHandler(
    isSecure = false,
    route = { route(get(), get()) },
)

fun route(userRepository: UserRepository, jwtService: JwtService): ContractRoute = "/register" meta {
    operationId = "auth_register"
    summary = "Register user with email and password"
    with(Input) {
        receiving(inputLens to sample)
    }
    with(AuthResult) {
        returning(Status.CREATED, outputLens to success(TOKEN_SAMPLE))
        returning(Status.FORBIDDEN, outputLens to failure())
        returning(Status.BAD_REQUEST, outputLens to failure())
        returning(Status.CONFLICT, outputLens to failure("User with this email already exists"))
    }
} bindContract POST to { request ->
    val token by lazy {
        { password: String, user: User -> jwtService.createToken(password, user) }
    }
    inputLens(request).let { input ->
        validateInput(input)
            .flatMap { userRepository.validateUserWithEmailNotExists(input.email) }
            .flatMap { register(userRepository, jwtService, input) }
            .map { Response(Status.CREATED).with(outputLens of success(token(input.password, it))) }
            .mapFailure { it.toResponse() }
            .get()
    }
}

private fun validateInput(input: Input): Result4k<Input, List<LambdaError>> =
    mutableListOf<LambdaError>().apply {
        EmailInput(input.email).asResultOr { add(InvalidEmail) }
        PasswordInput(input.password).asResultOr { add(InvalidPassword) }
        NameInput(input.name).asResultOr { add(InvalidName) }
    }
        .takeIf { it.isNotEmpty() }
        ?.let { Failure(it) }
        ?: Success(input)


private fun UserRepository.validateUserWithEmailNotExists(email: String): Result4k<Unit, List<LambdaError>> {
    findByEmail(email)?.let { return Failure(listOf(UserAlreadyExists)) }
    return Success(Unit)
}

private fun register(
    userRepository: UserRepository,
    jwtService: JwtService,
    input: Input
): Result4k<User, List<LambdaError>> {
    val id = UUID.randomUUID()
    val encryptedSecret = jwtService.createEncryptedSecret(input.password)
    val passwordHash = CryptoUtils.generatePasswordHash(input.password)
    val user = userRepository.create(
        User(
            id = id,
            email = input.email,
            passwordHash = passwordHash,
            encryptedSecret = encryptedSecret,
            name = input.name,
            lastLogin = Instant.now()
        )
    )

    return Success(user)
}

private sealed class LambdaError(val message: String)
private data object InvalidEmail : LambdaError("Invalid email")
private data object InvalidPassword : LambdaError("Invalid password")
private data object InvalidName : LambdaError("Invalid name")
private data object UserAlreadyExists : LambdaError("User already exists")

private fun List<LambdaError>.toResponse() = this
    .joinToString(", ") { it.message }
    .let { message -> Response(Status.BAD_REQUEST).with(outputLens of failure(message)) }
