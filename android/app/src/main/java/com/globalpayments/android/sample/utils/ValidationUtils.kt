package com.globalpayments.android.sample.utils

import java.util.regex.Pattern

/**
 * ValidationUtils - Input Validation Utilities
 * 
 * This utility class provides validation functions that match the validation
 * logic used in other language implementations of the starter template.
 */
object ValidationUtils {
    
    // Postal code pattern: allows alphanumeric characters and hyphens
    private val POSTAL_CODE_PATTERN = Pattern.compile("^[a-zA-Z0-9-]{1,10}$")
    
    // Email pattern for basic email validation
    private val EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    )
    
    /**
     * Sanitizes postal code input by removing invalid characters.
     * This follows the same sanitization logic as other language implementations.
     * 
     * @param postalCode The postal code to sanitize
     * @return Sanitized postal code containing only alphanumeric characters and hyphens,
     *         limited to 10 characters
     */
    fun sanitizePostalCode(postalCode: String?): String {
        if (postalCode.isNullOrEmpty()) {
            return ""
        }
        
        // Remove any characters that aren't alphanumeric or hyphen
        val sanitized = postalCode.replace(Regex("[^a-zA-Z0-9-]"), "")
        
        // Limit length to 10 characters
        return if (sanitized.length > 10) {
            sanitized.substring(0, 10)
        } else {
            sanitized
        }
    }
    
    /**
     * Validates if a postal code is in the correct format.
     * 
     * @param postalCode The postal code to validate
     * @return true if the postal code is valid, false otherwise
     */
    fun isValidPostalCode(postalCode: String?): Boolean {
        if (postalCode.isNullOrEmpty()) {
            return false
        }
        
        val sanitized = sanitizePostalCode(postalCode)
        return POSTAL_CODE_PATTERN.matcher(sanitized).matches()
    }
    
    /**
     * Validates if an email address is in the correct format.
     * 
     * @param email The email address to validate
     * @return true if the email is valid, false otherwise
     */
    fun isValidEmail(email: String?): Boolean {
        if (email.isNullOrEmpty()) {
            return false
        }
        
        return EMAIL_PATTERN.matcher(email.trim()).matches()
    }
    
    /**
     * Validates if an amount is valid for payment processing.
     * 
     * @param amount The amount to validate
     * @return true if the amount is valid, false otherwise
     */
    fun isValidAmount(amount: Double?): Boolean {
        return amount != null && amount > 0 && amount <= 999999.99
    }
    
    /**
     * Validates if an amount string is valid for payment processing.
     * 
     * @param amountString The amount string to validate
     * @return true if the amount string is valid, false otherwise
     */
    fun isValidAmountString(amountString: String?): Boolean {
        if (amountString.isNullOrEmpty()) {
            return false
        }
        
        return try {
            val amount = amountString.toDouble()
            isValidAmount(amount)
        } catch (e: NumberFormatException) {
            false
        }
    }
    
    /**
     * Sanitizes a string input by trimming whitespace and limiting length.
     * 
     * @param input The input string to sanitize
     * @param maxLength Maximum allowed length
     * @return Sanitized string
     */
    fun sanitizeString(input: String?, maxLength: Int = 255): String {
        if (input.isNullOrEmpty()) {
            return ""
        }
        
        val trimmed = input.trim()
        return if (trimmed.length > maxLength) {
            trimmed.substring(0, maxLength)
        } else {
            trimmed
        }
    }
}