import component.common.feature.user.User
import dev.forkhandles.values.NonBlankStringValueFactory
import dev.forkhandles.values.StringValue
import dev.forkhandles.values.ValueFactory
import dev.forkhandles.values.and
import dev.forkhandles.values.minLength
import dev.forkhandles.values.regex
import se.ansman.kotshi.JsonSerializable

const val REGEX_EMAIL =
    "^[A-Za-z0-9!#$%&'*+-/=?^_`{|}~]+(\\.[A-Za-z0-9!#$%&'*+-/=?^_`{|}~]+)*[^.]" +
        "@(?!\\.)([A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?\\.)+[A-Za-z]{2,}$"

class Email(value: String) : StringValue(value) {
    companion object : ValueFactory<Email, String>(
        ::Email,
        validation = 5.minLength.and(REGEX_EMAIL.regex),
        parseFn = String::lowercase
    )
}

class Password(value: String) : StringValue(value) {
    companion object : NonBlankStringValueFactory<Password>(::Password)
}

@JsonSerializable
data class LoginInput(
    val email: String,
    val password: String,
) {
    companion object {
        val lens = functionJson.autoBody<LoginInput>().toLens()
        val sample = LoginInput(email = User.EMAIL_SAMPLE, password = User.PASSWORD_SAMPLE)
    }
}
