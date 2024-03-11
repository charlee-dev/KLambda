import component.common.feature.user.model.User
import se.ansman.kotshi.JsonSerializable
import java.time.Instant
import java.util.UUID

@JsonSerializable
data class GetUserByIdResult(
    val userProfile: UserProfile?,
    val message: String? = null
) {
    companion object {
        val lens = functionJson.autoBody<GetUserByIdResult>().toLens()
        val success = GetUserByIdResult(userProfile = UserProfile.sample)
        fun failure(message: String) = GetUserByIdResult(userProfile = null, message = message)
    }
}

@JsonSerializable
data class UserProfile(
    val id: String,
    val email: String,
    val name: String,
    val lastLogin: Instant? = null
) {
    companion object {
        val sample = UserProfile(
            id = UUID.randomUUID().toString(),
            email = User.EMAIL_SAMPLE,
            name = User.NAME_SAMPLE,
            lastLogin = Instant.now()
        )
    }
}
