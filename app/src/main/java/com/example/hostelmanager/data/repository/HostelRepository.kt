package com.example.hostelmanager.data.repository

import com.example.hostelmanager.data.Hostel
import com.example.hostelmanager.data.HostelDao
import com.example.hostelmanager.data.RoomDao
import com.example.hostelmanager.data.StayDao
import kotlinx.coroutines.flow.Flow

interface HostelRepository {
    fun getAllHostels(): Flow<List<Hostel>>
    fun getHostelById(id: String): Flow<Hostel?>
    suspend fun insertHostel(hostel: Hostel)
    suspend fun updateHostel(hostel: Hostel)
    suspend fun deleteHostel(hostel: Hostel)
    fun getActiveResidentsCount(hostelId: String): Flow<Int>
    fun getTotalBedsForHostel(hostelId: String): Flow<Int>
    fun getCheckInsCount(hostelId: String, monthPattern: String): Flow<Int>
}

class HostelRepositoryImpl(
    private val hostelDao: HostelDao,
    private val stayDao: StayDao,
    private val roomDao: RoomDao
) : HostelRepository {
    override fun getAllHostels(): Flow<List<Hostel>> = hostelDao.getAllHostels()
    override fun getHostelById(id: String): Flow<Hostel?> = hostelDao.getHostelById(id)
    override suspend fun insertHostel(hostel: Hostel) = hostelDao.insertHostel(hostel)
    override suspend fun updateHostel(hostel: Hostel) = hostelDao.updateHostel(hostel)
    override suspend fun deleteHostel(hostel: Hostel) = hostelDao.deleteHostel(hostel)
    override fun getActiveResidentsCount(hostelId: String): Flow<Int> = stayDao.getActiveResidentsCount(hostelId)
    override fun getTotalBedsForHostel(hostelId: String): Flow<Int> = roomDao.getTotalBedsForHostel(hostelId)
    override fun getCheckInsCount(hostelId: String, monthPattern: String): Flow<Int> = stayDao.getCheckInsCount(hostelId, monthPattern)
}
