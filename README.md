# androidamplifyissue
An example of issue [#2295](https://github.com/aws-amplify/amplify-android/issues/2295) with the aws-amplify/amplify-android repository


## Describe the bug
If Amplify.Auth.rememberDevice() is called after confirmSignIn(), then I would expect that the device would be remembered and the user would not be challenged for MFA if they were to sign out and sign back in.

Currently, if a device is set to be remembered after a user confirms signin and then they sign out and sign in again, we receive a CONFIRM_SIGN_IN_WITH_SMS_MFA_CODE as the signIn() call's nextStep.signInStep. Unless I'm misunderstanding the expected outcome of a signOut() call, I think the user should be receiving DONE as the nextStep.signInStep

## Reproduction steps (if applicable)
1. Call `Amplify.Auth.signIn(username, password)`. Result should return `CONFIRM_SIGN_IN_WITH_SMS_MFA_CODE` as next step
2. Call `Amplify.Auth.confirmSignIn(code)`. Result should return `DONE` as next step
3. Call `Amplify.Auth.rememberDevice()`
4. Call `Amplify.Auth.signOut(options = AuthSignOutOptions.builder().globalSignOut(false).build())`
5. Call `Amplify.Auth.signIn(username, password)`. Result returns `CONFIRM_SIGN_IN_WITH_SMS_MFA_CODE` again as next step. Result should have returned `DONE` 🐛
