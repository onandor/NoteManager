package com.onandor.notemanager.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

private enum class DragAnchors {
    Open,
    Closed
}

@Composable
fun DraggableBottomDialog(
    modifier: Modifier = Modifier,
    height: Dp = 300.dp,
    visible: Boolean,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val interactionSource = remember { MutableInteractionSource() }
    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) { onDismiss() }
            )
        }
        AnimatedVisibility(
            modifier = Modifier.align(Alignment.BottomCenter),
            visible = visible,
            enter = slideInVertically {
                with(density) { height.roundToPx() }
            },
            exit = slideOutVertically {
                with(density) { height.roundToPx() }
            }
        ) {
            DialogCard(
                modifier = modifier,
                height = height,
                density = density,
                onDismiss = onDismiss,
                content = content
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DialogCard(
    modifier: Modifier,
    height: Dp,
    density: Density,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
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
                    DragAnchors.Closed at with(density) { height.toPx() }
                }
            )
        }
    }
    if (anchorState.currentValue == DragAnchors.Closed) {
        onDismiss()
    }
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
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
        content()
    }
}