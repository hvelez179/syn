//
// LocalDateExtensions.kt
// teva_cloud
//
// Copyright © 2017 Teva. All rights reserved.
//

package com.teva.cloud.extensions

import org.threeten.bp.LocalDate

//LocalDate does not have Companion so we cannot use Instant.Companion.
fun localDateFromGMTString(gmtString: String): LocalDate? {

    if(gmtString.isNullOrEmpty()) {
        return null
    }

    val indexOfT = gmtString.indexOf('T')

    val updatedGMTString = if( indexOfT != -1) {
        gmtString.substring(0, indexOfT)
    } else {
        gmtString
    }

    return LocalDate.parse(updatedGMTString)
}