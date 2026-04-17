package com.phoenix.tech.foodapp.ui.screens

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.phoenix.tech.foodapp.R
import com.phoenix.tech.foodapp.ui.state.LoginUiState
import com.phoenix.tech.foodapp.ui.state.LoginViewModel
import com.phoenix.tech.foodapp.ui.theme.AppTextFieldColors
import com.phoenix.tech.foodapp.ui.theme.AppTheme
import com.phoenix.tech.foodapp.utils.InputValidators

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    // Handle Login Success (navigation redirect callback)
    LaunchedEffect(uiState) {
        if (uiState is LoginUiState.Success) {
            onLoginSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            }
    ) {
        LoginContent(
            uiState = uiState,
            onLoginClick = { email, pswd ->
                viewModel.login(email, pswd)
            },
            onReset = {
                viewModel.resetState()
            }
        )
    }
}


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun LoginContent(
    uiState: LoginUiState,
    onLoginClick: (String, String) -> Unit,
    onReset: () -> Unit
) {
    val isLoading = uiState is LoginUiState.Loading
    val appName: String = stringResource(R.string.app_name)
    val scrollState = rememberScrollState()

    // Inputs Local States
    var emailInput by remember { mutableStateOf("") }
    var pswdInput by remember { mutableStateOf("") }
    var isPswdVisible by remember { mutableStateOf(false) }
    // Validations Local States
    val emailValidationError = InputValidators.getEmailError(emailInput)
    val pswdValidationError = InputValidators.getPswdError(pswdInput)
    val isFormValid = emailInput.isNotEmpty()
            && pswdInput.isNotEmpty()
            && emailValidationError == null
            && pswdValidationError == null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState) // Enables auto-scrolling when keyboard appears
            .padding(horizontal = 35.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Welcome to $appName",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(30.dp))

        EditTextField(
            label = "Email",
            iconRes = R.drawable.ic_email_outlined,
            inputValue = emailInput,
            onValueChange = {
                emailInput = it
                if (uiState is LoginUiState.Error) {
                    onReset()
                }
            },
            error = emailValidationError,
        )

        Spacer(modifier = Modifier.height(15.dp))

        EditTextField(
            isPswd = true,
            label = "Password",
            iconRes = R.drawable.ic_password,
            inputValue = pswdInput,
            onValueChange = {
                pswdInput = it
                if (uiState is LoginUiState.Error) {
                    onReset()
                }
            },
            isPswdVisible = isPswdVisible,
            onVisibilityChanged = { isPswdVisible = it },
            error = pswdValidationError,
            imeAction = ImeAction.Done
        )

        Spacer(modifier = Modifier.height(30.dp))

        // API Error message (Displayed only if state is Error)
        if (uiState is LoginUiState.Error) {
            Text(
                text = uiState.message,
                color = AppTextFieldColors.Error,
                style = MaterialTheme.typography.titleSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        Button(
            enabled = isFormValid && !isLoading,
            onClick = {
                onLoginClick(emailInput, pswdInput)
            },
            shape = RoundedCornerShape(15.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Text(
                    "Login",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(vertical = 5.dp)
                )
            }
        }
    }
}

@Composable
private fun EditTextField(
    label: String,
    inputValue: String,
    @DrawableRes iconRes: Int,
    onValueChange: (String) -> Unit,
    error: String?,
    modifier: Modifier = Modifier,
    isPswd: Boolean = false,
    isPswdVisible: Boolean = false,
    onVisibilityChanged: (Boolean) -> Unit = {},
//    onTogglePswd: () -> Unit = {},
    imeAction: ImeAction = ImeAction.Next
) {
    val focusManager = LocalFocusManager.current
    val isValid = inputValue.trim().isNotEmpty() && error == null
    val isError = error != null

    val stateColor = when {
        inputValue.isEmpty() -> AppTextFieldColors.Default
        isError -> AppTextFieldColors.Error
        isValid -> AppTextFieldColors.Success
        else -> AppTextFieldColors.Default
    }

    OutlinedTextField(
        value = inputValue,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        label = {
            Text(label)
        },
        isError = isError,
        supportingText = {
            if (isError) {
                Text(error, color = AppTextFieldColors.Error)
            }
        },
        leadingIcon = {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = "$label icon",
                tint = stateColor
            )
        },
        trailingIcon = {
            // EYE-ICON for Password
            if (isPswd) {
                val icon = if (isPswdVisible) {
                    R.drawable.ic_visibility_off
                } else {
                    R.drawable.ic_visibility
                }
                Icon(
                    painter = painterResource(icon),
                    contentDescription = "Toggle Password visibility",
                    modifier = Modifier.pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                try {
                                    onVisibilityChanged(true)
                                    awaitRelease()
                                } finally {
                                    onVisibilityChanged(false)
                                }
                            }
                        )
                    }
                )
            }
        },
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            fontWeight = if (inputValue.isEmpty()) FontWeight.Normal else FontWeight.Medium,
            color = if (isError) AppTextFieldColors.Error else MaterialTheme.colorScheme.onSurface
        ),
        shape = RoundedCornerShape(15.dp),
        visualTransformation = if (isPswd && !isPswdVisible) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(
            keyboardType = if (isPswd) KeyboardType.Password else KeyboardType.Email,
            imeAction = imeAction
        ),
        keyboardActions = KeyboardActions(
            onNext = { if (isValid) focusManager.moveFocus(FocusDirection.Down) },
            onDone = { if (isValid) focusManager.clearFocus() }
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AppTextFieldColors.Focused,
            unfocusedBorderColor = stateColor,
            errorBorderColor = AppTextFieldColors.Error,
            focusedLabelColor = AppTextFieldColors.Focused,
            errorLabelColor = AppTextFieldColors.Error
        )
    )
}


//@Composable
//private fun ShowPswdCheckbox(
//    isPswdVisible: Boolean,
//    onClick: () -> Unit
//) {
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .clickable(
//                interactionSource = remember { MutableInteractionSource() },
//                indication = ripple(),
//                onClick = onClick
//            )
//            .padding(vertical = 4.dp, horizontal = 5.dp)
//            .clip(shape = RoundedCornerShape(10.dp)),
//        verticalAlignment = Alignment.CenterVertically,
//        horizontalArrangement = Arrangement.Start
//    ) {
//        CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
//            Checkbox(
//                checked = isPswdVisible,
//                onCheckedChange = null, // Entire Row instead handles click
//                colors = CheckboxDefaults.colors(
//                    checkedColor = AppTextFieldColors.Focused,
//                    uncheckedColor = AppTextFieldColors.Default
//                ),
//            )
//        }
//        Spacer(modifier = Modifier.width(8.dp))
//        Text(
//            text = "Show Password",
//            style = MaterialTheme.typography.bodyMedium
//        )
//    }
//}


@Preview(showBackground = true)
@Composable
fun LoginPreview() {
    AppTheme {
        LoginContent(
            uiState = LoginUiState.Error("409: Invalid Credentials"),
            onLoginClick = { _, _ -> },
            onReset = {}
        )
    }
}