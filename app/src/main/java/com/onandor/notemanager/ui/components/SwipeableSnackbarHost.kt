package com.onandor.notemanager.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.DismissState
import androidx.compose.material3.DismissValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismiss
import androidx.compose.material3.rememberDismissState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    dismissSnackbarState: DismissState = rememberDismissState(
        confirmValueChange = { value ->
            if (value != DismissValue.Default) {
                hostState.currentSnackbarData?.dismiss()
                true
            }
            else {
                false
            }
        }
    ),
    dismissContent: @Composable RowScope.() -> Unit
) {
    LaunchedEffect(dismissSnackbarState.currentValue) {
        if (dismissSnackbarState.currentValue != DismissValue.Default) {
            dismissSnackbarState.reset()
        }
    }
    SwipeToDismiss(
        modifier = modifier,
        state = dismissSnackbarState,
        background = {},
        dismissContent = dismissContent,
    )
}