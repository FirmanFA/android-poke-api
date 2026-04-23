package code.id.poke.data.mapper

import code.id.poke.data.local.UserEntity
import code.id.poke.domain.model.User
import code.id.poke.domain.model.UserCredentials

fun UserEntity.toUser() = User(id = id, name = name, email = email)

fun UserEntity.toCredentials() = UserCredentials(passwordHash = passwordHash, salt = salt)
