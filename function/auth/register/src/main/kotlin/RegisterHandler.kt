import component.common.Handler
import component.common.feature.auth.JwtService
import component.common.feature.auth.model.AuthResult
import component.common.feature.user.UserRepository
import dev.forkhandles.result4k.get
import dev.forkhandles.result4k.map
import dev.forkhandles.result4k.mapFailure
import org.http4k.contract.ContractRoute
import org.http4k.contract.meta
import org.http4k.core.Method.POST
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import org.koin.core.Koin

// Access point for AWS Lambda
@Suppress("unused")
class RegisterHandler : Handler(
    isSecure = false,
    route = ::route,
)

context(Koin)
fun route(): ContractRoute {
    val userRepository by inject<UserRepository>()
    val jwtService by inject<JwtService>()
    val usecase = Usecase(userRepository, jwtService)

    return "/register" meta {
        operationId = "auth_register"
        summary = "Register user with email and password"
        with(RegisterInput) {
            receiving(lens to sample)
        }
        with(AuthResult) {
            returning(Status.CREATED, lens to success(TOKEN_SAMPLE))
            returning(Status.FORBIDDEN, lens to failure())
            returning(Status.BAD_REQUEST, lens to failure())
            returning(Status.CONFLICT, lens to failure("User with this email already exists"))
        }
    } bindContract POST to { request ->
        val input = RegisterInput.lens(request)
        usecase.register(input.email, input.password, input.name)
            .map {
                Response(Status.CREATED)
                    .with(AuthResult.lens of AuthResult.success(jwtService.createToken(input.password, it)))
            }
            .mapFailure { it.toResponse() }
            .get()
    }
}

private fun LambdaError.toResponse() = when (this) {
    InvalidEmail,
    InvalidPassword,
    InvalidName -> Response(Status.BAD_REQUEST)

    UserAlreadyExists -> Response(Status.CONFLICT)
}.with(AuthResult.lens of AuthResult.failure(message))
