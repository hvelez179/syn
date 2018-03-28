//
// Handler.java
// teva_devices
//
// Copyright (c) 2017 Teva. All rights reserved.
//

package android.os


import java.util.*

@Suppress("unused", "UNUSED_PARAMETER")
/**
 * Shadow version of the Android Handler class that allows testing of code that uses the
 * handler to post messages from a worker thread to the main thread.
 */
open class Handler {

    /**
     * Default Constructor
     */
    constructor() {}

    /**
     * Constructor
     * @param looper Ignored in this mock.
     */
    constructor(looper: Looper) {}

    /**
     * Adds a Runnable to the run queue that is executed later by the
     * mock when the unit test calls loop().
     * @param runnable The runnable to add.
     * *
     * @return True if the runnable was added to the queue, false otherwise
     */
    fun post(runnable: Runnable): Boolean {
        return queue.offer(QueueRecord(postCycle, runnable))
    }

    /**
     * Adds a Runnable to the run queue that is executed later by the
     * mock when the unit test calls loop().
     * @param runnable The runnable to add.
     * *
     * @param delayMilliseconds Ignored in this mock.
     * *
     * @return True if the runnable was added to the queue, false otherwise
     */
    fun postDelayed(runnable: Runnable, delayMilliseconds: Long): Boolean {
        val cycle = if (delayMilliseconds == 0L) postCycle else postCycle + 1
        return queue.offer(QueueRecord(cycle, runnable))
    }

    /**
     * Mocks the sending of a message by adding a runnable to the run queue
     * that will latter be executed by the mock when the unit test calls loop().
     * @param what The id of the message.
     * *
     * @param delayMillis Ignored by this mock.
     */
    fun sendEmptyMessageDelayed(what: Int, delayMillis: Long): Boolean {
        postDelayed(Runnable {
            val msg = Message()
            msg.what = what

            handleMessage(msg)
        }, delayMillis)

        return true
    }

    /**
     * Mocks the sending of a message by adding a runnable to the run queue
     * that will latter be executed by the mock when the unit test calls loop().
     * @param what The id of the message.
     */
    fun sendEmptyMessage(what: Int): Boolean {
        return sendEmptyMessageDelayed(what, 0)
    }

    /**
     * This method is the handler for messages.  It is intended to be
     * overridden by derived classes.
     * @param msg The message received.
     */
    open fun handleMessage(msg: Message) {

    }

    /**
     * This method checks to see if a message with a specific id has
     * already been queued.  It is not supported by this mock.
     * @param what Ignored by this mock
     * *
     * @return always returns false.
     */
    fun hasMessages(what: Int): Boolean {
        return false
    }

    /**
     * This method removes a message with a specific id from the queue.
     * It is not implemented in this mock.
     * @param what Ingnored by this mock.
     */
    fun removeMessages(what: Int) {

    }

    /**
     * This method removes all instances of a Runnable from the queue.
     *
     * @param runnable The runnable to remove, or null to remove all.
     */
    fun removeCallbacks(runnable: Runnable?) {
        if (runnable == null) {
            queue.clear()
        } else {
            queue.removeIf { it.runnable == runnable }
        }
    }

    data class QueueRecord(val cycle: Int, val runnable: Runnable)

    companion object {
        private var postCycle = 0
        private val queue = ArrayDeque<QueueRecord>()

        fun clearQueue() {
            queue.clear()
        }

        /**
         * This method executes all of the Runnables queued in the mock.
         * It is intended to be called by a unit test.
         */
        fun loop() {
            var record = queue.find { it.cycle == postCycle }
            while (record != null) {
                record.runnable.run()

                queue.remove(record)
                record = queue.find { it.cycle == postCycle }
            }

            postCycle++
        }
    }
}
