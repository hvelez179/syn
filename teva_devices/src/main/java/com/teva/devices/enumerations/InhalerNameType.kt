//
// InhalerNameType.kt
// teva_devices
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.devices.enumerations

/**
 * Enumeration that specifies the nickname type for an inhaler.
 */
enum class InhalerNameType {
    HOME,
    WORK,
    SPORTS,
    CARRY_WITH_ME,
    CUSTOM,
    OTHER;


    companion object {

        private val values = values()

        fun fromOrdinal(rawValue: Int?): InhalerNameType? {
            return if (rawValue != null) values[rawValue] else null
        }

        fun fromString(value: String): InhalerNameType {
            return when(value.trim().toLowerCase()) {
                "home" -> HOME
                "work" -> WORK
                "sports" -> SPORTS
                "travel" -> CARRY_WITH_ME
                else -> CUSTOM
            }
        }
    }
}
