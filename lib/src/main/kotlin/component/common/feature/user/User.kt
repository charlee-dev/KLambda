package component.common.feature.user

import com.squareup.moshi.Json
import component.common.libJson
import se.ansman.kotshi.JsonSerializable
import java.time.Instant
import java.util.UUID
import javax.crypto.SecretKey

data class User(
    val id: UUID,
    val email: String,
    val password: String,
    val encryptedSecret: String,
    val lastLogin: Instant? = null
) {
    companion object {
        const val EMAIL_SAMPLE = "john.smith@gmail.com"
        const val PASSWORD_SAMPLE = "password"
    }
}

data class UserContext(
    val userId: UUID,
    val encryptionKey: SecretKey
)

data class RegisterInput(
    @Json(name = "name") val name: String,
    @Json(name = "email") val email: String,
    @Json(name = "password") val password: String,
)

@JsonSerializable
data class RegisterResult(
    val token: String
)

@JsonSerializable
data class AuthResult(
    val success: Boolean,
    val message: String? = null,
    val token: String? = null
) {
    companion object {
        const val TOKEN_SAMPLE = "token 1234567890"
        val lens = libJson.autoBody<AuthResult>().toLens()
        fun success(token: String? = null) = AuthResult(success = true, token = token)
        fun failure(message: String? = "Authentication failed") = AuthResult(success = false, message = message)
    }
}
