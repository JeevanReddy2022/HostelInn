package com.example.hostelmanager.ui.residents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hostelmanager.data.Person
import com.example.hostelmanager.data.RoomWithOccupancy
import com.example.hostelmanager.data.Stay
import com.example.hostelmanager.data.repository.ResidentRepository
import com.example.hostelmanager.data.repository.RoomRepository
import com.example.hostelmanager.domain.BillingCycle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

class ResidentViewModel(
    private val residentRepository: ResidentRepository,
    private val roomRepository: RoomRepository
) : ViewModel() {

    private val _hostelId = MutableStateFlow("")

    private val _phoneError = MutableStateFlow<String?>(null)
    val phoneError: StateFlow<String?> = _phoneError

    private val _govtIdError = MutableStateFlow<String?>(null)
    val govtIdError: StateFlow<String?> = _govtIdError

    private val _emergencyError = MutableStateFlow<String?>(null)
    val emergencyError: StateFlow<String?> = _emergencyError

    val roomsForHostel: StateFlow<List<RoomWithOccupancy>> = _hostelId.flatMapLatest { id ->
        if (id.isEmpty()) flowOf(emptyList())
        else roomRepository.getRoomsWithOccupancy(id)
    }.stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = emptyList())

    fun setHostelId(id: String) {
        _hostelId.value = id
    }

    suspend fun addResident(
        hostelId: String,
        name: String, phone: String, govtIdNumber: String, emergencyContact: String,
        dateOfBirth: String, address: String, gender: String, guardianName: String,
        roomId: String, joinDate: String, rentPaise: Long, depositPaise: Long,
        govtFrontPath: String, govtBackPath: String, profilePhotoPath: String
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
        val newStay = Stay(
            personId = newPerson.id,
            hostelId = hostelId,
            roomId = roomId,
            joinDate = joinDate,
            monthlyRent = rentPaise,
            depositCollected = depositPaise,
            billingAnchorDay = BillingCycle.anchorDayOf(joinDate),
            isActive = true
        )

        residentRepository.addResident(newPerson, newStay)

        return true
    }
}
