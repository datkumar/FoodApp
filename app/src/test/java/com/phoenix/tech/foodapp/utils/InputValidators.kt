package com.phoenix.tech.foodapp.utils

import com.phoenix.tech.foodapp.utils.InputValidators.EmailErrorMsg
import com.phoenix.tech.foodapp.utils.InputValidators.PswdErrorMsg

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test


class InputValidatorsTest {

    // ========================================================================
    // Email Validation Tests
    // ========================================================================
    @Test
    fun `Email containing spaces give error`() {
        val result = InputValidators.getEmailError("abc @gmail.com")
        assertEquals(EmailErrorMsg.NO_SPACES, result)
    }

    @Test
    fun `Email starting with number give error`() {
        val result = InputValidators.getEmailError("1abc@gmail.com")
        assertEquals(EmailErrorMsg.START_WITH_ALPHA, result)
    }

    @Test
    fun `Email without @ symbol gives error`() {
        val result = InputValidators.getEmailError("abcdegmail.com")
        assertEquals(EmailErrorMsg.CONTAIN_AT_SYMBOL, result)
    }

    @Test
    fun `Email without dot symbol gives error`() {
        val result = InputValidators.getEmailError("abcd@gmailcom")
        assertEquals(EmailErrorMsg.CONTAIN_DOT_SYMBOL, result)
    }

    @Test
    fun `Email with short prefix gives error`() {
        val result = InputValidators.getEmailError("abc@gmail.com")
        assertEquals(EmailErrorMsg.PREFIX_IS_SHORT, result)
    }

    @Test
    fun `Malformed email value gives Invalid format error`() {
        val result = InputValidators.getEmailError("abcd@gmail...com")
        assertEquals(EmailErrorMsg.INVALID_FORMAT, result)
    }

    @Test
    fun `Empty email value returns null`() {
        assertNull(InputValidators.getEmailError(""))
    }

    @Test
    fun `Valid email value returns null`() {
        assertNull(InputValidators.getEmailError("abcd@phoenix.tech"))
        assertNull(InputValidators.getEmailError("john.doe123@gmail.com"))
    }

    // ADVANCED EMAIL CASES:

    @Test
    fun `Valid complex subdomains and hyphens returns null`() {
        // Multi-level subdomains and hyphens are perfectly valid domain structures
        assertNull(InputValidators.getEmailError("kdeshmukh@support.phoenix-tech.co.in"))
        assertNull(InputValidators.getEmailError("alex@internal.machinery.hospital"))
    }

    @Test
    fun `Valid separate dots in prefix returns null`() {
        // Dots are allowed in the prefix as long as they don't violate other rules
        assertNull(InputValidators.getEmailError("k.deshmukh.developer@gmail.com"))
    }

    @Test
    fun `Single letter TLD gives invalid format error`() {
        // TLDs must be at least 2 characters (e.g., .co, .com, .in)
        val result = InputValidators.getEmailError("abcd@gmail.c")
        assertEquals(EmailErrorMsg.INVALID_FORMAT, result)
    }

    @Test
    fun `Numeric TLD gives invalid format error`() {
        // TLDs cannot contain numbers
        val result = InputValidators.getEmailError("abcd@gmail.c0m")
        assertEquals(EmailErrorMsg.INVALID_FORMAT, result)
    }

    @Test
    fun `Consecutive dots in prefix gives invalid format error`() {
        // Real-world systems reject consecutive dots like "ab..cd"
        val result = InputValidators.getEmailError("ab..cd@gmail.com")
        assertEquals(EmailErrorMsg.INVALID_FORMAT, result)
    }

    @Test
    fun `Trailing dot in prefix returns invalid format error`() {
        // An email cannot have a dot right up against the @ symbol
        val result = InputValidators.getEmailError("abcd.@gmail.com")
        assertEquals(EmailErrorMsg.INVALID_FORMAT, result)
    }


    // ========================================================================
    // Password Validation Tests
    // ========================================================================
    @Test
    fun `Password containing spaces gives error`() {
        val result = InputValidators.getPswdError("Pass 123!")
        assertEquals(PswdErrorMsg.NO_SPACES, result)
    }

    @Test
    fun `Short password returns minimum character error`() {
        val result = InputValidators.getPswdError("P1s!")
        assertEquals(PswdErrorMsg.TOO_SHORT, result)
    }

    @Test
    fun `Password without lowercase character returns error`() {
        val result = InputValidators.getPswdError("PASSWORD123!")
        assertEquals(PswdErrorMsg.LOWERCASE_CHAR, result)
    }

    @Test
    fun `Password without UPPERCASE character returns error`() {
        val result = InputValidators.getPswdError("password123!")
        assertEquals(PswdErrorMsg.UPPERCASE_CHAR, result)
    }

    @Test
    fun `Password without Special character returns error`() {
        val result = InputValidators.getPswdError("Password123")
        assertEquals(PswdErrorMsg.SPECIAL_CHAR, result)
    }

    @Test
    fun `Password without numeric character returns error`() {
        val result = InputValidators.getPswdError("Password!")
        assertEquals(PswdErrorMsg.NUMBER_CHAR, result)
    }

    @Test
    fun `Empty password value returns null`() {
        assertNull(InputValidators.getPswdError(""))
    }

    @Test
    fun `Valid password returns null`() {
        assertNull(InputValidators.getPswdError("SecureP@ss123"))
    }
}