package component.common.feature.auth.model

import component.common.util.libJson
import se.ansman.kotshi.JsonSerializable

@JsonSerializable
data class AuthResult(
    val success: Boolean,
    val message: String? = null,
    val token: String? = null
) {
    companion object {
        const val TOKEN_SAMPLE = "token 1234567890"
        val outputLens = libJson.autoBody<AuthResult>().toLens()
        fun success(token: String? = null) = AuthResult(success = true, token = token)
        fun failure(message: String? = "Authentication failed") = AuthResult(success = false, message = message)
    }
}
