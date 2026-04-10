package code.id.poke.data.repository

import code.id.poke.data.local.UserDao
import code.id.poke.data.local.UserEntity
import code.id.poke.domain.repository.UserRepository

class UserRepositoryImpl(
    private val userDao: UserDao
) : UserRepository {

    override suspend fun register(user: UserEntity): Result<Unit> {
        return try {
            val existingUser = userDao.getUserByEmail(user.email)
            if (existingUser != null) {
                Result.failure(Exception("User already exists"))
            } else {
                userDao.insertUser(user)
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun login(email: String, password: String): Result<UserEntity> {
        return try {
            val user = userDao.getUserByEmail(email)
            if (user != null && user.password == password) {
                Result.success(user)
            } else {
                Result.failure(Exception("Invalid email or password"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserByEmail(email: String): UserEntity? {
        return try {
            userDao.getUserByEmail(email)
        } catch (e: Exception) {
            null
        }
    }
}
