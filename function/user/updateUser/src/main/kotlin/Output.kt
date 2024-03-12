import component.common.feature.user.model.User
import se.ansman.kotshi.JsonSerializable
import java.util.UUID

@JsonSerializable
data class Input(
    val id: String,
    val email: String? = null,
    val password: String? = null,
    val name: String? = null
) {
    companion object {
        val inputLens = functionJson.autoBody<Input>().toLens()
        val inputSample = Input(id = UUID.randomUUID().toString(), name = User.NAME_SAMPLE)
    }
}

@JsonSerializable
data class Output(
    val updatedUser: UpdatedUser?,
    val message: String? = null
) {
    internal companion object {
        val outputLens = functionJson.autoBody<Output>().toLens()
        val success = Output(updatedUser = UpdatedUser.sample)
        fun failure(message: String = "User update failed") = Output(updatedUser = null, message = message)
    }
}

@JsonSerializable
data class UpdatedUser(
    val id: String,
    val email: String,
    val name: String,
) {
    internal companion object {
        val sample = UpdatedUser(
            id = UUID.randomUUID().toString(),
            email = User.EMAIL_SAMPLE,
            name = User.NAME_SAMPLE,
        )
    }
}
