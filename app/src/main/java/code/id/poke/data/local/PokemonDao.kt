package code.id.poke.data.local

import androidx.paging.PagingSource
import androidx.room.*

@Dao
interface PokemonDao {
    @Query("SELECT * FROM pokemon WHERE name LIKE '%' || :query || '%' ORDER BY id ASC")
    fun searchPokemon(query: String): PagingSource<Int, PokemonEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPokemon(pokemon: List<PokemonEntity>)

    @Query("DELETE FROM pokemon")
    suspend fun deleteAllPokemon()

    @Query("SELECT COUNT(*) FROM pokemon")
    suspend fun getCount(): Int
}
