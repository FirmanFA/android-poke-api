package code.id.poke.domain.repository

import androidx.paging.PagingData
import code.id.poke.domain.model.Pokemon
import code.id.poke.domain.model.PokemonDetail
import kotlinx.coroutines.flow.Flow

interface PokeRepository {
    fun getPokemonPager(query: String): Flow<PagingData<Pokemon>>
    suspend fun getPokemonDetail(name: String): Result<PokemonDetail>
    suspend fun searchPokemon(query: String): Result<List<Pokemon>>
}
