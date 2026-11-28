package com.example.hostelmanager.di

import android.content.Context
import com.example.hostelmanager.data.AppDatabase
import com.example.hostelmanager.data.repository.ExpenseRepository
import com.example.hostelmanager.data.repository.ExpenseRepositoryImpl
import com.example.hostelmanager.data.repository.HostelRepository
import com.example.hostelmanager.data.repository.HostelRepositoryImpl
import com.example.hostelmanager.data.repository.PaymentEditRepository
import com.example.hostelmanager.data.repository.PaymentEditRepositoryImpl
import com.example.hostelmanager.data.repository.PaymentRepository
import com.example.hostelmanager.data.repository.PaymentRepositoryImpl
import com.example.hostelmanager.data.repository.ResidentRepository
import com.example.hostelmanager.data.repository.ResidentRepositoryImpl
import com.example.hostelmanager.data.repository.RoomRepository
import com.example.hostelmanager.data.repository.RoomRepositoryImpl

interface AppContainer {
    val hostelRepository: HostelRepository
    val roomRepository: RoomRepository
    val residentRepository: ResidentRepository
    val paymentRepository: PaymentRepository
    val expenseRepository: ExpenseRepository
    val paymentEditRepository: PaymentEditRepository // ✅ NEW
}

class AppContainerImpl(private val context: Context) : AppContainer {
    private val database: AppDatabase by lazy {
        AppDatabase.getDatabase(context)
    }

    override val hostelRepository: HostelRepository by lazy {
        HostelRepositoryImpl(
            hostelDao = database.hostelDao(),
            stayDao = database.stayDao(),
            roomDao = database.roomDao()
        )
    }

    override val roomRepository: RoomRepository by lazy {
        RoomRepositoryImpl(
            database = database,
            roomDao = database.roomDao(),
            stayDao = database.stayDao()
        )
    }

    override val residentRepository: ResidentRepository by lazy {
        ResidentRepositoryImpl(
            database = database,
            personDao = database.personDao(),
            stayDao = database.stayDao()
        )
    }

    override val paymentRepository: PaymentRepository by lazy {
        PaymentRepositoryImpl(
            paymentDao = database.paymentDao()
        )
    }

    override val expenseRepository: ExpenseRepository by lazy {
        ExpenseRepositoryImpl(
            expenseDao = database.expenseDao()
        )
    }

    // ✅ NEW: Wires up the audit trail repository
    override val paymentEditRepository: PaymentEditRepository by lazy {
        PaymentEditRepositoryImpl(
            database = database,
            paymentDao = database.paymentDao(),
            paymentEditDao = database.paymentEditDao(),
            stayDao = database.stayDao()
        )
    }
}