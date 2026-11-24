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
import com.example.hostelmanager.domain.validation.RoomRules
import com.example.hostelmanager.ui.components.ValidatedTextField
import com.example.hostelmanager.ui.theme.AppCard
import com.example.hostelmanager.ui.theme.AppGreen
import com.example.hostelmanager.ui.theme.AppMuted
import com.example.hostelmanager.ui.theme.AppText
import com.example.hostelmanager.util.paiseToRupees
import com.example.hostelmanager.util.rupeesToPaise

@Composable
fun RoomFormDialog(
    room: RoomEntity? = null,
    occupiedBeds: Int = 0,
    errorMessage: String? = null,
    onErrorCleared: () -> Unit = {},
    onDismiss: () -> Unit,
    onConfirm: (roomNumber: String, floor: Int, bedCount: Int, rentPaise: Long) -> Unit
) {
    val isNewRoom = room == null

    var roomNumber by remember { mutableStateOf(room?.roomNumber ?: "") }
    var floor by remember { mutableStateOf(room?.floor?.toString() ?: "1") }
    var beds by remember { mutableStateOf(room?.bedCount?.toString() ?: "1") }
    var rent by remember { mutableStateOf(room?.defaultRentPerBed?.paiseToRupees()?.toString() ?: "") }

    val roomNumErr = errorMessage ?: RoomRules.roomNumberError(roomNumber, isNewRoom)
    val floorErr = RoomRules.floorError(floor)
    val bedsErr = RoomRules.bedsError(beds, occupiedBeds)
    val rentErr = RoomRules.rentError(rent, isNewRoom)

    val isFormValid = roomNumber.isNotBlank() && roomNumErr == null && floorErr == null && bedsErr == null && rentErr == null && rent.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = AppCard,
        titleContentColor = AppText,
        textContentColor = AppText,
        title = {
            Text(
                if (isNewRoom) "Add Room" else "Edit Room",
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
                    error = roomNumErr
                )
                Spacer(modifier = Modifier.height(8.dp))

                ValidatedTextField(
                    value = floor,
                    onValueChange = { input -> floor = input.filter { it.isDigit() }.take(3) },
                    label = "Floor",
                    error = floorErr,
                    keyboardType = KeyboardType.Number
                )
                Spacer(modifier = Modifier.height(8.dp))

                ValidatedTextField(
                    value = beds,
                    onValueChange = { input -> beds = input.filter { it.isDigit() }.take(3) },
                    label = "Number of Beds *",
                    error = bedsErr,
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
                    val fInt = floor.toIntOrNull() ?: 0
                    val bInt = beds.toIntOrNull() ?: 1
                    onConfirm(roomNumber.trim(), fInt, bInt, rent.rupeesToPaise())
                },
                enabled = isFormValid
            ) {
                Text(
                    if (isNewRoom) "Add" else "Save",
                    color = AppGreen,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = AppMuted, fontWeight = FontWeight.Medium)
            }
        }
    )
}
