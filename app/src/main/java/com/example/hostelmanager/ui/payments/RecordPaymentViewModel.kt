package com.example.hostelmanager.ui.payments

import com.example.hostelmanager.data.Payment
import com.example.hostelmanager.data.ResidentWithRoom
import com.example.hostelmanager.data.repository.PaymentRepository
import com.example.hostelmanager.data.repository.ResidentRepository
import com.example.hostelmanager.ui.BaseHostelViewModel
import kotlinx.coroutines.flow.StateFlow

class RecordPaymentViewModel(
    private val residentRepository: ResidentRepository,
    private val paymentRepository: PaymentRepository
) : BaseHostelViewModel() {

    // All ACTIVE residents (monthly + daily), paid or not — so a second payment
    // can always be recorded, and the Daily Guests sheet can prefill anyone.
    val residents: StateFlow<List<ResidentWithRoom>> =
        hostelFlow(emptyList<ResidentWithRoom>()) { hostelId ->
            residentRepository.getActiveResidentsByHostel(hostelId)
        }

    suspend fun recordPayment(
        personId: String,
        amountPaise: Long,
        mode: String,
        paymentDate: String,
        recordedDate: String,
        note: String
    ): Boolean {
        if (personId.isEmpty() || paymentDate.isEmpty() || recordedDate.isEmpty()) return false
        if (amountPaise < 0) return false

        // ✅ ₹0 is allowed ONLY as a deliberate write-off with a real reason.
        if (amountPaise == 0L && note.trim().length < 5) return false

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
                note = note.trim()
            )
        )
        return true
    }
}