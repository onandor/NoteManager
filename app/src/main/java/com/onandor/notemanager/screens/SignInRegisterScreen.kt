package com.onandor.notemanager.screens

import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.onandor.notemanager.R
import com.onandor.notemanager.viewmodels.SignInRegisterFormType
import com.onandor.notemanager.viewmodels.SignInRegisterViewModel

@Composable
fun SignInRegisterScreen(
    viewModel: SignInRegisterViewModel = hiltViewModel(),
    goBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { SignInRegisterTopAppBar(goBack) }
    ) { innerPadding ->
        Crossfade(
            targetState = uiState.formType,
            label = "SignInRegisterFormCrossfade",
        ) { targetState ->
            if (targetState == SignInRegisterFormType.SIGN_IN) {
                SignInForm(
                    modifier = Modifier.padding(innerPadding),
                    email = uiState.form.email,
                    password = uiState.form.password,
                    onEmailChanged = viewModel::updateEmail,
                    onPasswordChanged = viewModel::updatePassword,
                    onChangeFormType = viewModel::changeFormType
                )
            }
            else {
                RegisterForm(
                    modifier = Modifier.padding(innerPadding),
                    email = uiState.form.email,
                    password = uiState.form.password,
                    passwordConfirmation = uiState.form.passwordConfirmation,
                    onEmailChanged = viewModel::updateEmail,
                    onPasswordChanged = viewModel::updatePassword,
                    onPasswordConfirmationChanged = viewModel::updatePasswordConfirmation,
                    onChangeFormType = viewModel::changeFormType
                )
            }
        }

    }
}

@Composable
fun SignInForm(
    modifier: Modifier,
    email: String,
    password: String,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onChangeFormType: (SignInRegisterFormType) -> Unit
) {
    Column (
        modifier = modifier
            .fillMaxSize()
            .padding(start = 50.dp, end = 50.dp, top = 150.dp, bottom = 30.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column {
            Text(
                modifier = Modifier.padding(bottom = 10.dp),
                text = stringResource(id = R.string.sign_in_register_title_sign_in),
                fontSize = MaterialTheme.typography.headlineLarge.fontSize
            )
            TextField(
                modifier = Modifier
                    .padding(bottom = 10.dp)
                    .fillMaxWidth(),
                value = email,
                onValueChange = onEmailChanged,
                placeholder = {
                    Text(text = stringResource(id = R.string.sign_in_register_hint_email))
                },
                singleLine = true
            )
            TextField(
                modifier = Modifier
                    .padding(bottom = 5.dp)
                    .fillMaxWidth(),
                value = password,
                onValueChange = onPasswordChanged,
                placeholder = {
                    Text(text = stringResource(id = R.string.sign_in_register_hint_password))
                },
                singleLine = true
            )
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
        Button(onClick = { }) {
            Spacer(modifier = Modifier.weight(1f))
            Text(text = stringResource(id = R.string.sign_in_register_button_sign_in))
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun RegisterForm(
    modifier: Modifier,
    email: String,
    password: String,
    passwordConfirmation: String,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onPasswordConfirmationChanged: (String) -> Unit,
    onChangeFormType: (SignInRegisterFormType) -> Unit
) {
    Column (
        modifier = modifier
            .fillMaxSize()
            .padding(start = 50.dp, end = 50.dp, top = 150.dp, bottom = 30.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column {
            Text(
                modifier = Modifier.padding(bottom = 10.dp),
                text = stringResource(id = R.string.sign_in_register_title_register),
                fontSize = MaterialTheme.typography.headlineLarge.fontSize
            )
            TextField(
                modifier = Modifier
                    .padding(bottom = 10.dp)
                    .fillMaxWidth(),
                value = email,
                onValueChange = onEmailChanged,
                placeholder = {
                    Text(text = stringResource(id = R.string.sign_in_register_hint_email))
                },
                singleLine = true
            )
            TextField(
                modifier = Modifier
                    .padding(bottom = 10.dp)
                    .fillMaxWidth(),
                value = password,
                onValueChange = onPasswordChanged,
                placeholder = {
                    Text(text = stringResource(id = R.string.sign_in_register_hint_password))
                },
                singleLine = true
            )
            TextField(
                modifier = Modifier
                    .padding(bottom = 5.dp)
                    .fillMaxWidth(),
                value = passwordConfirmation,
                onValueChange = onPasswordConfirmationChanged,
                placeholder = {
                    Text(text = stringResource(id = R.string.sign_in_register_hint_password_again))
                },
                singleLine = true
            )
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
        Button(onClick = { }) {
            Spacer(modifier = Modifier.weight(1f))
            Text(text = stringResource(id = R.string.sign_in_register_button_register))
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun SignInRegisterTopAppBar(goBack: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(65.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = goBack) {
                Icon(Icons.Filled.ArrowBack, stringResource(id = R.string.sign_in_register_go_back))
            }
        }
    }
}

@Preview
@Composable
fun SignInFormPreview() {
    SignInForm(
        modifier = Modifier.padding(all = 0.dp),
        email = "",
        password = "",
        onEmailChanged = { },
        onPasswordChanged = { },
        onChangeFormType = { }
    )
}

@Preview
@Composable
fun RegisterFormPreview() {
    RegisterForm(
        modifier = Modifier.padding(all = 0.dp),
        email = "",
        password = "",
        passwordConfirmation = "",
        onEmailChanged = { },
        onPasswordChanged = { },
        onPasswordConfirmationChanged = { },
        onChangeFormType = { }
    )
}