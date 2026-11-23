package com.example.hostelmanager.ui.expenses

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.hostelmanager.data.Expense
import com.example.hostelmanager.domain.validation.AmountRules
import com.example.hostelmanager.domain.validation.DateRules
import com.example.hostelmanager.ui.components.AppDateField
import com.example.hostelmanager.ui.components.ImagePickerField
import com.example.hostelmanager.ui.components.ValidatedTextField
import com.example.hostelmanager.ui.components.appTextFieldColors
import com.example.hostelmanager.ui.theme.AppCard
import com.example.hostelmanager.ui.theme.AppMuted
import com.example.hostelmanager.ui.theme.AppNavy
import com.example.hostelmanager.ui.theme.AppText
import com.example.hostelmanager.util.paiseToRupees
import com.example.hostelmanager.util.rupeesToPaise

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditExpenseDialog(
    expense: Expense,
    hostelId: String,
    onDismiss: () -> Unit,
    onUpdate: (
        id: String,
        expenseDate: String,
        enteredDate: String,
        category: String,
        amountPaise: Long,
        note: String,
        receiptImagePath: String?
    ) -> Unit
) {
    val focusManager = LocalFocusManager.current

    var expenseDate by remember { mutableStateOf(expense.expenseDate) }
    var enteredDate by remember { mutableStateOf(expense.enteredDate) }

    val categories = listOf("Electricity", "Water", "Maintenance", "Repairs", "Supplies", "Salary", "Other")
    var selectedCategory by remember { mutableStateOf(expense.category) }
    var expandedCategory by remember { mutableStateOf(false) }

    var amount by remember { mutableStateOf(expense.amountPaise.paiseToRupees().toString()) }
    var note by remember { mutableStateOf(expense.note) }
    var receiptImagePath by remember { mutableStateOf<String?>(expense.receiptImagePath) }

    val expenseDateErr = DateRules.validateDate(expenseDate, "Expense date", notFuture = true)
    val enteredDateErr = DateRules.validateDate(enteredDate, "Recorded date", notFuture = true)
    val amountErr = AmountRules.validateAmount(amount, "Amount")

    val isFormValid = expenseDateErr == null &&
            enteredDateErr == null &&
            selectedCategory.isNotBlank() &&
            amountErr == null

    val submitForm = {
        if (isFormValid) {
            onUpdate(
                expense.id,
                expenseDate.trim(),
                enteredDate.trim(),
                selectedCategory,
                amount.rupeesToPaise(),
                note.trim(),
                receiptImagePath
            )
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = AppCard,
        titleContentColor = AppText,
        textContentColor = AppText,
        title = {
            Text("Edit Expense", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                AppDateField(
                    label = "Expense Date (YYYY-MM-DD) *",
                    value = expenseDate,
                    onValueChange = { expenseDate = it },
                    error = expenseDateErr
                )
                Spacer(modifier = Modifier.height(8.dp))

                AppDateField(
                    label = "Recorded Date (YYYY-MM-DD) *",
                    value = enteredDate,
                    onValueChange = { enteredDate = it },
                    error = enteredDateErr
                )
                Spacer(modifier = Modifier.height(8.dp))

                ExposedDropdownMenuBox(
                    expanded = expandedCategory,
                    onExpandedChange = { expandedCategory = !expandedCategory }
                ) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Category *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = appTextFieldColors(),
                        isError = selectedCategory.isBlank()
                    )
                    DropdownMenu(
                        expanded = expandedCategory,
                        onDismissRequest = { expandedCategory = false }
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category) },
                                onClick = {
                                    selectedCategory = category
                                    expandedCategory = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                ValidatedTextField(
                    value = amount,
                    onValueChange = { input -> amount = input.filter { it.isDigit() }.take(9) },
                    label = "Amount (₹) *",
                    error = amountErr,
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                )
                Spacer(modifier = Modifier.height(8.dp))

                ValidatedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = "Note (optional)",
                    error = null,
                    imeAction = ImeAction.Done,
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            submitForm()
                        }
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))

                ImagePickerField(
                    label = "Invoice/Receipt Image (optional)",
                    imagePath = receiptImagePath,
                    isRequired = false,
                    onImageCaptured = { receiptImagePath = it }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = submitForm,
                enabled = isFormValid
            ) {
                Text(
                    "Save",
                    color = if (isFormValid) AppNavy else AppMuted,
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
