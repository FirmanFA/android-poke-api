package code.id.poke

import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.platform.LocalView
import code.id.poke.ui.auth.ProfileScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.core.view.WindowCompat
import code.id.poke.data.local.SessionManager
import code.id.poke.ui.PokemonDetailScreen
import code.id.poke.ui.PokemonListScreen
import code.id.poke.ui.auth.AuthViewModel
import code.id.poke.ui.auth.LoginScreen
import code.id.poke.ui.auth.RegisterScreen
import code.id.poke.ui.theme.PokeCODEIDTheme
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Set status bar icons to dark color
        WindowCompat.getInsetsController(window, window.decorView)?.isAppearanceLightStatusBars = true

        setContent {
            PokeCODEIDTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val sessionManager: SessionManager = koinInject()
    val authViewModel: AuthViewModel = koinViewModel()

    val startDestination = if (sessionManager.isLoggedIn()) "main" else "login"

    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("main") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate("register")
                }
            )
        }
        composable("register") {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate("login") {
                        popUpTo("register") { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate("login")
                }
            )
        }
        composable("main") {
            PokeCODEIDApp(
                onLogout = {
                    authViewModel.logout()
                    navController.navigate("login") {
                        popUpTo("main") { inclusive = true }
                    }
                },
                onPokemonClick = { pokemonName ->
                    navController.navigate("detail/$pokemonName")
                }
            )
        }
        composable("detail/{name}") { backStackEntry ->
            val pokemonName = backStackEntry.arguments?.getString("name") ?: ""
            PokemonDetailScreen(
                pokemonName = pokemonName,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}

@Composable
fun PokeCODEIDApp(
    onLogout: () -> Unit,
    onPokemonClick: (String) -> Unit
) {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach {
                item(
                    icon = {
                        Icon(
                            imageVector = it.icon,
                            contentDescription = it.label
                        )
                    },
                    label = { Text(it.label) },
                    selected = it == currentDestination,
                    onClick = {
                        currentDestination = it
                    }
                )
            }
        }
    ) {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                when (currentDestination) {
                    AppDestinations.HOME -> PokemonListScreen(onPokemonClick = onPokemonClick)
                    AppDestinations.PROFILE -> ProfileScreen(onLogout = onLogout)
                }
            }
        }
    }
}

enum class AppDestinations(
    val label: String,
    val icon: ImageVector,
) {
    HOME("Home", Icons.Default.Home),
    PROFILE("Profile", Icons.Default.AccountBox),
}

