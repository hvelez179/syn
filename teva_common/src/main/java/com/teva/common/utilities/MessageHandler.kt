//
// MessageHandler.kt
// teva_common
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.common.utilities

import android.os.Handler
import android.os.Message

import java.lang.ref.WeakReference

/**
 * Implementation of a Handler that avoids the memory leak that could occur
 * if the handler is declared as an inner class.
 *
 * @param The listener to receive messages.
 */
class MessageHandler(listener: MessageListener) : Handler() {

    internal var listenerWeakReference = WeakReference(listener)

    /**
     * This method handles messages received by the Handler and sends them
     * to the registered listener.
     *
     * @param msg
     */
    override fun handleMessage(msg: Message) {
        super.handleMessage(msg)
        val listener = listenerWeakReference.get()
        listener?.onMessage(msg.what)
    }

    /**
     * This method checks the message queue for a message of the specified
     * id and enqueues a message if one is not found.
     *
     * @param what The id of the message.
     */
    fun sendMessageIfNotQueued(what: Int) {
        if (!hasMessages(what)) {
            sendEmptyMessage(what)
        }
    }

    /**
     * Listener interface used to receive messages from the MessageHandler.
     */
    interface MessageListener {
        /**
         * Method called when a message is received by the MessageHandler.

         * @param message The id of the message.
         */
        fun onMessage(message: Int)
    }

}
