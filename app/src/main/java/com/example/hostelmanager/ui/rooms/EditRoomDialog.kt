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
import com.example.hostelmanager.data.RoomEntity
import com.example.hostelmanager.domain.validation.AmountRules
import com.example.hostelmanager.ui.components.ValidatedTextField
import com.example.hostelmanager.ui.theme.AppCard
import com.example.hostelmanager.ui.theme.AppGreen
import com.example.hostelmanager.ui.theme.AppMuted
import com.example.hostelmanager.ui.theme.AppText
import com.example.hostelmanager.util.paiseToRupees
import com.example.hostelmanager.util.rupeesToPaise

@Composable
fun EditRoomDialog(
    room: RoomEntity,
    occupiedBeds: Int,
    onDismiss: () -> Unit,
    onConfirm: (roomNumber: String, floor: Int, bedCount: Int, rentPerBedPaise: Long) -> Unit
) {
    var roomNumber by remember { mutableStateOf(room.roomNumber) }
    var floor by remember { mutableStateOf(room.floor.toString()) }
    var beds by remember { mutableStateOf(room.bedCount.toString()) }
    var rent by remember { mutableStateOf(room.defaultRentPerBed.paiseToRupees().toString()) }

    val bedsInt = beds.toIntOrNull() ?: 0
    val isBedsNumberValid = bedsInt >= 1
    val isBedsEnough = bedsInt >= occupiedBeds
    val rentErr = AmountRules.validateAmount(rent, "Rent per bed")
    val isFormValid = roomNumber.isNotBlank() && isBedsNumberValid && isBedsEnough && rentErr == null

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = AppCard,
        titleContentColor = AppText,
        textContentColor = AppText,
        title = {
            Text(
                "Edit Room",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                ValidatedTextField(
                    value = roomNumber,
                    onValueChange = { roomNumber = it },
                    label = "Room Number *",
                    error = if (roomNumber.isBlank()) "Room number is required" else null
                )
                Spacer(modifier = Modifier.height(8.dp))

                ValidatedTextField(
                    value = floor,
                    onValueChange = { input -> floor = input.filter { it.isDigit() } },
                    label = "Floor",
                    error = if (floor.isBlank()) "Floor is required" else null,
                    keyboardType = KeyboardType.Number
                )
                Spacer(modifier = Modifier.height(8.dp))

                ValidatedTextField(
                    value = beds,
                    onValueChange = { input -> beds = input.filter { it.isDigit() } },
                    label = "Number of Beds *",
                    error = when {
                        !isBedsNumberValid -> "Must be at least 1"
                        !isBedsEnough -> "$occupiedBeds resident(s) live here. Beds cannot be below $occupiedBeds."
                        else -> null
                    },
                    keyboardType = KeyboardType.Number
                )
                Spacer(modifier = Modifier.height(8.dp))

                ValidatedTextField(
                    value = rent,
                    onValueChange = { input -> rent = input.filter { it.isDigit() }.take(6) },
                    label = "Rent per Bed (₹) *",
                    error = rentErr,
                    keyboardType = KeyboardType.Number
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(
                        roomNumber.trim(),
                        floor.toIntOrNull() ?: 0,
                        bedsInt,
                        rent.rupeesToPaise()
                    )
                },
                enabled = isFormValid
            ) {
                Text("Save", color = AppGreen, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = AppMuted, fontWeight = FontWeight.Medium)
            }
        }
    )
}
