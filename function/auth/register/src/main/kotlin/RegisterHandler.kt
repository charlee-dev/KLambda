import component.common.Handler
import component.common.feature.auth.JwtService
import component.common.feature.user.UserRepository

// Access point for AWS Lambda
@Suppress("unused")
class RegisterHandler : Handler(
    usecase = { RegisterUsecase(get<UserRepository>(), get<JwtService>()) },
    routeSpec = routeSpec,
    isSecure = false,
)
