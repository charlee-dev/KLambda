import component.common.core.Usecase
import component.common.feature.auth.JwtService
import component.common.feature.auth.model.AuthResult
import component.common.feature.user.UserRepository
import component.common.feature.user.model.User
import component.common.util.CryptoUtils.generatePasswordHash
import component.common.util.CryptoUtils.verifyPasswordHash
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.get
import dev.forkhandles.result4k.map
import dev.forkhandles.result4k.mapFailure
import org.http4k.core.HttpHandler
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import java.time.Instant


internal class LoginUsecase(
    private val userRepository: UserRepository,
    private val jwtService: JwtService,
) : Usecase {
    override fun handler(): HttpHandler = { request ->
        val input = LoginInput.lens(request)
        login(input.email, input.password)
            .map {
                Response(Status.OK)
                    .with(AuthResult.lens of AuthResult.success(jwtService.createToken(input.password, it)))
            }
            .mapFailure { it.toResponse() }
            .get()
    }

    private fun login(email: String, password: String): Result4k<User, LambdaError> {
//        EmailInput.ofResult4k(email).mapFailure { Failure(LoginFailed) }
//        PasswordInput.ofResult4k(password).asResultOr { Failure(LoginFailed) }

        val user = userRepository.findByEmail(email) ?: run {
            // perform the same amount of work when the username was wrong
            // (so the timing of the login will be the same for a wrong username and a wrong password)
            generatePasswordHash("dummy")
            return Failure(LoginFailed)
        }

        if (!verifyPasswordHash(password, user.passwordHash)) return Failure(LoginFailed)

        userRepository.update(user.copy(lastLogin = Instant.now()))
        return Success(user)
    }
}


sealed class LambdaError(val message: String)

// Make all errors the same to not leak information about the user
data object LoginFailed : LambdaError("Login failed")

fun LambdaError.toResponse() = when (this) {
    LoginFailed -> Response(Status.FORBIDDEN)
}.with(AuthResult.lens of AuthResult.failure(message))
