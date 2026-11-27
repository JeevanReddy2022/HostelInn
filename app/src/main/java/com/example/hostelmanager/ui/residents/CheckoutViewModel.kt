package com.example.hostelmanager.ui.residents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hostelmanager.data.Person
import com.example.hostelmanager.data.RoomEntity
import com.example.hostelmanager.data.Stay
import com.example.hostelmanager.data.repository.PaymentRepository
import com.example.hostelmanager.data.repository.ResidentRepository
import com.example.hostelmanager.data.repository.RoomRepository
import com.example.hostelmanager.domain.BillingCycle
import com.example.hostelmanager.util.paiseToText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class CheckoutViewModel(
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

    /** Money still owed for this stay as of [checkoutDate]. */
    private suspend fun outstandingFor(stay: Stay, checkoutDate: String): Long {
        return if (stay.dailyRent > 0) {
            val days = runCatching {
                ChronoUnit.DAYS.between(
                    LocalDate.parse(stay.joinDate),
                    LocalDate.parse(checkoutDate)
                ) + 1
            }.getOrDefault(1L).coerceAtLeast(1L)
            val paid = paymentRepository.getTotalPaidForStayOnce(stay.id)
            (days * stay.dailyRent) - paid
        } else {
            val anchor = stay.billingAnchorDay.coerceIn(1, 31)
            val reference = runCatching { LocalDate.parse(checkoutDate) }
                .getOrDefault(LocalDate.now())
            val start = BillingCycle.cycleStart(reference, anchor)
            val end = start.plusMonths(1).minusDays(1)
            val paidInCycle = paymentRepository
                .getPaymentsInCycle(stay.id, start.toString(), end.toString())
                .sumOf { it.amountPaise }
            stay.monthlyRent - paidInCycle
        }
    }

    /** A ₹0 payment with a reason (>= 5 chars) inside the period = deliberate write-off. */
    private suspend fun hasWriteOff(stay: Stay, checkoutDate: String): Boolean {
        return if (stay.dailyRent > 0) {
            paymentRepository.countWriteOffsInRange(stay.id, stay.joinDate, checkoutDate) > 0
        } else {
            val anchor = stay.billingAnchorDay.coerceIn(1, 31)
            val reference = runCatching { LocalDate.parse(checkoutDate) }
                .getOrDefault(LocalDate.now())
            val start = BillingCycle.cycleStart(reference, anchor)
            val end = start.plusMonths(1).minusDays(1)
            paymentRepository.countWriteOffsInRange(stay.id, start.toString(), end.toString()) > 0
        }
    }

    suspend fun checkoutResident(
        checkoutDate: String,
        refundPaise: Long,
        refundMode: String?,
        refundDate: String?
    ): Boolean {
        val stay = activeStay.value
            ?: residentRepository.getActiveStayForPerson(_personId.value)
            ?: return false

        _checkoutError.value = null

        if (checkoutDate.isEmpty()) {
            _checkoutError.value = "Please choose a check-out date."
            return false
        }
        if (checkoutDate < stay.joinDate) {
            _checkoutError.value =
                "Check-out date cannot be before the join date (${stay.joinDate})."
            return false
        }
        if (refundPaise < 0) {
            _checkoutError.value = "Refund cannot be negative."
            return false
        }
        if (refundPaise > stay.depositCollected) {
            _checkoutError.value =
                "Refund cannot be more than the deposit held (${stay.depositCollected.paiseToText()})."
            return false
        }
        if (refundPaise > 0 && (refundMode.isNullOrBlank() || refundDate.isNullOrBlank())) {
            _checkoutError.value = "Choose a refund mode and refund date."
            return false
        }

        // ✅ THE GATE — every checkout path (details screen, daily sheet) passes here.
        val due = outstandingFor(stay, checkoutDate)
        if (due > 0 && !hasWriteOff(stay, checkoutDate)) {
            _checkoutError.value =
                "Payment pending: ${due.paiseToText()}. Record the payment, " +
                        "or record a ₹0 payment with a reason to write it off."
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