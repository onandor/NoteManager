package com.onandor.notemanager.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.onandor.notemanager.ui.components.AppModalDrawer
import com.onandor.notemanager.viewmodels.NavigationViewModel
import com.onandor.notemanager.ui.screens.AddEditNoteScreen
import com.onandor.notemanager.ui.screens.ArchiveScreen
import com.onandor.notemanager.ui.screens.EditLabelsScreen
import com.onandor.notemanager.ui.screens.NotesScreen
import com.onandor.notemanager.ui.screens.OnboardingScreen
import com.onandor.notemanager.ui.screens.SettingsScreen
import com.onandor.notemanager.ui.screens.SignInRegisterScreen
import com.onandor.notemanager.ui.screens.SignedOutScreen
import com.onandor.notemanager.ui.screens.TrashScreen
import com.onandor.notemanager.ui.screens.UserDetailsScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException

@Composable
fun NavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed),
    startDestination: String = NavDestinations.NOTES,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    viewModel: NavigationViewModel = hiltViewModel()
) {
    val currentNavBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentNavBackStackEntry?.destination?.route ?: startDestination
    val navManagerState by viewModel.navigationManager.navActions.collectAsState()

    // Change theme smoothly
    val animatedSurfaceColor = animateColorAsState(
        targetValue = MaterialTheme.colorScheme.surface,
        animationSpec = tween(500),
        label = ""
    )

    LaunchedEffect(navManagerState) {
        navManagerState?.let {
            try {
                navController.navigate(it.destination, it.navOptions)
                // Live Edit craps out here because the graph is null inside the controller for some
                // reason, it isn't a problem during normal use, so this solves the issue
            } catch (_: IllegalArgumentException) {}
        }
    }

    BackHandler(enabled = drawerState.isOpen || drawerState.isAnimationRunning) {
        coroutineScope.launch {
            drawerState.close()
        }
    }

    Surface(
        color = animatedSurfaceColor.value
    ) {
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = modifier
                .navigationBarsPadding()
                .imePadding()
        ) {
            composable(NavDestinations.NOTES) {
                AppModalDrawer(
                    drawerState = drawerState,
                    currentRoute = currentRoute,
                ) {
                    NotesScreen(
                        onOpenDrawer = { coroutineScope.launch { drawerState.open() } }
                    )
                }
            }
            composable(
                NavDestinations.ADD_EDIT_NOTE,
                arguments = listOf(
                    navArgument(NavDestinationArgs.NOTE_ID_ARG) {
                        type = NavType.StringType
                        nullable = true
                    }
                )
            ) {
                AddEditNoteScreen()
            }
            composable(NavDestinations.ARCHIVE) {
                AppModalDrawer(
                    drawerState = drawerState,
                    currentRoute = currentRoute,
                ) {
                    ArchiveScreen(
                        onOpenDrawer = { coroutineScope.launch { drawerState.open() } }
                    )
                }
            }
            composable(NavDestinations.TRASH) {
                AppModalDrawer(
                    drawerState = drawerState,
                    currentRoute = currentRoute,
                ) {
                    TrashScreen(
                        onOpenDrawer = { coroutineScope.launch { drawerState.open() } }
                    )
                }
            }
            composable(NavDestinations.SETTINGS) {
                SettingsScreen()
            }
            composable(NavDestinations.ONBOARDING) {
                OnboardingScreen()
            }
            composable(NavDestinations.SIGN_IN_REGISTER) {
                SignInRegisterScreen()
            }
            composable(NavDestinations.USER_DETAILS) {
                UserDetailsScreen()
            }
            composable(NavDestinations.SIGNED_OUT) {
                SignedOutScreen()
            }
            composable(NavDestinations.EDIT_LABELS) {
                EditLabelsScreen()
            }
        }
    }
}