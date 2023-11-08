package com.onandor.notemanager.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.onandor.notemanager.R
import kotlin.math.roundToInt

@Composable
fun PinEntryDialog(
    onConfirmPin: (String) -> Boolean,
    onDismissRequest: () -> Unit,
    description: String = ""
) {
    var pin by remember { mutableStateOf("") }
    var incorrectPinTrigger by remember { mutableLongStateOf(0L) }

    Dialog(onDismissRequest = onDismissRequest) {
        Card(shape = RoundedCornerShape(16.dp)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(description)
                }
                Spacer(modifier = Modifier.height(30.dp))
                PinDisplay(
                    pin = pin,
                    incorrectPinTrigger = incorrectPinTrigger,
                )
                Spacer(modifier = Modifier.height(30.dp))
                KeyPad(
                    onKeyPressed = { char ->
                        if (pin.length < 4) pin += char
                        else incorrectPinTrigger = System.currentTimeMillis()
                    },
                    onConfirm = {
                        if (!onConfirmPin(pin))
                            incorrectPinTrigger = System.currentTimeMillis()
                        },
                    onDelete = { pin = pin.dropLast(1) }
                )
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun PinDisplay(
    pin: String,
    incorrectPinTrigger: Long
) {
    val density = LocalDensity.current
    val shake = remember { Animatable(0f) }

    LaunchedEffect(incorrectPinTrigger) {
        if (incorrectPinTrigger == 0L)
            return@LaunchedEffect

        for (i in 0..10) {
            when (i % 2) {
                0 -> shake.animateTo(5f, spring(stiffness = 100_000f))
                else -> shake.animateTo(-5f, spring(stiffness = 100_000f))
            }
        }
        shake.animateTo(0f)
    }

    Row(
        modifier = Modifier
            .offset(
                x = with(density) { shake.value.roundToInt().toDp() },
                y = 0.dp
            )
    ) {
        for (idx in 0..3) {
            val color = if (idx < pin.length)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.surface

            val animatedColor = animateColorAsState(
                targetValue = color,
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                label = ""
            )

            Box(
                modifier = Modifier
                    .padding(start = 2.dp, end = 2.dp)
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(animatedColor.value)
                    .padding(start = 5.dp, end = 5.dp)
            )
        }
    }
}

@Composable
private fun KeyPad(
    onKeyPressed: (Char) -> Unit,
    onConfirm: () -> Unit,
    onDelete: () -> Unit
) {
    val keys = listOf(
        listOf('1', '2', '3'),
        listOf('4', '5', '6'),
        listOf('7', '8', '9')
    )

    keys.forEach { row ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            row.forEach { char ->
                KeyPadButton(character = char, onKeyPressed = { onKeyPressed(char) })
            }
        }
    }
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        IconButton(
            modifier = Modifier
                .padding(end = 19.dp)
                .width(40.dp)
                .height(40.dp),
            onClick = onDelete
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_backspace_filled),
                contentDescription = stringResource(id = R.string.keypad_delete_last_character)
            )
        }
        KeyPadButton(character = '0', onKeyPressed = { onKeyPressed('0') })
        Button(
            modifier = Modifier
                .padding(start = 19.dp)
                .width(40.dp)
                .height(40.dp),
            onClick = onConfirm,
            contentPadding = PaddingValues(all = 5.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = stringResource(id = R.string.keypad_confirm)
            )
        }
    }
}

@Composable
private fun KeyPadButton(
    character: Char,
    onKeyPressed: (Char) -> Unit
) {
    TextButton(
        modifier = Modifier.padding(start = 10.dp, end = 10.dp),
        onClick = { onKeyPressed(character) }
    ) {
        Text(
            text = character.toString(),
            fontSize = 30.sp
        )
    }
}

@Preview
@Composable
private fun PinEntryDialogPreview() {
    PinEntryDialog(
        description = "This note is locked with a PIN. Enter it to unlock the note.",
        onConfirmPin = { false },
        onDismissRequest = { }
    )
}