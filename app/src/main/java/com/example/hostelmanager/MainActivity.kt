package com.example.hostelmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.hostelmanager.di.LocalAppContainer
import com.example.hostelmanager.ui.dashboard.HostelDashboardScreen
import com.example.hostelmanager.ui.expenses.AddExpenseScreen
import com.example.hostelmanager.ui.hostels.HostelListScreen
import com.example.hostelmanager.ui.payments.PaymentHistoryScreen
import com.example.hostelmanager.ui.payments.RecordPaymentScreen
import com.example.hostelmanager.ui.reports.ReportsScreen
import com.example.hostelmanager.ui.residents.AddResidentScreen
import com.example.hostelmanager.ui.residents.CheckoutScreen
import com.example.hostelmanager.ui.residents.EditResidentScreen
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
        val initialDestination = if (!lastHostelId.isNullOrBlank()) {
            "hostel_dashboard"
        } else {
            "hostel_list"
        }

        setContent {
            CompositionLocalProvider(LocalAppContainer provides appContainer) {
                HostelManagerTheme (
                    container = appContainer
                )
                {
                    val navController = rememberNavController()

                    // Level C: the hostel ID lives HERE as state, not in the route.
                    // Switching hostels just updates this value — no navigation, no rebuild.
                    var currentHostelId by rememberSaveable {
                        mutableStateOf(lastHostelId.orEmpty())
                    }

                    NavHost(
                        navController = navController,
                        startDestination = initialDestination
                    ) {
                        composable("hostel_list") {
                            HostelListScreen(
                                onHostelClick = { hostelId ->
                                    currentHostelId = hostelId
                                    PreferencesManager.saveLastHostelId(
                                        this@MainActivity,
                                        hostelId
                                    )
                                    navController.navigate("hostel_dashboard") {
                                        popUpTo("hostel_list") { inclusive = true }
                                        launchSingleTop = true
                                    }
                                }
                            )
                        }

                        // Route is just "hostel_dashboard" — no {hostelId} argument
                        composable("hostel_dashboard") {
                            HostelDashboardScreen(
                                hostelId = currentHostelId,
                                onBackClick = {
                                    PreferencesManager.clearLastHostelId(this@MainActivity)
                                    navController.popBackStack()
                                },
                                onAddResidentClick = {
                                    navController.navigate("add_resident/$currentHostelId/none")
                                },
                                onRoomClick = { roomId ->
                                    navController.navigate("room_details/$currentHostelId/$roomId")
                                },
                                onResidentClick = { personId ->
                                    navController.navigate("resident_details/$personId")
                                },
                                onArchivedRoomsClick = {
                                    navController.navigate("archived_rooms/$currentHostelId")
                                },
                                onPastResidentsClick = {
                                    navController.navigate("past_residents/$currentHostelId")
                                },
                                onRecordPaymentClick = { personId ->
                                    navController.navigate("record_payment/$currentHostelId/$personId")
                                },
                                onPaymentHistoryClick = {
                                    navController.navigate("payment_history/$currentHostelId")
                                },
                                onAddExpenseClick = {
                                    navController.navigate("add_expense/$currentHostelId")
                                },
                                onReportsClick = {
                                    navController.navigate("reports/$currentHostelId")
                                },
                                onHostelSwitch = { newHostelId ->
                                    // Level C:
                                    // Do NOT navigate here.
                                    // Same screen, same ViewModels, just new hostel data.
                                    if (newHostelId != currentHostelId) {
                                        currentHostelId = newHostelId
                                        PreferencesManager.saveLastHostelId(
                                            this@MainActivity,
                                            newHostelId
                                        )
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
                            route = "reports/{hostelId}",
                            arguments = listOf(
                                navArgument("hostelId") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val hostelId = backStackEntry.arguments?.getString("hostelId") ?: ""
                            ReportsScreen(
                                hostelId = hostelId,
                                onBackClick = { navController.popBackStack() }
                            )
                        }

                        composable(
                            route = "add_resident/{hostelId}/{roomId}",
                            arguments = listOf(
                                navArgument("hostelId") { type = NavType.StringType },
                                navArgument("roomId") {
                                    type = NavType.StringType
                                    defaultValue = "none"
                                }
                            )
                        ) { backStackEntry ->
                            val hostelId = backStackEntry.arguments?.getString("hostelId") ?: ""
                            val roomId = backStackEntry.arguments?.getString("roomId") ?: "none"
                            AddResidentScreen(
                                hostelId = hostelId,
                                prefilledRoomId = roomId,
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
                                onResidentClick = { personId ->
                                    navController.navigate("resident_details/$personId")
                                },
                                onAddResidentClick = {
                                    navController.navigate("add_resident/$hostelId/$roomId")
                                }
                            )
                        }

                        composable(
                            route = "resident_details/{personId}",
                            arguments = listOf(
                                navArgument("personId") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val personId = backStackEntry.arguments?.getString("personId") ?: ""
                            ResidentDetailsScreen(
                                personId = personId,
                                onBackClick = { navController.popBackStack() },
                                onEditClick = {
                                    navController.navigate("edit_resident/$personId")
                                },
                                onTransferClick = {
                                    navController.navigate("transfer_room/$personId")
                                },
                                onCheckoutClick = {
                                    navController.navigate("checkout/$personId")
                                }
                            )
                        }

                        composable(
                            route = "edit_resident/{personId}",
                            arguments = listOf(
                                navArgument("personId") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val personId = backStackEntry.arguments?.getString("personId") ?: ""
                            EditResidentScreen(
                                personId = personId,
                                onBackClick = { navController.popBackStack() }
                            )
                        }

                        composable(
                            route = "transfer_room/{personId}",
                            arguments = listOf(
                                navArgument("personId") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val personId = backStackEntry.arguments?.getString("personId") ?: ""
                            TransferRoomScreen(
                                personId = personId,
                                onBackClick = { navController.popBackStack() }
                            )
                        }

                        composable(
                            route = "checkout/{personId}",
                            arguments = listOf(
                                navArgument("personId") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val personId = backStackEntry.arguments?.getString("personId") ?: ""
                            CheckoutScreen(
                                personId = personId,
                                onBackClick = { navController.popBackStack() },
                                onCheckoutComplete = {
                                    navController.popBackStack()
                                    navController.popBackStack()
                                }
                            )
                        }

                        composable(
                            route = "archived_rooms/{hostelId}",
                            arguments = listOf(
                                navArgument("hostelId") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val hostelId = backStackEntry.arguments?.getString("hostelId") ?: ""
                            ArchivedRoomsScreen(
                                hostelId = hostelId,
                                onBackClick = { navController.popBackStack() }
                            )
                        }

                        composable(
                            route = "past_residents/{hostelId}",
                            arguments = listOf(
                                navArgument("hostelId") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val hostelId = backStackEntry.arguments?.getString("hostelId") ?: ""
                            PastResidentsScreen(
                                hostelId = hostelId,
                                onBackClick = { navController.popBackStack() },
                                onReAdmitClick = { personId ->
                                    navController.navigate("readmit_resident/$hostelId/$personId")
                                },
                                onPastResidentClick = { personId ->
                                    navController.navigate("resident_details/$personId")
                                }
                            )
                        }

                        composable(
                            route = "readmit_resident/{hostelId}/{personId}",
                            arguments = listOf(
                                navArgument("hostelId") { type = NavType.StringType },
                                navArgument("personId") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val hostelId = backStackEntry.arguments?.getString("hostelId") ?: ""
                            val personId = backStackEntry.arguments?.getString("personId") ?: ""
                            ReAdmitResidentScreen(
                                personId = personId,
                                hostelId = hostelId,
                                onBackClick = {
                                    navController.popBackStack()
                                    navController.popBackStack()
                                }
                            )
                        }

                        composable(
                            route = "record_payment/{hostelId}/{personId}",
                            arguments = listOf(
                                navArgument("hostelId") { type = NavType.StringType },
                                navArgument("personId") {
                                    type = NavType.StringType
                                    defaultValue = ""
                                }
                            )
                        ) { backStackEntry ->
                            val hostelId = backStackEntry.arguments?.getString("hostelId") ?: ""
                            val personId = backStackEntry.arguments?.getString("personId") ?: ""
                            RecordPaymentScreen(
                                hostelId = hostelId,
                                prefilledPersonId = personId,
                                onBackClick = { navController.popBackStack() }
                            )
                        }

                        composable(
                            route = "payment_history/{hostelId}",
                            arguments = listOf(
                                navArgument("hostelId") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val hostelId = backStackEntry.arguments?.getString("hostelId") ?: ""
                            PaymentHistoryScreen(
                                hostelId = hostelId,
                                onBackClick = { navController.popBackStack() }
                            )
                        }

                        composable(
                            route = "add_expense/{hostelId}",
                            arguments = listOf(
                                navArgument("hostelId") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val hostelId = backStackEntry.arguments?.getString("hostelId") ?: ""
                            AddExpenseScreen(
                                hostelId = hostelId,
                                onBackClick = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}