package com.example.hostelmanager.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hostelmanager.R
import com.example.hostelmanager.data.Hostel
import com.example.hostelmanager.di.appViewModel
import com.example.hostelmanager.ui.expenses.ExpensesListScreen
import com.example.hostelmanager.ui.hostels.HostelViewModel
import com.example.hostelmanager.ui.payments.PaymentsListScreen
import com.example.hostelmanager.ui.residents.ResidentListScreen
import com.example.hostelmanager.ui.rooms.RoomListScreen
import com.example.hostelmanager.ui.theme.*
import kotlinx.coroutines.launch

private val GradientBlue = Color(0xFF1E40AF)
private val GradientLightGreen = Color(0xFF34D399)
private val HeaderFooterGradient = Brush.horizontalGradient(colors = listOf(GradientBlue, GradientLightGreen))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HostelDashboardScreen(
    hostelId: String,
    onBackClick: () -> Unit,
    onAddResidentClick: () -> Unit,
    onRoomClick: (String) -> Unit,
    onResidentClick: (String) -> Unit,
    onArchivedRoomsClick: () -> Unit,
    onPastResidentsClick: () -> Unit,
    onRecordPaymentClick: (String) -> Unit,
    onPaymentHistoryClick: () -> Unit,
    onAddExpenseClick: () -> Unit,
    onReportsClick: () -> Unit,
    onDailyGuestsClick: () -> Unit,
    onAboutClick: () -> Unit, // ✅ NEW
    onHostelSwitch: (String) -> Unit,
    onAllHostelsClick: () -> Unit,
    hostelViewModel: HostelViewModel = appViewModel()
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    val tabs = listOf("Dashboard", "Rooms", "Residents", "Payments", "Expenses")
    val icons = listOf(
        Icons.Default.Home,
        Icons.Default.Apartment,
        Icons.Default.People,
        Icons.Default.Payments,
        Icons.Default.Receipt
    )

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val hostels by hostelViewModel.hostels.collectAsStateWithLifecycle()
    val currentHostel = hostels.find { it.id == hostelId }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(modifier = Modifier.width(280.dp)) {
                DrawerContent(
                    currentHostel = currentHostel,
                    hostels = hostels,
                    onHostelClick = { newHostelId ->
                        scope.launch { drawerState.close() }
                        onHostelSwitch(newHostelId)
                    },
                    onArchivedRoomsClick = {
                        scope.launch { drawerState.close() }
                        onArchivedRoomsClick()
                    },
                    onPastResidentsClick = {
                        scope.launch { drawerState.close() }
                        onPastResidentsClick()
                    },
                    onReportsClick = {
                        scope.launch { drawerState.close() }
                        onReportsClick()
                    },
                    onDailyGuestsClick = {
                        scope.launch { drawerState.close() }
                        onDailyGuestsClick()
                    },
                    onAboutClick = { // ✅ NEW
                        scope.launch { drawerState.close() }
                        onAboutClick()
                    },
                    onAllHostelsClick = {
                        scope.launch { drawerState.close() }
                        onAllHostelsClick()
                    }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                Box(modifier = Modifier.background(HeaderFooterGradient)) {
                    TopAppBar(
                        title = {
                            Text(
                                text = tabs[selectedTab],
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent,
                            titleContentColor = Color.White,
                            navigationIconContentColor = Color.White
                        )
                    )
                }
            },
            bottomBar = {
                Box(modifier = Modifier.background(HeaderFooterGradient)) {
                    NavigationBar(
                        containerColor = Color.Transparent,
                        contentColor = Color.White,
                        tonalElevation = 0.dp
                    ) {
                        tabs.forEachIndexed { index, title ->
                            NavigationBarItem(
                                icon = { Icon(icons[index], contentDescription = title, modifier = Modifier.size(24.dp)) },
                                label = {
                                    Text(
                                        title,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.White,
                                    unselectedIconColor = Color.White.copy(alpha = 0.6f),
                                    selectedTextColor = Color.White,
                                    unselectedTextColor = Color.White.copy(alpha = 0.6f),
                                    indicatorColor = Color.White.copy(alpha = 0.2f)
                                )
                            )
                        }
                    }
                }
            }
        ) { padding ->
            val modifierWithPadding = Modifier.padding(padding)

            when (selectedTab) {
                0 -> DashboardTabContent(hostelId = hostelId, modifier = modifierWithPadding)
                1 -> RoomListScreen(hostelId = hostelId, onRoomClick = onRoomClick, modifier = modifierWithPadding)
                2 -> ResidentListScreen(hostelId = hostelId, onAddClick = onAddResidentClick, onResidentClick = onResidentClick, modifier = modifierWithPadding)
                3 -> PaymentsListScreen(hostelId = hostelId, onRecordPaymentClick = onRecordPaymentClick, onPaymentHistoryClick = onPaymentHistoryClick, modifier = modifierWithPadding)
                4 -> ExpensesListScreen(hostelId = hostelId, onAddExpenseClick = onAddExpenseClick, modifier = modifierWithPadding)
            }
        }
    }
}

@Composable
private fun DashboardTabContent(
    hostelId: String,
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = appViewModel()
) {
    SideEffect { viewModel.setHostelId(hostelId) }

    val stats by viewModel.stats.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        if (stats == null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = AppCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, AppBorder)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Loading dashboard...", style = MaterialTheme.typography.bodyLarge, color = AppMuted)
                }
            }
        } else {
            DashboardContent(
                stats = stats!!,
                onManageHostelClick = {},
                footerAlpha = 0f,
                onFooterHeightChanged = {}
            )
        }
    }
}

@Composable
private fun DrawerContent(
    currentHostel: Hostel?,
    hostels: List<Hostel>,
    onHostelClick: (String) -> Unit,
    onArchivedRoomsClick: () -> Unit,
    onPastResidentsClick: () -> Unit,
    onReportsClick: () -> Unit,
    onDailyGuestsClick: () -> Unit,
    onAboutClick: () -> Unit, // ✅ NEW
    onAllHostelsClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().background(AppBackground).verticalScroll(rememberScrollState())
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().background(HeaderFooterGradient).padding(20.dp)
        ) {
            Column {
                Text(
                    text = currentHostel?.name ?: "Hostel",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                if (currentHostel?.address?.isNotBlank() == true) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = currentHostel.address,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        DrawerMenuItem(icon = Icons.Default.Apartment, title = "All Hostels", onClick = onAllHostelsClick)

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp))

        Text(
            text = "Switch Hostel",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = AppMuted,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        hostels.forEach { hostel ->
            DrawerHostelItem(
                hostel = hostel,
                isSelected = hostel.id == currentHostel?.id,
                onClick = { if (hostel.id != currentHostel?.id) onHostelClick(hostel.id) }
            )
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp))

        DrawerMenuItem(icon = Icons.Default.Hotel, title = "Daily Guests", onClick = onDailyGuestsClick)

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp))

        Text(
            text = "Analytics & Archives",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = AppMuted,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        DrawerMenuItem(icon = Icons.Default.BarChart, title = "Reports", onClick = onReportsClick)
        DrawerMenuItem(icon = Icons.Default.Archive, title = "Archived Rooms", onClick = onArchivedRoomsClick)
        DrawerMenuItem(
            painter = painterResource(R.drawable.ic_person_walking_luggage),
            title = "Checkouts",
            onClick = onPastResidentsClick
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp))

        // ✅ NEW: About
        DrawerMenuItem(icon = Icons.Default.Info, title = "About HostelInn", onClick = onAboutClick)

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun DrawerMenuItem(icon: ImageVector, title: String, onClick: () -> Unit) {
    DrawerMenuItemInternal(
        icon = { Icon(imageVector = icon, contentDescription = null, tint = AppNavy, modifier = Modifier.size(24.dp)) },
        title = title,
        onClick = onClick
    )
}

@Composable
private fun DrawerMenuItem(painter: androidx.compose.ui.graphics.painter.Painter, title: String, onClick: () -> Unit) {
    DrawerMenuItemInternal(
        icon = { Icon(painter = painter, contentDescription = null, tint = AppNavy, modifier = Modifier.size(24.dp)) },
        title = title,
        onClick = onClick
    )
}

@Composable
private fun DrawerMenuItemInternal(icon: @Composable () -> Unit, title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon()
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = title, style = MaterialTheme.typography.bodyLarge, color = AppText, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun DrawerHostelItem(hostel: Hostel, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 10.dp)
            .then(if (isSelected) Modifier.background(AppSoftBlue.copy(alpha = 0.5f)) else Modifier),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(36.dp).clip(CircleShape).background(if (isSelected) AppNavy else AppCard),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Apartment,
                contentDescription = null,
                tint = if (isSelected) Color.White else AppNavy,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = hostel.name,
                style = MaterialTheme.typography.bodyLarge,
                color = AppText,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
            if (hostel.address.isNotBlank()) {
                Text(text = hostel.address, style = MaterialTheme.typography.bodySmall, color = AppMuted, maxLines = 1)
            }
        }
        if (isSelected) {
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(AppNavy))
        }
    }
}