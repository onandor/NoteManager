package com.onandor.notemanager.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.onandor.notemanager.R
import com.onandor.notemanager.viewmodels.SettingsViewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    Scaffold(
        modifier = Modifier.statusBarsPadding(),
        topBar = { SettingsTopBar(navigateBack = viewModel::navigateBack) }
    ) { innerPadding ->
        Column {
            Text(text = "Settings", modifier = Modifier.padding(innerPadding))
            Button(onClick = { viewModel.resetFirstLaunch() }) {
                Text(text = "Reset first launch to true")
            }
        }
    }

    BackHandler {
        viewModel.navigateBack()
    }
}

@Composable
private fun SettingsTopBar(navigateBack: () -> Unit) {
    Surface(modifier = Modifier
        .fillMaxWidth()
        .height(65.dp)) {
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