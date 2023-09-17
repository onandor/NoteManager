package com.onandor.notemanager.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.onandor.notemanager.components.TopBar

@Composable
fun ArchiveScreen(openDrawer: () -> Unit) {
    Scaffold(
        topBar = { TopBar(openDrawer) }
    ) { innerPadding ->
        Text(text = "Archive", modifier = Modifier.padding(innerPadding))
    }
}