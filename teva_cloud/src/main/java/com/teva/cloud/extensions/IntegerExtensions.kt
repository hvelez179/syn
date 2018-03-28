//
// IntegerExtensions.kt
// teva_cloud
//
// Copyright © 2017 Teva. All rights reserved.
//

package com.teva.cloud.extensions

import com.teva.cloud.models.CloudConstants


fun Int.toGMTOffset(): String {

    val hours = Math.abs(this) / 60
    val minutes = Math.abs(this) % 60

    if (this > 0) {
        return "GMT+$hours:" + "$minutes".padStart(2, '0')
    } else if (this < 0) {
        return "GMT-$hours:" + "$minutes".padStart(2, '0')
    }

    return "GMT"
}

fun Int.Companion.fromGMTOffset(gmt: String): Int {

    if (gmt == "GMT") {
        return 0
    }

    val parts = gmt.substring(3).split(":")

    if(parts.size < 2) {
        return 0
    }

    val hours = (parts[0]).toInt()
    val minutes =(parts[1]).toInt()

    return ((hours*60) + minutes)
}

fun Int.toServerTimeOffsetString(): String? {

    if (this == CloudConstants.unknownOffsetValue) {
        return "unknown"
    }

    if(this == 0) return "0"

    return (if(this >= 0) "+" else "-") + "${Math.abs(this)}"
}