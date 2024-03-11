import component.common.RouteSpec
import component.common.feature.auth.model.AuthResult
import org.http4k.contract.meta
import org.http4k.core.Method.POST
import org.http4k.core.Status

internal val routeSpec: RouteSpec = "/login" meta {
    operationId = "auth_login"
    summary = "Login user with email and password"
    with(LoginInput) {
        receiving(lens to sample)
    }
    with(AuthResult) {
        returning(Status.OK, lens to success(TOKEN_SAMPLE))
        returning(Status.FORBIDDEN, lens to failure())
    }
} bindContract POST
