import component.common.feature.user.model.User
import se.ansman.kotshi.JsonSerializable

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
