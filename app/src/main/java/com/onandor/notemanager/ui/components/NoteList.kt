package com.onandor.notemanager.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.onandor.notemanager.R
import com.onandor.notemanager.data.Label
import com.onandor.notemanager.data.Note
import com.onandor.notemanager.data.NoteLocation
import com.onandor.notemanager.utils.LabelColors
import com.onandor.notemanager.utils.NoteComparisonField
import com.onandor.notemanager.utils.NoteSorting
import com.onandor.notemanager.utils.Order
import kotlinx.coroutines.delay
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
    var _collapsedView by remember { mutableStateOf(collapsedView) }
    var alpha by remember { mutableFloatStateOf(1f) }
    val animatedAlpha by animateFloatAsState(
        targetValue = alpha,
        label = "",
        animationSpec = tween(
            durationMillis = 80,
            easing = FastOutSlowInEasing
        )
    )

    LaunchedEffect(collapsedView) {
        if (collapsedView == _collapsedView)
            return@LaunchedEffect

        alpha = 0f
        delay(100)
        _collapsedView = collapsedView
        alpha = 1f
    }

    if (_collapsedView) {
        LazyColumn(modifier = modifier.alpha(animatedAlpha)) {
            itemsIndexed(
                items = notes,
                key = { _, note -> note.id }
            ) { _, note ->
                NoteItem(
                    modifier = Modifier.animateItemPlacement(),
                    note = note,
                    selected = selectedNotes.contains(note),
                    collapsedView = true,
                    onNoteClick = onNoteClick,
                    onNoteLongClick = onNoteLongClick
                )
            }
        }
    } else {
        LazyColumn(modifier = modifier.alpha(animatedAlpha)) {
            itemsIndexed(
                items = notes,
                key = { _, note -> note.id }
            ) { _, note ->
                NoteItem(
                    modifier = Modifier.animateItemPlacement(),
                    note = note,
                    selected = selectedNotes.contains(note),
                    collapsedView = false,
                    onNoteClick = onNoteClick,
                    onNoteLongClick = onNoteLongClick
                )
            }
        }
    }
}

@Composable
private fun StatusIcons(
    bottomPaddingVisible: Boolean,
    pinned: Boolean,
    locked: Boolean
) {
    val bottomPadding = if ((pinned || locked) && bottomPaddingVisible) 10.dp else 0.dp
    Row(modifier = Modifier.padding(bottom = bottomPadding)) {
        if (pinned) {
            Icon(
                modifier = Modifier.size(18.dp),
                painter = painterResource(id = R.drawable.ic_note_pinned),
                contentDescription = ""
            )
        }
        if (pinned && locked) {
            Spacer(modifier = Modifier.width(5.dp))
        }
        if (locked) {
            Icon(
                modifier = Modifier.size(18.dp),
                imageVector = Icons.Filled.Lock,
                contentDescription = ""
            )
        }
        if (pinned || locked) {
            Spacer(modifier = Modifier.width(5.dp))
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
        Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 20.dp)) {
            if (!collapsedView) {
                StatusIcons(
                    bottomPaddingVisible = true,
                    pinned = note.pinned,
                    locked = note.pinHash.isNotEmpty()
                )
            }
            if (note.title.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (collapsedView) {
                        StatusIcons(
                            bottomPaddingVisible = false,
                            pinned = note.pinned,
                            locked = note.pinHash.isNotEmpty()
                        )
                    }
                    Text(
                        text = note.title,
                        fontSize = 21.sp,
                        maxLines = if (collapsedView) 1 else 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (!collapsedView) {
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
            if (collapsedView && note.title.isEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusIcons(
                        bottomPaddingVisible = false,
                        pinned = note.pinned,
                        locked = note.pinHash.isNotEmpty()
                    )
                    Text(
                        text = note.content.trim(),
                        lineHeight = MaterialTheme.typography.bodySmall.lineHeight,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            else if (!collapsedView) {
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
        pinned = true,
        pinHash = "asd",
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