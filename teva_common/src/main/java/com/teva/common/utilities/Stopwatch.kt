//
// Stopwatch.kt
// teva_common
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.common.utilities

import com.teva.utilities.utilities.Logger
import org.threeten.bp.Duration
import org.threeten.bp.Instant

/**
 * Logging utility class to measure the duration of activities.
 *
 * @param logger The logger to log messages to.
 */
class Stopwatch(private val logger: Logger) {
    private val startInstant: Instant = Instant.now()
    private var lastMark: Instant? = null

    init {
        lastMark = startInstant
    }

    /**
     * Logs a duration message with the current stopwatch time.
     * @param level The log level
     * *
     * @param message The message to log
     */
    fun mark(level: Logger.Level, message: String) {
        val now = Instant.now()
        val duration = Duration.between(startInstant, now)
        val durationSinceLastMark = Duration.between(lastMark!!, now)
        lastMark = now
        logger.log(level, "%s %s - %s", message, durationSinceLastMark.toString(), duration.toString())
    }

    companion object {
        /**
         * Creates and starts a new Stopwatch.
         * @param logger The logger to log messages to.
         * *
         * @return
         */
        fun Start(logger: Logger): Stopwatch {
            return Stopwatch(logger)
        }
    }
}
