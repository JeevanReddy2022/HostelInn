package com.example.hostelmanager.ui.residents

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hostelmanager.data.RoomWithOccupancy
import com.example.hostelmanager.di.appViewModel
import com.example.hostelmanager.domain.validation.AmountRules
import com.example.hostelmanager.domain.validation.DateRules
import com.example.hostelmanager.ui.components.AppDateField
import com.example.hostelmanager.ui.components.ValidatedTextField
import com.example.hostelmanager.ui.components.appTextFieldColors
import com.example.hostelmanager.ui.rooms.RoomPickerDialog
import com.example.hostelmanager.ui.theme.AppBackground
import com.example.hostelmanager.ui.theme.AppBorder
import com.example.hostelmanager.ui.theme.AppCard
import com.example.hostelmanager.ui.theme.AppMuted
import com.example.hostelmanager.ui.theme.AppNavy
import com.example.hostelmanager.ui.theme.AppText
import com.example.hostelmanager.util.paiseToRupees
import com.example.hostelmanager.util.rupeesToPaise
import kotlinx.coroutines.launch
import java.time.LocalDate

private val RoomWithOccupancy.vacancy get() = room.bedCount - occupiedBeds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReAdmitResidentScreen(
    personId: String,
    hostelId: String,
    onBackClick: () -> Unit,
    viewModel: ReAdmitResidentViewModel = appViewModel()
) {
    LaunchedEffect(personId, hostelId) {
        viewModel.setPersonId(personId)
        viewModel.setHostelId(hostelId)
    }

    val person by viewModel.person.collectAsStateWithLifecycle()
    val roomsForHostel by viewModel.roomsForHostel.collectAsStateWithLifecycle()
    val availableRooms = remember(roomsForHostel) { roomsForHostel.filter { it.vacancy > 0 } }

    val scope = rememberCoroutineScope()
    val today = remember { LocalDate.now().toString() }

    var selectedRoom by remember { mutableStateOf<RoomWithOccupancy?>(null) }
    var joinDate by remember { mutableStateOf(today) }
    var rent by remember { mutableStateOf("") }
    var deposit by remember { mutableStateOf("") }
    var showRoomPicker by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    val joinDateError = DateRules.validateDate(joinDate, "Join date", notFuture = true)
    val rentError = AmountRules.validateAmount(rent, "Monthly rent")
    val depositError = AmountRules.validateAmount(deposit, "Deposit amount", minRupees = 0)

    val isFormValid = selectedRoom != null && joinDateError == null && rentError == null && depositError == null

    Scaffold(
        containerColor = AppBackground,
        topBar = {
            TopAppBar(
                title = { Text("Re-admit Resident", fontWeight = FontWeight.Bold, color = AppText) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = AppText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppBackground,
                    titleContentColor = AppText,
                    navigationIconContentColor = AppText
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                person?.name ?: "Past Resident",
                style = MaterialTheme.typography.headlineSmall,
                color = AppText,
                fontWeight = FontWeight.Bold
            )

            ResidentDetailsCard(phone = person?.phone, govtId = person?.govtIdNumber)

            Text("New Stay Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = AppNavy)

            val currentRoom = selectedRoom
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = currentRoom?.room?.roomNumber ?: "",
                    onValueChange = {},
                    readOnly = true,
                    singleLine = true,
                    label = { Text("Select Room *") },
                    placeholder = { Text("Tap to search rooms", color = AppMuted) },
                    trailingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search rooms", tint = AppNavy)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = appTextFieldColors(),
                    supportingText = {
                        if (currentRoom != null) {
                            Text(
                                "Beds: ${currentRoom.room.bedCount} · Vacancy: ${currentRoom.vacancy}",
                                color = AppMuted
                            )
                        }
                    }
                )

                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { showRoomPicker = true }
                )
            }

            AppDateField(
                label = "Join Date (YYYY-MM-DD) *",
                value = joinDate,
                onValueChange = { joinDate = it },
                error = joinDateError
            )

            ValidatedTextField(
                value = rent,
                onValueChange = { input -> rent = input.filter { it.isDigit() }.take(6) },
                label = "Monthly Rent (₹) *",
                error = rentError,
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next,
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
            )

            ValidatedTextField(
                value = deposit,
                onValueChange = { input -> deposit = input.filter { it.isDigit() }.take(6) },
                label = "Deposit Collected (₹) *",
                error = depositError,
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done,
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { showConfirmDialog = true },
                enabled = isFormValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppNavy,
                    contentColor = Color.White,
                    disabledContainerColor = AppBorder,
                    disabledContentColor = AppMuted
                )
            ) {
                Text("Re-admit Resident", fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showRoomPicker) {
        RoomPickerDialog(
            rooms = availableRooms,
            onDismiss = { showRoomPicker = false },
            onSelect = { roomWithOccupants ->
                selectedRoom = roomWithOccupants
                rent = roomWithOccupants.room.defaultRentPerBed.paiseToRupees().toString()
                showRoomPicker = false
            }
        )
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Confirm Re-admission", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Re-admit ${person?.name ?: "this resident"} to Room ${selectedRoom?.room?.roomNumber ?: ""} starting $joinDate?"
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showConfirmDialog = false
                    val room = selectedRoom ?: return@TextButton
                    val rentPaise = rent.rupeesToPaise()
                    val depositPaise = deposit.rupeesToPaise()

                    scope.launch {
                        val ok = viewModel.reAdmitResident(
                            roomId = room.room.id,
                            joinDate = joinDate,
                            rentPaise = rentPaise,
                            depositPaise = depositPaise
                        )
                        if (ok) onBackClick()
                    }
                }) {
                    Text("Re-admit", color = AppNavy, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancel", color = AppMuted)
                }
            }
        )
    }
}

@Composable
private fun ResidentDetailsCard(phone: String?, govtId: String?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppCard),
        border = BorderStroke(1.dp, AppBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Phone", color = AppMuted, style = MaterialTheme.typography.bodyMedium)
                Text(phone ?: "—", color = AppText, fontWeight = FontWeight.SemiBold)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Govt ID", color = AppMuted, style = MaterialTheme.typography.bodyMedium)
                Text(govtId ?: "—", color = AppText, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
