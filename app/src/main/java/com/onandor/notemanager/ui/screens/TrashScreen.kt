package com.onandor.notemanager.ui.screens

import android.widget.Space
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.window.Dialog
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
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold (
        modifier = Modifier.statusBarsPadding(),
        topBar = {
            TrashTopAppBar(
                onNavigateBack = viewModel::navigateBack,
                onEmptyTrash = viewModel::openConfirmationDialog,
                emptyTrashEnabled = uiState.notes.isNotEmpty()
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
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

        if (uiState.confirmationDialogOpen) {
            ConfirmationDialog(
                onConfirmation = viewModel::emptyTrash,
                onDismissRequest = viewModel::closeConfirmationDialog
            )
        }

        BackHandler {
            viewModel.navigateBack()
        }
    }
}

@Composable
private fun ConfirmationDialog(
    onConfirmation: () -> Unit,
    onDismissRequest: () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Card(shape = RoundedCornerShape(16.dp)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = 15.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.height(10.dp))
                Icon(
                    modifier = Modifier.size(35.dp),
                    imageVector = Icons.Filled.Warning,
                    contentDescription = ""
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(stringResource(id = R.string.dialog_empty_trash_description))
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismissRequest) {
                        Text(stringResource(id = R.string.dialog_empty_trash_button_cancel))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = onConfirmation,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text(stringResource(id = R.string.dialog_empty_trash_button_confirm))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TrashTopAppBar(
    onNavigateBack: () -> Unit,
    onEmptyTrash: () -> Unit,
    emptyTrashEnabled: Boolean
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
            IconButton(
                onClick = onEmptyTrash,
                enabled = emptyTrashEnabled
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_trash_empty_filled),
                    contentDescription = stringResource(R.string.trash_empty_trash)
                )
            }
        }
    )
}