package com.example.hostelmanager.ui.expenses

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
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
import com.example.hostelmanager.di.appViewModel
import com.example.hostelmanager.ui.theme.AppOnNavy
import com.example.hostelmanager.domain.validation.AmountRules
import com.example.hostelmanager.domain.validation.DateRules
import com.example.hostelmanager.ui.components.AppDateField
import com.example.hostelmanager.ui.components.ImagePickerField
import com.example.hostelmanager.ui.components.ValidatedTextField
import com.example.hostelmanager.ui.components.appTextFieldColors
import com.example.hostelmanager.ui.theme.AppBackground
import com.example.hostelmanager.ui.theme.AppBorder
import com.example.hostelmanager.ui.theme.AppMuted
import com.example.hostelmanager.ui.theme.AppNavy
import com.example.hostelmanager.ui.theme.AppText
import com.example.hostelmanager.util.rupeesToPaise
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    hostelId: String,
    onBackClick: () -> Unit,
    viewModel: AddExpenseViewModel = appViewModel()
) {
    val focusManager = LocalFocusManager.current

    var expenseDate by remember { mutableStateOf("") }
    var enteredDate by remember {
        mutableStateOf(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
    }

    val categories = listOf("Electricity", "Water", "Maintenance", "Repairs", "Supplies", "Salary", "Other")
    var selectedCategory by remember { mutableStateOf("Electricity") }
    var expandedCategory by remember { mutableStateOf(false) }

    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var receiptImagePath by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    val expenseDateErr = if (expenseDate.isNotBlank()) DateRules.validateDate(expenseDate, "Expense date", notFuture = true) else null
    val enteredDateErr = if (enteredDate.isNotBlank()) DateRules.validateDate(enteredDate, "Recorded date", notFuture = true) else null
    val amountErr = AmountRules.validateAmount(amount, "Amount")

    val isFormValid = expenseDate.isNotBlank() && expenseDateErr == null &&
            enteredDate.isNotBlank() && enteredDateErr == null &&
            selectedCategory.isNotBlank() && amountErr == null

    Scaffold(
        containerColor = AppBackground,
        topBar = {
            TopAppBar(
                title = { Text("Add Expense", fontWeight = FontWeight.Bold, color = AppText) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = AppText)
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
                .imePadding()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AppDateField(
                label = "Expense Date (when occurred) *",
                value = expenseDate,
                onValueChange = { expenseDate = it },
                error = expenseDateErr
            )

            AppDateField(
                label = "Recorded Date (when added) *",
                value = enteredDate,
                onValueChange = { enteredDate = it },
                error = enteredDateErr
            )

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
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = appTextFieldColors()
                )
                ExposedDropdownMenu(
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

            ValidatedTextField(
                value = amount,
                onValueChange = { input -> amount = input.filter { it.isDigit() }.take(9) },
                label = "Amount (₹) *",
                error = amountErr,
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next,
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
            )

            ValidatedTextField(
                value = note,
                onValueChange = { note = it },
                label = "Note (optional)",
                error = null,
                imeAction = ImeAction.Done,
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
            )

            Spacer(modifier = Modifier.height(8.dp))

            ImagePickerField(
                label = "Invoice/Receipt Image (optional)",
                imagePath = receiptImagePath,
                isRequired = false,
                onImageCaptured = { receiptImagePath = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    scope.launch {
                        val success = viewModel.addExpense(
                            hostelId,
                            expenseDate,
                            enteredDate,
                            selectedCategory,
                            amount.rupeesToPaise(),
                            note,
                            receiptImagePath
                        )
                        if (success) onBackClick()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppNavy,
                    contentColor = AppOnNavy,
                    disabledContainerColor = AppBorder,
                    disabledContentColor = AppMuted
                ),
                enabled = isFormValid
            ) {
                Text(
                    "Save Expense",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}
