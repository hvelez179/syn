//
// 
// 
//
// Copyright © 2017 Teva. All rights reserved.
//

package com.teva.cloud.utilities

import org.threeten.bp.Instant
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.temporal.Temporal

class DateTimeConversionUtil {
    companion object {
        fun getGMTTimezoneOffsetString(time: Instant?): String {
            val zoneOffset = LocalDateTime.ofInstant(time, ZoneId.systemDefault()).atZone(ZoneId.of("GMT")).offset
            var offsetString = zoneOffset.toString()
            return offsetString.replace("Z", "GMT")
        }

        fun toGMTString(temporal: Temporal?, includeMilliSeconds: Boolean): String {
            val dateFormatter = if(includeMilliSeconds) DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS") else DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
            dateFormatter.withZone(ZoneId.of("GMT"))
            return dateFormatter.format(temporal)
        }


    }
}


