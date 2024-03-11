package component.common.feature.auth

import component.common.Config
import component.common.EncryptionSecret
import component.common.feature.user.model.UserContext
import component.common.util.CryptoUtils
import component.common.util.toBase64
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import java.security.MessageDigest
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.util.UUID
import javax.crypto.Mac
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

/**
 * Create and validate signed tokens for API authentication. This is similar to JWT, except that we don't need an
 * extra library for this (and tokens will not be passed to other applications, so there's no need for a common token
 * format).
 */
interface TokenSupport {
    fun createToken(id: UUID, pwdHash: String, userSecret: String, clock: Clock = Clock.systemDefaultZone()): String
    fun validateToken(token: String, resolvePwdHash: (UUID) -> String?): UserContext?
    fun generatePasswordSecret(password: String): SecretKey
}

internal class TokenSupportImpl(
    private val config: Config,
    private val logger: KLogger = KotlinLogging.logger {},
) : TokenSupport {
    private val secretKeySpec = config.signingSecret.use { SecretKeySpec(it.toByteArray(), ALGORITHM) }

    override fun createToken(
        id: UUID,
        pwdHash: String,
        userSecret: String,
        clock: Clock
    ): String {
        val expires = clock.instant().plus(Duration.ofDays(1)).toEpochMilli()
        val signature = sign(id.toString(), pwdHash, userSecret, expires.toString())
        return "$id.$pwdHash.$userSecret.$expires.$signature"
    }

    override fun validateToken(token: String, resolvePwdHash: (UUID) -> String?): UserContext? = try {
        val parts = token.split(".")
        require(parts.size == 5) { "Illegal token format" }
        require(sign(parts[0], parts[1], parts[2], parts[3]) == parts[4]) { "Invalid token signature" }
        require(Instant.ofEpochMilli(parts[3].toLong()).isAfter(Instant.now())) { "Token has expired" }
        val id = UUID.fromString(parts[0])
        val pwdHash = parts[1]
        val userSecret = EncryptionSecret.parse(parts[2])
        if (resolvePwdHash(id) == pwdHash) {
            UserContext(id, (userSecret xor config.encryptionSecret).toSecretKey())
        } else {
            null
        }
    } catch (ex: RuntimeException) {
        logger.info(ex) { "Token validation failed" }
        null
    }

    override fun generatePasswordSecret(password: String): SecretKey = config.passwordSalt.use { salt ->
        CryptoUtils.generatePasswordAesKey(password, salt)
    }

    private fun sign(vararg values: String): String {
        val mac = Mac.getInstance(ALGORITHM)
        mac.init(secretKeySpec)
        for (v in values) {
            mac.update(v.toByteArray())
        }
        return mac.doFinal().toBase64()
    }

    companion object {
        private const val ALGORITHM = "HmacSHA384"
    }
}

internal fun String.tokenHash(): String {
    val digest = MessageDigest.getInstance("SHA3-384")
    return digest.digest(toByteArray()).toBase64()
}
