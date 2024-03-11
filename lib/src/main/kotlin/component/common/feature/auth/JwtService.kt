package component.common.feature.auth

import component.common.feature.user.User
import component.common.util.CryptoUtils.decrypt

interface JwtService {
    fun createToken(password: String, user: User): String
    fun validateToken(token: String, resolvePwdHash: (String) -> String?): String?
    fun generatePasswordSecret(password: String): String
}

internal class JwtServiceImpl(
    private val tokenSupport: TokenSupport,
) : JwtService {
    override fun createToken(password: String, user: User): String {
        val passwordSecret = tokenSupport.generatePasswordSecret(password)
        val userSecret = user.encryptedSecret.decrypt(passwordSecret)
        val token = tokenSupport.createToken(user.id, password.tokenHash(), userSecret)
        return token
    }

    override fun validateToken(token: String, resolvePwdHash: (String) -> String?): String? {
        TODO("Not yet implemented")
    }

    override fun generatePasswordSecret(password: String): String {
        TODO("Not yet implemented")
    }
}
