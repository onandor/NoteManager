package com.onandor.notemanager.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.onandor.notemanager.R
import com.onandor.notemanager.ui.theme.LocalTheme
import com.onandor.notemanager.ui.theme.ThemeType
import com.onandor.notemanager.viewmodels.SettingsViewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val animatedSurfaceColor = animateColorAsState(
        targetValue = MaterialTheme.colorScheme.surface,
        animationSpec = tween(500),
        label = ""
    )
    val animatedPrimaryColor = animateColorAsState(
        targetValue = MaterialTheme.colorScheme.primary,
        animationSpec = tween(500),
        label = ""
    )

    Scaffold(
        modifier = Modifier.statusBarsPadding(),
        topBar = {
            SettingsTopBar(
                navigateBack = viewModel::navigateBack,
                surfaceColor = animatedSurfaceColor.value
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = animatedSurfaceColor.value)
        ) {
            Text(text = "Settings", modifier = Modifier.padding(innerPadding))
            Button(onClick = { viewModel.resetFirstLaunch() }) {
                Text(text = "Reset first launch to true")
            }
            Button(onClick = { viewModel.saveThemeType(ThemeType.SYSTEM) }) {
                Text(text = "Set system theme")
            }
            Button(onClick = { viewModel.saveThemeType(ThemeType.LIGHT) }) {
                Text(text = "Set light theme")
            }
            Button(onClick = { viewModel.saveThemeType(ThemeType.DARK) }) {
                Text(text = "Set dark theme")
            }
        }
    }

    BackHandler {
        viewModel.navigateBack()
    }
}

@Composable
private fun SettingsTopBar(
    navigateBack: () -> Unit,
    surfaceColor: Color
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(65.dp),
        color = surfaceColor
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navigateBack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.settings_go_back))
            }
            Text(stringResource(R.string.settings), fontSize = 20.sp)
        }
    }
}