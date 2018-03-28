package com.teva.common.utilities

import android.text.TextUtils.replace
import android.util.Base64
import android.util.Base64.DEFAULT

/**
 * Checks if the string contains only alphanumeric characters.
 * @param string The string to check
 * *
 * @return True if the string contains only alphanumeric characters, false otherwise
 */
fun String.isAlphaNumeric(): Boolean {
    return matches("^[a-zA-Z0-9]*$".toRegex())
}

/**
 * Checks if the string contains only numeric characters.
 * @param string The string to check
 * *
 * @return True if the string contains only digit characters, false otherwise
 */
fun String.isNumeric(): Boolean {
    return matches("^[0-9]*$".toRegex())
}

/**
 * Checks if the string contains only numeric characters.
 * @param string The string to check
 * *
 * @return True if the string contains only digit characters, false otherwise
 */
fun String.dataFromBase64URLEncoding(): ByteArray {
    this.replace('-', '+')
    this.replace('_', '/')

    var charsToAppend = ""
    when(this.length % 4) {
        1 -> charsToAppend = "==="
        2 -> charsToAppend = "=="
        3 -> charsToAppend = "="
    }

    return Base64.decode(this + charsToAppend, DEFAULT)
}