///
// LocalizationServiceTests.kt
// teva_common
//
// Copyright © 2017 Teva. All rights reserved
///

package com.teva.common.services

import android.content.Context
import android.content.res.Resources
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import com.teva.common.utilities.LocalizationServiceImpl
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.*

/**
 * This class defines unit tests for the LocalizationService class.
 */
class LocalizationServiceTests {
    private val TRADE_NAME_KEY = "trade_name_text"
    private val TRADE_NAME_ID = 7654320
    private val TRADE_NAME_WITH_CARRIAGE_RETURN_KEY = "trade_name_multiline_text"
    private val TRADE_NAME_WITH_CARRIAGE_RETURN_ID = 7654321
    private val APP_NAME_KEY = "app_name"
    private val APP_NAME_ID = 7654322
    private val packageName = "teva.AsthmaApp"

    private lateinit var context: Context
    private lateinit var resources: Resources

    @Before
    fun setup() {
        context = mock()
        resources = mock()
        whenever(context.resources).thenReturn(resources)
        whenever(resources.getIdentifier(eq(TRADE_NAME_KEY), any(), any())).thenReturn(TRADE_NAME_ID)
        whenever(resources.getIdentifier(eq(TRADE_NAME_WITH_CARRIAGE_RETURN_KEY), any(), any())).thenReturn(TRADE_NAME_WITH_CARRIAGE_RETURN_ID)
        whenever(resources.getIdentifier(eq(APP_NAME_KEY), any(), any())).thenReturn(APP_NAME_ID)
        whenever(context.getString(TRADE_NAME_ID)).thenReturn(TRADE_NAME_KEY)
        whenever(context.getString(TRADE_NAME_WITH_CARRIAGE_RETURN_ID)).thenReturn(TRADE_NAME_WITH_CARRIAGE_RETURN_KEY)
        whenever(context.getString(APP_NAME_ID)).thenReturn(APP_NAME_KEY)

        whenever(context.packageName).thenReturn(packageName)
    }

    @Test
    fun testGetStringByNumericResourceIdReturnsCorrectString() {
        val resourceId = 1
        val expectedResource = "Teva Android App"
        whenever(context.getString(eq(resourceId))).thenReturn(expectedResource)

        val localizationService = LocalizationServiceImpl(context)
        val resource = localizationService.getString(resourceId)

        assertTrue(resource == expectedResource)
    }

    @Test
    fun testGetStringByStringResourceIdReturnsCorrectString() {
        val resourceId = 1
        val resourceStringId = "APP_NAME"
        val expectedResource = "Teva Android App"
        whenever(resources.getIdentifier(eq(resourceStringId), eq("string"), eq(packageName))).thenReturn(resourceId)
        whenever(context.getString(eq(resourceId))).thenReturn(expectedResource)

        val localizationService = LocalizationServiceImpl(context)
        val resource = localizationService.getString(resourceStringId)

        assertTrue(resource == expectedResource)
    }

    @Test
    fun testGetStringByStringResourceIdWithReplacementsReturnsCorrectString() {
        val resourceId = 2
        val resourceStringId = "REGISTRATION_MESSAGE"
        val resourceString = "Your inhaler \"\$InhalerName$\" is registered."
        val expectedResource = "Your inhaler \"sports\" is registered."
        val replacements = HashMap<String, Any>()
        replacements.put("InhalerName", "sports")

        whenever(resources.getIdentifier(eq(resourceStringId), eq("string"), eq(packageName))).thenReturn(resourceId)
        whenever(context.getString(eq(resourceId))).thenReturn(resourceString)

        val localizationService = LocalizationServiceImpl(context)
        val resource = localizationService.getString(resourceStringId, replacements)

        assertTrue(resource == expectedResource)
    }

    @Test
    fun testGetStringByNumericResourceIdWithReplacementsReturnsCorrectString() {
        val resourceId = 2
        val resourceString = "Your inhaler \"\$InhalerName$\" is registered."
        val expectedResource = "Your inhaler \"sports\" is registered."
        val replacements = HashMap<String, Any>()
        replacements.put("InhalerName", "sports")

        whenever(context.getString(eq(resourceId))).thenReturn(resourceString)

        val localizationService = LocalizationServiceImpl(context)
        val resource = localizationService.getString(resourceId, replacements)

        assertTrue(resource == expectedResource)
    }
}
