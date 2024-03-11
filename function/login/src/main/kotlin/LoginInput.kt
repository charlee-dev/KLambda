import se.ansman.kotshi.JsonSerializable

@JsonSerializable
data class LoginInput(
    val email: String,
    val password: String,
)
