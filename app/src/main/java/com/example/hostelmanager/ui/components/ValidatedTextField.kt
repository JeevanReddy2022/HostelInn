package com.example.hostelmanager.ui.components

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.example.hostelmanager.ui.theme.AppBackground
import com.example.hostelmanager.ui.theme.AppBorder
import com.example.hostelmanager.ui.theme.AppNavy
import com.example.hostelmanager.ui.theme.AppRed
import com.example.hostelmanager.ui.theme.AppText

@Composable
fun appTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = AppNavy,
    unfocusedBorderColor = AppBorder,
    focusedContainerColor = AppBackground,
    unfocusedContainerColor = AppBackground,
    focusedTextColor = AppText,
    unfocusedTextColor = AppText,
    cursorColor = AppNavy,
    focusedLabelColor = AppNavy,
    unfocusedLabelColor = AppText.copy(alpha = 0.6f),
    errorBorderColor = AppRed,
    errorLabelColor = AppRed,
    errorSupportingTextColor = AppRed
)

@Composable
fun ValidatedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    error: String?,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = true,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        isError = error != null,
        supportingText = error?.let { err -> { Text(err, style = MaterialTheme.typography.bodySmall) } },
        colors = appTextFieldColors(),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        keyboardActions = keyboardActions,
        singleLine = singleLine,
        trailingIcon = trailingIcon,
        modifier = modifier
    )
}
