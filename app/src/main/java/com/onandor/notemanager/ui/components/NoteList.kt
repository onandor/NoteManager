package com.onandor.notemanager.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.onandor.notemanager.data.Label
import com.onandor.notemanager.data.Note
import com.onandor.notemanager.data.NoteLocation
import com.onandor.notemanager.utils.LabelColors
import com.onandor.notemanager.utils.NoteComparisonField
import com.onandor.notemanager.utils.NoteSorting
import com.onandor.notemanager.utils.Order
import java.time.LocalDateTime
import java.util.UUID

data class NoteListState(
    val collapsed: Boolean = false,
    val sorting: NoteSorting = NoteSorting(NoteComparisonField.ModificationDate, Order.Descending)
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteList(
    notes: List<Note>,
    selectedNotes: List<Note>,
    onNoteClick: (Note) -> Unit,
    onNoteLongClick: (Note) -> Unit,
    modifier: Modifier,
    collapsedView: Boolean
) {
    LazyColumn(modifier = modifier) {
        itemsIndexed(
            items = notes,
            key = { _, note -> note.id }
        ) { _, note ->
            NoteItem(
                modifier = Modifier.animateItemPlacement(),
                note = note,
                selected = selectedNotes.contains(note),
                collapsedView = collapsedView,
                onNoteClick = onNoteClick,
                onNoteLongClick = onNoteLongClick
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun NoteItem(
    modifier: Modifier,
    note: Note,
    selected: Boolean,
    collapsedView: Boolean,
    onNoteClick: (Note) -> Unit,
    onNoteLongClick: (Note) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val borderSize = if (selected) 4.dp else 3.dp
    val borderColor = if (selected)
        MaterialTheme.colorScheme.secondary
    else
        MaterialTheme.colorScheme.surfaceVariant

    Surface (
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
            .border(
                width = borderSize,
                color = borderColor,
                shape = RoundedCornerShape(20.dp)
            )
            .clip(RoundedCornerShape(20.dp))
            .combinedClickable(
                onClick = { onNoteClick(note) },
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onNoteLongClick(note)
                }
            ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 20.dp)
        ) {
            if (note.title.isNotEmpty()) {
                Text(
                    text = note.title,
                    fontSize = 21.sp
                )
                if (!collapsedView) {
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
            AnimatedVisibility(visible = collapsedView && note.title.isEmpty()) {
                Text(
                    text = note.content.trim(),
                    lineHeight = MaterialTheme.typography.bodySmall.lineHeight,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            AnimatedVisibility(visible = !collapsedView) {
                Column {
                    Text(
                        text = note.content,
                        lineHeight = 16.sp,
                        maxLines = 10,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (note.labels.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.Start),
                            verticalArrangement = Arrangement.spacedBy(5.dp, Alignment.Top)
                        ) {
                            note.labels.forEach { label ->
                                LabelComponent(
                                    label = label,
                                    fontSize = 14.sp,
                                    maxLength = 10,
                                    padding = 5.dp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewNoteItem() {
    val label = Label(
        id = UUID.randomUUID(),
        title = "Test label",
        color = LabelColors.green
    )
    val note = Note(
        id = UUID.randomUUID(),
        title = "Test note",
        content = "This is a test note",
        labels = listOf(label),
        location = NoteLocation.NOTES,
        creationDate = LocalDateTime.now(),
        modificationDate = LocalDateTime.now()
    )

    NoteItem(
        modifier = Modifier,
        note = note,
        selected = false,
        collapsedView = false,
        onNoteClick = { },
        onNoteLongClick = { }
    )
}