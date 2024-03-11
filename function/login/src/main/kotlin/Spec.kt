import component.common.RouteSpec
import component.common.feature.user.User
import component.common.feature.user.authResultFailureSample
import component.common.feature.user.authResultLens
import component.common.feature.user.authResultSuccessSample
import org.http4k.contract.meta
import org.http4k.core.Method.POST
import org.http4k.core.Status
import org.http4k.lens.Query
import org.http4k.lens.string

val emailQuery = Query.string().required("email")
val passwordQuery = Query.string().required("password")

val lens = functionJson.autoBody<LoginInput>().toLens()
val sample = LoginInput(email = User.EMAIL_SAMPLE, password = User.PASSWORD_SAMPLE)

internal val routeSpec: RouteSpec = "/login" meta {
    operationId = "login"
    summary = "Login user with email and password"
    returning(Status.OK, authResultLens to authResultSuccessSample)
    returning(Status.FORBIDDEN, authResultLens to authResultFailureSample)
} bindContract POST
