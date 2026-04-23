package code.id.poke.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import code.id.poke.domain.error.AppError
import code.id.poke.domain.model.PokemonDetail
import code.id.poke.domain.usecase.GetPokemonDetailUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class PokemonDetailUiState {
    object Loading : PokemonDetailUiState()
    data class Success(val data: PokemonDetail) : PokemonDetailUiState()
    data class Error(val error: AppError) : PokemonDetailUiState()
}

class PokemonDetailViewModel(
    private val getPokemonDetail: GetPokemonDetailUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<PokemonDetailUiState>(PokemonDetailUiState.Loading)
    val uiState: StateFlow<PokemonDetailUiState> = _uiState.asStateFlow()

    fun load(name: String) {
        viewModelScope.launch {
            _uiState.value = PokemonDetailUiState.Loading
            getPokemonDetail(name).fold(
                onSuccess = { _uiState.value = PokemonDetailUiState.Success(it) },
                onFailure = { _uiState.value = PokemonDetailUiState.Error(AppError.from(it)) }
            )
        }
    }
}
