package com.onandor.notemanager.ui.components

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.onandor.notemanager.R

@Composable
fun PinButton(
    pinned: Boolean,
    onChangePinned: (Boolean) -> Unit
) {
    if (pinned) {
        IconButton(onClick = { onChangePinned(false) }) {
            Icon(
                painter = painterResource(id = R.drawable.ic_note_pinned),
                contentDescription = stringResource(id = R.string.addeditnote_hint_unpin_note)
            )
        }
    } else {
        IconButton(onClick = { onChangePinned(true) }) {
            Icon(
                painter = painterResource(id = R.drawable.ic_note_unpinned),
                contentDescription = stringResource(id = R.string.addeditnote_hint_pin_note)
            )
        }
    }
}