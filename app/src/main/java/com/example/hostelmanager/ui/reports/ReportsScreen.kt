package com.example.hostelmanager.ui.reports

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hostelmanager.di.appViewModel
import com.example.hostelmanager.ui.theme.*
import com.example.hostelmanager.util.paiseToText
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    hostelId: String,
    onBackClick: () -> Unit, // ✅ Added back navigation
    modifier: Modifier = Modifier,
    viewModel: ReportsViewModel = appViewModel()
) {
    LaunchedEffect(hostelId) { viewModel.setHostelId(hostelId) }
    val report by viewModel.report.collectAsStateWithLifecycle()
    val selectedMonth by viewModel.selectedMonth.collectAsStateWithLifecycle()
    val yearMonth = YearMonth.parse(selectedMonth)
    val monthDisplay = "${yearMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${yearMonth.year}"

    Scaffold(
        containerColor = AppBackground,
        topBar = {
            TopAppBar(
                title = { Text("Reports", fontWeight = FontWeight.Bold, color = AppText) },
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
            modifier = modifier
                .fillMaxSize()
                .background(AppBackground)
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Month Picker Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = AppCard),
                border = BorderStroke(1.dp, AppBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = { viewModel.previousMonth() }) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Previous month", tint = AppNavy)
                    }
                    Text(monthDisplay, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = AppText)
                    IconButton(onClick = { viewModel.nextMonth() }) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Next month", tint = AppNavy)
                    }
                }
            }

            // Summary Cards Row
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SummaryCard(
                    title = "Total Income", amount = report.totalIncomePaise, icon = Icons.Default.AttachMoney,
                    backgroundColor = AppSoftGreen, iconColor = AppGreen, amountColor = AppGreen, modifier = Modifier.weight(1f)
                )
                SummaryCard(
                    title = "Total Expenses", amount = report.totalExpensePaise, icon = Icons.Default.ReceiptLong,
                    backgroundColor = AppSoftRed, iconColor = AppRed, amountColor = AppRed, modifier = Modifier.weight(1f)
                )
            }

            // Net Profit Card
            val isProfit = report.netProfitPaise >= 0
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = if (isProfit) AppSoftGreen else AppSoftRed),
                border = BorderStroke(1.dp, if (isProfit) AppGreen.copy(alpha = 0.3f) else AppRed.copy(alpha = 0.3f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Net Profit", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = AppText)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(if (isProfit) "Profit" else "Loss", style = MaterialTheme.typography.bodySmall, color = if (isProfit) AppGreen else AppRed, fontWeight = FontWeight.Medium)
                    }
                    Text(report.netProfitPaise.paiseToText(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = if (isProfit) AppGreen else AppRed)
                }
            }

            // Expense Breakdown
            Text("Expense Breakdown", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = AppText, modifier = Modifier.padding(top = 8.dp))
            if (report.expenseBreakdown.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    report.expenseBreakdown.forEach { item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = AppCard),
                            border = BorderStroke(1.dp, AppBorder),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(item.category, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, color = AppText)
                                Text(item.totalPaise.paiseToText(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = AppRed)
                            }
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AppCard),
                    border = BorderStroke(1.dp, AppBorder)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No expenses recorded for this month.", style = MaterialTheme.typography.bodyMedium, color = AppMuted)
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(
    title: String, amount: Long, icon: ImageVector, backgroundColor: Color, iconColor: Color, amountColor: Color, modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AppCard),
        border = BorderStroke(1.dp, AppBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(backgroundColor), contentAlignment = Alignment.Center) {
                    Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
                }
                Text(title, style = MaterialTheme.typography.bodyMedium, color = AppMuted, fontWeight = FontWeight.Medium)
            }
            Text(amount.paiseToText(), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = amountColor)
        }
    }
}