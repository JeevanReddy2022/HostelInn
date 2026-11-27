package com.example.hostelmanager.ui.residents

import com.example.hostelmanager.data.Stay
import com.example.hostelmanager.testutil.FakePaymentRepository
import com.example.hostelmanager.testutil.FakeResidentRepository
import com.example.hostelmanager.testutil.FakeRoomRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CheckoutViewModelTest {

    private fun dailyStay() = Stay(
        id = "s1", personId = "p1", hostelId = "h1", roomId = "r1",
        joinDate = "2026-02-01", monthlyRent = 0, dailyRent = 50000,
        depositCollected = 0, billingAnchorDay = 1, isActive = true
    )

    private fun monthlyStay() = Stay(
        id = "s1", personId = "p1", hostelId = "h1", roomId = "r1",
        joinDate = "2026-02-01", monthlyRent = 500000,
        depositCollected = 1000000, billingAnchorDay = 1, isActive = true
    )

    @Test
    fun `checkoutResident fails if checkout date is before join date`() = runTest {
        val residentRepo = FakeResidentRepository()
        val roomRepo = FakeRoomRepository()
        val paymentRepo = FakePaymentRepository()
        residentRepo.activeStay = monthlyStay()
        val viewModel = CheckoutViewModel(residentRepo, roomRepo, paymentRepo)
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
    fun `checkout blocked when daily guest has unpaid balance`() = runTest {
        val residentRepo = FakeResidentRepository()
        val roomRepo = FakeRoomRepository()
        val paymentRepo = FakePaymentRepository()
        residentRepo.activeStay = dailyStay()
        val viewModel = CheckoutViewModel(residentRepo, roomRepo, paymentRepo)
        viewModel.setPersonId("p1")
        val success = viewModel.checkoutResident(
            checkoutDate = "2026-02-03",
            refundPaise = 0,
            refundMode = null,
            refundDate = null
        )
        assertFalse(success)
        assertNotNull(viewModel.checkoutError.value)
    }

    @Test
    fun `checkout allowed when daily guest fully paid`() = runTest {
        val residentRepo = FakeResidentRepository()
        val roomRepo = FakeRoomRepository()
        val paymentRepo = FakePaymentRepository()
        paymentRepo.totalPaidOnce = 150000L // 3 days x 500
        residentRepo.activeStay = dailyStay()
        val viewModel = CheckoutViewModel(residentRepo, roomRepo, paymentRepo)
        viewModel.setPersonId("p1")
        val success = viewModel.checkoutResident(
            checkoutDate = "2026-02-03",
            refundPaise = 0,
            refundMode = null,
            refundDate = null
        )
        assertTrue(success)
    }

    @Test
    fun `checkout allowed with write-off despite unpaid balance`() = runTest {
        val residentRepo = FakeResidentRepository()
        val roomRepo = FakeRoomRepository()
        val paymentRepo = FakePaymentRepository()
        paymentRepo.writeOffCount = 1
        residentRepo.activeStay = dailyStay()
        val viewModel = CheckoutViewModel(residentRepo, roomRepo, paymentRepo)
        viewModel.setPersonId("p1")
        val success = viewModel.checkoutResident(
            checkoutDate = "2026-02-03",
            refundPaise = 0,
            refundMode = null,
            refundDate = null
        )
        assertTrue(success)
    }
}