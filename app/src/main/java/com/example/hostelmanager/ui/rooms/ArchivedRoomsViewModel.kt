package com.example.hostelmanager.ui.rooms

import androidx.lifecycle.viewModelScope
import com.example.hostelmanager.data.RoomEntity
import com.example.hostelmanager.data.repository.RoomRepository
import com.example.hostelmanager.ui.BaseHostelViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ArchivedRoomsViewModel(
    private val roomRepository: RoomRepository
) : BaseHostelViewModel() {

    val archivedRooms: StateFlow<List<RoomEntity>> = hostelFlow(emptyList<RoomEntity>()) { id ->
        roomRepository.getArchivedRooms(id)
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
