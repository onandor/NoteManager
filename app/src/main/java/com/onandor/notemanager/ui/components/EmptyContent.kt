package com.onandor.notemanager.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InnerEmptyContent(
    modifier: Modifier,
    text: String,
    icon: @Composable () -> Unit,
    refreshable: Boolean,
    refreshing: Boolean,
    onStartRefresh: () -> Unit
) {
    val pullToRefreshState = rememberPullToRefreshState()

    if (pullToRefreshState.isRefreshing) {
        LaunchedEffect(true) {
            onStartRefresh()
        }
    }
    if (!refreshing) {
        LaunchedEffect(true) {
            pullToRefreshState.endRefresh()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(pullToRefreshState.nestedScrollConnection)
            .verticalScroll(rememberScrollState())
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            icon()
            Text(
                modifier = Modifier.padding(start = 40.dp, end = 40.dp),
                text = text,
                textAlign = TextAlign.Center
            )
        }
        if (refreshable) {
            PullToRefreshContainer(
                modifier = Modifier.align(Alignment.TopCenter),
                state = pullToRefreshState
            )
        }
    }
}

@Composable
fun EmptyContent(
    modifier: Modifier = Modifier,
    imageVector: ImageVector,
    text: String,
    refreshable: Boolean = false,
    refreshing: Boolean = false,
    onStartRefresh: () -> Unit = { }
) {
    InnerEmptyContent(
        modifier = modifier,
        text = text,
        icon = {
            Icon(
                modifier = Modifier
                    .width(120.dp)
                    .height(120.dp),
                imageVector = imageVector,
                contentDescription = "",
                tint = MaterialTheme.colorScheme.secondary
            )
        },
        refreshable = refreshable,
        refreshing = refreshing,
        onStartRefresh = onStartRefresh
    )
}

@Composable
fun EmptyContent(
    modifier: Modifier = Modifier,
    painter: Painter,
    text: String,
    refreshable: Boolean = false,
    refreshing: Boolean = false,
    onStartRefresh: () -> Unit = { }
) {
    InnerEmptyContent(
        modifier = modifier,
        text = text,
        icon = {
            Icon(
                modifier = Modifier
                    .width(120.dp)
                    .height(120.dp),
                painter = painter,
                contentDescription = "",
                tint = MaterialTheme.colorScheme.secondary
            )
        },
        refreshable = refreshable,
        refreshing = refreshing,
        onStartRefresh = onStartRefresh
    )
}
