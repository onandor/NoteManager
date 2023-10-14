package com.onandor.notemanager.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.onandor.notemanager.R
import com.onandor.notemanager.components.NoteList
import com.onandor.notemanager.components.TopBar
import com.onandor.notemanager.data.Note
import com.onandor.notemanager.utils.AddEditResults
import com.onandor.notemanager.viewmodels.TrashViewModel
import kotlinx.coroutines.launch

@Composable
fun TrashScreen(
    onOpenDrawer: () -> Unit,
    viewModel: TrashViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
    Scaffold (
        topBar = {
            TrashTopBar(
                openDrawer = onOpenDrawer,
                onEmptyTrash = viewModel::emptyTrash
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        NoteList(
            notes = uiState.notes,
            onNoteClick = viewModel::noteClick,
            modifier = Modifier.padding(innerPadding),
            showNoteContent = true,
            emptyContent = { TrashEmptyContent() }
        )

        if (uiState.addEditResult != AddEditResults.NONE) {
            val resultText = stringResource(id = uiState.addEditResult.resource)
            LaunchedEffect(uiState.addEditResult) {
                scope.launch {
                    snackbarHostState.showSnackbar(resultText)
                }
                viewModel.addEditResultSnackbarShown()
            }
        }
    }
}

@Composable
fun TrashEmptyContent() {
    Text("The trash is empty")
}

@Composable
fun TrashTopBar(
    openDrawer: () -> Unit,
    onEmptyTrash: () -> Unit
) {
    Surface(modifier = Modifier.fillMaxWidth().height(65.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { openDrawer() }) {
                    Icon(
                        Icons.Filled.Menu,
                        contentDescription = stringResource(R.string.topbar_drawer)
                    )
                }
                Text(stringResource(R.string.trash), fontSize = 20.sp)
            }
            IconButton(onClick = { onEmptyTrash() }) {
                Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.trash_empty_trash))
            }
        }
    }
}