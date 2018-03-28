/*
 *
 *  StringExtensionsTests.kt
 *  teva_common
 *
 *  Copyright © 2018 Teva. All rights reserved.
 *
 */

package com.teva.common.utilities

import junit.framework.Assert.assertEquals
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * This class defines unit tests for the String Extension functions defined in this module.
 */
class StringExtensionsTests {
    @Before
    @Throws(Exception::class)
    fun setup() {

    }

    @After
    @Throws(Exception::class)
    fun tearDown() {

    }

    @Test
    fun testsplitWordsSeparatedByUnderscoresAndCapitalizeSplitsWordsCorrectly() {
        // arrange
        val string1 = "SPLIT_THIS_STRING"
        val string2 = "split_this_string"
        val string3 = "Split"

        // act
        val result1 = string1.splitWordsSeparatedByUnderscoresAndCapitalize()
        val result2 = string2.splitWordsSeparatedByUnderscoresAndCapitalize()
        val result3 = string3.splitWordsSeparatedByUnderscoresAndCapitalize()

        // assert
        assertEquals("Split This String", result1)
        assertEquals("Split This String", result2)
        assertEquals("Split", result3)
    }

    @Test
    fun testsplitFromCamelCaseSplitsWordsCorrectly() {
        // arrange
        val string1 = "thisIsCamelCase"

        // act
        val result1 = string1.splitFromCamelCase(true)
        val result2 = string1.splitFromCamelCase(false)

        // assert
        assertEquals("This Is Camel Case", result1)
        assertEquals("this Is Camel Case", result2)
    }
}