import DeleteUserResult.Companion.failure
import DeleteUserResult.Companion.lens
import component.common.LambdaHandler
import component.common.feature.user.UserRepository
import component.common.feature.user.model.User.Companion.idQueryLens
import dev.forkhandles.result4k.get
import dev.forkhandles.result4k.map
import dev.forkhandles.result4k.mapFailure
import org.http4k.contract.ContractRoute
import org.http4k.contract.meta
import org.http4k.core.Method
import org.http4k.core.Response
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.OK
import org.http4k.core.with
import org.koin.core.Koin

// Access point for AWS Lambda
@Suppress("unused")
class Handler : LambdaHandler(
    route = ::route,
    isSecure = false,
)

context(Koin)
fun route(): ContractRoute {
    val userRepository: UserRepository by inject()
    val usecase = Usecase(userRepository)

    return "/user" meta {
        operationId = "getUserById"
        summary = "Get User By Id"
        queries += idQueryLens

        with(DeleteUserResult) {
            returning(OK, lens to success)
            returning(BAD_REQUEST, lens to failure)
        }
    } bindContract Method.GET to { request ->
        val id = idQueryLens(request)
        usecase.deleteUser(id)
            .map { getUserByIdResult -> Response(OK).with(lens of getUserByIdResult) }
            .mapFailure { it.toResponse() }
            .get()
    }
}

private fun LambdaError.toResponse() = when (this) {
    InvalidId -> Response(BAD_REQUEST)
}.with(lens of failure)
