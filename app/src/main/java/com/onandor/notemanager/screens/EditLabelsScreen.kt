package com.onandor.notemanager.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.onandor.notemanager.R
import com.onandor.notemanager.data.Label
import com.onandor.notemanager.viewmodels.EditLabelsViewModel
import kotlin.math.roundToInt

enum class DragAnchors {
    Open,
    Closed
}

private const val dialogHeight: Int = 260

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
            AddEditLabelComponent(
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
}

@Composable
fun LabelList(
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
fun LabelItem(
    label: Label,
    onLabelClick: (Label) -> Unit,
    onDeleteLabel: (Label) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onLabelClick(label) }
            .border(width = 1.dp, color = Color.Black)
    ) {
        Text(label.id.toString())
        Text(label.color)
        Text(label.title)
        Button(onClick = { onDeleteLabel(label) }) {
            Text("Delete")
        }
    }
}

@Composable
fun AddEditLabelComponent(
    title: String,
    color: Color?,
    onTitleChanged: (String) -> Unit,
    onColorChanged: (Color?) -> Unit,
    onSubmitChange: () -> Unit,
    onCloseDialog: () -> Unit,
    visible: Boolean,
    colorSelection: List<Color>
) {
    val density = LocalDensity.current
    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Gray.copy(alpha = 0.5f))
                    .clickable { onCloseDialog() }
            )
        }
        AnimatedVisibility(
            modifier = Modifier.align(Alignment.BottomCenter),
            visible = visible,
            enter = slideInVertically {
                with(density) { dialogHeight.dp.roundToPx() }
            },
            exit = slideOutVertically {
                with(density) { dialogHeight.dp.roundToPx() }
            }
        ) {
            AddEditLabelCard(
                title = title,
                color = color,
                onTitleChanged = onTitleChanged,
                onColorChanged = onColorChanged,
                onSubmitChange = onSubmitChange,
                onCloseDialog = onCloseDialog,
                colorSelection = colorSelection
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AddEditLabelCard(
    title: String,
    color: Color?,
    onTitleChanged: (String) -> Unit,
    onColorChanged: (Color?) -> Unit,
    onSubmitChange: () -> Unit,
    onCloseDialog: () -> Unit,
    colorSelection: List<Color>
) {
    val density = LocalDensity.current
    val anchorState = remember {
        AnchoredDraggableState(
            initialValue = DragAnchors.Open,
            positionalThreshold = { totalDistance -> totalDistance * 0.5f },
            velocityThreshold = { with(density) { 100.dp.toPx() } },
            animationSpec = tween()
        ).apply {
            updateAnchors(
                DraggableAnchors {
                    DragAnchors.Open at 0f
                    DragAnchors.Closed at with(density) { dialogHeight.dp.toPx() }
                }
            )
        }
    }

    if (anchorState.currentValue == DragAnchors.Closed) {
        onCloseDialog()
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(dialogHeight.dp)
            .offset {
                IntOffset(
                    0,
                    anchorState
                        .requireOffset()
                        .roundToInt()
                )
            }
            .anchoredDraggable(
                orientation = Orientation.Vertical,
                state = anchorState
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp)
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
                        fontSize = 24.sp
                    )
                },
                colors = textFieldColors,
                textStyle = TextStyle(fontSize = 24.sp),
                singleLine = true
            )
            Row(
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
                colorSelection.forEach { colorChoice ->
                    ColorChoice(
                        color = colorChoice,
                        selected = colorChoice == color,
                        onClicked = onColorChanged
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
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
fun ColorChoice(
    color: Color,
    selected: Boolean,
    onClicked: (Color) -> Unit
) {
    OutlinedButton(
        modifier = Modifier
            .width(50.dp)
            .height(50.dp),
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

@Preview
@Composable
fun AddEditLabelCardPreview() {
    AddEditLabelCard(
        title = "",
        color = Color(200, 0, 0),
        onTitleChanged = { },
        onColorChanged = { },
        onSubmitChange = { },
        onCloseDialog = { },
        colorSelection = listOf(
            Color.Transparent,
            Color(200, 0, 0),
            Color(0, 200, 0),
            Color(0, 0, 200)
        )
    )
}