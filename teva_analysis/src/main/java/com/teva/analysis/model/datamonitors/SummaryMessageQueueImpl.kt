//
// SummaryMessageQueueImpl.kt
// teva_analysis
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.analysis.model.datamonitors

import com.teva.analysis.entities.SummaryInfo
import com.teva.analysis.messages.SummaryUpdatedMessage
import com.teva.utilities.services.DependencyProvider
import com.teva.common.utilities.Messenger
import com.teva.common.utilities.SortedArrayList

/**
 * This class maintains a list of messages that are intended
 * to be displayed in the dashboard.
 */

class SummaryMessageQueueImpl(private val dependencyProvider: DependencyProvider) : SummaryMessageQueue {
    private val messageList = SortedArrayList<SummaryInfo>()

    /**
     * This method returns the message at the beginning of the message queue.
     *
     * @return - the message at the beginning of the message queue.
     */
    override val topMessage: SummaryInfo?
        get() = messageList.firstOrNull()

    /**
     * This method adds a message in the queue sorted by its id if it does not already exist.
     *
     * @param message - the message to be added.
     */
    override fun addMessage(message: SummaryInfo) {

        if ( messageList.none { it.id == message.id }) {
            messageList.insertSorted(message, true)
            dependencyProvider.resolve<Messenger>().publish(SummaryUpdatedMessage())
        }
    }

    /**
     * This method removes a message from the queue if it exists.
     *
     * @param message - the message to be removed.
     */
    override fun removeMessage(message: SummaryInfo) {
        val messageToRemove = messageList.firstOrNull { it.id == message.id }

        if (messageToRemove != null) {
            messageList.remove(messageToRemove)
            dependencyProvider.resolve<Messenger>().publish(SummaryUpdatedMessage())
        }
    }
}
