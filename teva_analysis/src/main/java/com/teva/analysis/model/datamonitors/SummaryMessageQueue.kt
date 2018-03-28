//
// SummaryMessageQueue.kt
// teva_analysis
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.analysis.model.datamonitors

import com.teva.analysis.entities.SummaryInfo

/**
 * This class maintains a list of messages that are intended
 * to be displayed in the dashboard.
 */

interface SummaryMessageQueue {

    /**
     * The message at the beginning of the message queue.
     */
    val topMessage: SummaryInfo?

    /**
     * This method adds a message in the queue sorted by its id if it does not already exist.
     *
     * @param message - the message to be added.
     */
    fun addMessage(message: SummaryInfo)

    /**
     * This method removes a message from the queue if it exists.
     *
     * @param message - the message to be removed.
     */
    fun removeMessage(message: SummaryInfo)
}
