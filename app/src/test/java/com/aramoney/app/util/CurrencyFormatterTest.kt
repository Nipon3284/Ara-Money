package com.aramoney.app.util

import org.junit.Assert.assertEquals
import org.junit.Test

class CurrencyFormatterTest {

    @Test
    fun `formats rupiah without decimals using id-ID grouping`() {
        assertEquals("Rp0", CurrencyFormatter.formatRupiah(0.0))
        assertEquals("Rp35.000", CurrencyFormatter.formatRupiah(35_000.0))
        assertEquals("Rp1.250.000", CurrencyFormatter.formatRupiah(1_250_000.0))
    }

    @Test
    fun `negative amounts put minus sign before Rp`() {
        assertEquals("${CurrencyFormatter.MINUS}Rp5.000", CurrencyFormatter.formatRupiah(-5_000.0))
    }

    @Test
    fun `signed format uses plus for income and minus for expense`() {
        assertEquals("+Rp10.000", CurrencyFormatter.formatSigned(10_000.0, isIncome = true))
        assertEquals("${CurrencyFormatter.MINUS}Rp10.000", CurrencyFormatter.formatSigned(10_000.0, isIncome = false))
    }

    @Test
    fun `percent uses indonesian decimal comma`() {
        assertEquals("12,5%", CurrencyFormatter.formatPercent(12.5f))
        assertEquals("40%", CurrencyFormatter.formatPercent(40f))
        assertEquals("13%", CurrencyFormatter.formatPercent(12.7f, 0))
    }
}
