/*
 *
 *  StringExtensions.kt
 *  teva_common
 *
 *  Copyright © 2018 Teva. All rights reserved.
 *
 */

package com.teva.common.utilities

fun String.splitFromCamelCase(upperCaseFirstChar: Boolean = true) : String {
    val firstChar = this[0]

    if(firstChar == null) {
        return ""
    }

    var result = StringBuilder()
    result.append(if(upperCaseFirstChar) firstChar.toUpperCase() else firstChar)

    for(i in 1 until this.length) {
        if(this[i].isUpperCase()) {
            result.append(" " + this[i])
        } else {
            result.append(this[i])
        }
    }

    return result.toString()
}

fun String.splitWordsSeparatedByUnderscoresAndCapitalize() : String {

    for(i in 0 until this.length) {
        val wordEnd = this.indexOf('_', i)
        if(wordEnd == -1) {
            return this.toLowerCase().capitalize()
        } else {
            return this.substring(0, wordEnd).toLowerCase().capitalize() + if(wordEnd + 1 < this.length) " " + this.substring(wordEnd + 1, this.length).splitWordsSeparatedByUnderscoresAndCapitalize() else ""
        }
    }

    return ""
}
