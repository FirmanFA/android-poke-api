package code.id.poke.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [PokemonEntity::class, UserEntity::class, RemoteKeyEntity::class, PokemonFtsEntity::class],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun pokemonDao(): PokemonDao
    abstract fun userDao(): UserDao
    abstract fun remoteKeyDao(): RemoteKeyDao
}
