package code.id.poke

import android.os.Bundle
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
import androidx.core.view.WindowCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import code.id.poke.ui.MainViewModel
import code.id.poke.ui.PokemonDetailScreen
import code.id.poke.ui.PokemonListScreen
import code.id.poke.ui.auth.AuthViewModel
import code.id.poke.ui.auth.LoginScreen
import code.id.poke.ui.auth.ProfileScreen
import code.id.poke.ui.auth.RegisterScreen
import code.id.poke.ui.navigation.DetailRoute
import code.id.poke.ui.navigation.LoginRoute
import code.id.poke.ui.navigation.MainRoute
import code.id.poke.ui.navigation.RegisterRoute
import code.id.poke.ui.theme.PokeCODEIDTheme
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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
    val mainViewModel: MainViewModel = koinViewModel()
    val navController = rememberNavController()
    val startDestination = if (mainViewModel.isLoggedIn) MainRoute else LoginRoute

    NavHost(navController = navController, startDestination = startDestination) {
        composable<LoginRoute> {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(MainRoute) {
                        popUpTo<LoginRoute> { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate(RegisterRoute) }
            )
        }
        composable<RegisterRoute> {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(LoginRoute) {
                        popUpTo<RegisterRoute> { inclusive = true }
                    }
                },
                onNavigateToLogin = { navController.navigate(LoginRoute) }
            )
        }
        composable<MainRoute> {
            val authViewModel: AuthViewModel = koinViewModel()
            PokeCODEIDApp(
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(LoginRoute) {
                        popUpTo<MainRoute> { inclusive = true }
                    }
                },
                onPokemonClick = { name -> navController.navigate(DetailRoute(name)) }
            )
        }
        composable<DetailRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<DetailRoute>()
            PokemonDetailScreen(
                pokemonName = route.name,
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
                    icon = { Icon(imageVector = it.icon, contentDescription = it.label) },
                    label = { Text(it.label) },
                    selected = it == currentDestination,
                    onClick = { currentDestination = it }
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

enum class AppDestinations(val label: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.Home),
    PROFILE("Profile", Icons.Default.AccountBox),
}
