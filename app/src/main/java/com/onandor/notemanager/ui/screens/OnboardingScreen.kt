package com.onandor.notemanager.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.onandor.notemanager.R
import com.onandor.notemanager.viewmodels.OnboardingViewModel

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    OnboardingContent(
        onSkip = {
            viewModel.completeFirstLaunch()
            viewModel.skip()
        },
        onSignIn = {
            viewModel.completeFirstLaunch()
            viewModel.signIn()
        }
    )
}

@Composable
private fun OnboardingContent(
    onSkip: () -> Unit,
    onSignIn: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = 200.dp, bottom = 30.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(id = R.string.onboarding_title_1),
                fontSize = MaterialTheme.typography.titleLarge.fontSize,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 30.dp),
                text = stringResource(id = R.string.onboarding_title_2),
                fontSize = MaterialTheme.typography.headlineLarge.fontSize,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                modifier = Modifier.padding(bottom = 30.dp),
                text = stringResource(id = R.string.onboarding_account_description),
                textAlign = TextAlign.Center
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 15.dp),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(
                modifier = Modifier.padding(end = 20.dp),
                onClick = onSkip
            ) {
                Text(text = stringResource(id = R.string.onboarding_button_skip))
            }
            Button(onClick = onSignIn) {
                Text(text = stringResource(id = R.string.onboarding_button_sign_in))
            }
        }
    }
}

@Preview
@Composable
private fun OnboardingScreenPreview() {
    OnboardingContent(onSkip = { }, onSignIn = { })
}