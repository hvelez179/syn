///
// AsyncTaskHelper.java
// app
//
// Copyright © 2017 Teva. All rights reserved
///

package com.teva.respiratoryapp.testutils.mocks

import android.os.AsyncTask

/**
 * Helper class that allows the mock-specific AsyncTask methods to be called without
 * displaying errors in the editor for the test classes.
 */
object AsyncTaskHelper {
    /**
     * Begins queuing tasks to be run later.
     */
    fun beginTaskQueue() {
        AsyncTask.beginTaskQueue()
    }

    /**
     * Executes queued tasks synchronously on the calling thread.
     */
    fun executeQueuedTasks() {
        AsyncTask.executeQueuedTasks()
    }

    /**
     * Ends the queuing of tasks.
     */
    fun endTaskQueue() {
        AsyncTask.endTaskQueue()
    }
}
