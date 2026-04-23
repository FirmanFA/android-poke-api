package code.id.poke.data.mapper

import code.id.poke.data.local.PokemonEntity
import code.id.poke.data.remote.PokemonDetailResponse
import code.id.poke.domain.model.Pokemon
import code.id.poke.domain.model.PokemonDetail

fun PokemonEntity.toDomain() = Pokemon(id = id, name = name, imageUrl = imageUrl)

fun PokemonDetailResponse.toDomain(): PokemonDetail {
    val imageUrl = sprites.other?.official_artwork?.front_default
        ?: sprites.front_default
    return PokemonDetail(
        id = id,
        name = name,
        height = height,
        weight = weight,
        abilities = abilities.map { it.ability.name },
        imageUrl = imageUrl.orEmpty()
    )
}
