package com.onandor.notemanager.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.onandor.notemanager.R

@Composable
fun SimpleConfirmationDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    text: String
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(10.dp))
                Icon(
                    modifier = Modifier.size(35.dp),
                    imageVector = Icons.Filled.Warning,
                    contentDescription = "Warning"
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(text)
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismissRequest) {
                        Text(stringResource(id = R.string.dialog_simple_confirmation_cancel))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(onClick = onConfirmation) {
                        Text(stringResource(id = R.string.dialog_simple_confirmation_confirm))
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun DialogPreview() {
    SimpleConfirmationDialog(
        onDismissRequest = { },
        onConfirmation = { },
        text = "Are you sure you want to do the thing you were just about to do?"
    )
}