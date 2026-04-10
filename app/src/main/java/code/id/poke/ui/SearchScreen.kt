package code.id.poke.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import code.id.poke.data.remote.PokemonResult
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchDialog(
    onDismiss: () -> Unit,
    onPokemonClick: (String) -> Unit,
    viewModel: PokeViewModel
) {
    var query by remember { mutableStateOf("") }
    val searchState by viewModel.searchState.collectAsState()

    LaunchedEffect(query) {
        if (query.isEmpty()) {
            viewModel.clearSearch()
            return@LaunchedEffect
        }

        if (query.length < 2) {
            return@LaunchedEffect
        }

        delay(500)
        viewModel.searchPokemon(query)
    }

    fun handleDismiss() {
        viewModel.clearSearch()
        onDismiss()
    }

    Dialog(
        onDismissRequest = { handleDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                TopAppBar(
                    title = {
                        TextField(
                            value = query,
                            onValueChange = { query = it },
                            placeholder = { Text("Search Pokemon...") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                            ),
                            singleLine = true
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { handleDismiss() }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    },
                    actions = {
                        if (searchState is SearchUiState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        }
                    }
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    when (searchState) {
                        is SearchUiState.Success -> {
                            val results = (searchState as SearchUiState.Success).results
                            items(results) { pokemon ->
                                ListItem(
                                    headlineContent = {
                                        Text(pokemon.name.replaceFirstChar { it.uppercase() })
                                    },
                                    modifier = Modifier.clickable {
                                        onPokemonClick(pokemon.name)
                                        handleDismiss()
                                    }
                                )
                                HorizontalDivider()
                            }
                        }

                        is SearchUiState.Error -> {
                            item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Error: ${(searchState as SearchUiState.Error).error.message}",
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }

                        is SearchUiState.Loading -> {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }

                        SearchUiState.Idle -> {
                            if (query.length >= 2) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("No Pokemon found")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
