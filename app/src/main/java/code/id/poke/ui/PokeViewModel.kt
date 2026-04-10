package code.id.poke.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import code.id.poke.data.local.PokemonEntity
import code.id.poke.data.remote.PokemonDetailResponse
import code.id.poke.domain.repository.PokeRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class PokeViewModel(
    private val repository: PokeRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val basePokeData: Flow<PagingData<PokemonEntity>> =
        repository.getPokemonPager().cachedIn(viewModelScope)

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val pokemonPager: Flow<PagingData<PokemonEntity>> = _searchQuery
        .debounce(300)
        .flatMapLatest { query ->
            basePokeData.map { pagingData ->
                pagingData.filter { pokemon ->
                    query.isEmpty() || pokemon.name.contains(query, ignoreCase = true)
                }
            }
        }

    private val _pokemonDetail = MutableStateFlow<Result<PokemonDetailResponse>?>(null)
    val pokemonDetail: StateFlow<Result<PokemonDetailResponse>?> = _pokemonDetail.asStateFlow()

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun getPokemonDetail(name: String) {
        viewModelScope.launch {
            _pokemonDetail.value = null // Reset before fetching
            val result = repository.getPokemonDetail(name)
            _pokemonDetail.value = result
        }
    }
}
