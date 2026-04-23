package code.id.poke.domain.usecase

import code.id.poke.domain.repository.UserRepository
import code.id.poke.util.PasswordHasher

class RegisterUseCase(
    private val repository: UserRepository,
    private val passwordHasher: PasswordHasher
) {
    suspend operator fun invoke(name: String, email: String, password: String): Result<Unit> {
        val salt = passwordHasher.generateSalt()
        val hash = passwordHasher.hash(password, salt)
        return repository.register(name, email, hash, salt)
    }
}
