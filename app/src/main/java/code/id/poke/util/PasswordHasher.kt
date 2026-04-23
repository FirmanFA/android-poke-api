package code.id.poke.util

import java.security.MessageDigest
import java.util.UUID

class PasswordHasher {
    fun generateSalt(): String = UUID.randomUUID().toString()

    fun hash(password: String, salt: String): String {
        val input = "$salt$password"
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun verify(password: String, salt: String, expectedHash: String): Boolean =
        hash(password, salt) == expectedHash
}
