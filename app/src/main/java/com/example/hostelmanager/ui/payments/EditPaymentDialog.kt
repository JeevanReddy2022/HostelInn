package com.example.hostelmanager.ui.payments

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.hostelmanager.data.Payment
import com.example.hostelmanager.domain.validation.AmountRules
import com.example.hostelmanager.domain.validation.DateRules
import com.example.hostelmanager.ui.components.AppDateField
import com.example.hostelmanager.ui.components.ValidatedTextField
import com.example.hostelmanager.ui.components.appTextFieldColors
import com.example.hostelmanager.ui.theme.AppCard
import com.example.hostelmanager.ui.theme.AppMuted
import com.example.hostelmanager.ui.theme.AppNavy
import com.example.hostelmanager.ui.theme.AppOrange
import com.example.hostelmanager.ui.theme.AppRed
import com.example.hostelmanager.ui.theme.AppSoftOrange
import com.example.hostelmanager.ui.theme.AppText
import com.example.hostelmanager.util.paiseToRupees
import com.example.hostelmanager.util.rupeesToPaise
import kotlinx.coroutines.launch

private const val MIN_EDIT_REASON = 5
private val EDIT_MODES = listOf("UPI", "Cash", "Bank Transfer")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPaymentDialog(
    payment: Payment,
    personName: String,
    onDismiss: () -> Unit,
    onSave: suspend (
        amountPaise: Long,
        mode: String,
        paymentDate: String,
        recordedDate: String,
        note: String,
        reason: String
    ) -> String?
) {
    val scope = rememberCoroutineScope()

    var amount by remember { mutableStateOf(payment.amountPaise.paiseToRupees().toString()) }
    var mode by remember { mutableStateOf(payment.mode) }
    var modeExpanded by remember { mutableStateOf(false) }
    var paymentDate by remember { mutableStateOf(payment.paymentDate) }
    var recordedDate by remember { mutableStateOf(payment.date) }
    var note by remember { mutableStateOf(payment.note) }
    var reason by remember { mutableStateOf("") }
    var serverError by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    val isWriteOff = amount.trim().toLongOrNull() == 0L
    val amountErr = AmountRules.validateAmount(amount, "Amount", minRupees = 0)
    val paymentDateErr = DateRules.validateDate(paymentDate, "Payment date", notFuture = true)
    val recordedDateErr = DateRules.validateDate(recordedDate, "Recorded date", notFuture = true)
    val reasonErr = if (reason.trim().length < MIN_EDIT_REASON) {
        "A reason of at least $MIN_EDIT_REASON characters is required."
    } else null

    val canSave = amountErr == null && paymentDateErr == null &&
            recordedDateErr == null && reasonErr == null && !isSaving

    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        shape = RoundedCornerShape(24.dp),
        containerColor = AppCard,
        titleContentColor = AppText,
        textContentColor = AppText,
        title = {
            Text("Edit Payment", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    "$personName · edited ${payment.editCount} time(s) so far",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppMuted
                )
                Spacer(modifier = Modifier.height(12.dp))

                ValidatedTextField(
                    value = amount,
                    onValueChange = { amount = it.filter { c -> c.isDigit() }.take(9) },
                    label = "Amount (₹) *  — 0 = write off",
                    error = amountErr,
                    keyboardType = KeyboardType.Number
                )

                if (isWriteOff) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = AppSoftOrange)
                    ) {
                        Text(
                            "Saving ₹0 turns this into a WRITE-OFF of the remaining balance. " +
                                    "No income will be recorded for it.",
                            modifier = Modifier.padding(12.dp),
                            color = AppOrange,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                ExposedDropdownMenuBox(
                    expanded = modeExpanded,
                    onExpandedChange = { modeExpanded = !modeExpanded }
                ) {
                    OutlinedTextField(
                        value = mode,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Payment Mode *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = modeExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = appTextFieldColors()
                    )
                    ExposedDropdownMenu(
                        expanded = modeExpanded,
                        onDismissRequest = { modeExpanded = false }
                    ) {
                        EDIT_MODES.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = { mode = option; modeExpanded = false }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                AppDateField(
                    label = "Payment Date (when received) *",
                    value = paymentDate,
                    onValueChange = { paymentDate = it },
                    error = paymentDateErr
                )
                Spacer(modifier = Modifier.height(8.dp))
                AppDateField(
                    label = "Recorded Date (when entered) *",
                    value = recordedDate,
                    onValueChange = { recordedDate = it },
                    error = recordedDateErr
                )
                Spacer(modifier = Modifier.height(8.dp))
                ValidatedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = "Note (optional)",
                    error = null
                )

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    "Every change is permanently logged with your reason.",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppMuted
                )
                Spacer(modifier = Modifier.height(6.dp))
                ValidatedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = "Reason for edit *",
                    error = if (reason.isNotEmpty()) reasonErr else null,
                    imeAction = ImeAction.Done
                )

                if (serverError != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        serverError!!,
                        color = AppRed,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = canSave,
                onClick = {
                    isSaving = true
                    serverError = null
                    scope.launch {
                        val error = onSave(
                            amount.rupeesToPaise(),
                            mode,
                            paymentDate.trim(),
                            recordedDate.trim(),
                            note,
                            reason
                        )
                        isSaving = false
                        if (error == null) onDismiss() else serverError = error
                    }
                }
            ) {
                Text(
                    if (isSaving) "Saving…" else "Save Edit",
                    color = if (canSave) AppNavy else AppMuted,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving) {
                Text("Cancel", color = AppMuted)
            }
        }
    )
}