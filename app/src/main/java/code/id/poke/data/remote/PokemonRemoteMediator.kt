package code.id.poke.data.remote

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import code.id.poke.data.local.PokemonDao
import code.id.poke.data.local.PokemonEntity

@OptIn(ExperimentalPagingApi::class)
class PokemonRemoteMediator(
    private val pokeService: PokeService,
    private val pokemonDao: PokemonDao
) : RemoteMediator<Int, PokemonEntity>() {

    private val PAGE_SIZE = 10
    private var currentOffset = 0

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, PokemonEntity>
    ): MediatorResult {
        return try {
            when (loadType) {
                LoadType.REFRESH -> {
                    currentOffset = 0
                    pokemonDao.deleteAllPokemon()
                    fetchAndInsert(0)
                }
                LoadType.PREPEND -> MediatorResult.Success(endOfPaginationReached = true)
                LoadType.APPEND -> {
                    val itemCount = pokemonDao.getCount()
                    fetchAndInsert(itemCount)
                }
            }
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }

    private suspend fun fetchAndInsert(offset: Int): MediatorResult {
        val response = pokeService.getPokemonList(limit = PAGE_SIZE, offset = offset)

        val entities = response.results.map { result ->
            val id = result.url.split("/").last { it.isNotEmpty() }.toInt()
            PokemonEntity(
                id = id,
                name = result.name.replaceFirstChar { it.uppercase() },
                imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/$id.png"
            )
        }

        pokemonDao.insertPokemon(entities)
        currentOffset = offset + PAGE_SIZE

        return MediatorResult.Success(endOfPaginationReached = response.results.isEmpty())
    }
}
