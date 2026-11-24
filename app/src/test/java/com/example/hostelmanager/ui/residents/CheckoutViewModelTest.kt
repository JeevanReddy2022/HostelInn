package com.example.hostelmanager.ui.residents

import com.example.hostelmanager.data.Stay
import com.example.hostelmanager.testutil.FakeResidentRepository
import com.example.hostelmanager.testutil.FakeRoomRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CheckoutViewModelTest {

    @Test
    fun `checkoutResident fails if checkout date is before join date`() = runTest {
        val residentRepo = FakeResidentRepository()
        val roomRepo = FakeRoomRepository()

        residentRepo.activeStay = Stay(
            id = "s1",
            personId = "p1",
            hostelId = "h1",
            roomId = "r1",
            joinDate = "2026-02-01",
            monthlyRent = 500000,
            depositCollected = 1000000,
            billingAnchorDay = 1,
            isActive = true
        )

        val viewModel = CheckoutViewModel(residentRepo, roomRepo)
        viewModel.setPersonId("p1")

        val success = viewModel.checkoutResident(
            checkoutDate = "2026-01-15",
            refundPaise = 1000000,
            refundMode = "Cash",
            refundDate = "2026-01-15"
        )

        assertFalse(success)
        assertNotNull(viewModel.checkoutError.value)
    }

    @Test
    fun `checkoutResident succeeds with valid date and refund`() = runTest {
        val residentRepo = FakeResidentRepository()
        val roomRepo = FakeRoomRepository()

        residentRepo.activeStay = Stay(
            id = "s1",
            personId = "p1",
            hostelId = "h1",
            roomId = "r1",
            joinDate = "2026-02-01",
            monthlyRent = 500000,
            depositCollected = 1000000,
            billingAnchorDay = 1,
            isActive = true
        )

        val viewModel = CheckoutViewModel(residentRepo, roomRepo)
        viewModel.setPersonId("p1")

        val success = viewModel.checkoutResident(
            checkoutDate = "2026-03-01",
            refundPaise = 1000000,
            refundMode = "Cash",
            refundDate = "2026-03-01"
        )

        assertTrue(success)
    }
}
