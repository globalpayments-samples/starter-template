package com.globalpayments.android.sample

import com.globalpayments.android.sample.utils.ValidationUtils
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for ValidationUtils
 * These tests verify the validation logic matches other language implementations
 */
class ValidationUtilsTest {

    @Test
    fun sanitizePostalCode_validInput_returnsCleanedInput() {
        assertEquals("12345", ValidationUtils.sanitizePostalCode("12345"))
        assertEquals("K1A0A6", ValidationUtils.sanitizePostalCode("K1A 0A6"))
        assertEquals("12345-6789", ValidationUtils.sanitizePostalCode("12345-6789"))
    }

    @Test
    fun sanitizePostalCode_invalidCharacters_removesInvalidChars() {
        assertEquals("12345", ValidationUtils.sanitizePostalCode("1@2#3$4%5"))
        assertEquals("ABC123", ValidationUtils.sanitizePostalCode("A!B@C#1$2%3"))
        assertEquals("", ValidationUtils.sanitizePostalCode("!@#$%^&*()"))
    }

    @Test
    fun sanitizePostalCode_tooLong_truncatesTo10Chars() {
        assertEquals("1234567890", ValidationUtils.sanitizePostalCode("12345678901234567890"))
        assertEquals("ABCDEFGHIJ", ValidationUtils.sanitizePostalCode("ABCDEFGHIJKLMNOPQRSTUVWXYZ"))
    }

    @Test
    fun sanitizePostalCode_nullOrEmpty_returnsEmpty() {
        assertEquals("", ValidationUtils.sanitizePostalCode(null))
        assertEquals("", ValidationUtils.sanitizePostalCode(""))
        assertEquals("", ValidationUtils.sanitizePostalCode("   "))
    }

    @Test
    fun isValidPostalCode_validCodes_returnsTrue() {
        assertTrue(ValidationUtils.isValidPostalCode("12345"))
        assertTrue(ValidationUtils.isValidPostalCode("K1A0A6"))
        assertTrue(ValidationUtils.isValidPostalCode("12345-6789"))
        assertTrue(ValidationUtils.isValidPostalCode("ABC-123"))
    }

    @Test
    fun isValidPostalCode_invalidCodes_returnsFalse() {
        assertFalse(ValidationUtils.isValidPostalCode(null))
        assertFalse(ValidationUtils.isValidPostalCode(""))
        assertFalse(ValidationUtils.isValidPostalCode("   "))
        assertFalse(ValidationUtils.isValidPostalCode("12345678901")) // too long
        assertFalse(ValidationUtils.isValidPostalCode("123@45")) // invalid chars
    }

    @Test
    fun isValidAmount_validAmounts_returnsTrue() {
        assertTrue(ValidationUtils.isValidAmount(0.01))
        assertTrue(ValidationUtils.isValidAmount(10.00))
        assertTrue(ValidationUtils.isValidAmount(999999.99))
        assertTrue(ValidationUtils.isValidAmount(1.5))
    }

    @Test
    fun isValidAmount_invalidAmounts_returnsFalse() {
        assertFalse(ValidationUtils.isValidAmount(null))
        assertFalse(ValidationUtils.isValidAmount(0.0))
        assertFalse(ValidationUtils.isValidAmount(-1.0))
        assertFalse(ValidationUtils.isValidAmount(1000000.0)) // too large
    }

    @Test
    fun isValidAmountString_validStrings_returnsTrue() {
        assertTrue(ValidationUtils.isValidAmountString("0.01"))
        assertTrue(ValidationUtils.isValidAmountString("10.00"))
        assertTrue(ValidationUtils.isValidAmountString("999999.99"))
        assertTrue(ValidationUtils.isValidAmountString("1.5"))
    }

    @Test
    fun isValidAmountString_invalidStrings_returnsFalse() {
        assertFalse(ValidationUtils.isValidAmountString(null))
        assertFalse(ValidationUtils.isValidAmountString(""))
        assertFalse(ValidationUtils.isValidAmountString("abc"))
        assertFalse(ValidationUtils.isValidAmountString("0"))
        assertFalse(ValidationUtils.isValidAmountString("-10"))
        assertFalse(ValidationUtils.isValidAmountString("1000000"))
    }

    @Test
    fun isValidEmail_validEmails_returnsTrue() {
        assertTrue(ValidationUtils.isValidEmail("test@example.com"))
        assertTrue(ValidationUtils.isValidEmail("user.name@domain.co.uk"))
        assertTrue(ValidationUtils.isValidEmail("user+tag@example.org"))
    }

    @Test
    fun isValidEmail_invalidEmails_returnsFalse() {
        assertFalse(ValidationUtils.isValidEmail(null))
        assertFalse(ValidationUtils.isValidEmail(""))
        assertFalse(ValidationUtils.isValidEmail("invalid"))
        assertFalse(ValidationUtils.isValidEmail("@example.com"))
        assertFalse(ValidationUtils.isValidEmail("user@"))
        assertFalse(ValidationUtils.isValidEmail("user@.com"))
    }

    @Test
    fun sanitizeString_validInput_trimmedAndLimited() {
        assertEquals("hello", ValidationUtils.sanitizeString("  hello  "))
        assertEquals("test", ValidationUtils.sanitizeString("test", 10))
        
        val longString = "a".repeat(300)
        val sanitized = ValidationUtils.sanitizeString(longString, 255)
        assertEquals(255, sanitized.length)
    }

    @Test
    fun sanitizeString_nullOrEmpty_returnsEmpty() {
        assertEquals("", ValidationUtils.sanitizeString(null))
        assertEquals("", ValidationUtils.sanitizeString(""))
        assertEquals("", ValidationUtils.sanitizeString("   "))
    }
}