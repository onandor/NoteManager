package com.onandor.notemanager.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumInteractiveComponentEnforcement
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.onandor.notemanager.R
import com.onandor.notemanager.ui.theme.NoteManagerTheme
import com.onandor.notemanager.utils.NoteComparisonField
import com.onandor.notemanager.utils.NoteSorting
import com.onandor.notemanager.utils.Order

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    onOpenDrawer: () -> Unit,
    noteListCollapsedView: Boolean,
    onToggleNoteListCollapsedView: () -> Unit,
    currentSorting: NoteSorting,
    onNoteSortingChanged: (NoteSorting) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(50),
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 10.dp),
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.width(5.dp))
            IconButton(onClick = onOpenDrawer) {
                Icon(Icons.Filled.Menu, contentDescription = stringResource(R.string.topbar_drawer))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(stringResource(R.string.topbar_search_notes))
            Spacer(modifier = Modifier.weight(1f))
            Row {
                CompositionLocalProvider(LocalMinimumInteractiveComponentEnforcement provides false) {
                    NoteSortingMenu(
                        currentSorting = currentSorting,
                        onSortingClicked = onNoteSortingChanged
                    )
                    IconButton(onClick = onToggleNoteListCollapsedView) {
                        if (noteListCollapsedView) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_note_list_expanded_filled),
                                contentDescription = stringResource(R.string.topbar_change_view)
                            )
                        }
                        else {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_note_list_collapsed_filled),
                                contentDescription = stringResource(R.string.topbar_change_view)
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
        }
    }
}

@Preview(name = "Light mode")
@Preview(name = "Dark mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun TopBarPreview() {
    NoteManagerTheme {
        TopBar(
            onOpenDrawer = { },
            noteListCollapsedView = true,
            onToggleNoteListCollapsedView = { },
            currentSorting = NoteSorting(NoteComparisonField.CreationDate, Order.Ascending),
            onNoteSortingChanged = { }
        )
    }
}