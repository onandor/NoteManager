package com.onandor.notemanager.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.onandor.notemanager.data.Note

@Composable
fun NoteList(
    notes: List<Note>,
    onNoteClick: (Note) -> Unit,
    modifier: Modifier,
    emptyContent: @Composable () -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        if (notes.isEmpty()) {
            emptyContent()
        }
        else {
            LazyColumn {
                items(notes) { note ->
                    NoteItem(note, onNoteClick)
                }
            }
        }
    }
}

@Composable
fun NoteItem(
    note: Note,
    onNoteClick: (Note) -> Unit
) {
    Column (
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onNoteClick(note) }
    ) {
        Text(note.title)
        Text(note.content)
    }
}