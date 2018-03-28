//
// AsyncTask.kt
// teva_location
//
// Copyright (c) 2017 Teva. All rights reserved.
//
package android.os


import android.support.annotation.MainThread
import android.support.annotation.WorkerThread

import java.util.concurrent.Executor

/**
 * This class is a mock implementation for the AsyncTask class provided by Android
 * Api as the original class cannot be mocked.
 * @param <Params> - the type of the parameters sent to the task upon execution.
 * *
 * @param <Progress> - the type of the progress units published during the background computation.
 * *
 * @param <Result> - the type of the result of the background computation.
</Result></Progress></Params> */
open class AsyncTask<Params, Progress, Result> {

    /**
     * This step is used to perform background computation.
     * @param params - the parameters of the asynchronous task are passed to this method.
     * *
     * @return - the result of the computation.
     */
    @WorkerThread
    open protected fun doInBackground(vararg params: Params): Result? {
        return null
    }

    /**
     * This method is invoked by the overloaded onCancelled method.
     * We currently do not mock this.
     */
    open protected fun onCancelled() {}

    /**
     * This method is executed when the task is cancelled.
     * We currently do not mock this.
     */
    open protected fun onCancelled(result: Result?) {}

    /**
     * This method is invoked before the task is executed.
     */
    open protected fun onPreExecute() {}

    /**
     * This method is invoked after the background computation finishes.
     * @param result - the result of the background computation.
     */
    open protected fun onPostExecute(result: Result?) {}

    /**
     * This method is used to display progress in the user interface.
     * We currently do not mock this.
     * @param values - the values containing progress information.
     */
    open protected fun onProgressUpdate(vararg values: Progress) {}

    /**
     * Invoke the executeOnExecutor method for actual execution.
     * @param params - the parameters of the asynchronous task.
     * *
     * @return - the current AsyncTask.
     */
    fun execute(vararg params: Params): AsyncTask<Params, Progress, Result> {
        return executeOnExecutor(null, *params)
    }

    /**
     * We mock the actual AsyncTask implementation here by invoking the execution
     * methods in sequence.
     * @param exec - the executor.
     * *
     * @param params - the parameters of the asynchronous task.
     * *
     * @return - the current AsyncTask.
     */
    @Suppress("UNUSED_PARAMETER")
    fun executeOnExecutor(exec: Executor?, vararg params: Params): AsyncTask<Params, Progress, Result> {
        onPreExecute()
        val result = doInBackground(*params)
        onPostExecute(result)

        return this
    }
}
