package com.onandor.notemanager.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.DismissState
import androidx.compose.material3.DismissValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.rememberDismissState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    state: DismissState = rememberDismissState(),
    content: @Composable RowScope.() -> Unit
) {
    LaunchedEffect(state.currentValue) {
        if (state.currentValue != DismissValue.Default) {
            hostState.currentSnackbarData?.dismiss()
            delay(100)
            state.snapTo(DismissValue.Default)
        }
    }

    SwipeToDismissBox(
        modifier = modifier,
        state = state,
        backgroundContent = {},
        content = content
    )
}