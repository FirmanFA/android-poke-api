package code.id.poke.data.local

import androidx.room.Entity
import androidx.room.Fts4

@Entity(tableName = "pokemon_fts")
@Fts4(contentEntity = PokemonEntity::class)
data class PokemonFtsEntity(
    val name: String
)
