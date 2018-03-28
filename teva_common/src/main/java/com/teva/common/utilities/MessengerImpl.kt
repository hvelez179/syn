///
// MessengerImpl.kt
// teva_common
//
// Copyright © 2017 Teva. All rights reserved
///

package com.teva.common.utilities

import android.os.Handler
import com.teva.utilities.utilities.Logger

import org.greenrobot.eventbus.EventBus

import java.util.ArrayList

import com.teva.utilities.utilities.Logger.Level.VERBOSE

/**
 * This class provides a publish/subscribe message system using the EventBus class.
 *
 * @param eventBus The EventBus object that delivers the messages.
 */
class MessengerImpl(private val eventBus: EventBus) : Messenger {
    private val logger = Logger(Messenger::class)
    private var pendingMessageQueue: MutableList<Any>? = null
    private var deliverySuspended: Boolean = false

    private val handler: Handler

    init {
        pendingMessageQueue = ArrayList<Any>()

        handler = Handler(Handler.Callback { msg ->
            if (msg.what == POSTED_MESSAGE) {
                deliverPostedMessages()
            }
            false
        })
    }

    /**
     * Suspends the delivery of the posted messages so that repetitive messages can be combined.
     */
    override fun suspendPostedMessageDelivery() {
        synchronized(handler) {
            deliverySuspended = true
        }
    }

    /**
     * Resumes the delivery of the posted messages.
     */
    override fun resumePostedMessageDelivery() {
        synchronized(handler) {
            deliverySuspended = false
        }

        // Post a message to the handler if one doesn't already exist.
        if (!handler.hasMessages(POSTED_MESSAGE)) {
            handler.sendEmptyMessage(POSTED_MESSAGE)
        }
    }

    /**
     * Delivers the queued posted messages.
     */
    private fun deliverPostedMessages() {
        var queue: MutableList<Any>? = null

        // Copy the queue reference and then create a new queue.
        synchronized(handler) {
            queue = pendingMessageQueue
            pendingMessageQueue = ArrayList<Any>()
        }

        // Send the messages.
        queue?.let {
            for (obj in it) {
                logger.log(VERBOSE, "deliver: " + obj.javaClass.simpleName)
                eventBus.post(obj)
            }
        }
    }

    /**
     * Publishes a message immediately
     *
     * @param message The message to publish.
     */
    override fun publish(message: Any) {
        eventBus.post(message)
    }

    /**
     * Defers the publication of a message until control gets back to the Looper.
     * New messages will be combined with pending messages if they are combinable.
     *
     * @param message The message to publish.
     */
    override fun post(message: Any) {
        logger.log(VERBOSE, "post: " + message.javaClass.simpleName)

        synchronized(handler) {
            // Try to combine this message with existing messages.
            var found = false
            if (message is CombinableMessage) {

                for (pendingMessage in pendingMessageQueue!!) {
                    if (pendingMessage is CombinableMessage && pendingMessage.combineWith(message)) {
                        found = true
                        break
                    }
                }
            }

            // Add the message to the pending message queue if it wasn't combined with another.
            if (!found) {
                pendingMessageQueue!!.add(message)
            }

            // Post a message to the handler if one doesn't already exist.
            if (!deliverySuspended && !handler.hasMessages(POSTED_MESSAGE)) {
                handler.sendEmptyMessage(POSTED_MESSAGE)
            }
        }
    }

    /**
     * Adds the subscribers object methods marked with the @Subscribe attribute to the
     * EventBus's list of subscriptions.
     *
     * @param subscriber The object to subscribe.
     */
    override fun subscribe(subscriber: Any) {
        eventBus.register(subscriber)
    }

    /**
     * Unsubscribes all of the subscriber's methods.
     *
     * @param subscriber The object to subscribe.
     */
    override fun unsubscribeToAll(subscriber: Any) {
        eventBus.unregister(subscriber)
    }

    companion object {
        private val POSTED_MESSAGE = 1
    }
}
