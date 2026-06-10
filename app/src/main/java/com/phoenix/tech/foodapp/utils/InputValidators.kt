package com.phoenix.tech.foodapp.utils

object InputValidators {
    private val hasLower = "[a-z]".toRegex()
    private val hasUpper = "[A-Z]".toRegex()
    private val hasNumber = "[0-9]".toRegex()

    // Non-alphanumeric characters as special
    private val hasSpecial = "[^A-Za-z0-9]".toRegex()

    //    private val emailRegex = "^[A-Za-z][A-Za-z0-9.]{3,}@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
    private val emailRegex =
        "^[A-Za-z][A-Za-z0-9]*(?:\\.[A-Za-z0-9]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$".toRegex()


    enum class EmailErrorMsg(val message: String) {
        NO_SPACES("Cannot contain spaces"),
        START_WITH_ALPHA("Must start with an alphabet"),
        CONTAIN_AT_SYMBOL("Must include '@'"),
        CONTAIN_DOT_SYMBOL("Must include '.'"),
        PREFIX_IS_SHORT("Part before '@' must be 4 or more characters"),
        INVALID_FORMAT("Invalid Email format")
    }

    enum class PswdErrorMsg(val message: String) {
        NO_SPACES("Cannot contain spaces"),
        TOO_SHORT("Minimum 8 characters"),
        LOWERCASE_CHAR("At least one lowercase character"),
        UPPERCASE_CHAR("At least one UPPERCASE character"),
        SPECIAL_CHAR("At least one special character"),
        NUMBER_CHAR("At least one numeric character")
    }

    fun getEmailError(email: String): EmailErrorMsg? {
        if (email.isEmpty()) return null
        val trimmed = email.trim()
        if (trimmed.isEmpty() || trimmed.contains(" ")) {
            return EmailErrorMsg.NO_SPACES
        }
        val parts = trimmed.split("@")
        val beforeAt = parts.getOrNull(0) ?: ""
        return when {
            !trimmed[0].isLetter() -> EmailErrorMsg.START_WITH_ALPHA
            !trimmed.contains("@") -> EmailErrorMsg.CONTAIN_AT_SYMBOL
            !trimmed.contains(".") -> EmailErrorMsg.CONTAIN_DOT_SYMBOL
            beforeAt.length < 4 -> EmailErrorMsg.PREFIX_IS_SHORT
            !emailRegex.matches(trimmed) -> EmailErrorMsg.INVALID_FORMAT
            else -> null
        }
    }

    fun getPswdError(pswd: String): PswdErrorMsg? {
        if (pswd.isEmpty()) return null
        return when {
            pswd.contains(" ") -> PswdErrorMsg.NO_SPACES
            pswd.length < 8 -> PswdErrorMsg.TOO_SHORT
            !hasLower.containsMatchIn(pswd) -> PswdErrorMsg.LOWERCASE_CHAR
            !hasUpper.containsMatchIn(pswd) -> PswdErrorMsg.UPPERCASE_CHAR
            !hasSpecial.containsMatchIn(pswd) -> PswdErrorMsg.SPECIAL_CHAR
            !hasNumber.containsMatchIn(pswd) -> PswdErrorMsg.NUMBER_CHAR
            else -> null
        }
    }
}
