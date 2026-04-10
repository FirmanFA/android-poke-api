package code.id.poke.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import code.id.poke.data.local.PokemonEntity
import code.id.poke.data.remote.PokemonDetailResponse
import code.id.poke.data.remote.PokemonResult
import code.id.poke.domain.repository.PokeRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

sealed class PokemonDetailUiState {
    object Loading : PokemonDetailUiState()
    data class Success(val data: PokemonDetailResponse) : PokemonDetailUiState()
    data class Error(val message: String) : PokemonDetailUiState()
}

class PokeViewModel(
    private val repository: PokeRepository
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val pokemonPager: Flow<PagingData<PokemonEntity>> = repository.getPokemonPager("")
        .cachedIn(viewModelScope)

    private val _pokemonDetailState = MutableStateFlow<PokemonDetailUiState?>(null)
    val pokemonDetailState: StateFlow<PokemonDetailUiState?> = _pokemonDetailState.asStateFlow()

    suspend fun searchPokemonApi(query: String): List<PokemonResult> {
        // Since PokeAPI doesn't have a partial search, we fetch a large chunk and filter
        // In a real app, this would be a specific search endpoint
        return try {
            val response = repository.searchPokemonFromApi(query)
            response
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getPokemonDetail(name: String) {
        viewModelScope.launch {
            _pokemonDetailState.value = PokemonDetailUiState.Loading
            val result = repository.getPokemonDetail(name)
            _pokemonDetailState.value = result.fold(
                onSuccess = { PokemonDetailUiState.Success(it) },
                onFailure = { PokemonDetailUiState.Error(it.message ?: "Unknown Error") }
            )
        }
    }
}
