import Output.Companion.failure
import Output.Companion.outputLens
import component.common.LambdaHandler
import component.common.feature.user.UserRepository
import component.common.feature.user.model.User.Companion.idQueryLens
import component.common.feature.user.model.UuidInput
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.flatMap
import dev.forkhandles.result4k.get
import dev.forkhandles.result4k.map
import dev.forkhandles.result4k.mapFailure
import dev.forkhandles.result4k.valueOrNull
import dev.forkhandles.values.ofResult4k
import org.http4k.contract.ContractRoute
import org.http4k.contract.meta
import org.http4k.core.Method
import org.http4k.core.Response
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.OK
import org.http4k.core.with
import java.util.UUID

// Access point for AWS Lambda
@Suppress("unused")
class Handler : LambdaHandler(
    route = { route(get()) },
    isSecure = false,
)

internal fun route(userRepository: UserRepository): ContractRoute = "/user" meta {
    operationId = "getUserById"
    summary = "Get User By Id"
    queries += idQueryLens

    with(Output) {
        returning(OK, outputLens to success)
        returning(BAD_REQUEST, outputLens to failure)
    }
} bindContract Method.GET to { request ->
    validateInput(idQueryLens(request))
        .flatMap { userRepository.deleteUser(it) }
        .map { Response(OK).with(outputLens of it) }
        .mapFailure { it.toResponse() }
        .get()
}

private fun validateInput(id: String): Result4k<UUID, LambdaError> =
    UuidInput.ofResult4k(UUID.fromString(id))
        .valueOrNull()
        ?.let { Success(it.value) }
        ?: Failure(InvalidId)

private fun UserRepository.deleteUser(id: UUID): Result4k<Output, LambdaError> =
    delete(id).let { Success(Output.success) }

private sealed interface LambdaError
private data object InvalidId : LambdaError

private fun LambdaError.toResponse() = when (this) {
    InvalidId -> Response(BAD_REQUEST)
}.with(outputLens of failure)
