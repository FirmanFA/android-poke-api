package code.id.poke.domain.model

data class PokemonDetail(
    val id: Int,
    val name: String,
    val height: Int,
    val weight: Int,
    val abilities: List<String>,
    val imageUrl: String
)
