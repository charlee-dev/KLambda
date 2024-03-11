package component.common.feature.user

import java.util.UUID

interface UserRepository {
    fun findAll(): List<User>
    fun findById(id: UUID): User?
    fun findByEmail(email: String): User?
    fun create(user: User): User
    fun delete(id: UUID)
    fun update(user: User): User
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
            println("User Created $it")
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

    override fun update(user: User): User {
        val index = users.indexOfFirst { it.id == user.id }
        if (index != -1) {
            users[index] = user
        }
        return user
    }
}
