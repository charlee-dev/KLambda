import component.common.feature.user.model.User
import se.ansman.kotshi.JsonSerializable

@JsonSerializable
data class RegisterInput(
    val email: String,
    val password: String,
    val name: String
) {
    companion object {
        val lens = functionJson.autoBody<RegisterInput>().toLens()
        val sample = RegisterInput(email = User.EMAIL_SAMPLE, password = User.PASSWORD_SAMPLE, name = User.NAME_SAMPLE)
    }
}
