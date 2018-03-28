//
// WebLoginManagerTests.kt
// teva_cloud
//
// Copyright © 2017 Teva. All rights reserved.
//

package com.teva.cloud.models

import com.nhaarman.mockito_kotlin.*
import com.teva.cloud.enumerations.LoginResult
import com.teva.utilities.services.DependencyProvider
import com.teva.dhp.models.DHPManager
import junit.framework.Assert.assertEquals
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.net.URL


/**
 * This class defines unit tests for the WebLoginManager class
 */
class WebLoginManagerTests {
    val dhpManager: DHPManager = mock()
    @Before
    @Throws(Exception::class)
    fun setup() {
        DependencyProvider.default.register(DHPManager::class, dhpManager)
    }

    @After
    @Throws(Exception::class)
    fun tearDown() {
        DependencyProvider.default.unregisterAll()
    }

    @Test
    fun testGetLoginUrlReturnsLoginUrlFromDHPManager() {

        // arrange
        val testLoginUrl = URL("http://testLoginUrl")
        whenever(dhpManager.getLoginURL()).thenReturn(testLoginUrl)
        DependencyProvider.default.register(DHPManager::class, dhpManager)

        // act
        val webLoginManager = WebLoginManagerImpl()
        val loginUrl = webLoginManager.getLoginURL()

        // assert
        verify(dhpManager).getLoginURL()
        assertEquals(testLoginUrl, loginUrl)
    }

    @Test
    fun testGetRegistrationUrlReturnsRegistrationUrlFromDHPManager() {

        // arrange
        val testRegistrationUrl = URL("http://testRegistrationUrl")
        whenever(dhpManager.getRegistrationURL()).thenReturn(testRegistrationUrl)
        DependencyProvider.default.register(DHPManager::class, dhpManager)

        // act
        val webLoginManager = WebLoginManagerImpl()
        val registrationUrl = webLoginManager.getRegistrationURL()

        // assert
        verify(dhpManager).getRegistrationURL()
        assertEquals(testRegistrationUrl, registrationUrl)
    }

    @Test
    fun testIsLoginSuccessRedirectUrlReturnsTheValueRetrievedFromDHPManager() {

        // arrange
        val testLoginSuccessRedirectUrl = URL("http://LoginSuccessRedirectionUrl")
        val testRandomUrl = URL("http://RandomUrl")
        whenever(dhpManager.isLoginSuccessRedirectURL(eq(testLoginSuccessRedirectUrl))).thenReturn(true)
        whenever(dhpManager.isLoginSuccessRedirectURL(eq(testRandomUrl))).thenReturn(false)
        DependencyProvider.default.register(DHPManager::class, dhpManager)
        val urlArgumentCaptor = argumentCaptor<URL>()

        // act
        val webLoginManager = WebLoginManagerImpl()
        val firstValue = webLoginManager.isLoginSuccessRedirectURL(testLoginSuccessRedirectUrl)
        val secondValue = webLoginManager.isLoginSuccessRedirectURL(testRandomUrl)

        // assert
        verify(dhpManager, times(2)).isLoginSuccessRedirectURL(urlArgumentCaptor.capture())
        assertEquals(testLoginSuccessRedirectUrl, urlArgumentCaptor.firstValue)
        assertEquals(true, firstValue)
        assertEquals(testRandomUrl, urlArgumentCaptor.secondValue)
        assertEquals(false, secondValue)
    }

    @Test
    fun testCompleteLoginAsyncCallsTheCorrespondingMethodOnDHPManagerWithCorrectParameters() {

        // arrange
        val loginSuccessRedirectUrl = "http://loginSuccessRedirectUrl"
        fun callback(b: LoginResult) {}

        // act
        val webLoginManager = WebLoginManagerImpl()
        webLoginManager.completeLoginAsync(URL(loginSuccessRedirectUrl), ::callback)

        // assert
        verify(dhpManager).completeLoginAsync(eq(URL(loginSuccessRedirectUrl)), any())
    }

    @Test
    fun testGetAuthorizationUrlReturnsAuthorizationUrlFromDHPManager() {

        // arrange
        val testAuthorizationUrl = URL("http://authorizationUrl")
        whenever(dhpManager.getAuthorizationURL()).thenReturn(testAuthorizationUrl)
        DependencyProvider.default.register(DHPManager::class, dhpManager)

        // act
        val webLoginManager = WebLoginManagerImpl()
        val authorizationUrl = webLoginManager.getAuthorizationURL()

        // assert
        verify(dhpManager).getAuthorizationURL()
        assertEquals(testAuthorizationUrl, authorizationUrl)
    }

    @Test
    fun testIsAuthorizationSuccessRedirectUrlReturnsTheValueRetrievedFromDHPManager() {

        // arrange
        val testAuthorizationSuccessRedirectUrl = URL("http://AuthorizationSuccessRedirectionUrl")
        val testRandomUrl = URL("http://RandomUrl")
        whenever(dhpManager.isAuthorizationSuccessRedirectURL(eq(testAuthorizationSuccessRedirectUrl))).thenReturn(true)
        whenever(dhpManager.isAuthorizationSuccessRedirectURL(eq(testRandomUrl))).thenReturn(false)
        DependencyProvider.default.register(DHPManager::class, dhpManager)
        val urlArgumentCaptor = argumentCaptor<URL>()

        // act
        val webLoginManager = WebLoginManagerImpl()
        val firstValue = webLoginManager.isAuthorizationSuccessRedirectURL(testAuthorizationSuccessRedirectUrl)
        val secondValue = webLoginManager.isAuthorizationSuccessRedirectURL(testRandomUrl)

        // assert
        verify(dhpManager, times(2)).isAuthorizationSuccessRedirectURL(urlArgumentCaptor.capture())
        assertEquals(testAuthorizationSuccessRedirectUrl, urlArgumentCaptor.firstValue)
        assertEquals(testRandomUrl, urlArgumentCaptor.secondValue)
        assertEquals(true, firstValue)
        assertEquals(false, secondValue)
    }

}