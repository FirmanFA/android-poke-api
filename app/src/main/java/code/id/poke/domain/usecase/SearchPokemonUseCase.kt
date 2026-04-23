package code.id.poke.domain.usecase

import code.id.poke.domain.model.Pokemon
import code.id.poke.domain.repository.PokeRepository

class SearchPokemonUseCase(private val repository: PokeRepository) {
    suspend operator fun invoke(query: String): Result<List<Pokemon>> = repository.searchPokemon(query)
}
