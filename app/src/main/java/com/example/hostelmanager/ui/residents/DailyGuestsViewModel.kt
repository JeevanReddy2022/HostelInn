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
    private val paymentRepository: PaymentRepository // ✅ Added
) : BaseHostelViewModel() {

    val dailyGuests: StateFlow<List<ResidentWithRoom>> = hostelFlow(emptyList<ResidentWithRoom>()) { id ->
        residentRepository.getDailyGuestsByHostel(id)
    }

    // ✅ NEW: Fetch total paid for a specific stay
    fun getTotalPaid(stayId: String): StateFlow<Long> {
        return paymentRepository.getTotalPaidForStay(stayId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)
    }
}