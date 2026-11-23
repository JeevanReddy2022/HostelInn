package com.example.hostelmanager.ui.hostels

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.hostelmanager.domain.validation.Patterns
import com.example.hostelmanager.ui.components.ValidatedTextField
import com.example.hostelmanager.ui.theme.AppCard
import com.example.hostelmanager.ui.theme.AppGreen
import com.example.hostelmanager.ui.theme.AppMuted
import com.example.hostelmanager.ui.theme.AppText

@Composable
fun AddHostelDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, address: String, phone: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    val phoneErr = if (phone.isNotBlank() && !Patterns.Phone.matches(phone)) "Enter a valid 10-digit phone number starting with 6-9." else null
    val isFormValid = name.isNotBlank() && Patterns.Phone.matches(phone)

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = AppCard,
        titleContentColor = AppText,
        textContentColor = AppMuted,
        title = {
            Text(
                "Add Hostel",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                ValidatedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Name *",
                    error = if (name.isBlank() && phone.isNotBlank()) "Name is required" else null
                )
                Spacer(modifier = Modifier.height(8.dp))
                ValidatedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = "Address",
                    error = null
                )
                Spacer(modifier = Modifier.height(8.dp))
                ValidatedTextField(
                    value = phone,
                    onValueChange = { input -> phone = input.filter { it.isDigit() }.take(10) },
                    label = "Phone (10 digits) *",
                    error = phoneErr,
                    keyboardType = KeyboardType.Number
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name.trim(), address.trim(), phone) },
                enabled = isFormValid
            ) {
                Text(
                    "Add",
                    color = AppGreen,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = AppMuted)
            }
        }
    )
}
