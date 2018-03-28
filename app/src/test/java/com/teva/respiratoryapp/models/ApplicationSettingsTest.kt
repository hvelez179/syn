//
// ApplicationSettingsTest.java
// app
//
// Copyright (c) 2017 Teva. All rights reserved.
//

package com.teva.respiratoryapp.models

import android.content.SharedPreferences
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever

import com.teva.utilities.services.DependencyProvider
import com.teva.respiratoryapp.testutils.BaseTest

import org.junit.Before
import org.junit.Test

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue

/**
 * This class defines unit tests for the ApplicationSettings class.
 */

class ApplicationSettingsTest : BaseTest() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var dependencyProvider: DependencyProvider

    @Before
    fun setup() {
        sharedPreferences = mock()
        editor = mock()
        whenever(sharedPreferences.edit()).thenReturn(editor)
        dependencyProvider = DependencyProvider.default
        dependencyProvider.register(SharedPreferences::class, sharedPreferences)
    }

    @Test
    fun testGetCameraPermissionRequestedReturnsValueObtainedFromSharedPreferences() {
        val applicationSettings = ApplicationSettings(dependencyProvider)

        whenever(sharedPreferences.getBoolean(eq("CameraPermissionRequested"), eq(false))).thenReturn(false)
        var cameraPermissionRequested = applicationSettings.cameraPermissionRequested
        assertFalse(cameraPermissionRequested)

        whenever(sharedPreferences.getBoolean(eq("CameraPermissionRequested"), eq(false))).thenReturn(true)
        cameraPermissionRequested = applicationSettings.cameraPermissionRequested
        assertTrue(cameraPermissionRequested)
    }

    @Test
    fun testSetCameraPermissionRequestedWritesValueToSharedPreferencesEditor() {
        val applicationSettings = ApplicationSettings(dependencyProvider)

        applicationSettings.cameraPermissionRequested = false
        verify(editor).putBoolean(eq("CameraPermissionRequested"), eq(false))

        applicationSettings.cameraPermissionRequested = true
        verify(editor).putBoolean(eq("CameraPermissionRequested"), eq(true))
    }

    @Test
    fun testGetUserAcceptedTermsOfUseReturnsValueObtainedFromSharedPreferences() {
        val applicationSettings = ApplicationSettings(dependencyProvider)

        whenever(sharedPreferences.getBoolean(eq("UserAcceptedTermsOfUse"), eq(false))).thenReturn(false)
        var userAcceptedTermsOfUse = applicationSettings.hasUserAcceptedTermsOfUse
        assertFalse(userAcceptedTermsOfUse)

        whenever(sharedPreferences.getBoolean(eq("UserAcceptedTermsOfUse"), eq(false))).thenReturn(true)
        userAcceptedTermsOfUse = applicationSettings.hasUserAcceptedTermsOfUse
        assertTrue(userAcceptedTermsOfUse)
    }

    @Test
    fun testGetWalkthroughShownReturnsValueObtainedFromSharedPreferences() {
        val applicationSettings = ApplicationSettings(dependencyProvider)

        whenever(sharedPreferences.getBoolean(eq("WalkthroughShown"), eq(false))).thenReturn(false)
        var walkthroughShown = applicationSettings.isWalkthroughShown
        assertFalse(walkthroughShown)

        whenever(sharedPreferences.getBoolean(eq("WalkthroughShown"), eq(false))).thenReturn(true)
        walkthroughShown = applicationSettings.isWalkthroughShown
        assertTrue(walkthroughShown)
    }

    @Test
    fun testSetWalkthroughShownWritesValueToSharedPreferencesEditor() {
        val applicationSettings = ApplicationSettings(dependencyProvider)

        applicationSettings.isWalkthroughShown = false
        verify(editor).putBoolean(eq("WalkthroughShown"), eq(false))

        applicationSettings.isWalkthroughShown = true
        verify(editor).putBoolean(eq("WalkthroughShown"), eq(true))
    }

    @Test
    fun testSetUserHasAcceptedTermsOfUseWritesValueToSharedPreferencesEditor() {
        val applicationSettings = ApplicationSettings(dependencyProvider)

        applicationSettings.hasUserAcceptedTermsOfUse = false
        verify(editor).putBoolean(eq("UserAcceptedTermsOfUse"), eq(false))

        applicationSettings.hasUserAcceptedTermsOfUse = true
        verify(editor).putBoolean(eq("UserAcceptedTermsOfUse"), eq(true))
    }

    @Test
    fun testGetLocationPermissionRequestedReturnsValueObtainedFromSharedPreferences() {
        val applicationSettings = ApplicationSettings(dependencyProvider)

        whenever(sharedPreferences.getBoolean(eq("LocationPermissionRequested"), eq(false))).thenReturn(false)
        var locationPermissionRequested = applicationSettings.locationPermissionRequested
        assertFalse(locationPermissionRequested)

        whenever(sharedPreferences.getBoolean(eq("LocationPermissionRequested"), eq(false))).thenReturn(true)
        locationPermissionRequested = applicationSettings.locationPermissionRequested
        assertTrue(locationPermissionRequested)
    }

    @Test
    fun testSetLocationPermissionRequestedWritesValueToSharedPreferencesEditor() {
        val applicationSettings = ApplicationSettings(dependencyProvider)

        applicationSettings.locationPermissionRequested = false
        verify(editor).putBoolean(eq("LocationPermissionRequested"), eq(false))

        applicationSettings.locationPermissionRequested = true
        verify(editor).putBoolean(eq("LocationPermissionRequested"), eq(true))
    }
}
