//
// FragmentAnimation.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.mvvmframework.ui

/**
 * Enumeration indicating the type of animation to use during a fragment transition.
 */
enum class FragmentAnimation {
    NO_ANIMATION,
    FADE,
    SLIDE_OVER;

    companion object {

        private val values = values()

        /**
         * Converts a ordinal value to an enumeration value.
         *
         * @param rawValue The ordinal value.
         * @return The enumeration value that corresponds to the ordinal value.
         */
        fun fromOrdinal(rawValue: Int): FragmentAnimation {

            if (rawValue < 0 || rawValue >= values.size) {
                throw IndexOutOfBoundsException("Invalid air quality")
            }

            return values[rawValue]
        }
    }
}
