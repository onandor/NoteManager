package com.onandor.notemanager.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.onandor.notemanager.R
import com.onandor.notemanager.data.Label

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabelSelectionBottomDialog(
    onDismissRequest: () -> Unit,
    insets: WindowInsets,
    labels: List<Label>,
    selectedLabels: List<Label>,
    selectedText: String,
    unSelectedText: String,
    onChangeLabelSelection: (Label, Boolean) -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        windowInsets = WindowInsets(0, 0, 0, 0),
        dragHandle = {}
    ) {
        DialogContent(
            labels = labels,
            selectedLabels = selectedLabels,
            selectedText = selectedText,
            unSelectedText = unSelectedText,
            onChangeLabelSelection = onChangeLabelSelection,
            insets = insets
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DialogContent(
    labels: List<Label>,
    selectedLabels: List<Label>,
    selectedText: String,
    unSelectedText: String,
    onChangeLabelSelection: (Label, Boolean) -> Unit,
    insets: WindowInsets
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(insets)
            .padding(start = 15.dp, end = 15.dp, top = 35.dp, bottom = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        val unSelectedLabels = labels.filterNot { label ->
            selectedLabels.any { selectedLabel -> selectedLabel.id == label.id }
        }

        Text(
            text = selectedText,
            fontSize = 23.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        if (selectedLabels.isEmpty()) {
            Text(
                text = stringResource(id = R.string.dialog_label_selection_empty),
                fontStyle = FontStyle.Italic
            )
            Spacer(modifier = Modifier.height(20.dp))
        }
        else {
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(),
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.Start),
                verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.Top),
            ) {
                selectedLabels.forEach { label ->
                    LabelComponent(
                        label = label,
                        clickable = true,
                        onClick = { onChangeLabelSelection(label, false) },
                        padding = 10.dp,
                        fontSize = 20.sp,
                        borderWidth = 2.dp,
                        roundedCornerSize = 10.dp
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = unSelectedText,
            fontSize = 23.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        if (labels.isEmpty()) {
            Text(
                text = stringResource(id = R.string.dialog_label_selection_empty),
                fontStyle = FontStyle.Italic
            )
            Spacer(modifier = Modifier.height(20.dp))
        }
        else {
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(),
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.Start),
                verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.Top)
            ) {
                unSelectedLabels.forEach { label ->
                    LabelComponent(
                        label = label,
                        clickable = true,
                        onClick = { onChangeLabelSelection(label, true) },
                        padding = 10.dp,
                        fontSize = 20.sp,
                        borderWidth = 2.dp,
                        roundedCornerSize = 10.dp
                    )
                }
            }
        }
    }
}
