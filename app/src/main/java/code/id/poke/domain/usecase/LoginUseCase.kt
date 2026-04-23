package code.id.poke.domain.usecase

import code.id.poke.domain.error.AppError
import code.id.poke.domain.model.User
import code.id.poke.domain.repository.UserRepository
import code.id.poke.util.PasswordHasher

class LoginUseCase(
    private val repository: UserRepository,
    private val passwordHasher: PasswordHasher
) {
    suspend operator fun invoke(email: String, password: String): Result<User> {
        val credentials = repository.getUserCredentials(email)
            ?: return Result.failure(AppError.UnknownError("Invalid email or password"))

        if (!passwordHasher.verify(password, credentials.salt, credentials.passwordHash)) {
            return Result.failure(AppError.UnknownError("Invalid email or password"))
        }

        return repository.getUserByEmail(email)?.let { Result.success(it) }
            ?: Result.failure(AppError.UnknownError("User not found"))
    }
}
