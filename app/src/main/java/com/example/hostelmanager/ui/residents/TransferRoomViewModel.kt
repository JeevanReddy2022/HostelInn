package com.example.hostelmanager.ui.residents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hostelmanager.data.Person
import com.example.hostelmanager.data.RoomEntity
import com.example.hostelmanager.data.RoomWithOccupancy
import com.example.hostelmanager.data.Stay
import com.example.hostelmanager.data.repository.ResidentRepository
import com.example.hostelmanager.data.repository.RoomRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

class TransferRoomViewModel(
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

    val availableRooms: StateFlow<List<RoomWithOccupancy>> = _personId.flatMapLatest { id ->
        if (id.isEmpty()) flowOf(emptyList())
        else residentRepository.getActiveStayByPerson(id).flatMapLatest { stay ->
            if (stay == null) flowOf(emptyList()) else roomRepository.getRoomsWithOccupancy(stay.hostelId)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _transferError = MutableStateFlow<String?>(null)
    val transferError: StateFlow<String?> = _transferError

    fun setPersonId(id: String) {
        _personId.value = id
    }

    suspend fun transferRoom(newRoomId: String, transferDate: String, newRentPaise: Long): Boolean {
        val stay = activeStay.value ?: return false

        _transferError.value = null

        if (newRoomId.isEmpty() || transferDate.isEmpty()) return false

        if (newRoomId == stay.roomId) {
            _transferError.value = "Please choose a different room."
            return false
        }

        val target = availableRooms.value.firstOrNull { it.room.id == newRoomId }
        if (target == null) {
            _transferError.value = "That room is no longer available."
            return false
        }
        if (target.room.bedCount - target.occupiedBeds <= 0) {
            _transferError.value = "That room is now full."
            return false
        }

        if (transferDate < stay.joinDate) {
            _transferError.value = "Transfer date cannot be before the join date (${stay.joinDate})."
            return false
        }

        residentRepository.transferRoom(
            stay = stay,
            newRoomId = newRoomId,
            transferDate = transferDate,
            newRentPaise = newRentPaise
        )

        return true
    }
}
