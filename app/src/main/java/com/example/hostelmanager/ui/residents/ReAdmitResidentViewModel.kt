package com.example.hostelmanager.ui.residents

import androidx.lifecycle.viewModelScope
import com.example.hostelmanager.data.Person
import com.example.hostelmanager.data.RoomWithOccupancy
import com.example.hostelmanager.data.Stay
import com.example.hostelmanager.data.repository.ResidentRepository
import com.example.hostelmanager.data.repository.RoomRepository
import com.example.hostelmanager.domain.BillingCycle
import com.example.hostelmanager.ui.BaseHostelViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

class ReAdmitResidentViewModel(
    private val residentRepository: ResidentRepository,
    private val roomRepository: RoomRepository
) : BaseHostelViewModel() {

    private val _personId = MutableStateFlow("")

    // Left unchanged per Constraint 4 (keyed by _personId, not hostelId)
    val person: StateFlow<Person?> = _personId.flatMapLatest { id ->
        if (id.isEmpty()) flowOf(null) else residentRepository.getPersonByIdFlow(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val roomsForHostel: StateFlow<List<RoomWithOccupancy>> = hostelFlow(emptyList<RoomWithOccupancy>()) { id ->
        roomRepository.getRoomsWithOccupancy(id)
    }

    fun setPersonId(id: String) {
        _personId.value = id
    }

    suspend fun reAdmitResident(
        roomId: String,
        joinDate: String,
        rentPaise: Long,
        depositPaise: Long
    ): Boolean {
        val p = person.value ?: return false
        if (_hostelId.value.isEmpty() || roomId.isEmpty() || joinDate.isEmpty()) return false

        if (residentRepository.getActiveStayForPerson(p.id) != null) return false

        residentRepository.reAdmitResident(
            Stay(
                personId = p.id,
                hostelId = _hostelId.value,
                roomId = roomId,
                joinDate = joinDate,
                monthlyRent = rentPaise,
                depositCollected = depositPaise,
                billingAnchorDay = BillingCycle.anchorDayOf(joinDate),
                isActive = true
            )
        )
        return true
    }
}
