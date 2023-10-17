package com.onandor.notemanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.onandor.notemanager.navigation.NavGraph
import com.onandor.notemanager.ui.theme.CurrentTheme
import com.onandor.notemanager.ui.theme.LocalTheme
import com.onandor.notemanager.ui.theme.NoteManagerTheme
import com.onandor.notemanager.ui.theme.ThemeType
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
            val uiState by splashViewModel.uiState.collectAsStateWithLifecycle()
            val currentTheme = when (uiState.themeType) {
                ThemeType.SYSTEM -> CurrentTheme(isSystemInDarkTheme())
                ThemeType.LIGHT -> CurrentTheme(isDark = false)
                ThemeType.DARK -> CurrentTheme(isDark = true)
            }

            if (!uiState.isLoading) {
                CompositionLocalProvider(LocalTheme provides currentTheme) {
                    NoteManagerTheme(darkTheme = LocalTheme.current.isDark) {
                        NavGraph(startDestination = uiState.startDestination)
                    }
                }
            }
        }
    }
}