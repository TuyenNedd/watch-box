package dev.watchbox.tv.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dev.watchbox.tv.WatchBoxApplication
import dev.watchbox.tv.player.PlayerScreen
import dev.watchbox.tv.ui.components.NavDestination
import dev.watchbox.tv.ui.components.WatchBoxNavigationRail
import dev.watchbox.tv.ui.screens.DetailsScreen
import dev.watchbox.tv.ui.screens.HomeScreen
import dev.watchbox.tv.ui.screens.LibraryScreen
import dev.watchbox.tv.ui.screens.SearchScreen

object Routes {
    const val HOME = "home"
    const val SEARCH = "search"
    const val LIBRARY = "library"
    const val DETAILS = "details/{id}"
    const val PLAYER = "player/{id}"

    fun details(id: String) = "details/$id"
    fun player(id: String) = "player/$id"
}

@Composable
fun WatchBoxApp(
    application: WatchBoxApplication,
    modifier: Modifier = Modifier,
) {
    val viewModel: WatchBoxViewModel = viewModel(
        factory = WatchBoxViewModel.Factory(
            repository = application.repository,
            libraryStore = application.libraryStore,
        ),
    )
    val uiState by viewModel.uiState.collectAsState()
    val navController = rememberNavController()
    var selectedNav by remember { mutableStateOf(NavDestination.HOME) }

    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
        modifier = modifier.fillMaxSize(),
    ) {
        composable(Routes.HOME) {
            Row(modifier = Modifier.fillMaxSize()) {
                WatchBoxNavigationRail(
                    selectedDestination = selectedNav,
                    onDestinationSelected = { dest ->
                        selectedNav = dest
                        navController.navigate(dest.route) {
                            popUpTo(Routes.HOME) { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                )
                HomeScreen(
                    uiState = uiState,
                    onMovieClick = { id ->
                        viewModel.loadDetails(id)
                        navController.navigate(Routes.details(id))
                    },
                    onPlayClick = { id ->
                        viewModel.loadDetails(id)
                        navController.navigate(Routes.player(id))
                    },
                    onRetry = { viewModel.retry() },
                    modifier = Modifier.weight(1f),
                )
            }
        }
        composable(Routes.SEARCH) {
            Row(modifier = Modifier.fillMaxSize()) {
                WatchBoxNavigationRail(
                    selectedDestination = selectedNav,
                    onDestinationSelected = { dest ->
                        selectedNav = dest
                        navController.navigate(dest.route) {
                            popUpTo(Routes.HOME) { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                )
                SearchScreen(
                    uiState = uiState,
                    onQueryChanged = { viewModel.onSearchQueryChanged(it) },
                    onMovieClick = { id ->
                        viewModel.loadDetails(id)
                        navController.navigate(Routes.details(id))
                    },
                    onRetry = { viewModel.retry() },
                    modifier = Modifier.weight(1f),
                )
            }
        }
        composable(Routes.LIBRARY) {
            Row(modifier = Modifier.fillMaxSize()) {
                WatchBoxNavigationRail(
                    selectedDestination = selectedNav,
                    onDestinationSelected = { dest ->
                        selectedNav = dest
                        navController.navigate(dest.route) {
                            popUpTo(Routes.HOME) { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                )
                val allMovies = uiState.shelves.flatMap { it.movies }.distinctBy { it.id }
                LibraryScreen(
                    uiState = uiState,
                    allMovies = allMovies,
                    onMovieClick = { id ->
                        viewModel.loadDetails(id)
                        navController.navigate(Routes.details(id))
                    },
                    modifier = Modifier.weight(1f),
                )
            }
        }
        composable(
            route = Routes.DETAILS,
            arguments = listOf(navArgument("id") { type = NavType.StringType }),
        ) { backStackEntry ->
            val movieId = backStackEntry.arguments?.getString("id").orEmpty()
            val isFavorite = movieId in uiState.favorites
            val progress = uiState.progress[movieId]
            DetailsScreen(
                details = uiState.selectedDetails,
                isLoading = uiState.isLoadingDetails,
                isFavorite = isFavorite,
                progress = progress,
                onPlay = { navController.navigate(Routes.player(movieId)) },
                onToggleFavorite = { viewModel.toggleFavorite(movieId) },
                onBack = { navController.popBackStack() },
            )
        }
        composable(
            route = Routes.PLAYER,
            arguments = listOf(navArgument("id") { type = NavType.StringType }),
        ) { backStackEntry ->
            val movieId = backStackEntry.arguments?.getString("id").orEmpty()
            PlayerScreen(
                movieId = movieId,
                details = uiState.selectedDetails,
                savedProgress = uiState.progress[movieId],
                onSaveProgress = { positionMs, durationMs ->
                    viewModel.saveProgress(movieId, positionMs, durationMs)
                },
                onClearProgress = { viewModel.clearProgress(movieId) },
                onBack = { navController.popBackStack() },
            )
        }
    }
}
