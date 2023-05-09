package com.example.android_amplify_issue

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MfaCodeEntryScreen(
    modifier: Modifier = Modifier,
    onConfirmSigninClick: (code: String) -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(state = rememberScrollState())
            .padding(30.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val code = remember { mutableStateOf(TextFieldValue()) }
        val focusManager = LocalFocusManager.current
        Text(text = "Confirm Sign in", style = TextStyle(fontSize = 40.sp))
        Spacer(modifier = Modifier.height(20.dp))
        TextField(
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text(text = "Code") },
            value = code.value,
            onValueChange = { code.value = it },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go, keyboardType = KeyboardType.Number),
            keyboardActions = KeyboardActions(onGo = {
                focusManager.clearFocus()
                onConfirmSigninClick(code.value.text)
            }),
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            onClick = { onConfirmSigninClick(code.value.text) },
        ) {
            Text(text = "Confirm Sign in")
        }
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun MfaCodeEntryScreenPreview() {
    MfaCodeEntryScreen(onConfirmSigninClick = { })
}