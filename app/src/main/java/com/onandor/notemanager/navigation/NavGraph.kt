package com.onandor.notemanager.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Scaffold
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
import com.onandor.notemanager.components.AppModalDrawer
import com.onandor.notemanager.viewmodels.NavigationViewModel
import com.onandor.notemanager.screens.AddEditNoteScreen
import com.onandor.notemanager.screens.ArchiveScreen
import com.onandor.notemanager.screens.NotesScreen
import com.onandor.notemanager.screens.OnboardingScreen
import com.onandor.notemanager.screens.SettingsScreen
import com.onandor.notemanager.screens.SignInRegisterScreen
import com.onandor.notemanager.screens.SignedOutScreen
import com.onandor.notemanager.screens.TrashScreen
import com.onandor.notemanager.screens.UserDetailsScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

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

    LaunchedEffect(navManagerState) {
        navManagerState?.let {
            navController.navigate(it.destination, it.navOptions)
        }
    }

    Scaffold { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = modifier.padding(innerPadding)
        ) {
            composable(NavDestinations.NOTES) {
                AppModalDrawer(
                    drawerState = drawerState,
                    currentRoute = currentRoute,
                ) {
                    NotesScreen(
                        openDrawer = { coroutineScope.launch { drawerState.open() } }
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
                        openDrawer = { coroutineScope.launch { drawerState.open() } }
                    )
                }
            }
            composable(NavDestinations.TRASH) {
                AppModalDrawer(
                    drawerState = drawerState,
                    currentRoute = currentRoute,
                ) {
                    TrashScreen(
                        openDrawer = { coroutineScope.launch { drawerState.open() } }
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
        }
    }
}