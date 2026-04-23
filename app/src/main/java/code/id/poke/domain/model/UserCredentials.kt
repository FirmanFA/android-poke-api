package code.id.poke.domain.model

data class UserCredentials(
    val passwordHash: String,
    val salt: String
)
