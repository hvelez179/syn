//
// StringExtensions.kt
// teva_dhp
//
// Copyright © 2017 Teva. All rights reserved.
//

package com.teva.dhp.extensions

import android.util.Base64

fun String.dataFromBase64URLEncoding() : ByteArray {
    var updatedString = this.replace("-", "+")
            .replace("_", "/")

    // In order to return data, base64URL encoded string needs to be converted to base64, which must be divisible by 4.
    // If not divisible by 4, pad the end of the string.
    updatedString = when(updatedString.length % 4) {
        1 -> updatedString + "==="
        2 -> updatedString + "=="
        3 -> updatedString + "="
        else -> updatedString
    }

    return Base64.decode(updatedString, Base64.DEFAULT)
}



