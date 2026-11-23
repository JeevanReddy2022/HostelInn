package com.example.hostelmanager.ui.payments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hostelmanager.data.Payment
import com.example.hostelmanager.data.ResidentWithRoom
import com.example.hostelmanager.data.repository.PaymentRepository
import com.example.hostelmanager.data.repository.ResidentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

class RecordPaymentViewModel(
    private val residentRepository: ResidentRepository,
    private val paymentRepository: PaymentRepository
) : ViewModel() {

    private val _hostelId = MutableStateFlow("")

    val residents: StateFlow<List<ResidentWithRoom>> = _hostelId.flatMapLatest { hostelId ->
        if (hostelId.isEmpty()) {
            flowOf(emptyList())
        } else {
            residentRepository.getAllResidentsWithLatestStay(hostelId).map { allResidents ->
                paymentRepository.getUnpaidResidentsForHostel(
                    hostelId = hostelId,
                    allResidents = allResidents,
                    today = LocalDate.now()
                )
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setHostelId(id: String) {
        _hostelId.value = id
    }

    suspend fun recordPayment(
        personId: String,
        amountPaise: Long,
        mode: String,
        paymentDate: String,
        recordedDate: String,
        note: String
    ): Boolean {
        if (personId.isEmpty() || amountPaise <= 0 || paymentDate.isEmpty() || recordedDate.isEmpty()) return false

        val stay = residentRepository.getActiveStayForPerson(personId) ?: return false

        paymentRepository.insertPayment(
            Payment(
                stayId = stay.id,
                personId = personId,
                hostelId = stay.hostelId,
                amountPaise = amountPaise,
                mode = mode,
                paymentDate = paymentDate,
                date = recordedDate,
                note = note
            )
        )
        return true
    }
}
