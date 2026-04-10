package code.id.poke.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import code.id.poke.data.local.PokemonEntity
import code.id.poke.data.remote.PokemonDetailResponse
import code.id.poke.data.remote.PokemonResult
import code.id.poke.domain.error.AppError
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
    data class Error(val error: AppError) : PokemonDetailUiState()
}

sealed class SearchUiState {
    object Idle : SearchUiState()
    object Loading : SearchUiState()
    data class Success(val results: List<PokemonResult>) : SearchUiState()
    data class Error(val error: AppError) : SearchUiState()
}

class PokeViewModel(
    private val repository: PokeRepository
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val pokemonPager: Flow<PagingData<PokemonEntity>> = repository.getPokemonPager("")
        .cachedIn(viewModelScope)

    private val _pokemonDetailState = MutableStateFlow<PokemonDetailUiState?>(null)
    val pokemonDetailState: StateFlow<PokemonDetailUiState?> = _pokemonDetailState.asStateFlow()

    private val _searchState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val searchState: StateFlow<SearchUiState> = _searchState.asStateFlow()

    fun searchPokemon(query: String) {
        if (query.isEmpty()) {
            _searchState.value = SearchUiState.Idle
            return
        }

        viewModelScope.launch {
            _searchState.value = SearchUiState.Loading
            try {
                val results = repository.searchPokemonFromApi(query)
                _searchState.value = SearchUiState.Success(results)
            } catch (e: Exception) {
                val error = AppError.from(e)
                _searchState.value = SearchUiState.Error(error)
            }
        }
    }

    fun clearSearch() {
        _searchState.value = SearchUiState.Idle
    }

    fun getPokemonDetail(name: String) {
        viewModelScope.launch {
            _pokemonDetailState.value = PokemonDetailUiState.Loading
            try {
                val result = repository.getPokemonDetail(name)
                _pokemonDetailState.value = result.fold(
                    onSuccess = { PokemonDetailUiState.Success(it) },
                    onFailure = {
                        val error = AppError.from(it)
                        PokemonDetailUiState.Error(error)
                    }
                )
            } catch (e: Exception) {
                val error = AppError.from(e)
                _pokemonDetailState.value = PokemonDetailUiState.Error(error)
            }
        }
    }
}
