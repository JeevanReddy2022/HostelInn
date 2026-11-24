package com.example.hostelmanager.ui.payments

import com.example.hostelmanager.data.Payment
import com.example.hostelmanager.data.ResidentWithRoom
import com.example.hostelmanager.data.repository.PaymentRepository
import com.example.hostelmanager.data.repository.ResidentRepository
import com.example.hostelmanager.ui.BaseHostelViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class RecordPaymentViewModel(
    private val residentRepository: ResidentRepository,
    private val paymentRepository: PaymentRepository
) : BaseHostelViewModel() {

    val residents: StateFlow<List<ResidentWithRoom>> = hostelFlow(emptyList<ResidentWithRoom>()) { hostelId ->
        residentRepository.getAllResidentsWithLatestStay(hostelId).map { allResidents ->
            paymentRepository.getUnpaidResidentsForHostel(
                hostelId = hostelId,
                allResidents = allResidents,
                today = LocalDate.now()
            )
        }
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
