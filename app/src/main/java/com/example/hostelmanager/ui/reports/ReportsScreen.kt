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
import androidx.compose.material.icons.filled.MoneyOff
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
import com.example.hostelmanager.util.formatDisplayDate
import com.example.hostelmanager.util.paiseToText
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    hostelId: String,
    onBackClick: () -> Unit,
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

            // Net Profit Card (cash in − cash out)
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
                        Text(
                            if (isProfit) "Profit" else "Loss",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isProfit) AppGreen else AppRed,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Text(
                        report.netProfitPaise.paiseToText(),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isProfit) AppGreen else AppRed
                    )
                }
            }

            // ✅ NEW: Write-offs card — money you deliberately gave up
            WriteOffSummaryCard(
                writeOffPaise = report.writeOffPaise,
                writeOffCount = report.writeOffCount,
                collectedPaise = report.totalIncomePaise
            )

            // ✅ NEW: who / why, so the owner can audit every waiver
            if (report.writeOffs.isNotEmpty()) {
                Text(
                    "Write-off Details",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppText,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    report.writeOffs.forEach { entry ->
                        WriteOffRow(entry)
                    }
                }
            }

            // Expense Breakdown
            Text(
                "Expense Breakdown",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AppText,
                modifier = Modifier.padding(top = 8.dp)
            )
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

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun WriteOffSummaryCard(
    writeOffPaise: Long,
    writeOffCount: Int,
    collectedPaise: Long
) {
    val hasWriteOffs = writeOffPaise > 0
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (hasWriteOffs) AppSoftOrange else AppCard
        ),
        border = BorderStroke(
            1.dp,
            if (hasWriteOffs) AppOrange.copy(alpha = 0.3f) else AppBorder
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (hasWriteOffs) AppOrange.copy(alpha = 0.18f) else AppSoftBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.MoneyOff,
                            contentDescription = null,
                            tint = if (hasWriteOffs) AppOrange else AppMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            "Written Off",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = AppText
                        )
                        Text(
                            if (hasWriteOffs) "$writeOffCount waiver${if (writeOffCount == 1) "" else "s"}"
                            else "None this month",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppMuted
                        )
                    }
                }
                Text(
                    writeOffPaise.paiseToText(),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (hasWriteOffs) AppOrange else AppMuted
                )
            }

            if (hasWriteOffs) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Rent you gave up. Not counted as income or expense, so Net Profit is unchanged.",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppMuted
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "If everything had been collected: ${(collectedPaise + writeOffPaise).paiseToText()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppOrange,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun WriteOffRow(entry: WriteOffEntry) {
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
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    entry.personName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = AppText
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Room ${entry.roomNumber} · ${formatDisplayDate(entry.date)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppMuted
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    entry.reason,
                    style = MaterialTheme.typography.bodySmall,
                    color = AppOrange,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                entry.waivedPaise.paiseToText(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AppOrange
            )
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