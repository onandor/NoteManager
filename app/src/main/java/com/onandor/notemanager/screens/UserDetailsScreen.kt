package com.onandor.notemanager.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.onandor.notemanager.R
import com.onandor.notemanager.viewmodels.UserDetailsViewModel
import kotlinx.coroutines.launch

@Composable
fun UserDetailsScreen(
    goBack: () -> Unit,
    onSignIn: () -> Unit,
    viewModel: UserDetailsViewModel = hiltViewModel()
) {
    val snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = { UserDetailsTopBar(goBack) },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        Column(
            modifier = Modifier.padding(innerPadding)
        ) {
            if (uiState.loggedIn) {
                SignedInInComponent(
                    loading = uiState.loadingRequest,
                    email = uiState.email,
                    onSignOut = viewModel::logOut,
                    onDeleteAccount = viewModel::openDeleteUserDialog
                )
            }
            else {
                SignedOutComponent(
                    onSignIn = onSignIn
                )
            }
        }

        if (uiState.deleteUserDialogOpen) {
            DeleteUserDialog(
                password = uiState.passwordConfirmation,
                onDismissRequest = viewModel::dismissDeleteUserDialog,
                onConfirmation = viewModel::deleteUser,
                onPasswordChanged = viewModel::updatePasswordConfirmation
            )
        }
        if (uiState.snackbarMessageResource != null) {
            val snackbarText = stringResource(id = uiState.snackbarMessageResource!!)
            LaunchedEffect(uiState.snackbarMessageResource) {
                scope.launch {
                    snackbarHostState.showSnackbar(snackbarText)
                }
                viewModel.snackbarShown()
            }
        }
    }
}

@Composable
fun SignedInInComponent(
    loading: Boolean,
    email: String,
    onSignOut: () -> Unit,
    onDeleteAccount: () -> Unit
) {
    Surface(
        modifier = Modifier
            .padding(start = 10.dp, end = 10.dp)
            .clip(RoundedCornerShape(16.dp)),
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
            Spacer(modifier = Modifier.height(10.dp))
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
                    modifier = Modifier.padding(end = 10.dp),
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
            .padding(start = 10.dp, end = 10.dp)
            .clip(RoundedCornerShape(16.dp)),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 10.dp)
        ) {
            Text(
                text = stringResource(R.string.user_details_not_signed_in_title),
                fontSize = MaterialTheme.typography.titleLarge.fontSize
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(stringResource(id = R.string.user_details_not_signed_in_description))
            Spacer(modifier = Modifier.height(10.dp))
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

@Composable
fun DeleteUserDialog(
    password: String,
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    onPasswordChanged: (String) -> Unit
) {
    Dialog(onDismissRequest = { onDismissRequest() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(375.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(all = 15.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    modifier = Modifier.size(35.dp),
                    imageVector = Icons.Filled.Warning,
                    contentDescription = "Warning"
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(stringResource(id = R.string.dialog_delete_user_description))
                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(id = R.string.dialog_delete_user_enter_password))
                }
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = onPasswordChanged,
                    placeholder = {
                        Text(stringResource(id = R.string.dialog_delete_user_hint_password))
                    },
                    visualTransformation = PasswordVisualTransformation()
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TextButton(onClick = onDismissRequest) {
                        Text(stringResource(id = R.string.dialog_delete_user_button_cancel))
                    }
                    Button(
                        onClick = onConfirmation,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        ),
                    ) {
                        Text(stringResource(id = R.string.dialog_delete_user_button_confirm))
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun SignedInComponentPreview() {
    SignedInInComponent(
        loading = false,
        email = "test@email.com",
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

@Preview
@Composable
fun DeleteUserDialogPreview() {
    DeleteUserDialog(
        password = "",
        onDismissRequest = { },
        onConfirmation = { },
        onPasswordChanged = { }
    )
}