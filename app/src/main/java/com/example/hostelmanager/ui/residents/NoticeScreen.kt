package com.example.hostelmanager.ui.residents

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hostelmanager.data.ResidentWithRoom
import com.example.hostelmanager.di.appViewModel
import com.example.hostelmanager.ui.theme.AppBackground
import com.example.hostelmanager.ui.theme.AppBorder
import com.example.hostelmanager.ui.theme.AppCard
import com.example.hostelmanager.ui.theme.AppGreen
import com.example.hostelmanager.ui.theme.AppMuted
import com.example.hostelmanager.ui.theme.AppNavy
import com.example.hostelmanager.ui.theme.AppOrange
import com.example.hostelmanager.ui.theme.AppRed
import com.example.hostelmanager.ui.theme.AppSoftBlue
import com.example.hostelmanager.ui.theme.AppSoftGreen
import com.example.hostelmanager.ui.theme.AppSoftOrange
import com.example.hostelmanager.ui.theme.AppSoftRed
import com.example.hostelmanager.ui.theme.AppText
import com.example.hostelmanager.util.formatDisplayDate
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoticeScreen(
    hostelId: String,
    onBackClick: () -> Unit,
    onResidentClick: (String) -> Unit,
    onCheckoutClick: (String) -> Unit,
    viewModel: NoticeViewModel = appViewModel()
) {
    SideEffect { viewModel.setHostelId(hostelId) }
    val residents by viewModel.noticeResidents.collectAsStateWithLifecycle()
    var cancelTarget by remember { mutableStateOf<ResidentWithRoom?>(null) }

    Scaffold(
        containerColor = AppBackground,
        topBar = {
            TopAppBar(
                title = { Text("Notice", fontWeight = FontWeight.Bold, color = AppText) },
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
        if (residents.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier.size(80.dp).clip(CircleShape).background(AppSoftBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Notifications, contentDescription = null, tint = AppNavy, modifier = Modifier.size(40.dp))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No one has given notice.",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = AppText
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "When a resident tells you their planned check-out date, record it from their details screen.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppMuted
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(residents, key = { it.person.id }) { resident ->
                    NoticeCard(
                        resident = resident,
                        onClick = { onResidentClick(resident.person.id) },
                        onCheckout = { onCheckoutClick(resident.person.id) },
                        onCancelNotice = { cancelTarget = resident }
                    )
                }
            }
        }
    }

    cancelTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { cancelTarget = null },
            shape = RoundedCornerShape(24.dp),
            containerColor = AppCard,
            titleContentColor = AppText,
            textContentColor = AppMuted,
            title = { Text("Cancel this notice?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "${target.person.name} will disappear from the Notice list. " +
                            "Their stay stays active — nothing else changes."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.cancelNotice(target.person.id)
                    cancelTarget = null
                }) { Text("Yes, cancel notice", color = AppRed, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { cancelTarget = null }) { Text("Keep notice", color = AppMuted) }
            }
        )
    }
}

@Composable
private fun NoticeCard(
    resident: ResidentWithRoom,
    onClick: () -> Unit,
    onCheckout: () -> Unit,
    onCancelNotice: () -> Unit
) {
    val noticeDate = runCatching {
        LocalDate.parse(resident.noticeDate ?: "", DateTimeFormatter.ISO_LOCAL_DATE)
    }.getOrNull()
    val daysLeft = noticeDate?.let { ChronoUnit.DAYS.between(LocalDate.now(), it) } ?: 0L

    val (statusLabel, statusColor, statusSoft) = when {
        noticeDate == null -> Triple("—", AppMuted, AppSoftBlue)
        daysLeft > 1 -> Triple("in $daysLeft days", AppGreen, AppSoftGreen)
        daysLeft == 1L -> Triple("in 1 day", AppGreen, AppSoftGreen)
        daysLeft == 0L -> Triple("leaving today", AppOrange, AppSoftOrange)
        daysLeft == -1L -> Triple("1 day overdue", AppRed, AppSoftRed)
        else -> Triple("${-daysLeft} days overdue", AppRed, AppSoftRed)
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AppCard),
        border = BorderStroke(1.dp, AppBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ResidentAvatar(photoPath = resident.person.profilePhotoPath)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        resident.person.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AppText
                    )
                    Text(
                        "Room ${resident.roomNumber} · ${resident.person.phone}",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier.clip(RoundedCornerShape(50)).background(AppSoftBlue).padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        "Leaves ${noticeDate?.let { formatDisplayDate(it.toString()) } ?: "—"}",
                        fontSize = 11.sp, color = AppNavy, fontWeight = FontWeight.Bold
                    )
                }
                Box(
                    modifier = Modifier.clip(RoundedCornerShape(50)).background(statusSoft).padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(statusLabel, fontSize = 11.sp, color = statusColor, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onCancelNotice,
                    modifier = Modifier.weight(1f).height(40.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel Notice", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AppMuted)
                }
                Button(
                    onClick = onCheckout,
                    modifier = Modifier.weight(1f).height(40.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppRed, contentColor = Color.White)
                ) {
                    Text("Check Out", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}