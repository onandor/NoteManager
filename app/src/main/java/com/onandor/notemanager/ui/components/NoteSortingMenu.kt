package com.onandor.notemanager.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.onandor.notemanager.R
import com.onandor.notemanager.utils.NoteComparison
import com.onandor.notemanager.utils.NoteComparisonField
import com.onandor.notemanager.utils.NoteSorting
import com.onandor.notemanager.utils.Order

private val options = listOf(
    NoteComparison.titleAscending,
    NoteComparison.titleDescending,
    NoteComparison.modificationDateAscending,
    NoteComparison.modificationDateDescending,
    NoteComparison.creationDateAscending,
    NoteComparison.creationDateDescending
)

@Composable
fun NoteSortingMenu(
    currentSorting: NoteSorting,
    onSortingClicked: (NoteSorting) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box(contentAlignment = Alignment.Center) {
        IconButton(onClick = { expanded = true }) {
            Icon(
                painter = painterResource(id = R.drawable.ic_note_list_sort),
                contentDescription = stringResource(id = R.string.menu_note_sorting_hint_sorting)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        SortingOption(
                            sorting = option,
                            currentSorting = currentSorting
                        )
                    },
                    onClick = { onSortingClicked(option); expanded = false }
                )
            }
        }
    }
}

@Composable
private fun SortingOption(sorting: NoteSorting, currentSorting: NoteSorting) {
    val iconResource = if (sorting.order == Order.Ascending)
        R.drawable.ic_arrow_up
    else
        R.drawable.ic_arrow_down

    val textResource = when(sorting.compareBy) {
        NoteComparisonField.Title -> R.string.menu_note_sorting_title
        NoteComparisonField.ModificationDate -> R.string.menu_note_sorting_modification_date
        NoteComparisonField.CreationDate -> R.string.menu_note_sorting_creation_date
    }

    val color = if (sorting == currentSorting) {
        MaterialTheme.colorScheme.primary
    }
    else {
        MaterialTheme.colorScheme.onSurface
    }

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(id = textResource),
            color = color
        )
        Spacer(modifier = Modifier.width(10.dp).weight(1f))
        Icon(
            painter = painterResource(id = iconResource),
            contentDescription = "",
            tint = color
        )
    }
}