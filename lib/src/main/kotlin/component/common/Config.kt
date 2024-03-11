package component.common

import component.common.util.CryptoUtils.toSecretKey
import component.common.util.fromBase64
import component.common.util.xor
import dev.forkhandles.values.AbstractValue
import dev.forkhandles.values.ValueFactory
import org.http4k.cloudnative.env.Environment
import org.http4k.cloudnative.env.EnvironmentKey
import org.http4k.lens.secret
import org.http4k.lens.value

internal class EncryptionSecret private constructor(value: ByteArray) : AbstractValue<ByteArray>(value, { "*****" }) {
    companion object : ValueFactory<EncryptionSecret, ByteArray>(
        ::EncryptionSecret, { it.size == 32 }, { it.fromBase64() }, { "*****" }
    )

    infix fun xor(other: EncryptionSecret) = EncryptionSecret(value xor other.value)

    fun toSecretKey() = value.toSecretKey()

    override fun hashCode(): Int = value.contentHashCode()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EncryptionSecret

        return value.contentEquals(other.value)
    }
}

private val encryptionSecretEnvKey = EnvironmentKey.value(EncryptionSecret).required("ENCRYPTION_KEY")
private val passwordSaltEnvKey = EnvironmentKey.secret().required("PASSWORD_SALT")
private val signingSecretEnvKey = EnvironmentKey.secret().required("SIGNING_SECRET")

internal class Config(env: Environment) {
    val encryptionSecret = encryptionSecretEnvKey(env)
    val passwordSalt = passwordSaltEnvKey(env)
    val signingSecret = signingSecretEnvKey(env)
}
