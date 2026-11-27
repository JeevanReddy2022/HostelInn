package com.example.hostelmanager.ui.residents

import androidx.lifecycle.viewModelScope
import com.example.hostelmanager.data.ResidentWithRoom
import com.example.hostelmanager.data.Stay
import com.example.hostelmanager.data.repository.PaymentRepository
import com.example.hostelmanager.data.repository.ResidentRepository
import com.example.hostelmanager.domain.Balance
import com.example.hostelmanager.ui.BaseHostelViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

class DailyGuestsViewModel(
    private val residentRepository: ResidentRepository,
    private val paymentRepository: PaymentRepository
) : BaseHostelViewModel() {

    val dailyGuests: StateFlow<List<ResidentWithRoom>> =
        hostelFlow(emptyList<ResidentWithRoom>()) { id ->
            residentRepository.getDailyGuestsByHostel(id)
        }

    // ✅ Reactive balance flow. No more LaunchedEffect glitching.
    private val _selectedStayId = MutableStateFlow("")
    private val _selectedDailyRent = MutableStateFlow(0L)
    private val _selectedJoinDate = MutableStateFlow("")

    val balance: StateFlow<Long> = _selectedStayId.flatMapLatest { stayId ->
        if (stayId.isEmpty()) {
            flowOf(0L)
        } else {
            val rent = _selectedDailyRent.value
            val join = _selectedJoinDate.value
            paymentRepository.getPaymentsForStay(stayId).map { payments ->
                val stay = Stay(
                    id = stayId, personId = "", hostelId = "", roomId = "",
                    joinDate = join, monthlyRent = 0, dailyRent = rent,
                    billingAnchorDay = 1, isActive = true
                )
                Balance.balancePaise(stay, payments, LocalDate.now())
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    fun selectGuest(stayId: String, dailyRent: Long, joinDate: String) {
        _selectedStayId.value = stayId
        _selectedDailyRent.value = dailyRent
        _selectedJoinDate.value = joinDate
    }
}