package com.aramoney.app.domain.usecase

import com.aramoney.app.domain.model.SafeToSpendState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class CalculateSafeToSpendUseCaseTest {

    private lateinit var useCase: CalculateSafeToSpendUseCase
    private val today = LocalDate.of(2026, 9, 21)

    @Before
    fun setUp() {
        useCase = CalculateSafeToSpendUseCase()
    }

    @Test
    fun `when runway is 7 days or more and expense is controlled then return State Aman`() {
        // Saldo Rp600.000 dengan target harian Rp30.000 -> 20 hari (>= 7 hari), belanja rata-rata Rp20.000
        val result = useCase(
            saldoSaatIni = 600_000.0,
            dailyTargetBudget = 30_000.0,
            dailyAverageExpense = 20_000.0,
            todayExpense = 10_000.0
        )

        assertTrue("Expected SafeToSpendState.Aman but was $result", result is SafeToSpendState.Aman)
        val aman = result as SafeToSpendState.Aman
        assertEquals(20_000.0, aman.dailyAverageExpense, 0.01)
        assertEquals(20_000.0, aman.remainingTodayBudget, 0.01) // 30k - 10k
        assertEquals(20L, aman.remainingDays)
        assertTrue(aman.message.contains("lebih hemat"))
    }

    @Test
    fun `when daily average expense exceeds target then return State Waspada`() {
        // Saldo Rp600.000 (runway cukup), tapi belanja rata-rata Rp45.000 (> target Rp30.000)
        val result = useCase(
            saldoSaatIni = 600_000.0,
            dailyTargetBudget = 30_000.0,
            dailyAverageExpense = 45_000.0,
            todayExpense = 50_000.0
        )

        assertTrue("Expected SafeToSpendState.Waspada but was $result", result is SafeToSpendState.Waspada)
        val waspada = result as SafeToSpendState.Waspada
        assertEquals(45_000.0, waspada.dailyAverageExpense, 0.01)
        assertEquals(0.0, waspada.remainingTodayBudget, 0.01)
        assertTrue(waspada.message.contains("melampaui target"))
    }

    @Test
    fun `when runway is between 3 and 6 days then return State Waspada`() {
        // Saldo Rp150.000 dengan target harian Rp30.000 -> 5 hari (3..6 hari)
        val result = useCase(saldoSaatIni = 150_000.0, dailyTargetBudget = 30_000.0)

        assertTrue("Expected SafeToSpendState.Waspada but was $result", result is SafeToSpendState.Waspada)
        val waspada = result as SafeToSpendState.Waspada
        assertEquals(30_000.0, waspada.dailyBudget, 0.01)
        assertEquals(5L, waspada.remainingDays)
        assertTrue(waspada.message.contains("Pelan-pelan"))
    }

    @Test
    fun `when runway is below 3 days then return State Bahaya`() {
        // Saldo Rp50.000 dengan target harian Rp30.000 -> 1 hari (< 3 hari)
        val result = useCase(saldoSaatIni = 50_000.0, dailyTargetBudget = 30_000.0)

        assertTrue("Expected SafeToSpendState.Bahaya but was $result", result is SafeToSpendState.Bahaya)
        val bahaya = result as SafeToSpendState.Bahaya
        assertEquals(30_000.0, bahaya.dailyBudget, 0.01)
        assertEquals(1L, bahaya.remainingDays)
        assertTrue(bahaya.message.contains("dompet menipis"))
    }

    @Test
    fun `when balance is less than daily target then dailyBudget is capped to balance`() {
        // Saldo Rp15.000 dengan target harian Rp30.000 -> 0 hari (< 1 hari), budget capped to 15.000
        val result = useCase(saldoSaatIni = 15_000.0, dailyTargetBudget = 30_000.0)

        assertTrue(result is SafeToSpendState.Bahaya)
        val bahaya = result as SafeToSpendState.Bahaya
        assertEquals(15_000.0, bahaya.dailyBudget, 0.01)
        assertEquals(0L, bahaya.remainingDays)
    }

    @Test
    fun `when balance is negative then return State Bahaya with zero daily budget`() {
        val result = useCase(saldoSaatIni = -50_000.0, dailyTargetBudget = 30_000.0)

        assertTrue(result is SafeToSpendState.Bahaya)
        val bahaya = result as SafeToSpendState.Bahaya
        assertEquals(0.0, bahaya.dailyBudget, 0.01)
        assertEquals(-50_000.0, bahaya.totalBalance, 0.01)
        assertTrue(bahaya.message.contains("dompet minus"))
    }

    @Test
    fun `when balance is exactly zero then return State EmptyBalance`() {
        val result = useCase(saldoSaatIni = 0.0, dailyTargetBudget = 30_000.0)

        assertTrue(result is SafeToSpendState.EmptyBalance)
    }

    @Test
    fun `when daily target is zero or negative then fallback to default target`() {
        // Saldo Rp300.000, target 0 -> fallback to 30.000 -> 10 hari -> Aman
        val result = useCase(saldoSaatIni = 300_000.0, dailyTargetBudget = 0.0)

        assertTrue(result is SafeToSpendState.Aman)
        val aman = result as SafeToSpendState.Aman
        assertEquals(30_000.0, aman.dailyBudget, 0.01)
        assertEquals(10L, aman.remainingDays)
    }
}
