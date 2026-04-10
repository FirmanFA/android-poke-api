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
import code.id.poke.domain.error.AppError
import code.id.poke.domain.repository.PokeRepository
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException

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
        } catch (e: HttpException) {
            val error = when {
                e.code() in 400..499 -> AppError.ClientError(e.code(), e.message())
                e.code() in 500..599 -> AppError.ServerError(e.code(), e.message())
                else -> AppError.NetworkError(e.code(), e.message())
            }
            Result.failure(error)
        } catch (e: Exception) {
            Result.failure(AppError.from(e))
        }
    }

    override suspend fun searchPokemonFromApi(query: String): List<PokemonResult> {
        return try {
            // First, try to search from local database (much faster)
            val localResults = pokemonDao.searchPokemonFts(query)
            if (localResults.isNotEmpty()) {
                return localResults.map { PokemonResult(it.name, "") }
            }

            // If not found locally, fetch from API
            val response = pokeService.getPokemonList(limit = 1000, offset = 0)
            response.results.filter {
                it.name.contains(query, ignoreCase = true)
            }
        } catch (e: HttpException) {
            val error = when {
                e.code() in 400..499 -> AppError.ClientError(e.code(), e.message())
                e.code() in 500..599 -> AppError.ServerError(e.code(), e.message())
                else -> AppError.NetworkError(e.code(), e.message())
            }
            throw error
        } catch (e: Exception) {
            throw AppError.from(e)
        }
    }
}
