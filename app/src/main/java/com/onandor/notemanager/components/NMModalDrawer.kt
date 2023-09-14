package com.onandor.notemanager.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerState
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.runtime.Composable
import com.onandor.notemanager.NMNavigationActions
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.onandor.notemanager.NMDestinations
import com.onandor.notemanager.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun AppModalDrawer(
    drawerState: DrawerState,
    navActions: NMNavigationActions,
    currentRoute: String,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    content: @Composable () -> Unit
    ) {
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawer(
                navActions = navActions,
                currentRoute = currentRoute,
                closeDrawer = { coroutineScope.launch { drawerState.close() } }
            )
        }
    ) {
        content()
    }
}

@Composable
fun AppDrawer(
    currentRoute: String,
    navActions: NMNavigationActions,
    closeDrawer: () -> Unit
) {
    ModalDrawerSheet {
        Spacer(modifier = Modifier.height(12.dp))
        NavigationDrawerItem(
            icon = { Icon(Icons.Filled.List, contentDescription = "Notes") },
            label = { Text("Notes") },
            selected = currentRoute == NMDestinations.NOTE_LIST_ROUTE,
            modifier = Modifier
                .padding(NavigationDrawerItemDefaults.ItemPadding)
                .height(50.dp)
                .width(250.dp),
            onClick = {
                navActions.navigateToNoteList()
                closeDrawer()
            }
        )
        NavigationDrawerItem(
            icon = { Icon(painterResource(id = R.drawable.archive_list), contentDescription = "Archive") },
            label = { Text("Archive") },
            selected = currentRoute == NMDestinations.ARCHIVE_ROUTE,
            modifier = Modifier
                .padding(NavigationDrawerItemDefaults.ItemPadding)
                .height(50.dp)
                .width(250.dp),
            onClick = {
                navActions.navigateToArchive()
                closeDrawer()
            }
        )
        NavigationDrawerItem(
            icon = { Icon(Icons.Filled.Delete, contentDescription = "Trash") },
            label = { Text("Trash") },
            selected = currentRoute == NMDestinations.TRASH_ROUTE,
            modifier = Modifier
                .padding(NavigationDrawerItemDefaults.ItemPadding)
                .height(50.dp)
                .width(250.dp),
            onClick = {
                navActions.navigateToTrash()
                closeDrawer()
            }
        )
        NavigationDrawerItem(
            icon = { Icon(Icons.Filled.Settings, contentDescription = "Settings") },
            label = { Text("Settings") },
            selected = currentRoute == NMDestinations.SETTINGS_ROUTE,
            modifier = Modifier
                .padding(NavigationDrawerItemDefaults.ItemPadding)
                .height(50.dp)
                .width(250.dp),
            onClick = {
                navActions.navigateToSettings()
                closeDrawer()
            }
        )
    }
}