//
// DateTimeClassesExtensions.kt
// teva_common
//
// Copyright © 2017 Teva. All rights reserved.
//

package com.teva.common.utilities

import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime
import org.threeten.bp.ZoneId

fun LocalDate.toInstant(): Instant {
    return this.atStartOfDay(ZoneId.systemDefault()).toInstant()
}

fun Instant.toLocalDate(): LocalDate {
    return this.atZone(ZoneId.systemDefault()).toLocalDate()
}

fun Instant.toLocalTime() : LocalTime {
    return this.atZone(ZoneId.systemDefault()).toLocalTime()
}