package com.example.hostelmanager.ui.residents

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hostelmanager.di.appViewModel
import com.example.hostelmanager.ui.components.DetailRow
import com.example.hostelmanager.util.paiseToText
import com.example.hostelmanager.domain.validation.AmountRules
import com.example.hostelmanager.domain.validation.DateRules
import com.example.hostelmanager.ui.components.AppDateField
import com.example.hostelmanager.ui.components.ValidatedTextField
import com.example.hostelmanager.ui.components.appTextFieldColors
import com.example.hostelmanager.ui.theme.AppBackground
import com.example.hostelmanager.ui.theme.AppBorder
import com.example.hostelmanager.ui.theme.AppCard
import com.example.hostelmanager.ui.theme.AppMuted
import com.example.hostelmanager.ui.theme.AppNavy
import com.example.hostelmanager.ui.theme.AppRed
import com.example.hostelmanager.ui.theme.AppSoftRed
import com.example.hostelmanager.ui.theme.AppText
import com.example.hostelmanager.util.paiseToRupees
import com.example.hostelmanager.util.rupeesToPaise
import kotlinx.coroutines.launch
import java.time.LocalDate

private val REFUND_MODES = listOf("Cash", "UPI", "Bank Transfer")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    personId: String,
    onBackClick: () -> Unit,
    onCheckoutComplete: () -> Unit,
    viewModel: CheckoutViewModel = appViewModel()
) {
    LaunchedEffect(personId) { viewModel.setPersonId(personId) }

    val person by viewModel.person.collectAsStateWithLifecycle()
    val activeStay by viewModel.activeStay.collectAsStateWithLifecycle()
    val currentRoom by viewModel.currentRoom.collectAsStateWithLifecycle()
    val checkoutError by viewModel.checkoutError.collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val today = remember { LocalDate.now().toString() }

    var checkoutDate by remember { mutableStateOf(today) }
    var refundRupees by remember { mutableStateOf("") }
    var selectedRefundMode by remember { mutableStateOf("Cash") }
    var refundModeExpanded by remember { mutableStateOf(false) }
    var refundDate by remember { mutableStateOf(today) }

    val depositPaise = activeStay?.depositCollected ?: 0L
    val depositRupees = depositPaise.paiseToRupees()
    val joinDate = activeStay?.joinDate

    val checkoutDateError = DateRules.validateDate(checkoutDate, "Check-out date", minDateIso = joinDate, notFuture = true)
    val refundAmountError = if (refundRupees.isNotBlank()) {
        AmountRules.validateAmount(refundRupees, "Refund amount", minRupees = 0, maxRupees = depositRupees)
    } else null

    val isFormValid = activeStay != null && checkoutDateError == null && refundAmountError == null

    Scaffold(
        containerColor = AppBackground,
        topBar = {
            TopAppBar(
                title = { Text("Check Out", fontWeight = FontWeight.Bold, color = AppText) },
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
                    DetailRow("Monthly Rent", (activeStay?.monthlyRent ?: 0L).paiseToText())
                    DetailRow("Joined", activeStay?.joinDate ?: "—")
                    DetailRow("Deposit held", depositPaise.paiseToText())
                }
            }

            checkoutError?.let { BannerMessage(text = it, textColor = AppRed, bgColor = AppSoftRed) }

            Text("Check-out Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = AppNavy)

            AppDateField(
                label = "Check-out Date (YYYY-MM-DD) *",
                value = checkoutDate,
                onValueChange = { checkoutDate = it },
                error = checkoutDateError
            )

            ValidatedTextField(
                value = refundRupees,
                onValueChange = { input -> refundRupees = input.filter { it.isDigit() } },
                label = "Refund Deposit Amount (₹) - Max ${depositPaise.paiseToText()}",
                error = refundAmountError,
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next,
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
            )

            val wantsRefund = (refundRupees.toLongOrNull() ?: 0L) > 0

            if (wantsRefund) {
                ExposedDropdownMenuBox(
                    expanded = refundModeExpanded,
                    onExpandedChange = { refundModeExpanded = !refundModeExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedRefundMode,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Refund Mode *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = refundModeExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = appTextFieldColors()
                    )
                    ExposedDropdownMenu(
                        expanded = refundModeExpanded,
                        onDismissRequest = { refundModeExpanded = false }
                    ) {
                        REFUND_MODES.forEach { mode ->
                            DropdownMenuItem(
                                text = { Text(mode) },
                                onClick = {
                                    selectedRefundMode = mode
                                    refundModeExpanded = false
                                }
                            )
                        }
                    }
                }

                AppDateField(
                    label = "Refund Date (YYYY-MM-DD) *",
                    value = refundDate,
                    onValueChange = { refundDate = it },
                    error = null
                )
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                    val refundPaise = refundRupees.rupeesToPaise()
                    scope.launch {
                        val ok = viewModel.checkoutResident(
                            checkoutDate = checkoutDate,
                            refundPaise = refundPaise,
                            refundMode = if (refundPaise > 0) selectedRefundMode else null,
                            refundDate = if (refundPaise > 0) refundDate else null
                        )
                        if (ok) onCheckoutComplete()
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
                Text("Confirm Check-out", fontWeight = FontWeight.Bold)
            }
        }
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
