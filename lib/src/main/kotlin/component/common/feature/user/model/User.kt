package component.common.feature.user.model

import dev.forkhandles.values.NonBlankStringValueFactory
import dev.forkhandles.values.StringValue
import dev.forkhandles.values.ValueFactory
import dev.forkhandles.values.and
import dev.forkhandles.values.minLength
import dev.forkhandles.values.regex
import java.time.Instant
import java.util.UUID

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

internal const val REGEX_EMAIL =
    "^[A-Za-z0-9!#$%&'*+-/=?^_`{|}~]+(\\.[A-Za-z0-9!#$%&'*+-/=?^_`{|}~]+)*[^.]" +
        "@(?!\\.)([A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?\\.)+[A-Za-z]{2,}$"

class EmailInput(value: String) : StringValue(value) {
    companion object : ValueFactory<EmailInput, String>(
        ::EmailInput,
        validation = 5.minLength.and(REGEX_EMAIL.regex),
        parseFn = String::lowercase
    )
}

class PasswordInput(value: String) : StringValue(value) {
    companion object : NonBlankStringValueFactory<PasswordInput>(::PasswordInput)
}
