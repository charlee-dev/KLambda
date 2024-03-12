import component.common.feature.user.UserRepository
import component.common.feature.user.model.UuidInput
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.map
import dev.forkhandles.result4k.valueOrNull
import dev.forkhandles.values.ofResult4k
import java.util.UUID

internal class Usecase(private val userRepository: UserRepository) {
    fun deleteUser(id: String): Result4k<DeleteUserResult, LambdaError> {
        val uuid = UuidInput.ofResult4k(UUID.fromString(id))
            .map { it.value }
            .valueOrNull()
            ?: return Failure(InvalidId)

        userRepository.delete(uuid)
        return Success(DeleteUserResult.success)
    }
}

internal sealed interface LambdaError
internal data object InvalidId : LambdaError
