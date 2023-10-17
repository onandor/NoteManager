package com.onandor.notemanager.screens

import androidx.activity.compose.BackHandler
import androidx.compose.ui.graphics.Color
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.onandor.notemanager.R
import com.onandor.notemanager.viewmodels.SignInRegisterFormType
import com.onandor.notemanager.viewmodels.SignInRegisterViewModel
import kotlinx.coroutines.launch
import java.time.format.TextStyle

@Composable
fun SignInRegisterScreen(
    viewModel: SignInRegisterViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    Scaffold(
        topBar = { SignInRegisterTopAppBar(viewModel::navigateBack) },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Crossfade(
            targetState = uiState.formType,
            label = "SignInRegisterFormCrossfade",
        ) { targetState ->
            Column {
                Spacer(modifier = Modifier.weight(1f))
                if (targetState == SignInRegisterFormType.SIGN_IN) {
                    SignInForm(
                        modifier = Modifier.padding(innerPadding),
                        email = uiState.form.email,
                        password = uiState.form.password,
                        emailValid = uiState.form.emailValid,
                        passwordValid = uiState.form.passwordValid,
                        loading = uiState.loading,
                        onEmailChanged = viewModel::updateEmail,
                        onPasswordChanged = viewModel::updatePassword,
                        onChangeFormType = viewModel::changeFormType,
                        onSignIn = viewModel::signIn
                    )
                }
                else {
                    RegisterForm(
                        modifier = Modifier.padding(innerPadding),
                        email = uiState.form.email,
                        password = uiState.form.password,
                        passwordConfirmation = uiState.form.passwordConfirmation,
                        emailValid = uiState.form.emailValid,
                        passwordValid = uiState.form.passwordValid,
                        passwordConfirmationValid = uiState.form.passwordConfirmationValid,
                        loading = uiState.loading,
                        onEmailChanged = viewModel::updateEmail,
                        onPasswordChanged = viewModel::updatePassword,
                        onPasswordConfirmationChanged = viewModel::updatePasswordConfirmation,
                        onChangeFormType = viewModel::changeFormType,
                        onRegister = viewModel::register
                    )
                }
                Spacer(modifier = Modifier.weight(3f))
            }
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

    BackHandler {
        viewModel.navigateBack()
    }
}

@Composable
private fun SignInForm(
    modifier: Modifier,
    email: String,
    password: String,
    loading: Boolean,
    emailValid: Boolean,
    passwordValid: Boolean,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onChangeFormType: (SignInRegisterFormType) -> Unit,
    onSignIn: () -> Unit
) {
    Column (
        modifier = modifier.padding(start = 50.dp, end = 50.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column {
            Text(
                text = stringResource(id = R.string.sign_in_register_title_sign_in),
                fontSize = MaterialTheme.typography.headlineLarge.fontSize
            )
            Spacer(modifier = Modifier.height(10.dp))
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = email,
                onValueChange = onEmailChanged,
                placeholder = {
                    Text(text = stringResource(id = R.string.sign_in_register_hint_email))
                },
                singleLine = true,
                isError = !emailValid
            )
            Spacer(modifier = Modifier.height(5.dp))
            AnimatedVisibility(visible = !emailValid) {
                Text(
                    text = stringResource(id = R.string.sign_in_register_error_invalid_email),
                    color = MaterialTheme.colorScheme.error
                )
            }
            Spacer(modifier = Modifier.height(5.dp))
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = password,
                onValueChange = onPasswordChanged,
                placeholder = {
                    Text(text = stringResource(id = R.string.sign_in_register_hint_password))
                },
                singleLine = true,
                isError = !passwordValid,
                visualTransformation = PasswordVisualTransformation()
            )
            Spacer(modifier = Modifier.height(5.dp))
            AnimatedVisibility(visible = !passwordValid) {
                Text(
                    text = stringResource(id = R.string.sign_in_register_error_invalid_password),
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(5.dp))
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = stringResource(R.string.sign_in_register_need_an_account))
            TextButton(
                modifier = Modifier.padding(start = 10.dp),
                onClick = { onChangeFormType(SignInRegisterFormType.REGISTER) }
            ) {
                Text(text = stringResource(id = R.string.sign_in_register_button_register))
            }
        }
        Spacer(modifier = Modifier.height(5.dp))
        Button(
            modifier = Modifier.height(50.dp),
            onClick = onSignIn,
            enabled = !loading
        ) {
            Spacer(modifier = Modifier.weight(1f))
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .width(25.dp)
                        .aspectRatio(1f),
                    color = MaterialTheme.colorScheme.surface,
                    trackColor = MaterialTheme.colorScheme.primary
                )
            }
            else {
                Text(text = stringResource(id = R.string.sign_in_register_button_sign_in))
            }
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun RegisterForm(
    modifier: Modifier,
    email: String,
    password: String,
    passwordConfirmation: String,
    emailValid: Boolean,
    passwordValid: Boolean,
    passwordConfirmationValid: Boolean,
    loading: Boolean,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onPasswordConfirmationChanged: (String) -> Unit,
    onChangeFormType: (SignInRegisterFormType) -> Unit,
    onRegister: () -> Unit
) {
    Column (
        modifier = modifier.padding(start = 50.dp, end = 50.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column {
            Text(
                text = stringResource(id = R.string.sign_in_register_title_register),
                fontSize = MaterialTheme.typography.headlineLarge.fontSize
            )
            Spacer(modifier = Modifier.height(10.dp))
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = email,
                onValueChange = onEmailChanged,
                placeholder = {
                    Text(text = stringResource(id = R.string.sign_in_register_hint_email))
                },
                singleLine = true,
                isError = !emailValid
            )
            Spacer(modifier = Modifier.height(5.dp))
            AnimatedVisibility(visible = !emailValid) {
                Text(
                    text = stringResource(id = R.string.sign_in_register_error_invalid_email),
                    color = MaterialTheme.colorScheme.error
                )
            }
            Spacer(modifier = Modifier.height(5.dp))
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = password,
                onValueChange = onPasswordChanged,
                placeholder = {
                    Text(text = stringResource(id = R.string.sign_in_register_hint_password))
                },
                singleLine = true,
                isError = !passwordValid,
                visualTransformation = PasswordVisualTransformation()
            )
            Spacer(modifier = Modifier.height(5.dp))
            AnimatedVisibility(visible = !passwordValid) {
                Text(
                    text = stringResource(id = R.string.sign_in_register_error_invalid_password),
                    color = MaterialTheme.colorScheme.error
                )
            }
            Spacer(modifier = Modifier.height(5.dp))
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = passwordConfirmation,
                onValueChange = onPasswordConfirmationChanged,
                placeholder = {
                    Text(text = stringResource(id = R.string.sign_in_register_hint_password_again))
                },
                singleLine = true,
                isError = !passwordConfirmationValid,
                visualTransformation = PasswordVisualTransformation()
            )
            Spacer(modifier = Modifier.height(5.dp))
            AnimatedVisibility(visible = !passwordConfirmationValid) {
                Text(
                    text = stringResource(id = R.string.sign_in_register_error_invalid_password_confirmation),
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(5.dp))
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = stringResource(id = R.string.sign_in_register_have_an_account))
            TextButton(
                modifier = Modifier.padding(start = 10.dp),
                onClick = { onChangeFormType(SignInRegisterFormType.SIGN_IN) }
            ) {
                Text(text = stringResource(id = R.string.sign_in_register_button_sign_in))
            }
        }
        Spacer(modifier = Modifier.height(5.dp))
        Button(
            modifier = Modifier.height(50.dp),
            onClick = onRegister,
            enabled = !loading
        ) {
            Spacer(modifier = Modifier.weight(1f))
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .width(25.dp)
                        .aspectRatio(1f),
                    color = MaterialTheme.colorScheme.surface,
                    trackColor = MaterialTheme.colorScheme.primary
                )
            }
            else {
                Text(text = stringResource(id = R.string.sign_in_register_button_register))
            }
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun SignInRegisterTopAppBar(navigateBack: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(65.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = navigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(id = R.string.sign_in_register_go_back))
            }
        }
    }
}

@Preview
@Composable
private fun SignInFormPreview() {
    SignInForm(
        modifier = Modifier.padding(all = 0.dp),
        email = "",
        password = "",
        loading = false,
        emailValid = true,
        passwordValid = true,
        onEmailChanged = { },
        onPasswordChanged = { },
        onChangeFormType = { },
        onSignIn = { }
    )
}

@Preview
@Composable
private fun RegisterFormPreview() {
    RegisterForm(
        modifier = Modifier.padding(all = 0.dp),
        email = "",
        password = "",
        passwordConfirmation = "",
        emailValid = true,
        passwordValid = true,
        passwordConfirmationValid = true,
        loading = false,
        onEmailChanged = { },
        onPasswordChanged = { },
        onPasswordConfirmationChanged = { },
        onChangeFormType = { },
        onRegister = { }
    )
}