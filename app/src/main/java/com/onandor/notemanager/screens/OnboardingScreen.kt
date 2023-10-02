package com.onandor.notemanager.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.onandor.notemanager.viewmodels.OnboardingViewModel

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    Column {
        Text("Onboarding")
        Button(onClick = { viewModel.saveFirstLaunch(false) }) {
            Text(text = "Finish")
        }
    }
}