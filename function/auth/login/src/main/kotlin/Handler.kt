import Input.Companion.inputLens
import component.common.LambdaHandler
import component.common.feature.auth.model.AuthResult
import component.common.feature.auth.model.AuthResult.Companion.TOKEN_SAMPLE
import component.common.feature.auth.model.AuthResult.Companion.failure
import component.common.feature.auth.model.AuthResult.Companion.outputLens
import component.common.feature.user.UserRepository
import component.common.feature.user.model.EmailInput
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
import org.http4k.core.Method
import org.http4k.core.Response
import org.http4k.core.Status.Companion.FORBIDDEN
import org.http4k.core.Status.Companion.OK
import org.http4k.core.with
import java.time.Instant

// Access point for AWS Lambda
@Suppress("unused")
class Handler : LambdaHandler(
    isSecure = false,
    route = { route(get()) },
)

fun route(userRepository: UserRepository): ContractRoute = "/login" meta {
    operationId = "auth_login"
    summary = "Login user with email and password"
    with(Input) {
        receiving(inputLens to sample)
    }
    with(AuthResult) {
        returning(OK, outputLens to success(TOKEN_SAMPLE))
        returning(FORBIDDEN, outputLens to failure())
    }
} bindContract Method.POST to { request ->
    inputLens(request).let { input ->
        validateInput(input)
            .flatMap { userRepository.login(input.email, input.password) }
            .map { Response(OK).with(AuthResult.outputLens of AuthResult.success(TOKEN_SAMPLE)) }
            .mapFailure { it.toResponse() }
            .get()
    }
}

private fun validateInput(input: Input) =
    mutableListOf<LambdaError>().apply {
        EmailInput(input.email).asResultOr { add(InvalidEmail) }
        PasswordInput(input.password).asResultOr { add(InvalidPassword) }
    }
        .takeIf { it.isNotEmpty() }
        ?.let { Failure(it) }
        ?: Success(input)

private fun UserRepository.login(email: String, password: String): Result4k<User, List<LambdaError>> {
    val user = findByEmail(email) ?: run {
        // perform the same amount of work when the username was wrong
        // (so the timing of the login will be the same for a wrong username and a wrong password)
        CryptoUtils.generatePasswordHash("dummy")
        return Failure(listOf(LoginFailed))
    }

    if (!CryptoUtils.verifyPasswordHash(password, user.passwordHash)) return Failure(listOf(LoginFailed))

    update(
        id = user.id,
        name = null,
        email = null,
        password = null,
        lastLogin = Instant.now()
    )
    return Success(user)
}

private sealed class LambdaError(val message: String)
private data object LoginFailed : LambdaError("Login failed")
private data object InvalidEmail : LambdaError("Invalid email")
private data object InvalidPassword : LambdaError("Invalid password")

private fun List<LambdaError>.toResponse() = this
    .joinToString(", ") { it.message }
    .let { message -> Response(FORBIDDEN).with(outputLens of failure(message)) }
