package com.teva.userfeedback.enumerations

/**
 * Enum listing the user feeling values.
 * Supports conversion from ordinal values to enum values
 */
enum class UserFeeling {
    UNKNOWN,
    GOOD,
    POOR,
    BAD;


    companion object {

        private val values = values()

        /**
         * Converts an ordinal value into a UserFeeling enumerated value.
         *
         * @property rawValue The ordinal value to convert.
         */
        fun fromOrdinal(rawValue: Int): UserFeeling {

            if (rawValue < 0 || rawValue >= values.size) {
                throw IndexOutOfBoundsException("Invalid user feeling")
            }

            return values[rawValue]
        }
    }
}
