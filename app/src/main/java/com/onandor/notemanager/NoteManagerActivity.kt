package com.onandor.notemanager

import android.animation.ObjectAnimator
import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.animation.AnticipateInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalView
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
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
            setOnExitAnimationListener { splashScreenViewProvider ->
                val view = splashScreenViewProvider.view
                val fadeOut = ObjectAnimator.ofFloat(
                    view,
                    View.ALPHA,
                    1f,
                    0f
                )
                fadeOut.interpolator = AnticipateInterpolator()
                fadeOut.duration = 300L

                fadeOut.doOnEnd {
                    splashScreenViewProvider.remove()
                    splashViewModel.splashScreenRemoved()
                }
                fadeOut.start()
            }
        }

        setContent {
            val view = LocalView.current
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
                    LaunchedEffect(uiState.splashScreenVisible) {
                        /*
                         * Since the theme composable gets composed before the splash screen gets
                         * removed, the splash screen resets the status bar and navigation bar
                         * colors. The background color of these two is always transparent
                         * it can be statically set in the theme file, but the foreground color
                         * depends on the light/dark setting, so it needs to be set programmatically
                         * after the splash screen has been removed.
                         */
                        if (uiState.splashScreenVisible || savedInstanceState != null)
                            return@LaunchedEffect

                        val window = (view.context as Activity).window
                        WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars =
                            !currentTheme.isDark
                        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars =
                            !currentTheme.isDark
                    }
                }
            }

        }
    }
}