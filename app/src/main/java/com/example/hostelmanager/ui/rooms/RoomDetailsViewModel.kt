package com.example.hostelmanager.ui.rooms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hostelmanager.data.ResidentWithRoom
import com.example.hostelmanager.data.RoomEntity
import com.example.hostelmanager.data.repository.ResidentRepository
import com.example.hostelmanager.data.repository.RoomRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class RentRevisionPrompt(
    val oldRentPaise: Long,
    val newRentPaise: Long,
    val residentCount: Int
)

class RoomDetailsViewModel(
    private val roomRepository: RoomRepository,
    private val residentRepository: ResidentRepository
) : ViewModel() {

    private val _roomId = MutableStateFlow("")

    val room: StateFlow<RoomEntity?> = _roomId.flatMapLatest { id ->
        if (id.isEmpty()) flowOf(null) else roomRepository.getRoomById(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val residents: StateFlow<List<ResidentWithRoom>> = _roomId.flatMapLatest { id ->
        if (id.isEmpty()) flowOf(emptyList()) else residentRepository.getActiveResidentsByRoom(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalStays: StateFlow<Int> = _roomId.flatMapLatest { id ->
        if (id.isEmpty()) flowOf(0) else roomRepository.countStaysForRoom(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _rentPrompt = MutableStateFlow<RentRevisionPrompt?>(null)
    val rentPrompt: StateFlow<RentRevisionPrompt?> = _rentPrompt

    private val _roomRemoved = MutableStateFlow(false)
    val roomRemoved: StateFlow<Boolean> = _roomRemoved

    fun setRoomId(id: String) {
        _roomId.value = id
    }

    fun updateRoom(roomNumber: String, floor: Int, bedCount: Int, rentPerBedPaise: Long) {
        val current = room.value ?: return
        val oldRent = current.defaultRentPerBed
        val residentCount = residents.value.size

        viewModelScope.launch {
            roomRepository.updateRoom(
                current.copy(
                    roomNumber = roomNumber,
                    floor = floor,
                    bedCount = bedCount,
                    defaultRentPerBed = rentPerBedPaise,
                    updatedAt = System.currentTimeMillis()
                )
            )
            if (rentPerBedPaise != oldRent && residentCount > 0) {
                _rentPrompt.value = RentRevisionPrompt(oldRent, rentPerBedPaise, residentCount)
            }
        }
    }

    fun applyRentToAllResidents() {
        val current = room.value ?: return
        viewModelScope.launch {
            roomRepository.updateRoom(current, updateActiveStaysRent = true)
            _rentPrompt.value = null
        }
    }

    fun dismissRentPrompt() {
        _rentPrompt.value = null
    }

    fun archiveRoom() {
        val current = room.value ?: return
        viewModelScope.launch {
            roomRepository.updateRoom(
                current.copy(
                    isArchived = true,
                    updatedAt = System.currentTimeMillis()
                )
            )
            _roomRemoved.value = true
        }
    }

    fun deleteRoomPermanently() {
        val current = room.value ?: return
        viewModelScope.launch {
            roomRepository.deleteRoom(current)
            _roomRemoved.value = true
        }
    }
}
