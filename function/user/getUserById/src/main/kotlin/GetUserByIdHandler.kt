import GetUserByIdResult.Companion.failure
import GetUserByIdResult.Companion.lens
import component.common.Handler
import component.common.feature.user.UserRepository
import dev.forkhandles.result4k.get
import dev.forkhandles.result4k.map
import dev.forkhandles.result4k.mapFailure
import org.http4k.contract.ContractRoute
import org.http4k.contract.meta
import org.http4k.core.Method
import org.http4k.core.Response
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.Status.Companion.OK
import org.http4k.core.with
import org.http4k.lens.Query
import org.http4k.lens.string
import org.koin.core.Koin

// Access point for AWS Lambda
@Suppress("unused")
class GetUserByIdHandler : Handler(
    route = ::route,
    isSecure = false,
)

val idQueryLens = Query.string().required("id")

context(Koin)
fun route(): ContractRoute {
    val userRepository: UserRepository by inject()
    val usecase = Usecase(userRepository)

    return "/user" meta {
        operationId = "user_getUserById"
        summary = "Get User By Id"
        queries += idQueryLens

        with(GetUserByIdResult) {
            returning(OK, lens to success)
            returning(NOT_FOUND)
        }
    } bindContract Method.GET to { request ->
        val id = idQueryLens(request)
        usecase.getUserById(id)
            .map { getUserByIdResult -> Response(OK).with(lens of getUserByIdResult) }
            .mapFailure { it.toResponse() }
            .get()
    }
}

private fun LambdaError.toResponse() = when (this) {
    UserNotFound -> Response(NOT_FOUND)
    InvalidId -> Response(BAD_REQUEST)
}.with(lens of failure(message))
