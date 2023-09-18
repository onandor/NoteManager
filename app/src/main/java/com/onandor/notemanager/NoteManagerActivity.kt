package com.onandor.notemanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.onandor.notemanager.ui.theme.NoteManagerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NoteManagerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NoteManagerTheme {
                NoteManagerNavGraph()
            }
        }
    }
}