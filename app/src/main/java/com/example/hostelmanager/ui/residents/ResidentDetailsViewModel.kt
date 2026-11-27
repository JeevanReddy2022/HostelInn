package com.example.hostelmanager.ui.residents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hostelmanager.data.Payment
import com.example.hostelmanager.data.Person
import com.example.hostelmanager.data.RoomEntity
import com.example.hostelmanager.data.Stay
import com.example.hostelmanager.data.repository.PaymentRepository
import com.example.hostelmanager.data.repository.ResidentRepository
import com.example.hostelmanager.data.repository.RoomRepository
import com.example.hostelmanager.domain.Balance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class ResidentDetailsViewModel(
    private val residentRepository: ResidentRepository,
    private val roomRepository: RoomRepository,
    private val paymentRepository: PaymentRepository
) : ViewModel() {
    private val _personId = MutableStateFlow("")

    val person: StateFlow<Person?> = _personId.flatMapLatest { id ->
        if (id.isEmpty()) flowOf(null) else residentRepository.getPersonByIdFlow(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val activeStay: StateFlow<Stay?> = _personId.flatMapLatest { id ->
        if (id.isEmpty()) flowOf(null) else residentRepository.getActiveStayByPerson(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val room: StateFlow<RoomEntity?> = activeStay.flatMapLatest { stay ->
        if (stay == null) flowOf(null) else roomRepository.getRoomById(stay.roomId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val payments: StateFlow<List<Payment>> = _personId.flatMapLatest { id ->
        if (id.isEmpty()) flowOf(emptyList())
        else paymentRepository.getPaymentsForPerson(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ✅ NEW: Live ledger balance for the details screen
    val balance: StateFlow<Long> = combine(activeStay, payments) { stay, paymentsList ->
        if (stay == null) 0L
        else Balance.balancePaise(stay, paymentsList, Balance.referenceDateFor(stay, LocalDate.now()))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    fun setPersonId(id: String) {
        _personId.value = id
    }

    fun deletePayment(paymentId: String) {
        viewModelScope.launch {
            paymentRepository.deletePayment(paymentId)
        }
    }

    fun giveNotice(noticeDate: String) {
        viewModelScope.launch {
            val stay = activeStay.value ?: return@launch
            residentRepository.updateNoticeDate(stay, noticeDate)
        }
    }

    fun cancelNotice() {
        viewModelScope.launch {
            val stay = activeStay.value ?: return@launch
            residentRepository.updateNoticeDate(stay, null)
        }
    }
}