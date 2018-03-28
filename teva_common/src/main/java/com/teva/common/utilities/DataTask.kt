//
// DataTask.ktkt
// teva_common
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.common.utilities

import android.os.AsyncTask
import android.support.annotation.WorkerThread
import com.teva.utilities.services.DependencyProvider
import com.teva.utilities.utilities.Logger
import com.teva.utilities.utilities.Logger.Level.*
import org.threeten.bp.Instant
import org.threeten.bp.temporal.ChronoUnit

/**
 * A specialization of the AsyncTask class that is used by the app to perform database tasks
 * on a worker thread.
 *
 * @param <Params>   The input type.
 * @param <Progress> The progress type.
 * @param <Result>   The result type.
 * @param name The task name for logging purposes
*/
@Suppress("UNUSED_PARAMETER")
open class DataTask<Params, Result>(name: String?) {

    private val task: AsyncTask<Params, Unit, Result>
    private val taskName: String? = name ?: "<unnamed>"
    private val messenger: Messenger = DependencyProvider.default.resolve(Messenger::class.java)

    private var inBackgroundFunc: ((param: Array<out Params>)->Result?)? = null
    private var beforeTaskFunc: (()->Unit)? = null
    private var onResultFunc: ((result: Result?)->Unit)? = null
    private var whenCanceledFunc: ((result: Result?)->Unit)? = null
    private var whenCompleteFunc: (()->Unit)? = null

    init {
        task = object : AsyncTask<Params, Unit, Result>() {
            /**
             * Called in a background thread to execute the task.
             *
             * @param params The task parameters
             * @return The result of the task.
             */
            @SafeVarargs
            override fun doInBackground(vararg params: Params): Result? {
                logger.log(VERBOSE, taskName + " starting database operation")
                val result: Result?

                messenger.suspendPostedMessageDelivery()

                try {
                    val startInstant = Instant.now()

                    result = this@DataTask.doInBackground(*params)

                    //warn if Database Tasks are taking too long.
                    val elapsed = ChronoUnit.MILLIS.between(startInstant, Instant.now())

                    logger.log(if (elapsed > MAX_DATABASE_OPERATION_TIME) WARN else INFO,
                            "$taskName database operation took $elapsed ms")
                } finally {
                    messenger.resumePostedMessageDelivery()
                }

                return result
            }

            /**
             * Called in the main thread when the task is canceled.
             */
            override fun onCancelled() {
                this@DataTask.onCancelled()
            }

            /**
             * Called in the main thread when the task is cancelled and a result was provided.
             *
             * @param result The result of the canceled task.
             */
            override fun onCancelled(result: Result?) {
                this@DataTask.onCancelled(result)
                onCancelled()
                onComplete()
            }

            /**
             * Called in the main thread before the task is executed in a background thread.
             */
            override fun onPreExecute() {
                this@DataTask.onPreExecute()
            }

            /**
             * Called in the main thread after the task complets.
             *
             * @param result The result of the task.
             */
            override fun onPostExecute(result: Result?) {
                this@DataTask.onPostExecute(result)
                onComplete()
            }

            /**
             * Called in the main thread to deliver progress notifications.
             */
            @SafeVarargs
            override fun onProgressUpdate(vararg values: Unit) {
                this@DataTask.onProgressUpdate(*values)
            }
        }
    }

    /**
     * Called to perform the task on a worker thread.
     *
     * @param params The task parameters
     * @return The result of the task.
     */
    @WorkerThread
    protected open fun doInBackground(vararg params: Params): Result? {
        return inBackgroundFunc?.invoke(params)
    }

    /**
     * Called on the main thread when the task is canceled.
     */
    protected fun onCancelled() {}

    /**
     * Called on the main thread when the task is canceled after providing a result.
     *
     * @param result The result of the task.
     */
    protected fun onCancelled(result: Result?) {
        whenCanceledFunc?.invoke(result)
        whenCompleteFunc?.invoke()
    }

    /**
     * Called on the main thread before the task is executed.
     */
    protected fun onPreExecute() {
        beforeTaskFunc?.invoke()
    }

    /**
     * Called on the main thread after the task is executed.
     *
     * @param result The result of the task.
     */
    protected open fun onPostExecute(result: Result?) {
        onResultFunc?.invoke(result)
    }

    /**
     * Called on the main thread with progress from the task.
     *
     * @param values
     */
    protected fun onProgressUpdate(vararg values: Unit) {}

    /**
     * This method is called after the task completes and onPostExecute() or onCanceled()
     * has been called.
     */
    protected open fun onComplete() {
        whenCompleteFunc?.invoke()
    }

    /**
     * Executes the task with the specified parameters.
     *
     * @param params The parameters of the task.
     */
    fun execute(vararg params: Params) {
        task.executeOnExecutor(executor, *params)
    }

    /**
     * Builder method that sets the inBackground function.
     *
     * @param func The lamda method to call in a background thread.
     */
    fun inBackground(func: (param: Array<out Params>)->Result?) : DataTask<Params, Result> {
        inBackgroundFunc = func

        return this
    }

    /**
     * Builder method that sets the beforeTask function.
     *
     * @param func The lamda method to call before the task is run in the background.
     */
    fun beforeTask(func: ()->Unit) : DataTask<Params, Result> {
        beforeTaskFunc = func

        return this
    }

    /**
     * Builder method that sets the onResult function.
     *
     * @param func The lamda method to call in with the task result.
     */
    fun onResult(func: (result: Result?)->Unit) : DataTask<Params, Result> {
        onResultFunc = func

        return this
    }

    /**
     * Builder method that sets the whenCanceled function.
     *
     * @param func The lamda method to call when the task is canceled.
     */
    fun whenCanceled(func: (result: Result?)->Unit) : DataTask<Params, Result> {
        whenCanceledFunc = func

        return this
    }

    /**
     * Builder method that sets the whenComplete function.
     *
     * @param func The lamda method to call after onResult() or whenCanceled().
     */
    fun whenComplete(func: ()->Unit) : DataTask<Params, Result> {
        whenCompleteFunc = func

        return this
    }

    companion object {
        private val logger = Logger("DataTask")

        private val MAX_DATABASE_OPERATION_TIME: Long = 250

        private val executor = DBExecutor()
    }
}

