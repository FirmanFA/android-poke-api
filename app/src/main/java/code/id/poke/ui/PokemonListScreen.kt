package code.id.poke.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import code.id.poke.domain.model.Pokemon
import coil3.compose.AsyncImage
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PokemonListScreen(
    onPokemonClick: (String) -> Unit,
    viewModel: PokemonListViewModel = koinViewModel()
) {
    val pokemonItems = viewModel.pokemonPager.collectAsLazyPagingItems()
    var showSearchDialog by remember { mutableStateOf(false) }
    val isRefreshing = pokemonItems.loadState.refresh is LoadState.Loading

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pokedex") },
                actions = {
                    IconButton(onClick = { showSearchDialog = true }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                }
            )
        }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { pokemonItems.refresh() },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(count = pokemonItems.itemCount) { index ->
                        pokemonItems[index]?.let { pokemon ->
                            PokemonItem(
                                pokemon = pokemon,
                                onClick = { onPokemonClick(pokemon.name) }
                            )
                        }
                    }

                    item {
                        if (pokemonItems.loadState.append is LoadState.Loading) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }

                if (pokemonItems.loadState.refresh is LoadState.Error && !isRefreshing) {
                    val error = pokemonItems.loadState.refresh as LoadState.Error
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "Error: ${error.error.localizedMessage}")
                        Button(onClick = { pokemonItems.retry() }) {
                            Text("Retry")
                        }
                    }
                }
            }
        }
    }

    if (showSearchDialog) {
        SearchDialog(
            onDismiss = { showSearchDialog = false },
            onPokemonClick = { name ->
                onPokemonClick(name)
            },
            viewModel = viewModel
        )
    }
}

@Composable
fun PokemonItem(
    pokemon: Pokemon,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.small
                    ),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = pokemon.imageUrl,
                    contentDescription = pokemon.name,
                    modifier = Modifier.size(80.dp),
                    contentScale = ContentScale.Fit
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = pokemon.name.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}
