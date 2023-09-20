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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            stringResource(R.string.note_manager),
            fontSize = 22.sp,
            modifier = Modifier.padding(start = 25.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        NavigationDrawerItem(
            icon = { Icon(Icons.Filled.List, contentDescription = stringResource(id = R.string.drawer_notes)) },
            label = { Text(stringResource(id = R.string.drawer_notes)) },
            selected = currentRoute == NMDestinations.NOTES_ROUTE,
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
            icon = { Icon(painterResource(id = R.drawable.ic_menu_archive_list),
                contentDescription = stringResource(id = R.string.drawer_archive)) },
            label = { Text(stringResource(id = R.string.drawer_archive)) },
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
            icon = { Icon(Icons.Filled.Delete, contentDescription = stringResource(id = R.string.drawer_trash)) },
            label = { Text(stringResource(id = R.string.drawer_trash)) },
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
            icon = { Icon(Icons.Filled.Settings, contentDescription = stringResource(id = R.string.drawer_settings)) },
            label = { Text(stringResource(id = R.string.drawer_settings)) },
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