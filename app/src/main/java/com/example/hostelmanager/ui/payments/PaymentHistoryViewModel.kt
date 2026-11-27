package com.example.hostelmanager.ui.payments

import androidx.lifecycle.viewModelScope
import com.example.hostelmanager.data.PaymentEdit
import com.example.hostelmanager.data.PaymentWithDetails
import com.example.hostelmanager.data.ResidentWithRoom
import com.example.hostelmanager.data.repository.PaymentEditRepository
import com.example.hostelmanager.data.repository.PaymentRepository
import com.example.hostelmanager.data.repository.ResidentRepository
import com.example.hostelmanager.ui.BaseHostelViewModel
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
    private val residentRepository: ResidentRepository,
    private val paymentEditRepository: PaymentEditRepository
) : BaseHostelViewModel() {

    private val _selectedMonth = MutableStateFlow<String?>(null)
    private val _selectedPersonId = MutableStateFlow<String?>(null)

    val residents: StateFlow<List<ResidentWithRoom>> = hostelFlow(emptyList<ResidentWithRoom>()) { id ->
        kotlinx.coroutines.flow.combine(
            residentRepository.getActiveResidentsByHostel(id),
            residentRepository.getPastResidentsByHostel(id)
        ) { active, past -> (active + past).sortedBy { it.person.name } }
    }

    val payments: StateFlow<List<PaymentWithDetails>> = combine(
        _hostelId, _selectedMonth, _selectedPersonId
    ) { hostelId, month, personId ->
        Triple(hostelId, month, personId)
    }.flatMapLatest { (hostelId, month, personId) ->
        if (hostelId.isEmpty()) {
            flowOf(emptyList())
        } else {
            paymentRepository.getAllPaymentsWithDetailsForHostel(hostelId).map { all ->
                all.filter { detail ->
                    val p = detail.payment
                    val matchesMonth = month == null || p.paymentDate.startsWith(month)
                    val matchesPerson = personId == null || p.personId == personId
                    matchesMonth && matchesPerson
                }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ✅ Audit history for whichever payment the user opened
    private val _historyPaymentId = MutableStateFlow<String?>(null)

    val editHistory: StateFlow<List<PaymentEdit>> = _historyPaymentId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList())
        else paymentEditRepository.getEditsForPayment(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun openHistory(paymentId: String?) {
        _historyPaymentId.value = paymentId
    }

    fun setMonth(month: String?) {
        _selectedMonth.value = month
    }

    fun setPerson(personId: String?) {
        _selectedPersonId.value = personId
    }

    fun deletePayment(paymentId: String) {
        viewModelScope.launch { paymentRepository.deletePayment(paymentId) }
    }

    /** null = saved, otherwise the message to show in the dialog. */
    suspend fun editPayment(
        paymentId: String,
        amountPaise: Long,
        mode: String,
        paymentDate: String,
        recordedDate: String,
        note: String,
        reason: String
    ): String? = paymentEditRepository.editPayment(
        paymentId = paymentId,
        newAmountPaise = amountPaise,
        newMode = mode,
        newPaymentDate = paymentDate,
        newRecordedDate = recordedDate,
        newNote = note,
        reason = reason
    )
}