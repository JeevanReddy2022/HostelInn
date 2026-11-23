package com.example.hostelmanager.ui.payments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hostelmanager.data.PaymentWithDetails
import com.example.hostelmanager.data.ResidentWithRoom
import com.example.hostelmanager.data.repository.PaymentRepository
import com.example.hostelmanager.data.repository.ResidentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PaymentHistoryViewModel(
    private val paymentRepository: PaymentRepository,
    private val residentRepository: ResidentRepository
) : ViewModel() {

    private val _hostelId = MutableStateFlow("")
    private val _selectedMonth = MutableStateFlow<String?>(null)
    private val _selectedPersonId = MutableStateFlow<String?>(null)

    val residents: StateFlow<List<ResidentWithRoom>> = _hostelId.flatMapLatest { id ->
        if (id.isEmpty()) flowOf(emptyList())
        else residentRepository.getActiveResidentsByHostel(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val payments: StateFlow<List<PaymentWithDetails>> = combine(
        _hostelId,
        _selectedMonth,
        _selectedPersonId
    ) { hostelId, month, personId ->
        Triple(hostelId, month, personId)
    }.flatMapLatest { (hostelId, month, personId) ->
        if (hostelId.isEmpty()) {
            flowOf(emptyList())
        } else {
            paymentRepository.getAllPaymentsWithDetailsForHostel(hostelId).map { allPayments ->
                allPayments.filter { paymentWithDetails ->
                    val p = paymentWithDetails.payment
                    val matchesMonth = month == null || p.paymentDate.startsWith(month)
                    val matchesPerson = personId == null || p.personId == personId
                    matchesMonth && matchesPerson
                }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setHostelId(id: String) {
        _hostelId.value = id
    }

    fun setMonth(month: String?) {
        _selectedMonth.value = month
    }

    fun setPerson(personId: String?) {
        _selectedPersonId.value = personId
    }

    fun deletePayment(paymentId: String) {
        viewModelScope.launch {
            paymentRepository.deletePayment(paymentId)
        }
    }
}
