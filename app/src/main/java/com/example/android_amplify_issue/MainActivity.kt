package com.example.android_amplify_issue

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.amplifyframework.AmplifyException
import com.amplifyframework.auth.AuthException
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin
import com.amplifyframework.auth.options.AuthSignOutOptions
import com.amplifyframework.auth.result.step.AuthSignInStep.*
import com.amplifyframework.core.AmplifyConfiguration
import com.amplifyframework.kotlin.core.Amplify
import com.amplifyframework.logging.AndroidLoggingPlugin
import com.amplifyframework.logging.LogLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject

class MainActivity : AppCompatActivity() {
    companion object {
        private const val logTag: String = "MainActivity"

        // TODO replace these values with your own
        private const val poolId: String = "MY_POOL_ID"
        private const val clientId: String = "MY_CLIENT_ID"
        private const val region: String = "MY_REGION"
        private const val webDomain: String = "MY_WEB_DOMAIN" // e.g. "https://myapp.auth.us-east-1.amazoncognito.com

        private val amplifyConfigJson = JSONObject(
            """
            {
              "auth": {
                "plugins": {
                  "awsCognitoAuthPlugin": {
                    "IdentityManager": {
                      "Default": {}
                    },
                    "CognitoUserPool": {
                      "Default": {
                        "PoolId": "$poolId",
                        "AppClientId": "$clientId",
                        "Region": "$region"
                      }
                    },
                    "Auth": {
                      "Default": {
                        "authenticationFlowType": "USER_SRP_AUTH",
                        "OAuth": {
                          "WebDomain": "$webDomain",
                          "AppClientId": "$clientId",
                          "Scopes": [
                            "aws.cognito.signin.user.admin"
                          ]
                        }
                      }
                    }
                  }
                }
              }
            }
        """.trimIndent()
        )
    }

    private val viewState = MutableStateFlow(ViewState())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initAmplify()
        checkAuthState()
        setContent {
            val state by viewState.collectAsState()
            BackHandler(onBack = {
                when (viewState.value.screenToShow) {
                    Screen.Home -> signOut()
                    Screen.Login -> finish()
                    Screen.MFACodeEntry -> viewState.update { it.copy(screenToShow = Screen.Login) }
                }
            })
            MaterialTheme {
                Scaffold { paddingValues ->
                    Surface {
                        when (state.screenToShow) {
                            Screen.Login -> {
                                // A surface container using the 'background' color from the theme
                                LoginScreen(
                                    modifier = Modifier.padding(paddingValues),
                                    onSigninButtonClick = { username, password ->
                                        signIn(username, password)
                                    }
                                )
                            }
                            Screen.MFACodeEntry -> {
                                MfaCodeEntryScreen(
                                    modifier = Modifier.padding(paddingValues),
                                    onConfirmSigninClick = { code ->
                                        confirmSignin(code)
                                    }
                                )
                            }
                            Screen.Home -> {
                                HomeScreen(
                                    modifier = Modifier.padding(paddingValues),
                                    onSignOutClick = {
                                        signOut()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun checkAuthState() {
        lifecycleScope.launch(Dispatchers.IO) {
            val result = Amplify.Auth.fetchAuthSession()
            val isSignedIn = result.isSignedIn
            Log.d(logTag, "isSignedIn: $isSignedIn")
            if (isSignedIn) {
                viewState.update {
                    it.copy(screenToShow = Screen.Home)
                }
            } else {
                viewState.update {
                    it.copy(screenToShow = Screen.Login)
                }
            }
        }
    }

    private fun initAmplify() {
        try {
            Amplify.addPlugin(AndroidLoggingPlugin(LogLevel.VERBOSE))
            Amplify.addPlugin(AWSCognitoAuthPlugin())
            Amplify.configure(
                AmplifyConfiguration.builder(amplifyConfigJson).devMenuEnabled(true).build(),
                this
            )
            Log.d(logTag, "Initialized Amplify")
        } catch (error: AmplifyException) {
            Log.e(logTag, "Could not initialize Amplify", error)
        }
    }

    private fun signIn(
        username: String,
        password: String,
    ) {
        lifecycleScope.launch(Dispatchers.IO) {
            val nextStep = try {
                val result = Amplify.Auth.signIn(username, password)
                if (result.isSignedIn) {
                    Log.d(logTag, "Sign in succeeded")
                } else {
                    Log.d(logTag, "Sign in not complete")
                }
                result.nextStep.signInStep
            } catch (error: AuthException) {
                Log.e(logTag, "Sign in failed", error)
                null
            }
            if (nextStep == null) {
                Log.d(logTag, "Sign in failed")
                return@launch
            }
            when (nextStep) {
                CONFIRM_SIGN_IN_WITH_SMS_MFA_CODE ->
                    viewState.update { it.copy(screenToShow = Screen.MFACodeEntry) }

                else -> // Not concerned about other cases
                    viewState.update { it.copy(screenToShow = Screen.Home) }
            }
        }
    }

    private fun confirmSignin(code: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            val nextStep = try {
                val result = Amplify.Auth.confirmSignIn(challengeResponse = code)
                if (result.isSignedIn) {
                    Log.d(logTag, "Confirm sign in succeeded")
                } else {
                    Log.d(logTag, "Confirm sign in not complete")
                }
                result.nextStep.signInStep
            } catch (error: AuthException) {
                Log.d(logTag, "Confirm sign in failed", error)
                null
            }
            when (nextStep) {
                DONE -> {
                    viewState.update { it.copy(screenToShow = Screen.Home) }
                    rememberDevice()
                    Log.d(logTag, "Device remembered")
                }

                else -> {}// Not concerned about other cases
            }
        }
    }

    private suspend fun rememberDevice() {
        try {
            Amplify.Auth.rememberDevice()
            Log.d(logTag, "Remember device succeeded")
        } catch (error: AuthException) {
            Log.e(logTag, "Remember device failed", error)
        }
    }

    private fun signOut() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                Amplify.Auth.signOut(
                    options = AuthSignOutOptions.builder().globalSignOut(false).build()
                )
                Log.d(logTag, "Sign out succeeded")
                viewState.update { it.copy(screenToShow = Screen.Login) }
            } catch (error: AuthException) {
                Log.e(logTag, "Sign out failed", error)
            }
        }
    }
}

data class ViewState(
    val screenToShow: Screen = Screen.Login,
)

sealed class Screen {
    object Login : Screen()
    object MFACodeEntry : Screen()
    object Home : Screen()
}
