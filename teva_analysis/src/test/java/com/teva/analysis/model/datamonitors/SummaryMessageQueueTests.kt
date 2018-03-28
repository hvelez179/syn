//
// SummaryMessageQueueTests.kt
// teva_analysis
//
// Copyright (c) 2017 Teva. All rights reserved.
//

package com.teva.analysis.model.datamonitors

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.teva.analysis.entities.SummaryInfo
import com.teva.analysis.enumerations.SummaryTextId
import com.teva.utilities.services.DependencyProvider
import com.teva.common.utilities.Messenger
import junit.framework.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

/**
 * This class defines the unit tests for the SummaryMessageQueue class.
 */

class SummaryMessageQueueTests {

    private var messenger: Messenger = mock()
    private var dependencyProvider: DependencyProvider = DependencyProvider.default

    @Before
    fun setup() {
        DependencyProvider.default.unregisterAll()

        dependencyProvider.register(Messenger::class, messenger)
    }

    @Test
    fun testAddMessageAddsMessagesInTheProperOrderAndPublishesASummaryUpdatedMessage() {
        val summaryMessageQueue = SummaryMessageQueueImpl(dependencyProvider)

        // add messages in random order of priority and verify that
        // the messages are in the order of highest to lowest priority.

        // neutral
        val neutralMessage = SummaryInfo(SummaryTextId.NEUTRAL_MESSAGE, null)
        summaryMessageQueue.addMessage(neutralMessage)

        // overuse
        val overuseMessage = SummaryInfo(SummaryTextId.OVERUSE, null)
        summaryMessageQueue.addMessage(overuseMessage)

        // no inhaler
        val noInhalerMessage = SummaryInfo(SummaryTextId.NO_INHALERS, null)
        summaryMessageQueue.addMessage(noInhalerMessage)

        // environment
        val environmentMessage = SummaryInfo(SummaryTextId.ENVIRONMENT_MESSAGE, null)
        summaryMessageQueue.addMessage(environmentMessage)

        // empty inhaler
        val emptyInhalerMessage = SummaryInfo(SummaryTextId.EMPTY_INHALER, null)
        summaryMessageQueue.addMessage(emptyInhalerMessage)

        verify(messenger, times(5)).publish(any())

        // check message at the top to see if it is of the highest priority
        // and remove it to check the next message.
        assertEquals(overuseMessage, summaryMessageQueue.topMessage)
        summaryMessageQueue.removeMessage(overuseMessage)

        assertEquals(noInhalerMessage, summaryMessageQueue.topMessage)
        summaryMessageQueue.removeMessage(noInhalerMessage)

        assertEquals(emptyInhalerMessage, summaryMessageQueue.topMessage)
        summaryMessageQueue.removeMessage(emptyInhalerMessage)

        assertEquals(environmentMessage, summaryMessageQueue.topMessage)
        summaryMessageQueue.removeMessage(environmentMessage)

        assertEquals(neutralMessage, summaryMessageQueue.topMessage)
    }

    @Test
    fun testAddingAnExistingMessageDoesNothing() {
        val summaryMessageQueue = SummaryMessageQueueImpl(dependencyProvider)

        // add an overuse message
        val overuseMessage = SummaryInfo(SummaryTextId.OVERUSE, null)
        summaryMessageQueue.addMessage(overuseMessage)
        assertEquals(overuseMessage, summaryMessageQueue.topMessage)

        // add a second overuse message
        val overuseMessage1 = SummaryInfo(SummaryTextId.OVERUSE, null)
        summaryMessageQueue.addMessage(overuseMessage1)

        // verify that only the first message exists in the queue.
        assertEquals(overuseMessage, summaryMessageQueue.topMessage)
        summaryMessageQueue.removeMessage(overuseMessage)
        assertNull(summaryMessageQueue.topMessage)
    }

    @Test
    fun testRemoveMessageRemovesMessageFromAnyPositionInTheQueue() {
        val summaryMessageQueue = SummaryMessageQueueImpl(dependencyProvider)

        // add a low priority message.

        // neutral
        val neutralMessage = SummaryInfo(SummaryTextId.NEUTRAL_MESSAGE, null)
        summaryMessageQueue.addMessage(neutralMessage)
        // verify that the message is in the queue.
        assertEquals(neutralMessage, summaryMessageQueue.topMessage)

        // add a high priority message
        // overuse
        val overuseMessage = SummaryInfo(SummaryTextId.OVERUSE, null)
        summaryMessageQueue.addMessage(overuseMessage)

        // verify that the SummaryUpatedMessage was published twice
        // for the two add messages
        verify(messenger, times(2)).publish(any())

        // verify that the high priority message is at the top of teh queue.
        assertEquals(overuseMessage, summaryMessageQueue.topMessage)

        // remove the low priority message
        summaryMessageQueue.removeMessage(neutralMessage)

        //verify that the SummaryUpdatedMessage is published for the remove.
        verify(messenger, times(3)).publish(any())

        // verify that the high priority message is at the top of teh queue.
        assertEquals(overuseMessage, summaryMessageQueue.topMessage)

        // remove the high priority message from the queue.
        summaryMessageQueue.removeMessage(overuseMessage)

        //verify that the SummaryUpdatedMessage is published for the remove.
        verify(messenger, times(4)).publish(any())

        // verify that the low priority message was removed from the queue earlier
        // by checking that the queue is empty.
        assertNull(summaryMessageQueue.topMessage)
    }
}
