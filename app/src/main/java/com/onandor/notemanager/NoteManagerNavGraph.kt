package com.onandor.notemanager

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.onandor.notemanager.components.AppModalDrawer
import com.onandor.notemanager.screens.AddEditNoteScreen
import com.onandor.notemanager.screens.ArchiveScreen
import com.onandor.notemanager.screens.NotesScreen
import com.onandor.notemanager.screens.SettingsScreen
import com.onandor.notemanager.screens.TrashScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun NoteManagerNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed),
    startDestination: String = NMDestinations.NOTES_ROUTE,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    navActions: NMNavigationActions = remember(navController) {
        NMNavigationActions(navController)
    }
) {
    val currentNavBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentNavBackStackEntry?.destination?.route ?: startDestination

    Scaffold { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = modifier.padding(innerPadding)
        ) {
            composable(
                NMDestinations.NOTES_ROUTE,
                arguments = listOf(
                    navArgument(NMDestinationsArgs.NOTE_ID_ARG) {
                        type = NavType.StringType
                        nullable = true
                    })
            ) {
                AppModalDrawer(
                    drawerState = drawerState,
                    navActions = navActions,
                    currentRoute = currentRoute,
                ) {
                    NotesScreen(
                        onAddTask = { navActions.navigateToAddEditNote(null) },
                        openDrawer = { coroutineScope.launch { drawerState.open() } },
                        onNoteClick = { note -> navActions.navigateToAddEditNote(note.id) }
                    )
                }
            }
            composable(NMDestinations.ADD_EDIT_NOTE_ROUTE) {
                AddEditNoteScreen(
                    goBack = { navActions.navigateUp() }
                )
            }
            composable(NMDestinations.ARCHIVE_ROUTE) {
                AppModalDrawer(
                    drawerState = drawerState,
                    navActions = navActions,
                    currentRoute = currentRoute,
                ) {
                    ArchiveScreen(
                        openDrawer = { coroutineScope.launch { drawerState.open() } }
                    )
                }
            }
            composable(NMDestinations.TRASH_ROUTE) {
                AppModalDrawer(
                    drawerState = drawerState,
                    navActions = navActions,
                    currentRoute = currentRoute,
                ) {
                    TrashScreen(
                        openDrawer = { coroutineScope.launch { drawerState.open() } }
                    )
                }
            }
            composable(NMDestinations.SETTINGS_ROUTE) {
                SettingsScreen(goBack = { navActions.navigateUp() })
            }
        }
    }
}