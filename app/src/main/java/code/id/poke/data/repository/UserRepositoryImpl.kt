package code.id.poke.data.repository

import code.id.poke.data.local.UserDao
import code.id.poke.data.local.UserEntity
import code.id.poke.data.mapper.toCredentials
import code.id.poke.data.mapper.toUser
import code.id.poke.domain.error.AppError
import code.id.poke.domain.model.User
import code.id.poke.domain.model.UserCredentials
import code.id.poke.domain.repository.UserRepository

class UserRepositoryImpl(private val userDao: UserDao) : UserRepository {

    override suspend fun register(
        name: String,
        email: String,
        passwordHash: String,
        salt: String
    ): Result<Unit> = try {
        val existing = userDao.getUserByEmail(email)
        if (existing != null) {
            Result.failure(AppError.UnknownError("User already exists"))
        } else {
            userDao.insertUser(UserEntity(name = name, email = email, passwordHash = passwordHash, salt = salt))
            Result.success(Unit)
        }
    } catch (e: Exception) {
        Result.failure(AppError.from(e))
    }

    override suspend fun getUserCredentials(email: String): UserCredentials? = try {
        userDao.getUserByEmail(email)?.toCredentials()
    } catch (e: Exception) {
        null
    }

    override suspend fun getUserByEmail(email: String): User? = try {
        userDao.getUserByEmail(email)?.toUser()
    } catch (e: Exception) {
        null
    }
}
