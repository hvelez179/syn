///
// AsyncTask.kt
// teva_common
//
// Copyright © 2017 Teva. All rights reserved
///

package android.os


import android.support.annotation.MainThread
import android.support.annotation.WorkerThread

import java.util.concurrent.Executor

/**
 * Shadow version of the Android AsyncTask class that executes tasks immediately
 * instead of using a thread.
 */
class AsyncTask<Params, Progress, Result> {
    /**
     * This method is overridden to perform the actual task.
     */
    @WorkerThread
    protected fun doInBackground(vararg params: Params): Result? {
        return null
    }

    /**
     * Shadow method. Does nothing.
     */
    protected fun onCancelled() {}

    /**
     * Shadow method. Does nothing.
     */
    protected fun onCancelled(result: Result?) {}

    /**
     * This method is overridden to perform any operations before the actual task.
     */
    protected fun onPreExecute() {}

    /**
     * This method is overridden to perform any operations after the actual task.
     */
    protected fun onPostExecute(result: Result?) {}

    /**
     * Shadow method. Does nothing.
     */
    protected fun onProgressUpdate(vararg values: Progress) {}

    /**
     * This method executes the task immediately.
     */
    fun execute(vararg params: Params): AsyncTask<Params, Progress, Result> {
        return executeOnExecutor(null, *params)
    }

    /**
     * This method executes the task along with the pre and post operations.
     */
    fun executeOnExecutor(exec: Executor?, vararg params: Params): AsyncTask<Params, Progress, Result> {
        onPreExecute()
        val result = doInBackground(*params)
        onPostExecute(result)

        return this
    }
}
