//
// InstantExtensions.kt
// teva_cloud
//
// Copyright © 2017 Teva. All rights reserved.
//

package com.teva.cloud.extensions

import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter

fun Instant.toGMTString(withMilliSeconds: Boolean): String {
    val formatter = if(withMilliSeconds) {
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")
                .withZone(ZoneId.of("GMT"))
    } else {
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
                .withZone(ZoneId.of("GMT"))
    }

    return formatter.format(this)
}

fun Instant.toGMTOffset(): String {
    val offset = ZonedDateTime.ofInstant(this, ZoneId.systemDefault()).offset
    val offsetString = offset.toString()
    return "GMT$offsetString"
}

//Instant does not have Companion so we cannot use Instant.Companion.
fun instantFromGMTString(gmtString: String): Instant? {

    if(gmtString.isNullOrEmpty()) {
        return null
    }

    val updatedGMTString = if(gmtString[gmtString.lastIndex] != 'Z') {
        gmtString + "Z"
    } else {
        gmtString
    }

    return Instant.parse(updatedGMTString)
}
