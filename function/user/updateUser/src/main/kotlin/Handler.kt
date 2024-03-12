import Input.Companion.inputLens
import Output.Companion.failure
import Output.Companion.outputLens
import Output.Companion.success
import component.common.LambdaHandler
import component.common.feature.user.UserRepository
import component.common.feature.user.model.EmailInput
import component.common.feature.user.model.NameInput
import component.common.feature.user.model.PasswordInput
import component.common.feature.user.model.User.Companion.idQueryLens
import component.common.feature.user.model.UuidInput
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.asResultOr
import dev.forkhandles.result4k.flatMap
import dev.forkhandles.result4k.get
import dev.forkhandles.result4k.map
import dev.forkhandles.result4k.mapFailure
import dev.forkhandles.values.ofResult4k
import org.http4k.contract.ContractRoute
import org.http4k.contract.meta
import org.http4k.core.Method.PATCH
import org.http4k.core.Response
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.OK
import org.http4k.core.with
import org.koin.core.Koin
import java.util.UUID

// Access point for AWS Lambda
@Suppress("unused")
class Handler : LambdaHandler(
    route = ::route,
    isSecure = false,
)

context(Koin)
fun route(): ContractRoute {
    val userRepository: UserRepository by inject()

    return "/user" meta {
        operationId = "user_update"
        summary = "Get User By Id"
        queries += idQueryLens

        with(Input) {
            receiving(inputLens to inputSample)
        }
        with(Output) {
            returning(OK, outputLens to success)
            returning(BAD_REQUEST, outputLens to failure())
        }
    } bindContract PATCH to { request ->
        validateInput(inputLens(request))
            .flatMap { userRepository.updateUser(it) }
            .map { Response(OK).with(outputLens of success) }
            .mapFailure { it.toResponse() }
            .get()
    }
}

private fun validateInput(input: Input): Result4k<Input, List<LambdaError>> =
    mutableListOf<LambdaError>().apply {
        UuidInput.ofResult4k(UUID.fromString(input.id)).asResultOr { add(InvalidId) }
        input.email?.let { EmailInput.ofResult4k(it).asResultOr { add(InvalidEmail) } }
        input.password?.let { PasswordInput.ofResult4k(it).asResultOr { add(InvalidPassword) } }
        input.name?.let { NameInput.ofResult4k(it).asResultOr { add(InvalidName) } }
    }
        .takeIf { it.isNotEmpty() }
        ?.let { Failure(it) }
        ?: Success(input)

private fun UserRepository.updateUser(input: Input): Result4k<Output, List<LambdaError>> {
    val user = update(
        id = UUID.fromString(input.id),
        name = input.name,
        email = input.email,
        password = input.password
    )

    val output = Output(
        updatedUser = UpdatedUser(
            id = user.id.toString(),
            name = user.name,
            email = user.email,
        )
    )
    return Success(output)
}

internal sealed class LambdaError(val message: String)
internal data object InvalidId : LambdaError("Invalid id")
internal data object InvalidEmail : LambdaError("Invalid email")
internal data object InvalidPassword : LambdaError("Invalid password")
internal data object InvalidName : LambdaError("Invalid name")

private fun List<LambdaError>.toResponse() = this
    .joinToString(", ") { it.message }
    .let { message -> Response(BAD_REQUEST).with(outputLens of failure(message)) }
