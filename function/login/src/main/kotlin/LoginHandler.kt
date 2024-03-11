import component.common.Handler

// Access point for AWS Lambda
@Suppress("unused")
class LoginHandler : Handler(
    usecase = { LoginUsecase(userRepository, jwtService) },
    routeSpec = routeSpec,
    isSecure = false,
)
