package com.onandor.notemanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.onandor.notemanager.navigation.NavGraph
import com.onandor.notemanager.ui.theme.NoteManagerTheme
import com.onandor.notemanager.viewmodels.SplashViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NoteManagerActivity : ComponentActivity() {

    private lateinit var splashViewModel: SplashViewModel
    private lateinit var splashScreen: SplashScreen

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        splashViewModel = ViewModelProvider(this)[SplashViewModel::class.java]
        splashScreen = installSplashScreen()
        splashScreen.apply {
            setKeepOnScreenCondition {
                splashViewModel.uiState.value.isLoading
            }
        }

        setContent {
            NoteManagerTheme {
                val uiState by splashViewModel.uiState.collectAsStateWithLifecycle()
                if (!uiState.isLoading) {
                    NavGraph(startDestination = uiState.startDestination)
                }
            }
        }
    }
}