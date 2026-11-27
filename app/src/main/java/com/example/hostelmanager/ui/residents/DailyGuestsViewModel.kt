package com.example.hostelmanager.ui.residents

import androidx.lifecycle.viewModelScope
import com.example.hostelmanager.data.ResidentWithRoom
import com.example.hostelmanager.data.repository.PaymentRepository
import com.example.hostelmanager.data.repository.ResidentRepository
import com.example.hostelmanager.ui.BaseHostelViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class DailyGuestsViewModel(
    private val residentRepository: ResidentRepository,
    private val paymentRepository: PaymentRepository
) : BaseHostelViewModel() {

    val dailyGuests: StateFlow<List<ResidentWithRoom>> =
        hostelFlow(emptyList<ResidentWithRoom>()) { id ->
            residentRepository.getDailyGuestsByHostel(id)
        }

    /** Live total paid on a stay (₹0 write-offs contribute 0 — that's correct). */
    fun getTotalPaid(stayId: String): StateFlow<Long> {
        return paymentRepository.getTotalPaidForStay(stayId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)
    }

    /** ✅ NEW: same rule the checkout gate uses, so the sheet and the gate agree. */
    suspend fun hasWriteOff(stayId: String, startDate: String, endDate: String): Boolean {
        return paymentRepository.countWriteOffsInRange(stayId, startDate, endDate) > 0
    }
}