package com.onandor.notemanager.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.Spring.StiffnessMediumLow
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import androidx.compose.ui.unit.sp
import com.onandor.notemanager.R
import com.onandor.notemanager.data.Label
import kotlinx.coroutines.launch

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

private fun Modifier.animatePlacement(): Modifier = composed {
    val scope = rememberCoroutineScope()
    var targetOffset by remember { mutableStateOf(IntOffset.Zero) }
    var animatable by remember {
        mutableStateOf<Animatable<IntOffset, AnimationVector2D>?>(null)
    }
    this
        .onPlaced {
            targetOffset = it
                .positionInParent()
                .round()
        }
        .offset {
            val anim = animatable ?: Animatable(targetOffset, IntOffset.VectorConverter)
                .also { animatable = it }
            if (anim.targetValue != targetOffset) {
                scope.launch {
                    anim.animateTo(targetOffset, spring(stiffness = StiffnessMediumLow))
                }
            }
            animatable?.let { it.value - targetOffset } ?: IntOffset.Zero
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
            Spacer(modifier = Modifier.height(19.5.dp))
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
                    key(label.id) {
                        LabelComponent(
                            modifier = Modifier.animatePlacement(),
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
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = unSelectedText,
            fontSize = 23.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        if (unSelectedLabels.isEmpty()) {
            Text(
                text = stringResource(id = R.string.dialog_label_selection_empty),
                fontStyle = FontStyle.Italic
            )
            Spacer(modifier = Modifier.height(19.5.dp))
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
                    key(label.id) {
                        LabelComponent(
                            modifier = Modifier.animatePlacement(),
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
}
