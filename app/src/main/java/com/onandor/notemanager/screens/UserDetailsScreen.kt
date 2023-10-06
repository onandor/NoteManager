package com.onandor.notemanager.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.onandor.notemanager.R
import com.onandor.notemanager.viewmodels.UserDetailsViewModel

@Composable
fun UserDetailsScreen(
    goBack: () -> Unit,
    onSignIn: () -> Unit,
    viewModel: UserDetailsViewModel = hiltViewModel()
) {
    Scaffold(
        topBar = { UserDetailsTopBar(goBack) }
    ) { innerPadding ->
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        Column(
            modifier = Modifier.padding(innerPadding)
        ) {
            if (uiState.loggedIn) {
                SignedInInComponent(
                    loading = uiState.loadingRequest,
                    email = uiState.email,
                    noteCount = uiState.noteCount,
                    onSignOut = viewModel::logOut,
                    onDeleteAccount = viewModel::deleteUser
                )
            }
            else {
                SignedOutComponent(
                    onSignIn = onSignIn
                )
            }
        }
    }
}

@Composable
fun SignedInInComponent(
    loading: Boolean,
    email: String,
    noteCount: Int,
    onSignOut: () -> Unit,
    onDeleteAccount: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp)),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 10.dp)
        ) {
            Text(stringResource(id = R.string.user_details_signed_in_as))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = email,
                    fontSize = MaterialTheme.typography.titleLarge.fontSize
                )
            }
            Text(
                modifier = Modifier.padding(bottom = 10.dp),
                text = stringResource(id = R.string.user_details_you_have_notes_1)
                        + " $noteCount "
                        + stringResource(id = R.string.user_details_you_have_notes_2)
            )
            Row (
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ){
                AnimatedVisibility(visible = loading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .width(30.dp)
                            .aspectRatio(1f),
                        color = MaterialTheme.colorScheme.surface,
                        trackColor = MaterialTheme.colorScheme.primary
                    )
                }
                Button(
                    modifier = Modifier.padding(start = 10.dp, end = 10.dp),
                    onClick = onDeleteAccount,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    enabled = !loading
                ) {
                    Text(stringResource(id = R.string.user_details_button_delete_account))
                }
                Button(
                    onClick = onSignOut,
                    enabled = !loading
                ) {
                    Text(stringResource(id = R.string.user_details_button_sign_out))
                }
            }
        }
    }
}

@Composable
fun SignedOutComponent(
    onSignIn: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp)),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 10.dp)
        ) {
            Text(
                modifier = Modifier.padding(bottom = 10.dp),
                text = stringResource(R.string.user_details_not_signed_in_title),
                fontSize = MaterialTheme.typography.titleLarge.fontSize
            )
            Text(
                modifier = Modifier.padding(bottom = 10.dp),
                text = stringResource(id = R.string.user_details_not_signed_in_description)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(onClick = onSignIn) {
                    Text(stringResource(R.string.user_details_button_sign_in_register))
                }
            }
        }
    }
}

@Composable
fun UserDetailsTopBar(goBack: () -> Unit) {
    Surface(modifier = Modifier
        .fillMaxWidth()
        .height(65.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { goBack() }) {
                Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(R.string.settings_go_back))
            }
            Text(stringResource(R.string.account), fontSize = 20.sp)
        }
    }
}

@Preview
@Composable
fun SignedInComponentPreview() {
    SignedInInComponent(
        loading = false,
        email = "test@email.com",
        noteCount = 10,
        onSignOut = { },
        onDeleteAccount = { }
    )
}

@Preview
@Composable
fun SignedOutComponentPreview() {
    SignedOutComponent(
        onSignIn = { }
    )
}