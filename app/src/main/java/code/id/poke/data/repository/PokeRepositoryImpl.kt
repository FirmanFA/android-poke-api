package code.id.poke.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import code.id.poke.data.local.AppDatabase
import code.id.poke.data.local.PokemonEntity
import code.id.poke.data.remote.PokemonRemoteMediator
import code.id.poke.data.remote.PokeService
import code.id.poke.data.remote.PokemonDetailResponse
import code.id.poke.data.remote.PokemonResult
import code.id.poke.domain.repository.PokeRepository
import kotlinx.coroutines.flow.Flow

class PokeRepositoryImpl(
    private val pokeService: PokeService,
    private val database: AppDatabase
) : PokeRepository {

    private val pokemonDao = database.pokemonDao()

    @OptIn(ExperimentalPagingApi::class)
    override fun getPokemonPager(query: String): Flow<PagingData<PokemonEntity>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false,
                initialLoadSize = 20
            ),
            remoteMediator = PokemonRemoteMediator(pokeService, database),
            pagingSourceFactory = { 
                if (query.isEmpty()) pokemonDao.searchPokemon("") 
                else pokemonDao.searchPokemon(query) 
            }
        ).flow
    }

    override suspend fun getPokemonDetail(name: String): Result<PokemonDetailResponse> {
        return try {
            val response = pokeService.getPokemonDetail(name)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun searchPokemonFromApi(query: String): List<PokemonResult> {
        // Fetching a larger set of pokemon to filter by name since PokeAPI doesn't support partial search
        // We'll fetch 1000 items as a "cache" for the search dialog
        val response = pokeService.getPokemonList(limit = 1000, offset = 0)
        return response.results.filter { 
            it.name.contains(query, ignoreCase = true)
        }
    }
}
