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
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
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
import com.example.hostelmanager.ui.theme.AppOnNavy
import com.example.hostelmanager.ui.theme.AppOrange
import com.example.hostelmanager.ui.theme.AppSoftBlue
import com.example.hostelmanager.ui.theme.AppSoftOrange
import com.example.hostelmanager.ui.theme.AppText
import com.example.hostelmanager.util.paiseToText
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyGuestsScreen(
    hostelId: String,
    onBackClick: () -> Unit,
    onAddDailyGuestClick: () -> Unit,
    onGuestClick: (String) -> Unit,
    onRecordPaymentClick: (String, Long) -> Unit,
    onCheckoutClick: (String) -> Unit,
    viewModel: DailyGuestsViewModel = appViewModel()
) {
    SideEffect { viewModel.setHostelId(hostelId) }
    val guests by viewModel.dailyGuests.collectAsStateWithLifecycle()

    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }
    var currentGuest by remember { mutableStateOf<ResidentWithRoom?>(null) }

    Scaffold(
        containerColor = AppBackground,
        topBar = {
            TopAppBar(
                title = { Text("Daily Guests", fontWeight = FontWeight.Bold, color = AppText) },
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddDailyGuestClick,
                shape = RoundedCornerShape(18.dp),
                containerColor = AppNavy,
                contentColor = AppOnNavy,
                elevation = FloatingActionButtonDefaults.elevation(6.dp)
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = "Add Daily Guest")
            }
        }
    ) { padding ->
        if (guests.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier.size(80.dp).clip(CircleShape).background(AppSoftBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Hotel, contentDescription = null, tint = AppNavy, modifier = Modifier.size(40.dp))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No daily guests right now.", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = AppText)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Tap the + button to add a short-term stay.", style = MaterialTheme.typography.bodyMedium, color = AppMuted)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(guests, key = { it.person.id }) { guest ->
                    DailyGuestCard(
                        guest = guest,
                        onClick = {
                            // ✅ Tell ViewModel to start tracking this guest's balance
                            viewModel.selectGuest(guest.stayId, guest.dailyRent, guest.joinDate)
                            currentGuest = guest
                            showBottomSheet = true
                        }
                    )
                }
            }
        }
    }

    if (showBottomSheet && currentGuest != null) {
        val guest = currentGuest!!

        // ✅ Read balance directly from the reactive flow (no glitching)
        val balance by viewModel.balance.collectAsStateWithLifecycle()

        val today = LocalDate.now()
        val daysStayed = try {
            ChronoUnit.DAYS.between(
                LocalDate.parse(guest.joinDate, DateTimeFormatter.ISO_LOCAL_DATE),
                today
            ) + 1
        } catch (e: Exception) { 1L }.coerceAtLeast(1L)

        val totalDuePaise = daysStayed * guest.dailyRent
        val isSettled = balance <= 0

        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(modifier = Modifier.width(40.dp).height(4.dp).clip(RoundedCornerShape(2.dp)).background(AppBorder))
                Spacer(modifier = Modifier.height(24.dp))

                Text(guest.person.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = AppText)
                Text("Room ${guest.roomNumber}", style = MaterialTheme.typography.bodyMedium, color = AppMuted)
                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AppSoftBlue)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Daily Rate", color = AppNavy, fontWeight = FontWeight.Medium)
                            Text("${guest.dailyRent.paiseToText()}/day", color = AppNavy, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Days Stayed", color = AppNavy, fontWeight = FontWeight.Medium)
                            Text("$daysStayed days", color = AppNavy, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Due", color = AppNavy, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(totalDuePaise.paiseToText(), color = AppNavy, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        // ✅ Live balance row
                        val (balLabel, balColor) = when {
                            balance > 0 -> "Balance Due" to AppOrange
                            balance < 0 -> "Credit" to AppGreen
                            else -> "Fully Settled" to AppGreen
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(balLabel, color = balColor, fontWeight = FontWeight.Bold)
                            Text(
                                if (balance == 0L) "₹0" else "${if (balance < 0) "-" else ""}${kotlin.math.abs(balance).paiseToText()}",
                                color = balColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedButton(
                    onClick = {
                        showBottomSheet = false
                        onGuestClick(guest.person.id)
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("View Full Details", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        showBottomSheet = false
                        onRecordPaymentClick(guest.person.id, balance.coerceAtLeast(0L))
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppNavy, contentColor = AppOnNavy)
                ) {
                    Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (balance > 0) "Record Payment (${balance.paiseToText()})"
                        else "Record Extra Payment",
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = {
                        showBottomSheet = false
                        onCheckoutClick(guest.person.id)
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    enabled = isSettled,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFE11D48),
                        disabledContentColor = AppMuted
                    )
                ) {
                    Text(
                        if (isSettled) "Check Out & Settle" else "Settle Balance First",
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun DailyGuestCard(guest: ResidentWithRoom, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AppCard),
        border = BorderStroke(1.dp, AppBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ResidentAvatar(photoPath = guest.person.profilePhotoPath)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(guest.person.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = AppText)
                Spacer(modifier = Modifier.height(4.dp))
                Text("📞 ${guest.person.phone}", style = MaterialTheme.typography.bodySmall, color = AppMuted)
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    Box(modifier = Modifier.clip(RoundedCornerShape(50)).background(AppSoftBlue).padding(horizontal = 10.dp, vertical = 4.dp)) {
                        Text("Room ${guest.roomNumber}", fontSize = 11.sp, color = AppNavy, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(modifier = Modifier.clip(RoundedCornerShape(50)).background(AppSoftOrange).padding(horizontal = 10.dp, vertical = 4.dp)) {
                        Text("${guest.dailyRent.paiseToText()}/day", fontSize = 11.sp, color = AppOrange, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}