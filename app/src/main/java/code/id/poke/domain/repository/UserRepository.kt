package code.id.poke.domain.repository

import code.id.poke.domain.model.User
import code.id.poke.domain.model.UserCredentials

interface UserRepository {
    suspend fun register(name: String, email: String, passwordHash: String, salt: String): Result<Unit>
    suspend fun getUserCredentials(email: String): UserCredentials?
    suspend fun getUserByEmail(email: String): User?
}
