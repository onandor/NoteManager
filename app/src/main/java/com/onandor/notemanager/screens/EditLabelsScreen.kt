package com.onandor.notemanager.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.onandor.notemanager.R
import com.onandor.notemanager.components.DraggableBottomDialog
import com.onandor.notemanager.data.Label
import com.onandor.notemanager.viewmodels.EditLabelsViewModel
import java.util.UUID

@Composable
fun EditLabelsScreen(
    viewModel: EditLabelsViewModel = hiltViewModel()
) {
    val snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    Scaffold(
        topBar = { EditLabelsTopAppBar(viewModel::navigateBack) },
        floatingActionButton = {
            if (!uiState.addEditLabelDialogOpen) {
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
                LabelList(
                    labels = uiState.labels,
                    onLabelClick = viewModel::labelClick,
                    onDeleteLabel = viewModel::deleteLabel
                )
            }
            AddEditLabelDialog(
                title = uiState.addEditLabelForm.title,
                color = uiState.addEditLabelForm.color,
                onTitleChanged = viewModel::addEditLabelUpdateTitle,
                onColorChanged = viewModel::addEditLabelUpdateColor,
                onSubmitChange = viewModel::saveLabel,
                onCloseDialog = viewModel::hideAddEditLabelDialog,
                visible = uiState.addEditLabelDialogOpen,
                colorSelection = viewModel.colorSelection
            )
        }
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
private fun LabelList(
    labels: List<Label>,
    onLabelClick: (Label) -> Unit,
    onDeleteLabel: (Label) -> Unit
) {
    LazyColumn {
        items(labels) { label ->
            LabelItem(label, onLabelClick, onDeleteLabel)
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
        val color = if (label.color.isEmpty())
            MaterialTheme.colorScheme.surfaceVariant
        else
            Color(android.graphics.Color.parseColor(label.color))

        Spacer(modifier = Modifier.width(10.dp))
        ColorChoice(color = color, size = 30.dp, onClicked = { onLabelClick(label) })
        Spacer(modifier = Modifier.width(10.dp))
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
        Spacer(modifier = Modifier.width(5.dp))
    }
}

@Composable
private fun AddEditLabelDialog(
    title: String,
    color: Color?,
    onTitleChanged: (String) -> Unit,
    onColorChanged: (Color?) -> Unit,
    onSubmitChange: () -> Unit,
    onCloseDialog: () -> Unit,
    visible: Boolean,
    colorSelection: List<Color>
) {
    DraggableBottomDialog(
        visible = visible,
        onDismiss = onCloseDialog,
        height = 260.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
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
                singleLine = true
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
                    selected = color == null,
                    onClick = { onColorChanged(null) }
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
                        color = colorChoice,
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
}

@Composable
private fun ColorChoice(
    color: Color,
    selected: Boolean = false,
    onClicked: (Color) -> Unit = { },
    size: Dp
) {
    OutlinedButton(
        modifier = Modifier
            .width(size)
            .height(size),
        onClick = { onClicked(color) },
        contentPadding = PaddingValues(0.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color)
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
private fun AddEditLabelDialogPreview() {
    AddEditLabelDialog(
        title = "",
        color = Color(200, 0, 0),
        onTitleChanged = { },
        onColorChanged = { },
        onSubmitChange = { },
        onCloseDialog = { },
        visible = true,
        colorSelection = listOf(
            Color.Transparent,
            Color(200, 0, 0),
            Color(0, 200, 0),
            Color(0, 0, 200)
        )
    )
}

@Preview
@Composable
private fun LabelItemPreview() {
    val label = Label(UUID.randomUUID(), "Very long test label wow very very long this is the longest", "#005500")
    LabelItem(
        label = label,
        onLabelClick = { },
        onDeleteLabel = { }
    )
}