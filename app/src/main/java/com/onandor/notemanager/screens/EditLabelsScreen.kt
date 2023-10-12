package com.onandor.notemanager.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.onandor.notemanager.R
import com.onandor.notemanager.data.Label
import com.onandor.notemanager.viewmodels.EditLabelsViewModel

@Composable
fun EditLabelsScreen(
    viewModel: EditLabelsViewModel = hiltViewModel()
) {
    val snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
    Scaffold(
        topBar = { EditLabelsTopAppBar(viewModel::navigateBack) },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            if (uiState.labels.isEmpty()) {
                Text(stringResource(id = R.string.edit_labels_no_labels))
            }
            else {
                LabelList(
                    labels = uiState.labels,
                    onLabelClick = viewModel::labelClick
                )
            }
        }
    }
}

@Composable
fun LabelList(
    labels: List<Label>,
    onLabelClick: (Label) -> Unit
) {
    LazyColumn {
        items(labels) { label ->
            LabelItem(label, onLabelClick)
        }
    }
}

@Composable
fun LabelItem(
    label: Label,
    onLabelClick: (Label) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onLabelClick(label) }
    ) {
        Text(label.id.toString())
        Text(label.color)
        Text(label.title)
    }
}

@Composable
fun EditLabelsTopAppBar(navigateBack: () -> Unit) {
    Surface(modifier = Modifier
        .fillMaxWidth()
        .height(65.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navigateBack() }) {
                Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(R.string.edit_labels_go_back))
            }
            Text(stringResource(R.string.labels), fontSize = 20.sp)
        }
    }
}