package code.id.poke.data.local

import androidx.paging.PagingSource
import androidx.room.*

@Dao
interface PokemonDao {
    @Query("SELECT * FROM pokemon WHERE name LIKE '%' || :query || '%' ORDER BY id ASC")
    fun searchPokemon(query: String): PagingSource<Int, PokemonEntity>

    @Query("""
        SELECT pokemon.* FROM pokemon
        INNER JOIN pokemon_fts ON pokemon.rowid = pokemon_fts.rowid
        WHERE pokemon_fts MATCH :query
        ORDER BY pokemon.id ASC
    """)
    suspend fun searchPokemonFts(query: String): List<PokemonEntity>

    @Query("SELECT * FROM pokemon ORDER BY id ASC")
    suspend fun getAllPokemon(): List<PokemonEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPokemon(pokemon: List<PokemonEntity>)

    @Query("DELETE FROM pokemon")
    suspend fun deleteAllPokemon()

    @Query("DELETE FROM pokemon_fts")
    suspend fun deleteAllPokemonFts()

    @Query("SELECT COUNT(*) FROM pokemon")
    suspend fun getCount(): Int
}
