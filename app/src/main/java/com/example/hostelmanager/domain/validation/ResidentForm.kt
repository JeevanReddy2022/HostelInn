package com.example.hostelmanager.domain.validation

data class ResidentFormInput(
    val name: String,
    val phone: String,
    val govtIdNumber: String,
    val emergencyContact: String,
    val dateOfBirth: String = "",
    val joinDate: String = "",
    val isNewResident: Boolean = true
)

data class ResidentFormErrors(
    val name: String? = null,
    val phone: String? = null,
    val govtId: String? = null,
    val emergency: String? = null,
    val dob: String? = null,
    val joinDate: String? = null
) {
    val isValid: Boolean get() = listOf(name, phone, govtId, emergency, dob, joinDate).all { it == null }
}

fun validateResidentForm(input: ResidentFormInput): ResidentFormErrors {
    val nameErr = if (input.name.trim().isEmpty()) "Full name is required." else null
    val phoneErr = if (!Patterns.Phone.matches(input.phone.trim())) "Enter a valid 10-digit phone number starting with 6-9." else null
    val govtErr = IdType.validateGovtId(input.govtIdNumber)
    val emergencyErr = if (!Patterns.Phone.matches(input.emergencyContact.trim())) "Enter a valid 10-digit emergency phone number." else null
    val dobErr = if (input.dateOfBirth.isNotBlank()) DateRules.validateDate(input.dateOfBirth, "Date of birth", notFuture = true) else null
    val joinErr = if (input.isNewResident && input.joinDate.isNotBlank()) DateRules.validateDate(input.joinDate, "Join date", notFuture = true) else null

    return ResidentFormErrors(
        name = nameErr,
        phone = phoneErr,
        govtId = govtErr,
        emergency = emergencyErr,
        dob = dobErr,
        joinDate = joinErr
    )
}
