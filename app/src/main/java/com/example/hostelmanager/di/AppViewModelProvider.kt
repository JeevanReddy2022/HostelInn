package com.example.hostelmanager.di

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.hostelmanager.ui.dashboard.DashboardViewModel
import com.example.hostelmanager.ui.expenses.AddExpenseViewModel
import com.example.hostelmanager.ui.expenses.ExpensesViewModel
import com.example.hostelmanager.ui.hostels.HostelViewModel
import com.example.hostelmanager.ui.payments.PaymentHistoryViewModel
import com.example.hostelmanager.ui.payments.PaymentsViewModel
import com.example.hostelmanager.ui.payments.RecordPaymentViewModel
import com.example.hostelmanager.ui.reports.ReportsViewModel
import com.example.hostelmanager.ui.residents.CheckoutViewModel
import com.example.hostelmanager.ui.residents.DailyGuestsViewModel
import com.example.hostelmanager.ui.residents.EditResidentViewModel
import com.example.hostelmanager.ui.residents.PastResidentsViewModel
import com.example.hostelmanager.ui.residents.ReAdmitResidentViewModel
import com.example.hostelmanager.ui.residents.ResidentDetailsViewModel
import com.example.hostelmanager.ui.residents.ResidentListViewModel
import com.example.hostelmanager.ui.residents.ResidentViewModel
import com.example.hostelmanager.ui.residents.TransferRoomViewModel
import com.example.hostelmanager.ui.rooms.ArchivedRoomsViewModel
import com.example.hostelmanager.ui.rooms.RoomDetailsViewModel
import com.example.hostelmanager.ui.rooms.RoomViewModel

object AppViewModelProvider {
    fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
        initializer { DashboardViewModel(container.hostelRepository, container.residentRepository, container.roomRepository, container.paymentRepository, container.expenseRepository) }
        initializer { AddExpenseViewModel(container.expenseRepository) }
        initializer {
            val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application
            ExpensesViewModel(app, container.expenseRepository)
        }
        initializer { HostelViewModel(container.hostelRepository) }
        initializer { PaymentHistoryViewModel(container.paymentRepository, container.residentRepository) }
        initializer { PaymentsViewModel(container.paymentRepository, container.residentRepository) }
        initializer { RecordPaymentViewModel(container.residentRepository, container.paymentRepository) }
        initializer { ReportsViewModel(container.hostelRepository, container.roomRepository, container.residentRepository, container.paymentRepository, container.expenseRepository) }
        initializer { CheckoutViewModel(container.residentRepository, container.roomRepository) }
        initializer {
            val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application
            EditResidentViewModel(app, container.residentRepository)
        }
        initializer { PastResidentsViewModel(container.residentRepository) }
        initializer { ReAdmitResidentViewModel(container.residentRepository, container.roomRepository) }
        initializer { ResidentDetailsViewModel(container.residentRepository, container.roomRepository, container.paymentRepository) }
        initializer { ResidentListViewModel(container.residentRepository) }
        initializer { ResidentViewModel(container.residentRepository, container.roomRepository) }
        initializer { TransferRoomViewModel(container.residentRepository, container.roomRepository) }
        initializer { ArchivedRoomsViewModel(container.roomRepository) }
        initializer { RoomDetailsViewModel(container.roomRepository, container.residentRepository) }
        initializer { RoomViewModel(container.roomRepository) }

        // ✅ UPDATED: Pass paymentRepository to DailyGuestsViewModel
        initializer { DailyGuestsViewModel(container.residentRepository, container.paymentRepository) }
    }
}