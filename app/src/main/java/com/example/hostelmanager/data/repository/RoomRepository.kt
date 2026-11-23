package com.example.hostelmanager.data.repository

import androidx.room.withTransaction
import com.example.hostelmanager.data.AppDatabase
import com.example.hostelmanager.data.RoomDao
import com.example.hostelmanager.data.RoomEntity
import com.example.hostelmanager.data.RoomWithOccupancy
import com.example.hostelmanager.data.StayDao
import kotlinx.coroutines.flow.Flow

interface RoomRepository {
    fun getRoomsWithOccupancy(hostelId: String): Flow<List<RoomWithOccupancy>>
    fun getAllActiveRooms(): Flow<List<RoomEntity>>
    fun getRoomById(id: String): Flow<RoomEntity?>
    fun getArchivedRooms(hostelId: String): Flow<List<RoomEntity>>
    suspend fun insertRoom(room: RoomEntity)
    suspend fun updateRoom(room: RoomEntity, updateActiveStaysRent: Boolean = false)
    suspend fun deleteRoom(room: RoomEntity)
    suspend fun countRoomsWithNumber(hostelId: String, roomNumber: String): Int
    fun countStaysForRoom(roomId: String): Flow<Int>
}

class RoomRepositoryImpl(
    private val database: AppDatabase,
    private val roomDao: RoomDao,
    private val stayDao: StayDao
) : RoomRepository {
    override fun getRoomsWithOccupancy(hostelId: String): Flow<List<RoomWithOccupancy>> =
        roomDao.getRoomsWithOccupancy(hostelId)

    override fun getAllActiveRooms(): Flow<List<RoomEntity>> =
        roomDao.getAllActiveRooms()

    override fun getRoomById(id: String): Flow<RoomEntity?> =
        roomDao.getRoomById(id)

    override fun getArchivedRooms(hostelId: String): Flow<List<RoomEntity>> =
        roomDao.getArchivedRooms(hostelId)

    override suspend fun insertRoom(room: RoomEntity) =
        roomDao.insertRoom(room)

    override suspend fun updateRoom(room: RoomEntity, updateActiveStaysRent: Boolean) {
        if (updateActiveStaysRent) {
            database.withTransaction {
                roomDao.updateRoom(room)
                stayDao.updateRentForActiveStays(room.id, room.defaultRentPerBed, System.currentTimeMillis())
            }
        } else {
            roomDao.updateRoom(room)
        }
    }

    override suspend fun deleteRoom(room: RoomEntity) =
        roomDao.deleteRoom(room)

    override suspend fun countRoomsWithNumber(hostelId: String, roomNumber: String): Int =
        roomDao.countRoomsWithNumber(hostelId, roomNumber)

    override fun countStaysForRoom(roomId: String): Flow<Int> =
        stayDao.countStaysForRoom(roomId)
}
