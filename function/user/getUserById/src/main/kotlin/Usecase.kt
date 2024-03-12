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
    fun getUserById(id: String): Result4k<GetUserByIdResult, LambdaError> {
        val uuid = UuidInput.ofResult4k(UUID.fromString(id))
            .map { it.value }
            .valueOrNull()
            ?: return Failure(InvalidId)

        val user = userRepository.findById(uuid) ?: return Failure(UserNotFound)

        val getUserByIdResult = GetUserByIdResult(
            userProfile = UserProfile(
                id = user.id.toString(),
                email = user.email,
                name = user.name,
                lastLogin = user.lastLogin
            )
        )
        return Success(getUserByIdResult)
    }
}

internal sealed class LambdaError(val message: String)
internal data object InvalidId : LambdaError("Invalid id")
internal data object UserNotFound : LambdaError("User not found")
