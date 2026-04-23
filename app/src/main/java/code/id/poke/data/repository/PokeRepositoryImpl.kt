package code.id.poke.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import code.id.poke.data.local.PokemonDao
import code.id.poke.data.mapper.toDomain
import code.id.poke.data.remote.PokemonRemoteMediator
import code.id.poke.data.remote.PokeService
import code.id.poke.data.remote.safeApiCall
import code.id.poke.domain.model.Pokemon
import code.id.poke.domain.model.PokemonDetail
import code.id.poke.domain.repository.PokeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PokeRepositoryImpl(
    private val pokeService: PokeService,
    private val pokemonDao: PokemonDao,
    private val remoteMediator: PokemonRemoteMediator
) : PokeRepository {

    @OptIn(ExperimentalPagingApi::class)
    override fun getPokemonPager(query: String): Flow<PagingData<Pokemon>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false,
                initialLoadSize = 20
            ),
            remoteMediator = remoteMediator,
            pagingSourceFactory = { pokemonDao.searchPokemon(query) }
        ).flow.map { pagingData -> pagingData.map { it.toDomain() } }
    }

    override suspend fun getPokemonDetail(name: String): Result<PokemonDetail> =
        safeApiCall { pokeService.getPokemonDetail(name).toDomain() }

    override suspend fun searchPokemon(query: String): Result<List<Pokemon>> =
        safeApiCall {
            pokemonDao.searchPokemonFts(query).map { it.toDomain() }
        }
}
