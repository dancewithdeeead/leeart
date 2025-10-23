package com.vibewardrobe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Checkroom
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.vibewardrobe.data.ProfileSettings
import com.vibewardrobe.data.WardrobeLook
import com.vibewardrobe.ui.screens.ProfileScreen
import com.vibewardrobe.ui.screens.TryOnScreen
import com.vibewardrobe.ui.screens.WardrobeScreen
import com.vibewardrobe.ui.theme.VibeWardrobeTheme
import kotlinx.coroutines.launch

sealed class VibeDestination(val route: String) {
    data object Wardrobe : VibeDestination("wardrobe")
    data object TryOn : VibeDestination("tryOn")
    data object Profile : VibeDestination("profile")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VibeWardrobeApp()
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun VibeWardrobeApp() {
    var looks by remember { mutableStateOf(sampleLooks()) }
    var selectedLook by remember { mutableStateOf<WardrobeLook?>(null) }
    var profileSettings by remember {
        mutableStateOf(
            ProfileSettings(
                displayName = "Nova",
                apiKey = "",
                enableHaptics = true,
                isDarkTheme = false
            )
        )
    }
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()

    VibeWardrobeTheme(darkTheme = profileSettings.isDarkTheme) {

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                VibeBottomBar(
                    navController = navController,
                    onProfileClick = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        ) { paddingValues ->
            Box(modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
            ) {
                NavHost(
                    navController = navController,
                    startDestination = VibeDestination.Wardrobe.route,
                    enterTransition = {
                        slideInHorizontally { it / 2 } + fadeIn()
                    },
                    exitTransition = {
                        fadeOut()
                    },
                    popEnterTransition = {
                        fadeIn() + slideInHorizontally { -it / 2 }
                    },
                    popExitTransition = {
                        fadeOut() + slideOutHorizontally { it / 2 }
                    }
                ) {
                    composable(VibeDestination.Wardrobe.route) {
                        WardrobeScreen(
                            looks = looks,
                            onAddLook = { newLook -> looks = looks + newLook },
                            onSelectLook = { look ->
                                selectedLook = look
                                navController.navigate(VibeDestination.TryOn.route)
                            },
                            onDeleteLook = { look -> looks = looks - look },
                            onEditLook = { look -> selectedLook = look },
                            enableHaptics = profileSettings.enableHaptics
                        )
                    }
                    composable(VibeDestination.TryOn.route) {
                        TryOnScreen(
                            selectedLook = selectedLook,
                            profileSettings = profileSettings,
                            onBack = { navController.popBackStack() },
                            onLookGenerated = { generated ->
                                scope.launch {
                                    looks = if (generated.id == null) {
                                        looks + generated.copy(id = looks.size + 1)
                                    } else {
                                        looks.map { look ->
                                            if (look.id == generated.id) generated else look
                                        }
                                    }
                                }
                            }
                        )
                    }
                    composable(VibeDestination.Profile.route) {
                        ProfileScreen(
                            profileSettings = profileSettings,
                            onSettingsChanged = { profileSettings = it }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VibeBottomBar(
    navController: NavHostController,
    onProfileClick: (String) -> Unit
) {
    val items = listOf(
        VibeDestination.Wardrobe,
        VibeDestination.TryOn,
        VibeDestination.Profile
    )
    val icons = listOf(
        Icons.Rounded.Checkroom,
        Icons.Rounded.Favorite,
        Icons.Rounded.AccountCircle
    )
    val labels = listOf("Wardrobe", "Try On", "Profile")
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)) {
        items.forEachIndexed { index, destination ->
            NavigationBarItem(
                selected = currentRoute == destination.route,
                onClick = { onProfileClick(destination.route) },
                icon = { androidx.compose.material3.Icon(icons[index], contentDescription = labels[index]) },
                label = null,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

private fun sampleLooks(): List<WardrobeLook> = listOf(
    WardrobeLook(
        id = 1,
        title = "Pastel Dream",
        imageUrl = null,
        thumbnailUrl = null,
        style = "Casual",
        intensity = 0.3f
    ),
    WardrobeLook(
        id = 2,
        title = "Mint Aura",
        imageUrl = null,
        thumbnailUrl = null,
        style = "Fantasy",
        intensity = 0.8f
    )
)
