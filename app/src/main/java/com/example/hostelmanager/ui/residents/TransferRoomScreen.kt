package com.example.hostelmanager.ui.residents

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import com.example.hostelmanager.ui.theme.AppBackground
import com.example.hostelmanager.ui.theme.AppBorder
import com.example.hostelmanager.ui.theme.AppCard
import com.example.hostelmanager.ui.theme.AppMuted
import com.example.hostelmanager.ui.theme.AppNavy
import com.example.hostelmanager.ui.theme.AppRed
import com.example.hostelmanager.ui.theme.AppSoftBlue
import com.example.hostelmanager.ui.theme.AppSoftRed
import com.example.hostelmanager.ui.theme.AppText
import com.example.hostelmanager.util.paiseToRupees
import com.example.hostelmanager.util.rupeesToPaise
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferRoomScreen(
    personId: String,
    onBackClick: () -> Unit,
    viewModel: TransferRoomViewModel = appViewModel()
) {
    LaunchedEffect(personId) { viewModel.setPersonId(personId) }

    val person by viewModel.person.collectAsStateWithLifecycle()
    val stay by viewModel.activeStay.collectAsStateWithLifecycle()
    val currentRoom by viewModel.currentRoom.collectAsStateWithLifecycle()
    val allRooms by viewModel.availableRooms.collectAsStateWithLifecycle()
    val transferError by viewModel.transferError.collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    val transferTargets = remember(allRooms, currentRoom) {
        allRooms.filter { it.room.id != currentRoom?.id && (it.room.bedCount - it.occupiedBeds) > 0 }
    }

    var selectedTarget by remember { mutableStateOf<RoomWithOccupancy?>(null) }
    var transferDate by remember { mutableStateOf(LocalDate.now().toString()) }
    var newRent by remember { mutableStateOf("") }

    LaunchedEffect(selectedTarget) {
        val target = selectedTarget
        if (target != null && newRent.isEmpty()) {
            newRent = target.room.defaultRentPerBed.paiseToRupees().toString()
        }
    }

    val transferDateError = DateRules.validateDate(transferDate, "Transfer date", minDateIso = stay?.joinDate, notFuture = true)
    val newRentError = AmountRules.validateAmount(newRent, "New room rent")

    val isFormValid = selectedTarget != null && transferDateError == null && newRentError == null

    Scaffold(
        containerColor = AppBackground,
        topBar = {
            TopAppBar(
                title = { Text("Transfer Room", fontWeight = FontWeight.Bold, color = AppText) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = AppText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppBackground)
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
                text = person?.name ?: "Resident",
                style = MaterialTheme.typography.headlineSmall,
                color = AppText,
                fontWeight = FontWeight.Bold
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = AppCard),
                border = BorderStroke(1.dp, AppBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("Current Stay", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = AppNavy)
                    Spacer(Modifier.height(10.dp))
                    DetailRow("Room", currentRoom?.roomNumber ?: "—")
                    DetailRow("Monthly Rent", "₹${(stay?.monthlyRent ?: 0L).paiseToRupees()}")
                    DetailRow("Joined", stay?.joinDate ?: "—")
                    DetailRow("Deposit held", "₹${(stay?.depositCollected ?: 0L).paiseToRupees()}")
                }
            }

            transferError?.let { BannerMessage(text = it, textColor = AppRed, bgColor = AppSoftRed) }

            Text("Transfer To", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = AppNavy)

            if (transferTargets.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AppCard),
                    border = BorderStroke(1.dp, AppBorder)
                ) {
                    Text(
                        "No other room with vacant beds is available in this hostel right now.",
                        color = AppMuted,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    transferTargets.forEach { target ->
                        val isSelected = target.room.id == selectedTarget?.room?.id
                        val vacancy = target.room.bedCount - target.occupiedBeds

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedTarget = target
                                    newRent = target.room.defaultRentPerBed.paiseToRupees().toString()
                                },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) AppSoftBlue else AppCard
                            ),
                            border = BorderStroke(1.dp, if (isSelected) AppNavy else AppBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Room ${target.room.roomNumber}", fontWeight = FontWeight.Bold, color = AppText)
                                    Text("$vacancy free bed(s) · ₹${target.room.defaultRentPerBed.paiseToRupees()}/mo", style = MaterialTheme.typography.bodySmall, color = AppMuted)
                                }
                                if (isSelected) {
                                    Text("Selected", color = AppNavy, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            if (selectedTarget != null) {
                AppDateField(
                    label = "Transfer Date (YYYY-MM-DD) *",
                    value = transferDate,
                    onValueChange = { transferDate = it },
                    error = transferDateError
                )

                ValidatedTextField(
                    value = newRent,
                    onValueChange = { input -> newRent = input.filter { it.isDigit() } },
                    label = "New Monthly Rent (₹) *",
                    error = newRentError,
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done,
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                )
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    val target = selectedTarget ?: return@Button
                    val rentPaise = newRent.rupeesToPaise()
                    scope.launch {
                        val ok = viewModel.transferRoom(
                            newRoomId = target.room.id,
                            transferDate = transferDate,
                            newRentPaise = rentPaise
                        )
                        if (ok) onBackClick()
                    }
                },
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
                Text("Confirm Transfer", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = AppMuted, style = MaterialTheme.typography.bodyMedium)
        Text(value, color = AppText, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun BannerMessage(text: String, textColor: Color, bgColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Text(
            text = text,
            color = textColor,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(14.dp)
        )
    }
}
