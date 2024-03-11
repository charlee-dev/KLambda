import component.common.RouteSpec
import component.common.feature.auth.model.AuthResult
import org.http4k.contract.meta
import org.http4k.core.Method.POST
import org.http4k.core.Status

internal val routeSpec: RouteSpec = "/register" meta {
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
} bindContract POST
