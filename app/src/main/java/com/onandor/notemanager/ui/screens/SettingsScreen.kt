package com.onandor.notemanager.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.onandor.notemanager.R
import com.onandor.notemanager.ui.components.ColoredStatusBarTopAppBar
import com.onandor.notemanager.ui.theme.ThemeType
import com.onandor.notemanager.viewmodels.SettingsViewModel

data class DropdownItem(
    val onClick: () -> Unit,
    val text: @Composable () -> Unit
)

@Composable
fun AppDropdownMenu(
    icon: @Composable () -> Unit,
    textToTheLeft: @Composable () -> Unit = { },
    textToTheRight: @Composable () -> Unit = { },
    items: List<DropdownItem>
) {
    var expanded by remember { mutableStateOf(false) }
    Box(contentAlignment = Alignment.Center) {
        Row(
            modifier = Modifier
                .clip(CircleShape)
                .clickable { expanded = true }
        ) {
            Row(modifier = Modifier.padding(10.dp)) {
                textToTheLeft()
                icon()
                textToTheRight()
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            items.forEach { option ->
                DropdownMenuItem(
                    text = { option.text() },
                    onClick = { option.onClick(); expanded = false }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val animatedSurfaceColor = animateColorAsState(
        targetValue = MaterialTheme.colorScheme.surface,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = ""
    )

    val themeItems = listOf(
        DropdownItem(onClick = { viewModel.saveThemeType(ThemeType.SYSTEM) }) {
            Text(text = stringResource(id = R.string.settings_theme_system))
        },
        DropdownItem(onClick = { viewModel.saveThemeType(ThemeType.LIGHT) }) {
            Text(text = stringResource(id = R.string.settings_theme_light))
        },
        DropdownItem(onClick = { viewModel.saveThemeType(ThemeType.DARK) }) {
            Text(text = stringResource(id = R.string.settings_theme_dark))
        }
    )

    val currentTheme = viewModel.currentTheme.collectAsStateWithLifecycle()
    val currentThemeString = when(currentTheme.value) {
        ThemeType.SYSTEM -> stringResource(id = R.string.settings_theme_system)
        ThemeType.LIGHT -> stringResource(id = R.string.settings_theme_light)
        ThemeType.DARK -> stringResource(id = R.string.settings_theme_dark)
    }

    Scaffold(
        topBar = {
            ColoredStatusBarTopAppBar(
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
                .padding(innerPadding)
                .fillMaxSize()
                .background(color = animatedSurfaceColor.value)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.settings_theme),
                    fontSize = 20.sp
                )
                AppDropdownMenu(
                    icon = { Icon(Icons.Filled.KeyboardArrowDown, "") },
                    textToTheLeft = { Text(text = currentThemeString) },
                    items = themeItems
                )
            }
            /*
            Button(onClick = { viewModel.resetFirstLaunch() }) {
                Text(text = "Reset first launch to true")
            }
             */
        }
    }

    BackHandler {
        viewModel.navigateBack()
    }
}