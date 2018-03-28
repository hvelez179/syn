//
// StringExtensions.kt
// teva_cloud
//
// Copyright © 2017 Teva. All rights reserved.
//

package com.teva.cloud.extensions

import com.teva.cloud.models.CloudConstants


fun String?.toStringOrUnknown(): String {
    return if(this.isNullOrEmpty()) "Unknown" else this!!
}

fun String?.fromStringOrUnknown(): String {
    return if(this.isNullOrBlank() || this == "Unknown") "" else this!!
}

fun String?.fromServerTimeOffsetString(): Int {

    if (this == "unknown") {
        return CloudConstants.unknownOffsetValue
    }

    return (this ?: "0").toInt()
}

/*fun String?.toInt(): Int {
    return ((this ?: "0") as String).toInt()
}

fun String?.toLong(): Long {
    return (this ?: "0").toLong()
}

fun String?.toBoolean(): Boolean {
    return (this ?: "false").toBoolean()
}*/


