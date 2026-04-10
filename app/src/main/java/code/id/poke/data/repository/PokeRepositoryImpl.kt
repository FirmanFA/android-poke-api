package code.id.poke.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import code.id.poke.data.local.PokemonDao
import code.id.poke.data.local.PokemonEntity
import code.id.poke.data.remote.PokemonRemoteMediator
import code.id.poke.data.remote.PokeService
import code.id.poke.data.remote.PokemonDetailResponse
import code.id.poke.domain.repository.PokeRepository
import kotlinx.coroutines.flow.Flow

class PokeRepositoryImpl(
    private val pokeService: PokeService,
    private val pokemonDao: PokemonDao
) : PokeRepository {

    @OptIn(ExperimentalPagingApi::class)
    override fun getPokemonPager(): Flow<PagingData<PokemonEntity>> {
        return Pager(
            config = PagingConfig(pageSize = 10, enablePlaceholders = false),
            remoteMediator = PokemonRemoteMediator(pokeService, pokemonDao),
            pagingSourceFactory = { pokemonDao.pagingSource() }
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
}
