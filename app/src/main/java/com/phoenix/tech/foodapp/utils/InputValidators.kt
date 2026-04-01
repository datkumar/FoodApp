package com.phoenix.tech.foodapp.utils

object InputValidators {
    private val hasLower = "[a-z]".toRegex()
    private val hasUpper = "[A-Z]".toRegex()
    private val hasNumber = "[0-9]".toRegex()

    // Non-alphanumeric characters as special
    private val hasSpecial = "[^A-Za-z0-9]".toRegex()

    private val emailRegex = "^[A-Za-z][A-Za-z0-9.]{3,}@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()

    fun getEmailError(email: String): String? {
        if (email.isEmpty()) return null
        val trimmed = email.trim()
        if (trimmed.isEmpty() || trimmed.contains(" ")) {
            return "Cannot contain spaces"
        }
        val parts = trimmed.split("@")
        val beforeAt = parts.getOrNull(0) ?: ""
        return when {
            !trimmed[0].isLetter() -> "Must start with an alphabet"
            !trimmed.contains("@") -> "Must include '@'"
            !trimmed.contains(".") -> "Must include '.'"
            beforeAt.length < 4 -> "Part before '@' must be 4 or more characters"
            !emailRegex.matches(trimmed) -> "Invalid Email format"
            else -> null
        }
    }

    fun getPswdError(pswd: String): String? {
        if (pswd.isEmpty()) return null
        return when {
            pswd.contains(" ") -> "No spaces allowed"
            pswd.length < 8 -> "Minimum 8 characters"
            !hasLower.containsMatchIn(pswd) -> "At least 1 lowercase"
            !hasUpper.containsMatchIn(pswd) -> "At least 1 UPPERCASE"
            !hasSpecial.containsMatchIn(pswd) -> "At least 1 special"
            !hasNumber.containsMatchIn(pswd) -> "At least 1 number"
            else -> null
        }
    }

}
