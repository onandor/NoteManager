package com.onandor.notemanager.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.onandor.notemanager.R
import com.onandor.notemanager.viewmodels.SignedOutViewModel

@Composable
fun SignedOutScreen(
    viewModel: SignedOutViewModel = hiltViewModel()
) {
    val showLearnMore by viewModel.showLearnMore.collectAsState()
    SignedOutContent(
        onSignIn = viewModel::signIn,
        onDismiss = viewModel::dismiss,
        onShowLearnMore = viewModel::showLearnMore,
        showLearnMore = showLearnMore
    )
}

@Composable
fun SignedOutContent(
    onSignIn: () -> Unit,
    onDismiss: () -> Unit,
    onShowLearnMore: () -> Unit,
    showLearnMore: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 20.dp, end = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(50.dp))
        Text(
            text = stringResource(id = R.string.signed_out_title),
            fontSize = MaterialTheme.typography.titleLarge.fontSize
        )
        Spacer(modifier = Modifier.height(10.dp))
        ReasonsContent()
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            AnimatedVisibility(visible = !showLearnMore) {
                TextButton(onClick = onShowLearnMore) {
                    Text(stringResource(id = R.string.signed_out_button_learn_more))
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            TextButton(onClick = onDismiss) {
                Text(stringResource(id = R.string.signed_out_button_dismiss))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Button(onClick = onSignIn) {
                Text(stringResource(id = R.string.signed_out_button_sign_in))
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        AnimatedVisibility(visible = showLearnMore) {
            LearnMoreContent()
        }
    }
}

@Composable
fun ReasonsContent() {
    Text(stringResource(id = R.string.signed_out_desc_reasons))
    Spacer(modifier = Modifier.height(5.dp))
    Text(stringResource(id = R.string.signed_out_desc_reasons_list), modifier = Modifier.padding(start = 10.dp))
    Spacer(modifier = Modifier.height(5.dp))
    Text(stringResource(id = R.string.signed_out_desc_recommendation))
}

@Composable
fun LearnMoreContent() {
    Surface(
        modifier = Modifier.clip(RoundedCornerShape(16.dp)),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 12.dp)
        ) {
            Text(stringResource(id = R.string.signed_out_learn_more_1))
            Spacer(modifier = Modifier.height(10.dp))
            Text(stringResource(id = R.string.signed_out_learn_more_2))
        }
    }
}

@Preview
@Composable
fun SignedOutContentPreview() {
    SignedOutContent(
        onSignIn = { },
        onDismiss = { },
        onShowLearnMore = { },
        showLearnMore = false
    )
}