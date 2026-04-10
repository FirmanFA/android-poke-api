package code.id.poke.domain.repository

import androidx.paging.PagingData
import code.id.poke.data.local.PokemonEntity
import code.id.poke.data.remote.PokemonDetailResponse
import code.id.poke.data.remote.PokemonResult
import kotlinx.coroutines.flow.Flow

interface PokeRepository {
    fun getPokemonPager(query: String): Flow<PagingData<PokemonEntity>>
    suspend fun getPokemonDetail(name: String): Result<PokemonDetailResponse>
    suspend fun searchPokemonFromApi(query: String): List<PokemonResult>
}
