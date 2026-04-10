package code.id.poke.data.remote

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import code.id.poke.data.local.AppDatabase
import code.id.poke.data.local.PokemonEntity
import code.id.poke.data.local.RemoteKeyEntity

@OptIn(ExperimentalPagingApi::class)
class PokemonRemoteMediator(
    private val pokeService: PokeService,
    private val database: AppDatabase
) : RemoteMediator<Int, PokemonEntity>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, PokemonEntity>
    ): MediatorResult {
        return try {
            val offset = when (loadType) {
                LoadType.REFRESH -> 0
                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
                LoadType.APPEND -> {
                    val remoteKeys = getRemoteKeyForLastItem(state)
                    val nextKey = remoteKeys?.nextKey
                        ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                    nextKey
                }
            }

            val response = pokeService.getPokemonList(
                limit = state.config.pageSize,
                offset = offset
            )

            val endOfPaginationReached = response.results.isEmpty()

            database.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    database.remoteKeyDao().clearRemoteKeys()
                    database.pokemonDao().deleteAllPokemon()
                }

                val prevKey = if (offset == 0) null else offset - state.config.pageSize
                val nextKey = if (endOfPaginationReached) null else offset + state.config.pageSize

                val entities = response.results.map { result ->
                    val id = result.url.split("/").last { it.isNotEmpty() }.toInt()
                    PokemonEntity(
                        id = id,
                        name = result.name.replaceFirstChar { it.uppercase() },
                        imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/$id.png"
                    )
                }

                val keys = entities.map {
                    RemoteKeyEntity(pokemonId = it.id, prevKey = prevKey, nextKey = nextKey)
                }

                database.remoteKeyDao().insertAll(keys)
                database.pokemonDao().insertPokemon(entities)
            }

            MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, PokemonEntity>): RemoteKeyEntity? {
        return state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()
            ?.let { pokemon ->
                database.remoteKeyDao().getRemoteKeyById(pokemon.id)
            }
    }
}
