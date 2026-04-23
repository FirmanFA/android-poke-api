package code.id.poke.domain.usecase

import code.id.poke.domain.model.PokemonDetail
import code.id.poke.domain.repository.PokeRepository

class GetPokemonDetailUseCase(private val repository: PokeRepository) {
    suspend operator fun invoke(name: String): Result<PokemonDetail> = repository.getPokemonDetail(name)
}
