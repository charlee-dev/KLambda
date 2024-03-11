import component.common.Handler
import component.common.feature.auth.JwtService
import component.common.feature.auth.model.AuthResult
import component.common.feature.user.UserRepository
import dev.forkhandles.result4k.get
import dev.forkhandles.result4k.map
import dev.forkhandles.result4k.mapFailure
import org.http4k.contract.ContractRoute
import org.http4k.contract.meta
import org.http4k.core.Method
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import org.koin.core.Koin

// Access point for AWS Lambda
@Suppress("unused")
class LoginHandler : Handler(
    isSecure = false,
    route = ::route,
)

context(Koin)
fun route(): ContractRoute {
    val userRepository: UserRepository by inject()
    val jwtService: JwtService by inject()
    val usecase = Usecase(userRepository)

    return "/login" meta {
        operationId = "auth_login"
        summary = "Login user with email and password"
        with(LoginInput) {
            receiving(lens to sample)
        }
        with(AuthResult) {
            returning(Status.OK, lens to success(TOKEN_SAMPLE))
            returning(Status.FORBIDDEN, lens to failure())
        }
    } bindContract Method.POST to { request ->
        val input = LoginInput.lens(request)
        usecase.login(input.email, input.password)
            .map {
                Response(Status.OK)
                    .with(AuthResult.lens of AuthResult.success(jwtService.createToken(input.password, it)))
            }
            .mapFailure { it.toResponse() }
            .get()
    }
}

private fun LambdaError.toResponse() = when (this) {
    LoginFailed -> Response(Status.FORBIDDEN)
}.with(AuthResult.lens of AuthResult.failure(message))
