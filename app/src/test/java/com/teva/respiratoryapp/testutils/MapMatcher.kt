//
// MapMatcher.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.testutils

import org.mockito.ArgumentMatcher

/**
 * Mockito ArgumentMatcher that compares two Map<String></String>,Object> objects.
 */

class MapMatcher(private val expectedMap: Map<String, Any>) : ArgumentMatcher<Map<String, Any>> {

    /**
     * Returns whether this matcher accepts the given argument.
     *
     *
     * The method should **never** assert if the argument doesn't match. It
     * should only return false.

     * @param otherMap the argument
     * *
     * @return whether this matcher accepts the given argument.
     */
    override fun matches(otherMap: Map<String, Any>?): Boolean {
        if (otherMap == null) {
            return false
        }

        for (key in expectedMap.keys) {
            val expectedValue = expectedMap[key]
            val otherValue = otherMap[key]

            if (expectedValue == null && otherValue == null) {
                continue
            }

            if (expectedValue == null) {
                return false
            }

            if (otherValue == null) {
                return false
            }

            if (expectedValue != otherValue) {
                return false
            }
        }

        return true
    }
}
