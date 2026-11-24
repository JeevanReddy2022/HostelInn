package com.example.hostelmanager.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface StayDao {

    @Upsert
    suspend fun insertStay(stay: Stay)

    @Update
    suspend fun updateStay(stay: Stay)

    @Query("SELECT * FROM stays WHERE personId = :personId ORDER BY joinDate DESC")
    fun getStaysByPerson(personId: String): Flow<List<Stay>>

    @Query("SELECT * FROM stays WHERE hostelId = :hostelId AND isActive = 1 ORDER BY joinDate DESC")
    fun getActiveStaysByHostel(hostelId: String): Flow<List<Stay>>

    @Query("SELECT * FROM stays WHERE id = :id")
    suspend fun getStayById(id: String): Stay?

    @Query("UPDATE stays SET monthlyRent = :rentPaise, updatedAt = :updatedAt WHERE roomId = :roomId AND isActive = 1")
    suspend fun updateRentForActiveStays(roomId: String, rentPaise: Long, updatedAt: Long)

    @Query("SELECT COUNT(*) FROM stays WHERE roomId = :roomId")
    fun countStaysForRoom(roomId: String): Flow<Int>

    // NEW: the currently-active stay for one person (details screen)
    @Query("SELECT * FROM stays WHERE personId = :personId AND isActive = 1 ORDER BY joinDate DESC LIMIT 1")
    fun getActiveStayByPerson(personId: String): Flow<Stay?>

    // One-shot lookup for write paths (record payment, re-admit guard).
    @Query("SELECT * FROM stays WHERE personId = :personId AND isActive = 1 ORDER BY joinDate DESC LIMIT 1")
    suspend fun getActiveStayForPerson(personId: String): Stay?

    @Query("SELECT COUNT(*) FROM stays WHERE hostelId = :hostelId AND isActive = 1")
    fun getActiveResidentsCount(hostelId: String): Flow<Int>

    @Query("""
    SELECT COUNT(*) FROM stays 
    WHERE hostelId = :hostelId 
    AND joinDate LIKE :monthPattern
""")
    fun getCheckInsCount(hostelId: String, monthPattern: String): Flow<Int>
}