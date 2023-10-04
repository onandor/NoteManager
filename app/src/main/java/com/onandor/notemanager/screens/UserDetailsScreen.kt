package com.onandor.notemanager.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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
                    email = uiState.email,
                    onSignOut = viewModel::logOut
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
    email: String,
    onSignOut: () -> Unit
) {
    Column {
        Text("Logged in.\nEmail: $email")
        Button(onClick = onSignOut) {
            Text("Sign out")
        }
    }
}

@Composable
fun SignedOutComponent(
    onSignIn: () -> Unit
) {
    Column {
        Text("Logged out.")
        Button(onClick = onSignIn) {
            Text("Sign in/Register")
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