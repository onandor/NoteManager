package com.onandor.notemanager.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.onandor.notemanager.R

@Composable
fun AddEditNoteScreen(
    goBack: () -> Unit
) {
    Scaffold(
        topBar = { AddEditNoteTopBar(goBack) }
    ) { innerPadding ->
        AddEditTitleAndContent(Modifier.padding(innerPadding))
    }
}

@Composable
fun AddEditTitleAndContent(modifier: Modifier) {
    Column (
        modifier = modifier
            .fillMaxWidth()
            //.padding(all = 16.dp)
            .verticalScroll(rememberScrollState())
    ){
        val textFieldColors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        )
        val title: String = ""

        TextField(
            value = title,
            onValueChange = { },
            colors = textFieldColors,
            placeholder = {
                Text("Title")
            }
        )
        TextField(
            value = title,
            onValueChange = { },
            colors = textFieldColors,
            placeholder = {
                Text("Your note goes here...")
            }
        )
    }
}

@Composable
fun AddEditNoteTopBar(goBack: () -> Unit) {
    Surface(modifier = Modifier
        .fillMaxWidth()
        .height(65.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { goBack() }) {
                Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(R.string.settings_go_back))
            }
        }
    }
}