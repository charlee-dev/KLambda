import component.common.feature.user.model.User
import se.ansman.kotshi.JsonSerializable

@JsonSerializable
data class Input(
    val email: String,
    val password: String,
) {
    internal companion object {
        val inputLens = functionJson.autoBody<Input>().toLens()
        val sample = Input(email = User.EMAIL_SAMPLE, password = User.PASSWORD_SAMPLE)
    }
}
