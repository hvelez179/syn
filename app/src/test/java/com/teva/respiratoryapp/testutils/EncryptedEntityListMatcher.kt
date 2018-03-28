//
// EncryptedEntityListMatcher.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.testutils

import com.teva.respiratoryapp.services.data.encrypteddata.EncryptedEntity

import org.mockito.ArgumentMatcher

fun List<EncryptedEntity>.matches(other: List<EncryptedEntity>?): Boolean {
    if (other == null) {
        return false
    }

    if (size != other.size) {
        return false
    }

    for (i in indices) {
        val expectedEntity = get(i)
        val otherEntity = other[i]

        if (expectedEntity.javaClass != otherEntity.javaClass) {
            return false
        }

        val expectedSchemaMap = expectedEntity.schemaMap
        val otherSchemaMap = otherEntity.schemaMap

        for (key in expectedSchemaMap.keys) {
            val expectedValue = expectedSchemaMap[key]
            val otherValue = otherSchemaMap[key]

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
    }

    return true
}

/**
 * Mockito ArgumentMatcher that compares two lists of EncryptedEntity objects.
 */
class EncryptedEntityListMatcher(private val expected: List<*>) : ArgumentMatcher<List<*>> {

    override fun matches(other: List<*>?): Boolean {
        if (other == null) {
            return false
        }

        if (expected.size != other.size) {
            return false
        }

        for (i in expected.indices) {
            val expectedEntity = expected[i] as EncryptedEntity
            val otherEntity = other[i] as EncryptedEntity

            if (expectedEntity.javaClass != otherEntity.javaClass) {
                return false
            }

            val expectedSchemaMap = expectedEntity.schemaMap
            val otherSchemaMap = otherEntity.schemaMap

            for (key in expectedSchemaMap.keys) {
                val expectedValue = expectedSchemaMap[key]
                val otherValue = otherSchemaMap[key]

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
        }

        return true
    }
}
