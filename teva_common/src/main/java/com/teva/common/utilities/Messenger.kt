//
// Messenger.kt
// teva_common
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.common.utilities

/**
 * This class provides a publish/subscribe message system using the EventBus class.
 *
 * @param eventBus The EventBus object that delivers the messages.
 */
interface Messenger {

    /**
     * Suspends the delivery of the posted messages so that repetitive messages can be combined.
     */
    fun suspendPostedMessageDelivery()
    /**
     * Resumes the delivery of the posted messages.
     */
    fun resumePostedMessageDelivery()

    /**
     * Publishes a message immediately
     *
     * @param message The message to publish.
     */
    fun publish(message: Any)

    /**
     * Defers the publication of a message until control gets back to the Looper.
     * New messages will be combined with pending messages if they are combinable.
     *
     * @param message The message to publish.
     */
    fun post(message: Any)
    /**
     * Adds the subscribers object methods marked with the @Subscribe attribute to the
     * EventBus's list of subscriptions.
     *
     * @param subscriber The object to subscribe.
     */
    fun subscribe(subscriber: Any)

    /**
     * Unsubscribes all of the subscriber's methods.
     *
     * @param subscriber The object to subscribe.
     */
    fun unsubscribeToAll(subscriber: Any)

    companion object {
        private val POSTED_MESSAGE = 1
    }
}
