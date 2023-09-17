package com.onandor.notemanager.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import com.onandor.notemanager.NMNavigationActions
import com.onandor.notemanager.R

@Composable
fun SettingsScreen(navActions: NMNavigationActions) {
    Scaffold(
        topBar = { SettingsTopBar(navActions = navActions)}
    ) { innerPadding ->
        Text(text = "Settings", modifier = Modifier.padding(innerPadding))
    }
}

@Composable
fun SettingsTopBar(navActions: NMNavigationActions) {
    Surface(modifier = Modifier.fillMaxWidth().height(65.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navActions.navigateUp() }) {
                Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(R.string.settings_go_back))
            }
            Text(stringResource(R.string.settings), fontSize = 20.sp)
        }
    }
}