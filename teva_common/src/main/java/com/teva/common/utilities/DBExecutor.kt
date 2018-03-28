//
// DBExecutor.kt
// teva_common
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.common.utilities

import com.teva.utilities.utilities.Logger
import java.util.*
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger

/**
 * Executor used by the DataTask and other background threads
 * lock the database and execute database operations.
 */
class DBExecutor : Executor {

    private val tasks = ArrayDeque<Runnable>()
    private var active: Runnable? = null

    /**
     * Queues up a Runnable to be executed within the database lock.
     *
     * @param r The Runnable to run.
     */
    @Synchronized override fun execute(r: Runnable) {
        tasks.offer(Runnable {
            try {
                doWork(r)
            } finally {
                scheduleNext()
            }
        })

        if (active == null) {
            scheduleNext()
        }
    }

    /**
     * Pulls the next Runnable from the queue and executes it in a background thread.
     */
    @Synchronized private fun scheduleNext() {
        active = tasks.poll()
        if (active != null) {
            threadPoolExecutor.execute(active)
        }
    }

    companion object {
        private val logger = Logger(DBExecutor::class)

        private val CORE_POOL_SIZE = 1
        private val MAXIMUM_POOL_SIZE = 1
        private val KEEP_ALIVE_SECONDS = 30

        private val dbLock = Any()

        private val workQueue = LinkedBlockingQueue<Runnable>(128)
        private val threadFactory = object : ThreadFactory {
            private val count = AtomicInteger(1)

            override fun newThread(r: Runnable): Thread {
                return Thread(r, "DB Thread #" + count.getAndIncrement())
            }
        }

        private val threadPoolExecutor = ThreadPoolExecutor(
                CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE_SECONDS.toLong(),
                TimeUnit.SECONDS, workQueue, threadFactory)

        /**
         * Wraps a Runnable with a synchronized block to serialize access
         * to the database.
         *
         * @param runnable The runnable to execute.
         */
        fun doWork(runnable: Runnable) {
            synchronized(dbLock) {
                runnable.run()
            }
        }
    }
}
