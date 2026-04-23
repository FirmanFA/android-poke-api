package code.id.poke.ui.navigation

import kotlinx.serialization.Serializable

@Serializable object LoginRoute
@Serializable object RegisterRoute
@Serializable object MainRoute
@Serializable data class DetailRoute(val name: String)
