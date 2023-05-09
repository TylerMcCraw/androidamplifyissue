# androidamplifyissue
An example of issue [#2295](https://github.com/aws-amplify/amplify-android/issues/2295) with the aws-amplify/amplify-android repository


## Describe the bug
If Amplify.Auth.rememberDevice() is called after confirmSignIn(), then I would expect that the device would be remembered and the user would not be challenged for MFA if they were to sign out and sign back in.

Currently, if a device is set to be remembered after a user confirms signin and then they sign out and sign in again, we receive a CONFIRM_SIGN_IN_WITH_SMS_MFA_CODE as the signIn() call's nextStep.signInStep. Unless I'm misunderstanding the expected outcome of a signOut() call, I think the user should be receiving DONE as the nextStep.signInStep

## Prerequisites
1. You'll need your own AWS Cognito User Pool and App Client ID setup with MFA and remember device enabled
2. Authentication type will need to be set to USER_SRP_AUTH
3. Update the constants Pool ID, App Client ID, Web Domain, and Region in MainActivity.kt

## Reproduction steps
1. Call `Amplify.Auth.signIn(username, password)`. Result should return `CONFIRM_SIGN_IN_WITH_SMS_MFA_CODE` as next step
2. Call `Amplify.Auth.confirmSignIn(code)`. Result should return `DONE` as next step
3. Call `Amplify.Auth.rememberDevice()`
4. Call `Amplify.Auth.signOut(options = AuthSignOutOptions.builder().globalSignOut(false).build())`
5. Call `Amplify.Auth.signIn(username, password)`. Result returns `CONFIRM_SIGN_IN_WITH_SMS_MFA_CODE` again as next step. Result should have returned `DONE` 🐛

You can accomplish this by:
1. Enter username and password and click Sign In button
2. On the next screen, enter the code sent to you and click Confirm Sign In button
3. On the next screen, click the Sign Out button
4. Back on the Sign In screen, enter username and password and click Sign In button again

## Expected behavior
After signing out and signing back in, the user should not be challenged for MFA if they have already confirmed their sign in and remembered their device.

## Actual behavior
After signing out and signing back in, the user is challenged for MFA again even though they have already confirmed their sign in and remembered their device.