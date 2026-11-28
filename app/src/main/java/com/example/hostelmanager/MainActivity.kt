package com.example.hostelmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.hostelmanager.ui.about.AboutScreen
import com.example.hostelmanager.ui.dashboard.HostelDashboardScreen
import com.example.hostelmanager.ui.expenses.AddExpenseScreen
import com.example.hostelmanager.ui.hostels.HostelListScreen
import com.example.hostelmanager.ui.payments.PaymentHistoryScreen
import com.example.hostelmanager.ui.payments.RecordPaymentScreen
import com.example.hostelmanager.ui.reports.ReportsScreen
import com.example.hostelmanager.ui.residents.AddResidentScreen
import com.example.hostelmanager.ui.residents.CheckoutScreen
import com.example.hostelmanager.ui.residents.DailyGuestsScreen
import com.example.hostelmanager.ui.residents.EditResidentScreen
import com.example.hostelmanager.ui.residents.NoticeScreen
import com.example.hostelmanager.ui.residents.PastResidentsScreen
import com.example.hostelmanager.ui.residents.ReAdmitResidentScreen
import com.example.hostelmanager.ui.residents.ResidentDetailsScreen
import com.example.hostelmanager.ui.residents.TransferRoomScreen
import com.example.hostelmanager.ui.rooms.ArchivedRoomsScreen
import com.example.hostelmanager.ui.rooms.RoomDetailsScreen
import com.example.hostelmanager.ui.theme.HostelManagerTheme
import com.example.hostelmanager.util.PreferencesManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val appContainer = (application as HostelManagerApplication).container
        val lastHostelId = PreferencesManager.getLastHostelId(this)
        val initialDestination = if (!lastHostelId.isNullOrBlank()) "hostel_dashboard" else "hostel_list"

        setContent {
            HostelManagerTheme(container = appContainer) {
                val navController = rememberNavController()
                var currentHostelId by rememberSaveable { mutableStateOf(lastHostelId.orEmpty()) }

                NavHost(navController = navController, startDestination = initialDestination) {

                    composable("about") {
                        AboutScreen(onBackClick = { navController.popBackStack() })
                    }

                    composable("hostel_list") {
                        HostelListScreen(onHostelClick = { hostelId ->
                            currentHostelId = hostelId
                            PreferencesManager.saveLastHostelId(this@MainActivity, hostelId)
                            navController.navigate("hostel_dashboard") {
                                popUpTo("hostel_list") { inclusive = true }
                                launchSingleTop = true
                            }
                        })
                    }

                    composable("hostel_dashboard") {
                        HostelDashboardScreen(
                            hostelId = currentHostelId,
                            onBackClick = {
                                PreferencesManager.clearLastHostelId(this@MainActivity)
                                navController.popBackStack()
                            },
                            onAddResidentClick = { navController.navigate("add_resident/$currentHostelId/none") },
                            onRoomClick = { roomId -> navController.navigate("room_details/$currentHostelId/$roomId") },
                            onResidentClick = { personId -> navController.navigate("resident_details/$personId") },
                            onArchivedRoomsClick = { navController.navigate("archived_rooms/$currentHostelId") },
                            onPastResidentsClick = { navController.navigate("past_residents/$currentHostelId") },
                            onRecordPaymentClick = { personId -> navController.navigate("record_payment/$currentHostelId/$personId") },
                            onPaymentHistoryClick = { navController.navigate("payment_history/$currentHostelId") },
                            onAddExpenseClick = { navController.navigate("add_expense/$currentHostelId") },
                            onReportsClick = { navController.navigate("reports/$currentHostelId") },
                            onDailyGuestsClick = { navController.navigate("daily_guests/$currentHostelId") },
                            onNoticeClick = { navController.navigate("notice/$currentHostelId") },
                            onAboutClick = { navController.navigate("about") },
                            onHostelSwitch = { newHostelId ->
                                if (newHostelId != currentHostelId) {
                                    currentHostelId = newHostelId
                                    PreferencesManager.saveLastHostelId(this@MainActivity, newHostelId)
                                }
                            },
                            onAllHostelsClick = {
                                PreferencesManager.clearLastHostelId(this@MainActivity)
                                navController.navigate("hostel_list") {
                                    popUpTo("hostel_dashboard") { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        )
                    }

                    composable(
                        route = "daily_guests/{hostelId}",
                        arguments = listOf(navArgument("hostelId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val hostelId = backStackEntry.arguments?.getString("hostelId") ?: ""
                        DailyGuestsScreen(
                            hostelId = hostelId,
                            onBackClick = { navController.popBackStack() },
                            onAddDailyGuestClick = { navController.navigate("add_resident/$hostelId/none") },
                            onGuestClick = { personId -> navController.navigate("resident_details/$personId") },
                            onRecordPaymentClick = { personId, amountPaise ->
                                navController.navigate("record_payment/$hostelId/$personId?amount=$amountPaise")
                            },
                            onCheckoutClick = { personId -> navController.navigate("checkout/$personId") }
                        )
                    }

                    composable(
                        route = "notice/{hostelId}",
                        arguments = listOf(navArgument("hostelId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val hostelId = backStackEntry.arguments?.getString("hostelId") ?: ""
                        NoticeScreen(
                            hostelId = hostelId,
                            onBackClick = { navController.popBackStack() },
                            onResidentClick = { personId -> navController.navigate("resident_details/$personId") },
                            onCheckoutClick = { personId -> navController.navigate("checkout/$personId") }
                        )
                    }

                    composable(
                        route = "reports/{hostelId}",
                        arguments = listOf(navArgument("hostelId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        ReportsScreen(
                            hostelId = backStackEntry.arguments?.getString("hostelId") ?: "",
                            onBackClick = { navController.popBackStack() }
                        )
                    }

                    composable(
                        route = "add_resident/{hostelId}/{roomId}",
                        arguments = listOf(
                            navArgument("hostelId") { type = NavType.StringType },
                            navArgument("roomId") { type = NavType.StringType; defaultValue = "none" }
                        )
                    ) { backStackEntry ->
                        AddResidentScreen(
                            hostelId = backStackEntry.arguments?.getString("hostelId") ?: "",
                            prefilledRoomId = backStackEntry.arguments?.getString("roomId") ?: "none",
                            onBackClick = { navController.popBackStack() }
                        )
                    }

                    composable(
                        route = "room_details/{hostelId}/{roomId}",
                        arguments = listOf(
                            navArgument("hostelId") { type = NavType.StringType },
                            navArgument("roomId") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        val hostelId = backStackEntry.arguments?.getString("hostelId") ?: ""
                        val roomId = backStackEntry.arguments?.getString("roomId") ?: ""
                        RoomDetailsScreen(
                            roomId = roomId,
                            hostelId = hostelId,
                            onBackClick = { navController.popBackStack() },
                            onResidentClick = { personId -> navController.navigate("resident_details/$personId") },
                            onAddResidentClick = { navController.navigate("add_resident/$hostelId/$roomId") }
                        )
                    }

                    composable(
                        route = "resident_details/{personId}",
                        arguments = listOf(navArgument("personId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val personId = backStackEntry.arguments?.getString("personId") ?: ""
                        ResidentDetailsScreen(
                            personId = personId,
                            onBackClick = { navController.popBackStack() },
                            onEditClick = { navController.navigate("edit_resident/$personId") },
                            onTransferClick = { navController.navigate("transfer_room/$personId") },
                            onCheckoutClick = { navController.navigate("checkout/$personId") }
                        )
                    }

                    composable(
                        route = "edit_resident/{personId}",
                        arguments = listOf(navArgument("personId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        EditResidentScreen(
                            personId = backStackEntry.arguments?.getString("personId") ?: "",
                            onBackClick = { navController.popBackStack() }
                        )
                    }

                    composable(
                        route = "transfer_room/{personId}",
                        arguments = listOf(navArgument("personId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        TransferRoomScreen(
                            personId = backStackEntry.arguments?.getString("personId") ?: "",
                            onBackClick = { navController.popBackStack() }
                        )
                    }

                    composable(
                        route = "checkout/{personId}",
                        arguments = listOf(navArgument("personId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        CheckoutScreen(
                            personId = backStackEntry.arguments?.getString("personId") ?: "",
                            onBackClick = { navController.popBackStack() },
                            onCheckoutComplete = {
                                navController.popBackStack()
                                navController.popBackStack()
                            }
                        )
                    }

                    composable(
                        route = "archived_rooms/{hostelId}",
                        arguments = listOf(navArgument("hostelId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        ArchivedRoomsScreen(
                            hostelId = backStackEntry.arguments?.getString("hostelId") ?: "",
                            onBackClick = { navController.popBackStack() }
                        )
                    }

                    composable(
                        route = "past_residents/{hostelId}",
                        arguments = listOf(navArgument("hostelId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val hostelId = backStackEntry.arguments?.getString("hostelId") ?: ""
                        PastResidentsScreen(
                            hostelId = hostelId,
                            onBackClick = { navController.popBackStack() },
                            onReAdmitClick = { personId -> navController.navigate("readmit_resident/$hostelId/$personId") },
                            onPastResidentClick = { personId -> navController.navigate("resident_details/$personId") }
                        )
                    }

                    composable(
                        route = "readmit_resident/{hostelId}/{personId}",
                        arguments = listOf(
                            navArgument("hostelId") { type = NavType.StringType },
                            navArgument("personId") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        ReAdmitResidentScreen(
                            personId = backStackEntry.arguments?.getString("personId") ?: "",
                            hostelId = backStackEntry.arguments?.getString("hostelId") ?: "",
                            onBackClick = {
                                navController.popBackStack()
                                navController.popBackStack()
                            }
                        )
                    }

                    composable(
                        route = "record_payment/{hostelId}/{personId}?amount={amount}",
                        arguments = listOf(
                            navArgument("hostelId") { type = NavType.StringType },
                            navArgument("personId") { type = NavType.StringType; defaultValue = "" },
                            navArgument("amount") { type = NavType.LongType; defaultValue = 0L }
                        )
                    ) { backStackEntry ->
                        RecordPaymentScreen(
                            hostelId = backStackEntry.arguments?.getString("hostelId") ?: "",
                            prefilledPersonId = backStackEntry.arguments?.getString("personId") ?: "",
                            prefilledAmountPaise = backStackEntry.arguments?.getLong("amount") ?: 0L,
                            onBackClick = { navController.popBackStack() }
                        )
                    }

                    composable(
                        route = "payment_history/{hostelId}",
                        arguments = listOf(navArgument("hostelId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        PaymentHistoryScreen(
                            hostelId = backStackEntry.arguments?.getString("hostelId") ?: "",
                            onBackClick = { navController.popBackStack() }
                        )
                    }

                    composable(
                        route = "add_expense/{hostelId}",
                        arguments = listOf(navArgument("hostelId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        AddExpenseScreen(
                            hostelId = backStackEntry.arguments?.getString("hostelId") ?: "",
                            onBackClick = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}