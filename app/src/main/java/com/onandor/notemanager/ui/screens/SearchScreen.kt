package com.onandor.notemanager.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.onandor.notemanager.R
import com.onandor.notemanager.data.Note
import com.onandor.notemanager.viewmodels.SearchViewModel

@Composable
fun SearchScreen(viewModel: SearchViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier
            .statusBarsPadding()
            .navigationBarsPadding(),
        topBar = {
            SearchBar(
                text = uiState.searchForm.text,
                onTextChanged = viewModel::updateSearchText,
                onBackClicked = viewModel::goBack
            )
        }
    ) { innerPadding ->
        if (uiState.loading) {
            Text("Loading", modifier = Modifier.padding(innerPadding))
        }
        else {
            ResultList(
                modifier = Modifier.padding(innerPadding),
                mainNotes = uiState.mainNotes,
                archiveNotes = uiState.archiveNotes
            )
        }
    }
}

@Composable
fun ResultList(
    modifier: Modifier,
    mainNotes: List<Note>,
    archiveNotes: List<Note>
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text("Notes:")
        LazyColumn {
            itemsIndexed(
                items = mainNotes,
                key = { _, note -> note.id }
            ) { _, note ->
                Row {
                    Text(note.title)
                }
            }
        }
        Text("Archive:")
        LazyColumn {
            itemsIndexed(
                items = archiveNotes,
                key = { _, note -> note.id }
            ) { _, note ->
                Row {
                    Text(note.title)
                }
            }
        }
    }
}

@Composable
fun SearchBar(
    text: String,
    onTextChanged: (String) -> Unit,
    onBackClicked: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            val textFieldColors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
            IconButton(onClick = onBackClicked) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go back")
            }
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = text,
                onValueChange = onTextChanged,
                placeholder = {
                    Text(
                        text = stringResource(id = R.string.edit_labels_hint_title),
                        //fontSize = 20.sp
                    )
                },
                colors = textFieldColors,
                //textStyle = TextStyle(fontSize = 24.sp),
                singleLine = true
            )
        }
    }
}