//
// DHPRequestQueueManagerTests.kt
// teva_cloud
//
// Copyright © 2017 Teva. All rights reserved.
//

package com.teva.cloud.services

import com.nhaarman.mockito_kotlin.*
import com.teva.utilities.services.DependencyProvider
import com.teva.dhp.DataEntities.DHPDataTypes.GenericDHPRequest
import com.teva.dhp.DataEntities.DHPDataTypes.ResponseBodies.DHPResponseBody
import com.teva.dhp.models.DHPManager
import junit.framework.Assert.assertEquals
import org.junit.After
import org.junit.Before
import org.junit.Test


/**
 * This class contains the unit tests for the DHPRequestQueueManager classes.
 */
class DHPRequestQueueManagerTests {
    private var dependencyProvider: DependencyProvider = DependencyProvider.default
    private val dhpManager: DHPManager = mock()

    @Before
    @Throws(Exception::class)
    fun setup() {
        dependencyProvider.register(DHPManager::class, dhpManager)
    }

    @After
    @Throws(Exception::class)
    fun tearDown() {
        DependencyProvider.default.unregisterAll()
    }

    @Test
    fun testThatAddRequestExecutesRequestIfCurrentlyNoneAreInProgress() {
        // arrange
        val dhpRequestQueueManager = DHPRequestQueueManager()
        val request: GenericDHPRequest<DHPResponseBody> = mock()
        val genericRequestArgumentCaptor = argumentCaptor<GenericDHPRequest<DHPResponseBody>>()

        // act
        dhpRequestQueueManager.add(request)

        // assert
        verify(dhpManager).executeAsync(genericRequestArgumentCaptor.capture(), any())
        assertEquals(request, genericRequestArgumentCaptor.firstValue)
    }

    @Test
    fun testThatAddRequestDoesNotExecuteRequestIfARequestIsInProgress() {
        // arrange
        val dhpRequestQueueManager = DHPRequestQueueManager()
        val request1: GenericDHPRequest<DHPResponseBody> = mock()
        val request2: GenericDHPRequest<DHPResponseBody> = mock()
        val genericRequestArgumentCaptor = argumentCaptor<GenericDHPRequest<DHPResponseBody>>()

        // act
        dhpRequestQueueManager.add(request1)
        dhpRequestQueueManager.add(request2)

        // assert
        verify(dhpManager,times(1)).executeAsync(genericRequestArgumentCaptor.capture(), any())
        assertEquals(request1, genericRequestArgumentCaptor.firstValue)
    }

    @Test
    fun testThatRequestCallbackInvokesTheOriginalCallbackAndExecutesTheNextRequest() {
        // arrange
        val dhpRequestQueueManager = DHPRequestQueueManager()
        val request1: GenericDHPRequest<DHPResponseBody> = mock()
        val request2: GenericDHPRequest<DHPResponseBody> = mock()
        val callback : (b: Boolean, s: String, s2: String?)-> Unit = mock()
        whenever(request1.callback).thenReturn(callback)
        val genericRequestArgumentCaptor = argumentCaptor<GenericDHPRequest<DHPResponseBody>>()

        // act
        dhpRequestQueueManager.add(request1)
        dhpRequestQueueManager.add(request2)
        val callbackMethod = DHPRequestQueueManager::class.java.getDeclaredMethod("requestCallback", Boolean::class.java, String::class.java, String::class.java)
        callbackMethod.isAccessible = true
        callbackMethod.invoke(dhpRequestQueueManager, true, "", "")


        // assert
        verify(dhpManager,times(2)).executeAsync(genericRequestArgumentCaptor.capture(), any())
        assertEquals(request1, genericRequestArgumentCaptor.firstValue)
        assertEquals(request2, genericRequestArgumentCaptor.secondValue)
        verify(callback)(eq(true), eq(""), eq(""))
    }
}