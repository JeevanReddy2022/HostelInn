package com.example.hostelmanager.domain.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidationTest {

    @Test
    fun `phone pattern matches valid 10-digit Indian mobile numbers starting 6-9`() {
        assertTrue(Patterns.Phone.matches("9876543210"))
        assertTrue(Patterns.Phone.matches("6123456789"))
        assertFalse(Patterns.Phone.matches("5123456789"))
        assertFalse(Patterns.Phone.matches("12345"))
    }

    @Test
    fun `IdType detects Aadhaar, Voter ID, and Driving License`() {
        assertEquals(IdType.AADHAAR, IdType.detect("234567890123"))
        assertEquals(IdType.VOTER_ID, IdType.detect("ABC1234567"))
        assertNull(IdType.detect("invalid"))
    }

    @Test
    fun `validateDate rejects invalid format or future date when notFuture is true`() {
        assertNotNull(DateRules.validateDate("2099-01-01", "Join date", notFuture = true))
        assertNull(DateRules.validateDate("2026-01-01", "Join date", notFuture = true))
    }

    @Test
    fun `validateAmount checks min and max rupees`() {
        assertNotNull(AmountRules.validateAmount("0", "Rent"))
        assertNull(AmountRules.validateAmount("5000", "Rent"))
        assertNotNull(AmountRules.validateAmount("10000", "Rent", maxRupees = 8000))
    }

    @Test
    fun `validateAmount boundary checks`() {
        assertNotNull(AmountRules.validateAmount("0", "Amount"))
        assertNull(AmountRules.validateAmount("99", "Amount"))
        assertNull(AmountRules.validateAmount("100", "Amount"))
        assertNull(AmountRules.validateAmount("150000", "Amount"))
    }

    @Test
    fun `validateResidentForm validates all fields`() {
        val validForm = ResidentFormInput(
            name = "John Doe",
            phone = "9876543210",
            govtIdNumber = "234567890123",
            emergencyContact = "8765432109",
            dateOfBirth = "2000-01-01",
            joinDate = "2026-01-01"
        )
        val errors = validateResidentForm(validForm)
        assertTrue(errors.isValid)
    }
}
