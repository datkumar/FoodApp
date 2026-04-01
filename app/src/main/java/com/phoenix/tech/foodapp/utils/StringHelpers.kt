package com.phoenix.tech.foodapp.utils


fun trimOverflowingText(
    text: String,
    maxLength: Int = 30
): String {
    if (text.length <= maxLength) {
        return text
    }
    return text.substring(0, maxLength - 2) + "..."

}