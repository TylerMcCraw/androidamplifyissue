package com.example.android_amplify_issue

import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    onSigninButtonClick: (username: String, password: String) -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(state = rememberScrollState())
            .padding(30.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val username = remember { mutableStateOf(TextFieldValue()) }
        val password = remember { mutableStateOf(TextFieldValue()) }
        val focusManager = LocalFocusManager.current
        Text(text = "Login", style = TextStyle(fontSize = 40.sp))
        Spacer(modifier = Modifier.height(20.dp))
        TextField(
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text(text = "Username") },
            value = username.value,
            onValueChange = { username.value = it },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next, keyboardType = KeyboardType.Email),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
        )
        Spacer(modifier = Modifier.height(20.dp))
        TextField(
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text(text = "Password") },
            value = password.value,
            visualTransformation = PasswordVisualTransformation(),
            onValueChange = { password.value = it },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go, keyboardType = KeyboardType.Password),
            keyboardActions = KeyboardActions(onGo = {
                focusManager.clearFocus()
                onSigninButtonClick(username.value.text, password.value.text)
            }),
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            onClick = { onSigninButtonClick(username.value.text, password.value.text) },
        ) {
            Text(text = "Sign in")
        }
        Spacer(modifier = Modifier.height(20.dp))
        Column(modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start) {
            Text(text = "Repro Instructions:", style = TextStyle(fontSize = 20.sp))
            Text(
                style = TextStyle(fontSize = 16.sp),
                text = "1. Sign in\n\t\tResult should return CONFIRM_SIGN_IN_WITH_SMS_MFA_CODE as next step"
            )
            Text(
                style = TextStyle(fontSize = 16.sp),
                text = "2. Confirm sign in with MFA code\n\t\tResult should return DONE as next step"
            )
            Text(
                style = TextStyle(fontSize = 16.sp),
                text = "3. Device will be remembered"
            )
            Text(
                style = TextStyle(fontSize = 16.sp),
                text = "4. Sign out"
            )
            Text(
                style = TextStyle(fontSize = 16.sp),
                color = Color.Red,
                text = "5. Sign in again\n\t\tResult should return DONE as next step. But it doesn't!\n\t\tIt returns CONFIRM_SIGN_IN_WITH_SMS_MFA_CODE again, even though the devices is remembered."
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen(onSigninButtonClick = { _, _ -> })
}