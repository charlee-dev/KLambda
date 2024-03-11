import component.common.feature.user.UserRepository
import component.common.feature.user.model.EmailInput
import component.common.feature.user.model.PasswordInput
import component.common.feature.user.model.User
import component.common.util.CryptoUtils.generatePasswordHash
import component.common.util.CryptoUtils.verifyPasswordHash
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.asResultOr
import dev.forkhandles.result4k.mapFailure
import dev.forkhandles.values.ofResult4k
import java.time.Instant

internal class Usecase(private val userRepository: UserRepository) {
    fun login(email: String, password: String): Result4k<User, LambdaError> {
        EmailInput.ofResult4k(email).mapFailure { Failure(LoginFailed) }
        PasswordInput.ofResult4k(password).asResultOr { Failure(LoginFailed) }

        val user = userRepository.findByEmail(email) ?: run {
            // perform the same amount of work when the username was wrong
            // (so the timing of the login will be the same for a wrong username and a wrong password)
            generatePasswordHash("dummy")
            return Failure(LoginFailed)
        }

        if (!verifyPasswordHash(password, user.passwordHash)) return Failure(LoginFailed)

        userRepository.update(user.copy(lastLogin = Instant.now()))
        return Success(user)
    }
}

internal sealed class LambdaError(val message: String)

// Make all errors the same to not leak information about the user
internal data object LoginFailed : LambdaError("Login failed")
