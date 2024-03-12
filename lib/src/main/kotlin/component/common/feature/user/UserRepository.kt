package component.common.feature.user

import component.common.feature.user.model.User
import java.util.UUID

interface UserRepository {
    fun findAll(): List<User>
    fun findById(id: UUID): User?
    fun findByEmail(email: String): User?
    fun create(user: User): User
    fun delete(id: UUID)
    fun update(id: UUID, name: String?, email: String?, password: String?): User
}

internal class UserRepositoryImpl : UserRepository {
    private val users = mutableListOf<User>()

    override fun findAll(): List<User> = users

    override fun findById(id: UUID): User? {
        return users.firstOrNull { it.id == id }
    }

    override fun findByEmail(email: String): User? {
        return users.firstOrNull { it.email == email }
    }

    override fun create(user: User): User {
        users.add(user).also {
            println("User Created $it. All users: $users")
        }
        return user
    }

    override fun delete(id: UUID) {
        val user = findById(id) ?: error("User not found")
        if (users.remove(user)) {
            println("User removed")
        } else {
            println("User not removed")
        }
    }

    override fun update(id: UUID, name: String?, email: String?, password: String?): User {
        val user = findById(id) ?: error("User not found")
        val updatedUser = user.copy( // FIXME: We  want to be able to update single properties
            name = name ?: user.name,
            email = email ?: user.email,
            passwordHash = password ?: user.passwordHash // FIXME: password need to be hashed
        )
        users.remove(user)
        users.add(updatedUser)
        return updatedUser
    }
}
