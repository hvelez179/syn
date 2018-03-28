//
// AsyncTask.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package android.os


import android.support.annotation.WorkerThread
import java.util.*
import java.util.concurrent.Executor

/**
 * Shadow version of the Android AsyncTask class that queues tasks to be run later
 * instead of using a thread when running unit tests.

 * See the Android AsyncTask document for usage information.
 */
open class AsyncTask<Params, Progress, Result> {

    /**
     * Runs in the background to perform the task's work.
     * See Android AsyncTask documentation for more information.
     */
    @WorkerThread
    open protected fun doInBackground(vararg params: Params): Result? {
        return null
    }

    /**
     * Runs in the main thread when the task is cancelled.
     * See Android AsyncTask documentation for more information.
     */
    open protected fun onCancelled() {}

    /**
     * Runs in the main thread when the task is cancelled.
     * See Android AsyncTask documentation for more information.
     */
    open protected fun onCancelled(result: Result) {}

    /**
     * Runs in the main thread before the task's background work is performed
     * See Android AsyncTask documentation for more information.
     */
    open protected fun onPreExecute() {}

    /**
     * Runs in the main thread after the task's background work is performed
     * See Android AsyncTask documentation for more information.
     */
    open protected fun onPostExecute(result: Result?) {}

    /**
     * Runs in the main thread when the task reports progress.
     * See Android AsyncTask documentation for more information.
     */
    open protected fun onProgressUpdate(vararg values: Progress) {}

    /**
     * Runs the task. This shadow version calls the
     * executeOnExecutor() method.
     */
    fun execute(vararg params: Params): AsyncTask<Params, Progress, Result> {
        return executeOnExecutor(null, *params)
    }

    /**
     * Queues the task to be run later on the calling thread when executeQueuedTask()
     * is called.
     */
    fun executeOnExecutor(exec: Executor?, vararg params: Params): AsyncTask<Params, Progress, Result> {
        val runnable = Runnable {
            onPreExecute()
            val result = doInBackground(*params)
            onPostExecute(result)
        }

        if (runQueue != null) {
            runQueue!!.add(runnable)
        } else {
            runnable.run()
        }

        return this
    }

    companion object {
        var runQueue: MutableList<Runnable>? = null

        @JvmField
        val THREAD_POOL_EXECUTOR: Executor? = null

        /**
         * Begins queuing tasks to be run later.
         */
        @JvmStatic
        fun beginTaskQueue() {
            runQueue = ArrayList<Runnable>()
        }

        /**
         * Executes queued tasks synchronously on the calling thread.
         */
        @JvmStatic
        fun executeQueuedTasks() {
            while (!runQueue!!.isEmpty()) {
                val runnable = runQueue!!.removeAt(0)
                runnable.run()
            }
        }

        /**
         * Ends the queuing of tasks.
         */
        @JvmStatic
        fun endTaskQueue() {
            runQueue = null
        }
    }
}
