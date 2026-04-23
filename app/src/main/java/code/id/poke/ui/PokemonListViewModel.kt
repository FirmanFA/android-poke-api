package code.id.poke.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import code.id.poke.domain.error.AppError
import code.id.poke.domain.model.Pokemon
import code.id.poke.domain.usecase.GetPokemonListUseCase
import code.id.poke.domain.usecase.SearchPokemonUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class SearchUiState {
    object Idle : SearchUiState()
    object Loading : SearchUiState()
    data class Success(val results: List<Pokemon>) : SearchUiState()
    data class Error(val error: AppError) : SearchUiState()
}

class PokemonListViewModel(
    getPokemonList: GetPokemonListUseCase,
    private val searchPokemon: SearchPokemonUseCase
) : ViewModel() {

    val pokemonPager: Flow<PagingData<Pokemon>> = getPokemonList("")
        .cachedIn(viewModelScope)

    private val _searchState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val searchState: StateFlow<SearchUiState> = _searchState.asStateFlow()

    fun search(query: String) {
        if (query.isEmpty()) {
            _searchState.value = SearchUiState.Idle
            return
        }
        viewModelScope.launch {
            _searchState.value = SearchUiState.Loading
            searchPokemon(query).fold(
                onSuccess = { _searchState.value = SearchUiState.Success(it) },
                onFailure = { _searchState.value = SearchUiState.Error(AppError.from(it)) }
            )
        }
    }

    fun clearSearch() {
        _searchState.value = SearchUiState.Idle
    }
}
