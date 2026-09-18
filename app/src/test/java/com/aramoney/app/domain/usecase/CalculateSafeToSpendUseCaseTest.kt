package com.aramoney.app.domain.usecase

import com.aramoney.app.domain.model.SafeToSpendState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class CalculateSafeToSpendUseCaseTest {

    private lateinit var useCase: CalculateSafeToSpendUseCase
    private val today = LocalDate.of(2026, 9, 11)

    @Before
    fun setUp() {
        useCase = CalculateSafeToSpendUseCase()
    }

    @Test
    fun `when daily budget is above 20000 then return State Aman`() {
        // Saldo Rp600.000 dengan sisa 10 hari -> Rp60.000/hari (> Rp20.000)
        val nextDate = today.plusDays(10)
        val result = useCase(saldoSaatIni = 600_000.0, tanggalKirimanBerikutnya = nextDate, today = today)

        assertTrue("Expected SafeToSpendState.Aman but was $result", result is SafeToSpendState.Aman)
        val aman = result as SafeToSpendState.Aman
        assertEquals(60_000.0, aman.dailyBudget, 0.01)
        assertEquals(10L, aman.remainingDays)
        assertTrue(aman.message.contains("jajan santai"))
    }

    @Test
    fun `when daily budget is between 5000 and 20000 then return State Waspada`() {
        // Saldo Rp150.000 dengan sisa 10 hari -> Rp15.000/hari (<= Rp20.000)
        val nextDate = today.plusDays(10)
        val result = useCase(saldoSaatIni = 150_000.0, tanggalKirimanBerikutnya = nextDate, today = today)

        assertTrue("Expected SafeToSpendState.Waspada but was $result", result is SafeToSpendState.Waspada)
        val waspada = result as SafeToSpendState.Waspada
        assertEquals(15_000.0, waspada.dailyBudget, 0.01)
        assertTrue(waspada.message.contains("Kurangi boba"))
    }

    @Test
    fun `when daily budget is below 5000 then return State Bahaya`() {
        // Saldo Rp30.000 dengan sisa 10 hari -> Rp3.000/hari (< Rp5.000)
        val nextDate = today.plusDays(10)
        val result = useCase(saldoSaatIni = 30_000.0, tanggalKirimanBerikutnya = nextDate, today = today)

        assertTrue("Expected SafeToSpendState.Bahaya but was $result", result is SafeToSpendState.Bahaya)
        val bahaya = result as SafeToSpendState.Bahaya
        assertEquals(3_000.0, bahaya.dailyBudget, 0.01)
        assertTrue(bahaya.message.contains("dompet menipis"))
    }

    @Test
    fun `when balance is negative then return State Bahaya immediately`() {
        val nextDate = today.plusDays(5)
        val result = useCase(saldoSaatIni = -50_000.0, tanggalKirimanBerikutnya = nextDate, today = today)

        assertTrue(result is SafeToSpendState.Bahaya)
        val bahaya = result as SafeToSpendState.Bahaya
        assertEquals(0.0, bahaya.dailyBudget, 0.01)
        assertEquals(-50_000.0, bahaya.totalBalance, 0.01)
    }

    @Test
    fun `when allowance date is null then return State NeedsSetup`() {
        val result = useCase(saldoSaatIni = 500_000.0, tanggalKirimanBerikutnya = null, today = today)

        assertTrue(result is SafeToSpendState.NeedsSetup)
    }

    @Test
    fun `when allowance date has already passed then return State NeedsDateUpdate`() {
        val pastDate = today.minusDays(2)
        val result = useCase(saldoSaatIni = 500_000.0, tanggalKirimanBerikutnya = pastDate, today = today)

        assertTrue(result is SafeToSpendState.NeedsDateUpdate)
    }

    @Test
    fun `when balance is exactly zero then return State EmptyBalance`() {
        val nextDate = today.plusDays(7)
        val result = useCase(saldoSaatIni = 0.0, tanggalKirimanBerikutnya = nextDate, today = today)

        assertTrue(result is SafeToSpendState.EmptyBalance)
    }

    @Test
    fun `when today is allowance date then coerce remaining days to at least 1`() {
        val result = useCase(saldoSaatIni = 50_000.0, tanggalKirimanBerikutnya = today, today = today)

        assertTrue(result is SafeToSpendState.Aman)
        val aman = result as SafeToSpendState.Aman
        assertEquals(1L, aman.remainingDays)
        assertEquals(50_000.0, aman.dailyBudget, 0.01)
    }
}
