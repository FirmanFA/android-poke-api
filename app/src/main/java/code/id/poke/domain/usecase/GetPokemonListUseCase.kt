package code.id.poke.domain.usecase

import androidx.paging.PagingData
import code.id.poke.domain.model.Pokemon
import code.id.poke.domain.repository.PokeRepository
import kotlinx.coroutines.flow.Flow

class GetPokemonListUseCase(private val repository: PokeRepository) {
    operator fun invoke(query: String): Flow<PagingData<Pokemon>> = repository.getPokemonPager(query)
}
