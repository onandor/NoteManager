package com.onandor.notemanager.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.onandor.notemanager.R
import com.onandor.notemanager.data.Note
import com.onandor.notemanager.ui.components.NoteItem
import com.onandor.notemanager.viewmodels.SearchViewModel

@Composable
fun SearchScreen(viewModel: SearchViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    Scaffold(
        modifier = Modifier
            .navigationBarsPadding()
            .imePadding(),
        topBar = {
            SearchBar(
                text = uiState.searchForm.text,
                onTextChanged = viewModel::updateSearchText,
                onBackClicked = { focusManager.clearFocus(); viewModel.navigateBack() }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            LoadingIndicator(loading = uiState.loading)
            AnimatedVisibility(
                visible = uiState.emptySearch,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                EmptyContent(text = stringResource(id = R.string.search_empty_search))
            }
            AnimatedVisibility(
                visible = !uiState.emptySearch && !uiState.loading && uiState.emptyResult,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                EmptyContent(text = stringResource(id = R.string.search_no_results))
            }
            AnimatedVisibility(
                visible = !uiState.emptySearch && !uiState.loading && !uiState.emptyResult,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                ResultList(
                    mainNotes = uiState.mainNotes,
                    archiveNotes = uiState.archiveNotes
                )
            }
        }
    }

    BackHandler {
        viewModel.navigateBack()
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ResultList(
    mainNotes: List<Note>,
    archiveNotes: List<Note>
) {
    LazyColumn(
        modifier = Modifier
            .padding(top = 5.dp)
            .fillMaxWidth()
            .animateContentSize()
    ) {
        itemsIndexed(
            items = mainNotes,
            key = { _, note -> note.id }
        ) { _, note ->
            NoteItem(
                modifier = Modifier.animateItemPlacement(),
                note = note,
                collapsedView = false,
                onNoteClick = { }
            )
        }
        item {
            if (archiveNotes.isNotEmpty()) {
                Text(
                    modifier = Modifier
                        .padding(start = 30.dp, top = 5.dp, bottom = 10.dp)
                        .animateItemPlacement(),
                    text = stringResource(id = R.string.search_archive),
                    fontSize = 15.sp
                )
            }
        }
        itemsIndexed(
            items = archiveNotes,
            key = { _, note -> note.id }
        ) { _, note ->
            NoteItem(
                modifier = Modifier.animateItemPlacement(),
                note = note,
                collapsedView = false,
                onNoteClick = { }
            )
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
            modifier = Modifier.statusBarsPadding(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val textFieldColors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
            IconButton(
                modifier = Modifier.padding(start = 5.dp),
                onClick = onBackClicked
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(id = R.string.search_button_hint_go_back)
                )
            }
            TextField(
                modifier = Modifier.weight(1f),
                value = text,
                onValueChange = onTextChanged,
                placeholder = {
                    Text(
                        text = stringResource(id = R.string.search_hint_search)
                    )
                },
                colors = textFieldColors,
                singleLine = true
            )
            if (text.isNotEmpty()) {
                IconButton(
                    modifier = Modifier.padding(end = 5.dp),
                    onClick = { onTextChanged("") }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Clear,
                        contentDescription = stringResource(id = R.string.search_hint_clear_search)
                    )
                }
            }
        }
    }
}

@Composable
fun LoadingIndicator(loading: Boolean) {
    AnimatedVisibility(
        visible = loading,
        enter = fadeIn() + slideInVertically(),
        exit = fadeOut() + slideOutVertically()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier
                    .padding(top = 10.dp)
                    .width(40.dp)
                    .height(40.dp),
                shape = RoundedCornerShape(100.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(7.dp)
                        .width(20.dp)
                        .aspectRatio(1f)
                )
            }
        }
    }
}

@Composable
fun EmptyContent(text: String) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                modifier = Modifier
                    .width(120.dp)
                    .height(120.dp),
                imageVector = Icons.Filled.Search,
                contentDescription = ""
            )
            Text(
                modifier = Modifier.padding(start = 40.dp, end = 40.dp),
                text = text,
                textAlign = TextAlign.Center
            )
        }
    }

}

@Preview
@Composable
fun PreviewLoadingIndicator() {
    LoadingIndicator(loading = true)
}