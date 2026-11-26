package com.example.hostelmanager.ui.residents

import com.example.hostelmanager.data.Person
import com.example.hostelmanager.data.RoomWithOccupancy
import com.example.hostelmanager.data.Stay
import com.example.hostelmanager.data.repository.ResidentRepository
import com.example.hostelmanager.data.repository.RoomRepository
import com.example.hostelmanager.domain.BillingCycle
import com.example.hostelmanager.ui.BaseHostelViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ResidentViewModel(
    private val residentRepository: ResidentRepository,
    private val roomRepository: RoomRepository
) : BaseHostelViewModel() {
    private val _phoneError = MutableStateFlow<String?>(null)
    val phoneError: StateFlow<String?> = _phoneError
    private val _govtIdError = MutableStateFlow<String?>(null)
    val govtIdError: StateFlow<String?> = _govtIdError
    private val _emergencyError = MutableStateFlow<String?>(null)
    val emergencyError: StateFlow<String?> = _emergencyError

    val roomsForHostel: StateFlow<List<RoomWithOccupancy>> = hostelFlow(emptyList<RoomWithOccupancy>()) { id ->
        roomRepository.getRoomsWithOccupancy(id)
    }

    suspend fun addResident(
        hostelId: String,
        name: String, phone: String, govtIdNumber: String, emergencyContact: String,
        dateOfBirth: String, address: String, gender: String, guardianName: String,
        roomId: String, joinDate: String, rentPaise: Long, depositPaise: Long,
        govtFrontPath: String, govtBackPath: String, profilePhotoPath: String,
        isDailyStay: Boolean // NEW: Tracks if this is a daily guest
    ): Boolean {
        if (hostelId.isEmpty() || roomId.isEmpty()) return false

        _phoneError.value = null
        _govtIdError.value = null
        _emergencyError.value = null

        var hasDuplicates = false
        if (residentRepository.isPhoneDuplicate(phone)) {
            _phoneError.value = "This phone number is already in use!"
            hasDuplicates = true
        }
        if (residentRepository.isGovtIdDuplicate(govtIdNumber)) {
            _govtIdError.value = "This Govt ID is already in use!"
            hasDuplicates = true
        }
        if (residentRepository.isEmergencyDuplicate(emergencyContact)) {
            _emergencyError.value = "This emergency contact is already in use!"
            hasDuplicates = true
        }
        if (hasDuplicates) return false

        val newPerson = Person(
            name = name, phone = phone, govtIdNumber = govtIdNumber,
            emergencyContact = emergencyContact, dateOfBirth = dateOfBirth,
            address = address, gender = gender, guardianName = guardianName,
            govtIdPhotoPath = govtFrontPath,
            govtIdBackPath = govtBackPath,
            profilePhotoPath = profilePhotoPath
        )

        // NEW: Split the rent into the correct column based on the toggle
        val monthlyRentPaise = if (isDailyStay) 0L else rentPaise
        val dailyRentPaise = if (isDailyStay) rentPaise else 0L

        val newStay = Stay(
            personId = newPerson.id,
            hostelId = hostelId,
            roomId = roomId,
            joinDate = joinDate,
            monthlyRent = monthlyRentPaise,
            dailyRent = dailyRentPaise,
            depositCollected = depositPaise,
            billingAnchorDay = BillingCycle.anchorDayOf(joinDate),
            isActive = true
        )

        residentRepository.addResident(newPerson, newStay)
        return true
    }
}