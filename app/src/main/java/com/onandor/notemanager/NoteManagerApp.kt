package com.onandor.notemanager

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntSize
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.onandor.notemanager.components.AppModalDrawer
import com.onandor.notemanager.screens.AddEditNoteScreen
import com.onandor.notemanager.screens.ArchiveScreen
import com.onandor.notemanager.screens.NoteListScreen
import com.onandor.notemanager.screens.SettingsScreen
import com.onandor.notemanager.screens.TrashScreen

@Composable
fun NoteManagerApp(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed),
    startDestination: String = NMDestinations.NOTE_LIST_ROUTE,
    navActions: NMNavigationActions = remember(navController) {
        NMNavigationActions(navController)
    }
) {
    val currentNavBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentNavBackStackEntry?.destination?.route ?: startDestination

    Scaffold(
        floatingActionButton = {
            AnimatedVisibility(
                visible = currentRoute == NMDestinations.NOTE_LIST_ROUTE,
                enter = fadeIn() + expandIn { IntSize(width = 1, height = 1) }
            ) {
                FloatingActionButton(onClick = { navActions.navigateToAddEditNote() }) {
                    Icon(Icons.Default.Add, contentDescription = "Add note")
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = modifier.padding(innerPadding)
        ) {
            composable(NMDestinations.NOTE_LIST_ROUTE) {
                AppModalDrawer(
                    drawerState = drawerState,
                    navActions = navActions,
                    currentRoute = currentRoute,
                ) {
                    NoteListScreen()
                }
            }
            composable(NMDestinations.ADD_EDIT_NOTE_ROUTE) {
                AddEditNoteScreen()
            }
            composable(NMDestinations.ARCHIVE_ROUTE) {
                AppModalDrawer(
                    drawerState = drawerState,
                    navActions = navActions,
                    currentRoute = currentRoute,
                ) {
                    ArchiveScreen()
                }
            }
            composable(NMDestinations.TRASH_ROUTE) {
                AppModalDrawer(
                    drawerState = drawerState,
                    navActions = navActions,
                    currentRoute = currentRoute,
                ) {
                    TrashScreen()
                }
            }
            composable(NMDestinations.SETTINGS_ROUTE) {
                AppModalDrawer(
                    drawerState = drawerState,
                    navActions = navActions,
                    currentRoute = currentRoute,
                ) {
                    SettingsScreen()
                }
            }
        }
    }
}