//
// AsyncTask.java
// teva_devices
//
// Copyright (c) 2017 Teva. All rights reserved.
//

package android.os


import android.support.annotation.WorkerThread
import java.util.concurrent.Executor

/**
 * Shadow version of the Android AsyncTask class that executes tasks immediately
 * instead of using a thread.
 */
@Suppress("unused", "UNUSED_PARAMETER")
open class AsyncTask<Params, Progress, Result> {
    @WorkerThread
    open protected fun doInBackground(vararg params: Params): Result? {
        return null
    }

    open protected fun onCancelled() {}

    open protected fun onCancelled(result: Result?) {}

    open protected fun onPreExecute() {}

    open protected fun onPostExecute(result: Result?) {}

    open protected fun onProgressUpdate(vararg values: Progress) {}

    fun execute(vararg params: Params): AsyncTask<Params, Progress, Result> {
        return executeOnExecutor(null, *params)
    }

    fun executeOnExecutor(exec: Executor?, vararg params: Params): AsyncTask<Params, Progress, Result> {
        onPreExecute()
        val result = doInBackground(*params)
        onPostExecute(result)

        return this
    }
}
