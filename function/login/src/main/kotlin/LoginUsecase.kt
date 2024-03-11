import component.common.core.Usecase
import component.common.feature.auth.JwtService
import component.common.feature.user.AuthResult
import component.common.feature.user.User
import component.common.feature.user.UserRepository
import component.common.feature.user.authResultLens
import component.common.util.CryptoUtils.generatePasswordHash
import component.common.util.CryptoUtils.verifyPasswordHash
import org.http4k.core.HttpHandler
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import java.time.Instant

private fun success(token: String? = null) = AuthResult(success = true, token = token)
private fun failure() = AuthResult(success = false, message = "Authentication failed")

internal class LoginUsecase(
    private val userRepository: UserRepository,
    private val jwtService: JwtService,
) : Usecase {
    override fun handler(): HttpHandler = { request ->
        val (email, password) = lens(request)
        login(email, password)
            ?.let { Response(Status.OK).with(authResultLens of success(jwtService.createToken(password, it))) }
            ?: run { Response(Status.FORBIDDEN).with(authResultLens of failure()) }
    }

    private fun login(username: String, password: String): User? =
        userRepository.findByEmail(username)
            .also {
                // perform the same amount of work when the username was wrong
                // (so the timing of the login will be the same for a wrong username and a wrong password)
                if (it == null) generatePasswordHash("dummy")
            }
            ?.takeIf { verifyPasswordHash(password, it.password) }
            ?.also { userRepository.update(it.copy(lastLogin = Instant.now())) }
}
