package com.onandor.notemanager.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.onandor.notemanager.R
import com.onandor.notemanager.ui.components.SimpleConfirmationDialog
import com.onandor.notemanager.data.Label
import com.onandor.notemanager.ui.theme.LocalTheme
import com.onandor.notemanager.utils.LabelColor
import com.onandor.notemanager.utils.LabelColorType
import com.onandor.notemanager.utils.LabelColors
import com.onandor.notemanager.utils.getAccentColor
import com.onandor.notemanager.utils.getColor
import com.onandor.notemanager.viewmodels.EditLabelsViewModel
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditLabelsScreen(
    viewModel: EditLabelsViewModel = hiltViewModel()
) {
    val snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val labelDialogState = rememberModalBottomSheetState()
    val scrollState = rememberLazyListState()

    Scaffold(
        modifier = Modifier.statusBarsPadding(),
        topBar = { EditLabelsTopAppBar(viewModel::navigateBack) },
        floatingActionButton = {
            AnimatedVisibility(
                visible = !scrollState.canScrollBackward,
                enter = scaleIn(),
                exit = scaleOut()
            ) {
                FloatingActionButton(onClick = viewModel::showAddEditLabelDialog) {
                    Icon(Icons.Default.Add, stringResource(R.string.edit_labels_new_label))
                }
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            if (uiState.labels.isEmpty()) {
                Text(stringResource(id = R.string.edit_labels_no_labels))
            }
            else {
                LazyColumn(
                    modifier = Modifier.padding(start = 20.dp, end = 10.dp),
                    state = scrollState
                ) {
                    items(uiState.labels) { label ->
                        LabelItem(label, viewModel::labelClick, viewModel::showConfirmDeleteLabel)
                    }
                }
            }
        }
    }

    fun hideAddEditLabelDialog() {
        scope.launch {
            labelDialogState.hide()
            viewModel.hideAddEditLabelDialog()
        }
    }

    if (uiState.addEditLabelDialogOpen) {
        // For some reason on API 28 WI.navigationBars returns 0 size insets in the child
        // composable, so the insets need to be read here
        val navBarInsets = WindowInsets.navigationBars
        ModalBottomSheet(
            onDismissRequest = viewModel::hideAddEditLabelDialog,
            sheetState = labelDialogState,
            windowInsets = WindowInsets(0, 0, 0, 0),
            dragHandle = { }
        ) {
            AddEditLabelDialogContent(
                title = uiState.addEditLabelForm.title,
                color = uiState.addEditLabelForm.color,
                onTitleChanged = viewModel::addEditLabelUpdateTitle,
                onColorChanged = viewModel::addEditLabelUpdateColor,
                onSubmitChange = { viewModel.saveLabel(); hideAddEditLabelDialog() },
                colorSelection = viewModel.colorSelection,
                navBarInsets = navBarInsets
            )
        }
    }
    
    if (uiState.deleteDialogOpen) {
        SimpleConfirmationDialog(
            onDismissRequest = viewModel::cancelDeleteLabel,
            onConfirmation = viewModel::deleteLabel,
            text = stringResource(id = R.string.edit_labels_delete_confirmation)
        )
    }

    BackHandler {
        if (uiState.addEditLabelDialogOpen) {
            viewModel.hideAddEditLabelDialog()
        }
        else {
            viewModel.navigateBack()
        }
    }
}

@Composable
private fun LabelItem(
    label: Label,
    onLabelClick: (Label) -> Unit,
    onDeleteLabel: (Label) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onLabelClick(label) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        ColorChoice(labelColor = label.color, size = 30.dp, onClicked = { onLabelClick(label) })
        Spacer(modifier = Modifier.width(15.dp))
        Text(
            modifier = Modifier.weight(100f),
            text = label.title,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontSize = 18.sp
        )
        Spacer(modifier = Modifier.weight(1f))
        IconButton(onClick = { onDeleteLabel(label) }) {
            Icon(Icons.Filled.Delete, stringResource(R.string.edit_labels_hint_delete))
        }
    }
}

@Composable
private fun AddEditLabelDialogContent(
    title: String,
    color: LabelColor,
    onTitleChanged: (String) -> Unit,
    onColorChanged: (LabelColor) -> Unit,
    onSubmitChange: () -> Unit,
    colorSelection: List<LabelColor>,
    navBarInsets: WindowInsets
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(navBarInsets)
            .padding(top = 15.dp, bottom = 15.dp)
    ) {
        val colorSelectionScrollState = rememberScrollState()
        val textFieldColors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        )

        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = title,
            onValueChange = onTitleChanged,
            placeholder = {
                Text(
                    text = stringResource(id = R.string.edit_labels_hint_title),
                    fontSize = 20.sp
                )
            },
            colors = textFieldColors,
            textStyle = TextStyle(fontSize = 24.sp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
        )
        Row(
            modifier = Modifier.padding(start = 15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id = R.string.edit_labels_color),
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(stringResource(id = R.string.edit_labels_no_color))
            RadioButton(
                selected = color.type == LabelColorType.None,
                onClick = { onColorChanged(LabelColors.none) }
            )
        }
        Row(
            modifier = Modifier
                .horizontalScroll(colorSelectionScrollState)
                .fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.width(15.dp))
            colorSelection.forEach { colorChoice ->
                ColorChoice(
                    labelColor = colorChoice,
                    selected = colorChoice == color,
                    onClicked = onColorChanged,
                    size = 50.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
            }
            Spacer(modifier = Modifier.width(5.dp))
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 15.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Button(onClick = onSubmitChange) {
                Text(text = stringResource(id = R.string.edit_labels_save))
            }
        }
    }

}

@Composable
private fun ColorChoice(
    labelColor: LabelColor,
    selected: Boolean = false,
    onClicked: (LabelColor) -> Unit = { },
    size: Dp
) {
    val color: Color
    val accentColor: Color
    if (labelColor.type == LabelColorType.None) {
        color = MaterialTheme.colorScheme.surface
        accentColor = MaterialTheme.colorScheme.onSurface
    }
    else {
        color = labelColor.getColor(LocalTheme.current.isDark)
        accentColor = labelColor.getAccentColor(LocalTheme.current.isDark)
    }

    OutlinedButton(
        modifier = Modifier
            .width(size)
            .height(size),
        onClick = { onClicked(labelColor) },
        contentPadding = PaddingValues(0.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            contentColor = accentColor,
        )
    ) {
        if (selected) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null
            )
        }
    }
}

@Composable
private fun EditLabelsTopAppBar(navigateBack: () -> Unit) {
    Surface(modifier = Modifier
        .fillMaxWidth()
        .height(65.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navigateBack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.edit_labels_go_back))
            }
            Text(stringResource(R.string.labels), fontSize = 20.sp)
        }
    }
}

@Preview
@Composable
private fun LabelItemPreview() {
    val label = Label(UUID.randomUUID(), "Very long test label", LabelColors.green)
    LabelItem(
        label = label,
        onLabelClick = { },
        onDeleteLabel = { }
    )
}