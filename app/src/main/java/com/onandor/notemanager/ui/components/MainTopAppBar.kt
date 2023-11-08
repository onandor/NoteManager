package com.onandor.notemanager.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.onandor.notemanager.R
import com.onandor.notemanager.utils.NoteSorting

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTopAppBar(
    title: String,
    scrollBehavior: TopAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(),
    onOpenDrawer: () -> Unit,
    noteListCollapsedView: Boolean,
    onToggleNoteListCollapsedView: () -> Unit,
    currentSorting: NoteSorting,
    onNoteSortingChanged: (NoteSorting) -> Unit,
    onSearchClicked: () -> Unit
) {
    ColoredStatusBarTopAppBar(
        scrollBehavior = scrollBehavior,
        title = {
            Text(
                text = title,
                modifier = Modifier.padding(start = 5.dp)
            )
        },
        navigationIcon = {
            IconButton(onClick = onOpenDrawer) {
                Icon(Icons.Filled.Menu, contentDescription = stringResource(R.string.topbar_drawer))
            }
        },
        actions = {
            IconButton(onClick = onSearchClicked) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = stringResource(id = R.string.topbar_search_notes)
                )
            }
            NoteSortingMenu(
                currentSorting = currentSorting,
                onSortingClicked = onNoteSortingChanged
            )
            IconButton(onClick = onToggleNoteListCollapsedView) {
                if (noteListCollapsedView) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_note_list_expanded_outlined),
                        contentDescription = stringResource(R.string.topbar_change_view)
                    )
                }
                else {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_note_list_collapsed_outlined),
                        contentDescription = stringResource(R.string.topbar_change_view)
                    )
                }
            }
        }
    )
}