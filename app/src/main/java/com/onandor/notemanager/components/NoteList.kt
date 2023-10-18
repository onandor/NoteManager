package com.onandor.notemanager.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.onandor.notemanager.data.Label
import com.onandor.notemanager.data.Note
import com.onandor.notemanager.data.NoteLocation
import com.onandor.notemanager.utils.LabelColors
import java.time.LocalDateTime
import java.util.UUID

@Composable
fun NoteList(
    notes: List<Note>,
    onNoteClick: (Note) -> Unit,
    modifier: Modifier,
    collapsedView: Boolean,
    emptyContent: @Composable () -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        if (notes.isEmpty()) {
            emptyContent()
        }
        else {
            LazyColumn {
                items(notes) { note ->
                    NoteItem(note, collapsedView, onNoteClick)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NoteItem(
    note: Note,
    collapsedView: Boolean,
    onNoteClick: (Note) -> Unit
) {
    Surface (
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 5.dp, end = 5.dp, bottom = 5.dp)
            .border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(20.dp)
            )
            .clip(RoundedCornerShape(20.dp))
            .clickable { onNoteClick(note) },
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(15.dp)
        ) {
            if (note.title.isNotEmpty()) {
                Text(
                    text = note.title,
                    fontSize = 22.sp
                )
                if (!collapsedView) {
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
            AnimatedVisibility(visible = collapsedView && note.title.isEmpty()) {
                Text(
                    text = note.content,
                    lineHeight = 16.sp,
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
        note = note,
        collapsedView = false,
        onNoteClick = { }
    )
}