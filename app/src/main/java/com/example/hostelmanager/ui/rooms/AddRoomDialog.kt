package com.example.hostelmanager.ui.rooms

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
import com.example.hostelmanager.domain.validation.AmountRules
import com.example.hostelmanager.ui.components.ValidatedTextField
import com.example.hostelmanager.ui.theme.AppCard
import com.example.hostelmanager.ui.theme.AppGreen
import com.example.hostelmanager.ui.theme.AppMuted
import com.example.hostelmanager.ui.theme.AppText
import com.example.hostelmanager.util.rupeesToPaise

@Composable
fun AddRoomDialog(
    onDismiss: () -> Unit,
    onConfirm: (roomNumber: String, floor: Int, bedCount: Int, rentPerBed: Long) -> Unit,
    errorMessage: String? = null,
    onErrorCleared: () -> Unit = {}
) {
    var roomNumber by remember { mutableStateOf("") }
    var floor by remember { mutableStateOf("1") }
    var bedCount by remember { mutableStateOf("1") }
    var rent by remember { mutableStateOf("") }

    val roomNumberValid = roomNumber.isNotBlank() && (roomNumber.toLongOrNull()?.let { it > 0 } ?: true)
    val floorValid = floor.toIntOrNull()?.let { it >= 0 } == true
    val bedCountValid = bedCount.toIntOrNull()?.let { it > 0 } == true
    val rentErr = if (rent.isNotBlank()) AmountRules.validateAmount(rent, "Rent per bed") else null

    val isInputValid = roomNumberValid && floorValid && bedCountValid && rent.isNotBlank() && rentErr == null

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = AppCard,
        titleContentColor = AppText,
        textContentColor = AppText,
        title = {
            Text(
                "Add Room",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                ValidatedTextField(
                    value = roomNumber,
                    onValueChange = {
                        roomNumber = it
                        onErrorCleared()
                    },
                    label = "Room Number *",
                    error = errorMessage ?: if (roomNumber.isNotBlank() && !roomNumberValid) "Room number must be greater than zero" else null
                )
                Spacer(modifier = Modifier.height(8.dp))

                ValidatedTextField(
                    value = floor,
                    onValueChange = { floor = it },
                    label = "Floor",
                    error = if (!floorValid) "Floor cannot be negative or empty" else null,
                    keyboardType = KeyboardType.Number
                )
                Spacer(modifier = Modifier.height(8.dp))

                ValidatedTextField(
                    value = bedCount,
                    onValueChange = { bedCount = it },
                    label = "Number of Beds",
                    error = if (!bedCountValid) "Number of beds must be greater than zero" else null,
                    keyboardType = KeyboardType.Number
                )
                Spacer(modifier = Modifier.height(8.dp))

                ValidatedTextField(
                    value = rent,
                    onValueChange = { input -> rent = input.filter { it.isDigit() }.take(6) },
                    label = "Rent per Bed (₹)",
                    error = rentErr,
                    keyboardType = KeyboardType.Number
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(roomNumber.trim(), floor.toInt(), bedCount.toInt(), rent.rupeesToPaise())
                },
                enabled = isInputValid && errorMessage == null
            ) {
                Text("Add", color = AppGreen, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = AppMuted, fontWeight = FontWeight.Medium)
            }
        }
    )
}
