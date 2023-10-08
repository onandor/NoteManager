package com.onandor.notemanager.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerState
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.runtime.Composable
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.onandor.notemanager.R
import com.onandor.notemanager.navigation.NavDestinations
import com.onandor.notemanager.viewmodels.DrawerViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun AppModalDrawer(
    viewModel: DrawerViewModel = hiltViewModel(),
    drawerState: DrawerState,
    currentRoute: String,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    content: @Composable () -> Unit
    ) {
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawer(
                currentRoute = currentRoute,
                closeDrawer = { coroutineScope.launch { drawerState.close() } },
                onNavigateToNotes = viewModel::navigateToNotes,
                onNavigateToArchive = viewModel::navigateToArchive,
                onNavigateToTrash = viewModel::navigateToTrash,
                onNavigateToSettings = viewModel::navigateToSettings,
                onNavigateToUserDetails = viewModel::navigateToUserDetails
            )
        }
    ) {
        content()
    }
}

@Composable
fun AppDrawer(
    currentRoute: String,
    closeDrawer: () -> Unit,
    onNavigateToNotes: () -> Unit,
    onNavigateToArchive: () -> Unit,
    onNavigateToTrash: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToUserDetails: () -> Unit

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
            selected = currentRoute == NavDestinations.NOTES,
            modifier = Modifier
                .padding(NavigationDrawerItemDefaults.ItemPadding)
                .height(50.dp)
                .width(250.dp),
            onClick = {
                onNavigateToNotes()
                closeDrawer()
            }
        )
        NavigationDrawerItem(
            icon = { Icon(painterResource(id = R.drawable.ic_menu_archive_list),
                contentDescription = stringResource(id = R.string.drawer_archive)) },
            label = { Text(stringResource(id = R.string.drawer_archive)) },
            selected = currentRoute == NavDestinations.ARCHIVE,
            modifier = Modifier
                .padding(NavigationDrawerItemDefaults.ItemPadding)
                .height(50.dp)
                .width(250.dp),
            onClick = {
                onNavigateToArchive()
                closeDrawer()
            }
        )
        NavigationDrawerItem(
            icon = { Icon(Icons.Filled.Delete, contentDescription = stringResource(id = R.string.drawer_trash)) },
            label = { Text(stringResource(id = R.string.drawer_trash)) },
            selected = currentRoute == NavDestinations.TRASH,
            modifier = Modifier
                .padding(NavigationDrawerItemDefaults.ItemPadding)
                .height(50.dp)
                .width(250.dp),
            onClick = {
                onNavigateToTrash()
                closeDrawer()
            }
        )
        NavigationDrawerItem(
            icon = { Icon(Icons.Filled.Settings, contentDescription = stringResource(id = R.string.drawer_settings)) },
            label = { Text(stringResource(id = R.string.drawer_settings)) },
            selected = currentRoute == NavDestinations.SETTINGS,
            modifier = Modifier
                .padding(NavigationDrawerItemDefaults.ItemPadding)
                .height(50.dp)
                .width(250.dp),
            onClick = {
                onNavigateToSettings()
                closeDrawer()
            }
        )
        Spacer(modifier = Modifier.weight(1f))
        NavigationDrawerItem(
            icon = { Icon(Icons.Filled.AccountCircle, contentDescription = stringResource(id = R.string.drawer_account)) },
            label = { Text(stringResource(id = R.string.drawer_account)) },
            selected = currentRoute == NavDestinations.USER_DETAILS,
            modifier = Modifier
                .padding(NavigationDrawerItemDefaults.ItemPadding)
                .padding(bottom = 10.dp)
                .height(50.dp)
                .width(250.dp),
            onClick = {
                onNavigateToUserDetails()
                closeDrawer()
            }
        )
    }
}