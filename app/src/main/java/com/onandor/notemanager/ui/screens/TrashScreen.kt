package com.onandor.notemanager.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.onandor.notemanager.R
import com.onandor.notemanager.ui.components.NoteList
import com.onandor.notemanager.utils.AddEditResults
import com.onandor.notemanager.viewmodels.TrashViewModel
import kotlinx.coroutines.launch

@Composable
fun TrashScreen(
    viewModel: TrashViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }

    Scaffold (
        modifier = Modifier.statusBarsPadding(),
        topBar = {
            TrashTopAppBar(
                onNavigateBack = viewModel::navigateBack,
                onEmptyTrash = viewModel::emptyTrash
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        if (uiState.notes.isEmpty()) {
            Box(modifier = Modifier.padding(innerPadding)) {
                Text("The trash is empty")
            }
        }
        else {
            NoteList(
                notes = uiState.notes,
                onNoteClick = viewModel::noteClick,
                modifier = Modifier.padding(innerPadding),
                collapsedView = false
            )
        }

        if (uiState.addEditResult != AddEditResults.NONE) {
            val resultText = stringResource(id = uiState.addEditResult.resource)
            LaunchedEffect(uiState.addEditResult) {
                scope.launch {
                    snackbarHostState.showSnackbar(resultText)
                }
                viewModel.addEditResultSnackbarShown()
            }
        }

        BackHandler {
            viewModel.navigateBack()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TrashTopAppBar(
    onNavigateBack: () -> Unit,
    onEmptyTrash: () -> Unit
) {
    TopAppBar(
        title = { Text(stringResource(R.string.trash)) },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.topbar_drawer)
                )
            }
        },
        actions = {
            IconButton(onClick = onEmptyTrash) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_trash_empty_filled),
                    contentDescription = stringResource(R.string.trash_empty_trash)
                )
            }
        }
    )
}