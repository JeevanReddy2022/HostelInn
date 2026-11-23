package com.example.hostelmanager.ui.rooms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hostelmanager.data.RoomEntity
import com.example.hostelmanager.data.repository.RoomRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ArchivedRoomsViewModel(
    private val roomRepository: RoomRepository
) : ViewModel() {

    private val _hostelId = MutableStateFlow("")

    val archivedRooms: StateFlow<List<RoomEntity>> = _hostelId.flatMapLatest { id ->
        if (id.isEmpty()) flowOf(emptyList()) else roomRepository.getArchivedRooms(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setHostelId(id: String) {
        _hostelId.value = id
    }

    fun restoreRoom(room: RoomEntity) {
        viewModelScope.launch {
            roomRepository.updateRoom(
                room.copy(
                    isArchived = false,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }
}
