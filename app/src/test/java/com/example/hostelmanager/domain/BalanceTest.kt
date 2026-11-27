package com.example.hostelmanager.domain

import com.example.hostelmanager.data.Payment
import com.example.hostelmanager.data.Stay
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class BalanceTest {

    private fun date(value: String) = LocalDate.parse(value)

    private fun monthlyStay(join: String = "2026-01-10", rent: Long = 500_000) = Stay(
        id = "s1", personId = "p1", hostelId = "h1", roomId = "r1",
        joinDate = join, monthlyRent = rent, billingAnchorDay = 10, isActive = true
    )

    private fun dailyStay(join: String = "2026-03-01", rate: Long = 50_000) = Stay(
        id = "s2", personId = "p1", hostelId = "h1", roomId = "r1",
        joinDate = join, dailyRent = rate, billingAnchorDay = 1, isActive = true
    )

    private fun payment(amount: Long, waived: Long = 0) = Payment(
        id = "pay${amount}_${waived}", stayId = "s1", personId = "p1", hostelId = "h1",
        amountPaise = amount, mode = "Cash", paymentDate = "2026-01-15", date = "2026-01-15",
        waivedPaise = waived
    )

    @Test
    fun `monthly stay owes one cycle inside the join cycle`() {
        assertEquals(1, Balance.cyclesOwed("2026-01-10", date("2026-01-20"), 10))
        assertEquals(500_000, Balance.owedPaise(monthlyStay(), date("2026-01-20")))
    }

    @Test
    fun `monthly arrears carry over across cycles`() {
        // Join cycle (Jan) + Feb cycle = 2 cycles as of 5 Mar
        assertEquals(2, Balance.cyclesOwed("2026-01-10", date("2026-03-05"), 10))
        assertEquals(1_000_000, Balance.owedPaise(monthlyStay(), date("2026-03-05")))
        // On 10 Mar the third cycle starts
        assertEquals(3, Balance.cyclesOwed("2026-01-10", date("2026-03-10"), 10))
    }

    @Test
    fun `anchor clamping in short months does not miscount`() {
        // Join 31 Jan, anchor 31: cycles are Jan31, Feb28, Mar31
        val stay = monthlyStay(join = "2026-01-31")
        assertEquals(2, Balance.cyclesOwed("2026-01-31", date("2026-03-05"), 31))
        assertEquals(1_000_000, Balance.owedPaise(stay, date("2026-03-05")))
    }

    @Test
    fun `daily stay owes per day inclusive`() {
        assertEquals(3, Balance.daysStayed("2026-03-01", date("2026-03-03")))
        assertEquals(150_000, Balance.owedPaise(dailyStay(), date("2026-03-03")))
    }

    @Test
    fun `partial payment leaves a balance`() {
        val stay = monthlyStay()
        val balance = Balance.balancePaise(stay, listOf(payment(300_000)), date("2026-01-20"))
        assertEquals(200_000, balance)
    }

    @Test
    fun `advance payment becomes credit`() {
        val stay = monthlyStay()
        val balance = Balance.balancePaise(stay, listOf(payment(600_000)), date("2026-01-20"))
        assertEquals(-100_000, balance)
    }

    @Test
    fun `write-off cancels the balance`() {
        val stay = monthlyStay()
        val writeOff = payment(amount = 0, waived = 500_000)
        val balance = Balance.balancePaise(stay, listOf(writeOff), date("2026-01-20"))
        assertEquals(0, balance)
    }

    @Test
    fun `checked-out stay is evaluated at checkout date not today`() {
        val stay = monthlyStay().copy(checkoutDate = "2026-02-15", isActive = false)
        val ref = Balance.referenceDateFor(stay, date("2026-11-28"))
        assertEquals(date("2026-02-15"), ref)
        // Jan cycle + Feb cycle = 2
        assertEquals(1_000_000, Balance.owedPaise(stay, ref))
    }
}