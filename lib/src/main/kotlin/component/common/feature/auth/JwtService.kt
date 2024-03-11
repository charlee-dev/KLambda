package component.common.feature.auth

import component.common.feature.user.model.User
import component.common.util.CryptoUtils
import component.common.util.CryptoUtils.decrypt

interface JwtService {
    fun createToken(password: String, user: User): String
    fun verifyPasswordHash(password: String, hash: String): Boolean
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

    override fun verifyPasswordHash(password: String, hash: String): Boolean {
        return CryptoUtils.verifyPasswordHash(password, hash)
    }

    override fun generatePasswordSecret(password: String): String {
        return CryptoUtils.generatePasswordHash(password)
    }
}
