package com.example.hostelmanager.ui.residents

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.hostelmanager.data.Person
import com.example.hostelmanager.data.repository.ResidentRepository
import com.example.hostelmanager.util.ImageStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

class EditResidentViewModel(
    application: Application,
    private val residentRepository: ResidentRepository
) : AndroidViewModel(application) {

    private val _personId = MutableStateFlow("")

    val person: StateFlow<Person?> = _personId.flatMapLatest { id ->
        if (id.isEmpty()) flowOf(null) else residentRepository.getPersonByIdFlow(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _phoneError = MutableStateFlow<String?>(null)
    val phoneError: StateFlow<String?> = _phoneError

    private val _govtIdError = MutableStateFlow<String?>(null)
    val govtIdError: StateFlow<String?> = _govtIdError

    private val _emergencyError = MutableStateFlow<String?>(null)
    val emergencyError: StateFlow<String?> = _emergencyError

    fun setPersonId(id: String) {
        _personId.value = id
    }

    suspend fun updateResident(
        name: String, phone: String, govtIdNumber: String, emergencyContact: String,
        dateOfBirth: String, address: String, gender: String, guardianName: String,
        govtFrontPath: String, govtBackPath: String, profilePhotoPath: String
    ): Boolean {
        val current = person.value ?: return false
        val personId = current.id

        _phoneError.value = null
        _govtIdError.value = null
        _emergencyError.value = null

        var hasDuplicates = false

        if (residentRepository.isPhoneDuplicate(phone, personId)) {
            _phoneError.value = "This phone number is already in use!"
            hasDuplicates = true
        }
        if (residentRepository.isGovtIdDuplicate(govtIdNumber, personId)) {
            _govtIdError.value = "This Govt ID is already in use!"
            hasDuplicates = true
        }
        if (residentRepository.isEmergencyDuplicate(emergencyContact, personId)) {
            _emergencyError.value = "This emergency contact is already in use!"
            hasDuplicates = true
        }

        if (hasDuplicates) return false

        residentRepository.updatePerson(
            current.copy(
                name = name,
                phone = phone,
                govtIdNumber = govtIdNumber,
                emergencyContact = emergencyContact,
                dateOfBirth = dateOfBirth,
                address = address,
                gender = gender,
                guardianName = guardianName,
                govtIdPhotoPath = govtFrontPath,
                govtIdBackPath = govtBackPath,
                profilePhotoPath = profilePhotoPath,
                updatedAt = System.currentTimeMillis()
            )
        )

        // Delete unreferenced previous images
        val previousImages = setOf(
            current.govtIdPhotoPath,
            current.govtIdBackPath,
            current.profilePhotoPath
        )
        val currentImages = setOf(govtFrontPath, govtBackPath, profilePhotoPath)
        previousImages.minus(currentImages).forEach { path ->
            ImageStore.deleteImage(getApplication(), path)
        }

        return true
    }
}
