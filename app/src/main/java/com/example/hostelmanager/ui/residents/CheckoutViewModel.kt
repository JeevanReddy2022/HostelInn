package com.example.hostelmanager.ui.residents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hostelmanager.data.Person
import com.example.hostelmanager.data.RoomEntity
import com.example.hostelmanager.data.Stay
import com.example.hostelmanager.data.repository.ResidentRepository
import com.example.hostelmanager.data.repository.RoomRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

class CheckoutViewModel(
    private val residentRepository: ResidentRepository,
    private val roomRepository: RoomRepository
) : ViewModel() {

    private val _personId = MutableStateFlow("")

    val person: StateFlow<Person?> = _personId.flatMapLatest { id ->
        if (id.isEmpty()) flowOf(null) else residentRepository.getPersonByIdFlow(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val activeStay: StateFlow<Stay?> = _personId.flatMapLatest { id ->
        if (id.isEmpty()) flowOf(null) else residentRepository.getActiveStayByPerson(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val currentRoom: StateFlow<RoomEntity?> = _personId.flatMapLatest { id ->
        if (id.isEmpty()) flowOf(null)
        else residentRepository.getActiveStayByPerson(id).flatMapLatest { stay ->
            if (stay == null) flowOf(null) else roomRepository.getRoomById(stay.roomId)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _checkoutError = MutableStateFlow<String?>(null)
    val checkoutError: StateFlow<String?> = _checkoutError

    fun setPersonId(id: String) {
        _personId.value = id
    }

    suspend fun checkoutResident(
        checkoutDate: String,
        refundPaise: Long,
        refundMode: String?,
        refundDate: String?
    ): Boolean {
        val stay = activeStay.value ?: residentRepository.getActiveStayForPerson(_personId.value) ?: return false

        _checkoutError.value = null

        if (checkoutDate.isEmpty()) {
            _checkoutError.value = "Please choose a check-out date."
            return false
        }

        if (checkoutDate < stay.joinDate) {
            _checkoutError.value = "Check-out date cannot be before the join date (${stay.joinDate})."
            return false
        }

        if (refundPaise < 0) {
            _checkoutError.value = "Refund cannot be negative."
            return false
        }

        if (refundPaise > stay.depositCollected) {
            _checkoutError.value = "Refund cannot be more than the deposit held (₹${stay.depositCollected / 100})."
            return false
        }

        if (refundPaise > 0 && (refundMode.isNullOrBlank() || refundDate.isNullOrBlank())) {
            _checkoutError.value = "Choose a refund mode and refund date."
            return false
        }

        residentRepository.checkoutResident(
            stay = stay,
            checkoutDate = checkoutDate,
            refundPaise = refundPaise,
            refundMode = refundMode,
            refundDate = refundDate
        )

        return true
    }
}
