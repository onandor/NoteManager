package com.onandor.notemanager.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.runtime.Composable
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.onandor.notemanager.R
import com.onandor.notemanager.data.Label
import com.onandor.notemanager.navigation.NavDestinations
import com.onandor.notemanager.ui.theme.LocalTheme
import com.onandor.notemanager.utils.LabelColorType
import com.onandor.notemanager.viewmodels.DrawerViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun AppModalDrawer(
    viewModel: DrawerViewModel,
    drawerState: DrawerState,
    currentRoute: String,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    content: @Composable () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(currentRoute) {
        viewModel.changeCurrentRoute(currentRoute)
    }
    
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawer(
                currentRoute = currentRoute,
                closeDrawer = { coroutineScope.launch { drawerState.close() } },
                labels = uiState.labels,
                selectedLabel = uiState.selectedLabel,
                onNavigateToNotes = viewModel::navigateToNotes,
                onNavigateToArchive = viewModel::navigateToArchive,
                onNavigateToTrash = viewModel::navigateToTrash,
                onNavigateToSettings = viewModel::navigateToSettings,
                onNavigateToUserDetails = viewModel::navigateToUserDetails,
                onNavigateToEditLabels = viewModel::navigateToEditLabels,
                onLabelClick = viewModel::navigateToLabelSearch
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
    labels: List<Label>,
    selectedLabel: Label?,
    onNavigateToNotes: () -> Unit,
    onNavigateToArchive: () -> Unit,
    onNavigateToTrash: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToUserDetails: () -> Unit,
    onNavigateToEditLabels: () -> Unit,
    onLabelClick: (Label) -> Unit,
) {
    ModalDrawerSheet {
        val isDarkTheme = LocalTheme.current.isDark
        val itemModifier = Modifier
            .padding(NavigationDrawerItemDefaults.ItemPadding)
            .height(50.dp)
            .width(250.dp)

        Column(
            modifier = Modifier
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                stringResource(R.string.note_manager),
                fontSize = 22.sp,
                modifier = Modifier.padding(start = 25.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            NavigationDrawerItem(
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_drawer_notes_filled),
                        contentDescription = stringResource(id = R.string.drawer_notes)
                    )
                },
                label = { Text(stringResource(id = R.string.drawer_notes)) },
                selected = currentRoute == NavDestinations.NOTES,
                modifier = itemModifier,
                onClick = {
                    onNavigateToNotes()
                    closeDrawer()
                }
            )
            NavigationDrawerItem(
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_drawer_archive_filled),
                        contentDescription = stringResource(id = R.string.drawer_archive)
                    )
                },
                label = { Text(stringResource(id = R.string.drawer_archive)) },
                selected = currentRoute == NavDestinations.ARCHIVE,
                modifier = itemModifier,
                onClick = {
                    onNavigateToArchive()
                    closeDrawer()
                }
            )
            NavigationDrawerItem(
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = stringResource(id = R.string.drawer_trash)
                    )
                },
                label = { Text(stringResource(id = R.string.drawer_trash)) },
                selected = currentRoute == NavDestinations.TRASH,
                modifier = itemModifier,
                onClick = {
                    onNavigateToTrash()
                    closeDrawer()
                }
            )
            HorizontalDivider(modifier = Modifier
                .width(274.dp)
                .padding(top = 5.dp, bottom = 5.dp))
            Row(
                modifier = itemModifier.padding(start = 15.dp, end = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(id = R.string.drawer_labels),
                    fontSize = 16.sp
                )
                TextButton(onClick = { onNavigateToEditLabels(); closeDrawer() }) {
                    Text(text = stringResource(id = R.string.drawer_edit))
                }
            }
            labels.forEach { label ->
                NavigationDrawerItem(
                    icon = {
                        if (label.color.type == LabelColorType.None) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_label_filled),
                                contentDescription = ""
                            )
                        } else {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_label_filled),
                                contentDescription = "",
                                tint = if (isDarkTheme) label.color.darkColor else label.color.lightColor
                            )
                        }
                    },
                    label = {
                        Text(
                            text = label.title,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    selected = label.title == selectedLabel?.title && currentRoute == NavDestinations.LABEL_SEARCH,
                    modifier = itemModifier,
                    onClick = {
                        onLabelClick(label)
                        closeDrawer()
                    }
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            HorizontalDivider(modifier = Modifier
                .width(274.dp)
                .padding(top = 5.dp, bottom = 5.dp))
            NavigationDrawerItem(
                icon = {
                    Icon(
                        imageVector = Icons.Filled.AccountCircle,
                        contentDescription = stringResource(id = R.string.drawer_account)
                    )
                },
                label = { Text(stringResource(id = R.string.drawer_account)) },
                selected = currentRoute == NavDestinations.USER_DETAILS,
                modifier = itemModifier,
                onClick = {
                    onNavigateToUserDetails()
                    closeDrawer()
                }
            )
            NavigationDrawerItem(
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = stringResource(id = R.string.drawer_settings)
                    )
                },
                label = { Text(stringResource(id = R.string.drawer_settings)) },
                selected = currentRoute == NavDestinations.SETTINGS,
                modifier = itemModifier.padding(bottom = 10.dp),
                onClick = {
                    onNavigateToSettings()
                    closeDrawer()
                }
            )
        }
    }
}