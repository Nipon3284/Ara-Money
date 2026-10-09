package com.aramoney.app.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AmountInputTest {

    @Test
    fun `append ignores leading zeros`() {
        assertEquals("", AmountInput.append("", "0"))
        assertEquals("", AmountInput.append("", "000"))
        assertEquals("5", AmountInput.append("", "5"))
        assertEquals("50", AmountInput.append("5", "0"))
    }

    @Test
    fun `append triple zero respects max digits`() {
        assertEquals("1000", AmountInput.append("1", "000"))
        // 7 digit + "000" = 10 digit > MAX_DIGITS (9) -> ditolak
        assertEquals("1234567", AmountInput.append("1234567", "000"))
        assertEquals("123456000", AmountInput.append("123456", "000"))
    }

    @Test
    fun `append single digit stops at max digits`() {
        val nine = "123456789"
        assertEquals(nine, AmountInput.append(nine, "1"))
    }

    @Test
    fun `backspace removes last digit`() {
        assertEquals("12", AmountInput.backspace("123"))
        assertEquals("", AmountInput.backspace(""))
    }

    @Test
    fun `quick add never exceeds max amount`() {
        assertEquals("10000", AmountInput.addQuick("", 10_000))
        assertEquals("60000", AmountInput.addQuick("10000", 50_000))
        assertEquals("99950000", AmountInput.addQuick("99950000", 100_000))
    }

    @Test
    fun `sanitize keeps digits only and strips leading zeros`() {
        assertEquals("25000", AmountInput.sanitize("Rp 25.000"))
        assertEquals("5", AmountInput.sanitize("005"))
        assertEquals("123456789", AmountInput.sanitize("1234567890123"))
    }

    @Test
    fun `validation flags amounts over the limit`() {
        assertNull(AmountInput.validationMessage("100000000"))
        assertNotNull(AmountInput.validationMessage("100000001"))
        assertTrue(AmountInput.isOverLimit("999999999"))
        assertFalse(AmountInput.isOverLimit(""))
    }
}
