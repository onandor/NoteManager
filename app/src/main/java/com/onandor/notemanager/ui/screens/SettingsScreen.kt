package com.onandor.notemanager.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.onandor.notemanager.R
import com.onandor.notemanager.ui.theme.ThemeType
import com.onandor.notemanager.viewmodels.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
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
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = viewModel::navigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.settings_go_back))
                    }
                }
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