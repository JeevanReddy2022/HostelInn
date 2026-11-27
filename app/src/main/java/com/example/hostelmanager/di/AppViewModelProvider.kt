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
import com.example.hostelmanager.ui.residents.NoticeViewModel
import com.example.hostelmanager.ui.residents.PastResidentsViewModel
import com.example.hostelmanager.ui.residents.ReAdmitResidentViewModel
import com.example.hostelmanager.ui.residents.ResidentDetailsViewModel
import com.example.hostelmanager.ui.residents.ResidentListViewModel
import com.example.hostelmanager.ui.residents.ResidentViewModel
import com.example.hostelmanager.ui.residents.TransferRoomViewModel
import com.example.hostelmanager.ui.rooms.ArchivedRoomsViewModel
import com.example.hostelmanager.ui.rooms.RoomDetailsViewModel
import com.example.hostelmanager.ui.rooms.RoomViewModel
import com.example.hostelmanager.data.repository.PaymentEditRepository

object AppViewModelProvider {
    fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
        initializer {
            DashboardViewModel(
                hostelRepository = container.hostelRepository,
                residentRepository = container.residentRepository,
                roomRepository = container.roomRepository,
                paymentRepository = container.paymentRepository,
                expenseRepository = container.expenseRepository
            )
        }
        initializer { AddExpenseViewModel(expenseRepository = container.expenseRepository) }
        initializer {
            val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application
            ExpensesViewModel(application = app, expenseRepository = container.expenseRepository)
        }
        initializer { HostelViewModel(hostelRepository = container.hostelRepository) }
        initializer { PaymentHistoryViewModel(paymentRepository = container.paymentRepository, residentRepository = container.residentRepository, paymentEditRepository = container.paymentEditRepository) }
        initializer { PaymentsViewModel(paymentRepository = container.paymentRepository, residentRepository = container.residentRepository) }
        initializer { RecordPaymentViewModel(residentRepository = container.residentRepository, paymentRepository = container.paymentRepository) }
        initializer {
            ReportsViewModel(
                hostelRepository = container.hostelRepository,
                roomRepository = container.roomRepository,
                residentRepository = container.residentRepository,
                paymentRepository = container.paymentRepository,
                expenseRepository = container.expenseRepository
            )
        }
        initializer {
            CheckoutViewModel(
                residentRepository = container.residentRepository,
                roomRepository = container.roomRepository,
                paymentRepository = container.paymentRepository
            )
        }
        initializer {
            val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application
            EditResidentViewModel(application = app, residentRepository = container.residentRepository)
        }
        initializer { PastResidentsViewModel(residentRepository = container.residentRepository) }
        initializer { ReAdmitResidentViewModel(residentRepository = container.residentRepository, roomRepository = container.roomRepository) }
        initializer { ResidentDetailsViewModel(residentRepository = container.residentRepository, roomRepository = container.roomRepository, paymentRepository = container.paymentRepository) }
        initializer { ResidentListViewModel(residentRepository = container.residentRepository) }
        initializer { ResidentViewModel(residentRepository = container.residentRepository, roomRepository = container.roomRepository) }
        initializer { TransferRoomViewModel(residentRepository = container.residentRepository, roomRepository = container.roomRepository) }
        initializer { ArchivedRoomsViewModel(roomRepository = container.roomRepository) }
        initializer { RoomDetailsViewModel(roomRepository = container.roomRepository, residentRepository = container.residentRepository) }
        initializer { RoomViewModel(roomRepository = container.roomRepository) }
        initializer { DailyGuestsViewModel(residentRepository = container.residentRepository, paymentRepository = container.paymentRepository) }
        initializer { NoticeViewModel(residentRepository = container.residentRepository) } // ✅ NEW
    }
}