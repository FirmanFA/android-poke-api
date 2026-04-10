package code.id.poke.domain.repository

import code.id.poke.data.local.UserEntity

interface UserRepository {
    suspend fun register(user: UserEntity): Result<Unit>
    suspend fun login(email: String, password: String): Result<UserEntity>
    suspend fun getUserByEmail(email: String): UserEntity?
}
