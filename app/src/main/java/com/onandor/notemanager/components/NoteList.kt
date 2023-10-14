package com.onandor.notemanager.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.onandor.notemanager.data.Label
import com.onandor.notemanager.data.Note
import com.onandor.notemanager.data.NoteLocation
import java.time.LocalDateTime
import java.util.UUID

@Composable
fun NoteList(
    notes: List<Note>,
    onNoteClick: (Note) -> Unit,
    modifier: Modifier,
    showNoteContent: Boolean,
    emptyContent: @Composable () -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        if (notes.isEmpty()) {
            emptyContent()
        }
        else {
            LazyColumn {
                items(notes) { note ->
                    NoteItem(note, showNoteContent, onNoteClick)
                }
            }
        }
    }
}

@Composable
fun NoteItem(
    note: Note,
    showNoteContent: Boolean,
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
                if (showNoteContent) {
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
            AnimatedVisibility(visible = !showNoteContent && note.title.isEmpty()) {
                Text(
                    text = note.content,
                    lineHeight = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            AnimatedVisibility(visible = showNoteContent) {
                Text(
                    text = note.content,
                    lineHeight = 16.sp,
                    maxLines = 10,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Preview
@Composable
fun PreviewNoteItem() {
    val note = Note(
        id = UUID.randomUUID(),
        title = "Test note",
        content = "This is a test note",
        labels = emptyList(),
        location = NoteLocation.NOTES,
        creationDate = LocalDateTime.now(),
        modificationDate = LocalDateTime.now()
    )

    NoteItem(
        note = note,
        showNoteContent = true,
        onNoteClick = { }
    )
}