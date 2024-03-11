package component.common.feature.user.model

import java.util.UUID
import javax.crypto.SecretKey

data class UserContext(
    val userId: UUID,
    val encryptionKey: SecretKey
)
