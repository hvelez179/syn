/*
 *
 *  Logger.kt
 *  teva_utilities
 *
 *  Copyright © 2018 Teva. All rights reserved.
 *
 */

package com.teva.utilities.utilities

import android.util.Log
import org.jetbrains.annotations.NonNls
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.ByteBuffer
import kotlin.reflect.KClass

/**
 * Configurable logger
 */
class Logger(val tag: String) {
    val logLevel = config[tag] ?: config["Default"] ?: Level.INFO

    constructor(type: KClass<*>) : this(type.java.simpleName)

    /**
     * Gets a value indicating whether the specified log level is enabled.
     *
     * @param requestedLevel The log level to check.
     * @return True if the log level is enabled, false otherwise.
     */
    fun isEnabled(requestedLevel: Level): Boolean {
        return requestedLevel.level >= logLevel.level
    }

    /**
     * Calls the appropriate log method based the log level.
     *
     * @param level     The log level.
     * @param message   The log message.
     * @param throwable An exception to log.
     */
    private fun sendToLog(level: Level, @NonNls message: String, throwable: Throwable?) {
        when (level) {
            Level.VERBOSE -> Log.v(tag, message, throwable)
            Level.DEBUG -> Log.d(tag, message, throwable)
            Level.INFO -> Log.i(tag, message, throwable)
            Level.WARN -> Log.w(tag, message, throwable)
            Level.ERROR -> Log.e(tag, message, throwable)
            else -> {}
        }
    }

    /**
     * Logs a message without any string formating.
     * Used when the message might contain format characters.
     *
     * @param level   The log level
     * @param message The log message
     */
    fun log(level: Level, @NonNls message: String) {
        if (logLevel.level <= level.level) {
            sendToLog(level, message, null)
        }
    }

    /**
     * Logs a message with string formatting.
     *
     * @param level   The log level
     * @param message The format string for the log message
     * @param params  The message parameters
     */
    fun log(level: Level, @NonNls message: String, vararg params: Any) {
        if (logLevel.level <= level.level) {
            sendToLog(level, String.format(message, *params), null)
        }
    }

    /**
     * Logs a message with an exception.
     *
     * @param level     The log level
     * @param message   The message format string
     * @param throwable The exception object
     * @param params    The message parameters
     */
    fun logException(level: Level, @NonNls message: String, throwable: Throwable, vararg params: Any) {
        if (logLevel.level <= level.level) {
            sendToLog(level, String.format(message, *params), throwable)
        }
    }

    /**
     * Enumeration used to specify the log level
     *
     * @param level The raw level value
     */
    enum class Level(internal var level: Int) {
        VERBOSE(0),
        DEBUG(1),
        INFO(2),
        WARN(3),
        ERROR(4),
        DISABLED(5)
    }

    companion object {
        val config = HashMap<String, Level>()

        fun configure(inputStream: InputStream) {
            val sb = StringBuilder()
            val br = BufferedReader(InputStreamReader(inputStream))

            try {
                var line = br.readLine()
                while (line != null) {
                    sb.append(line)
                    line = br.readLine()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                try {
                    br.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }

            try {
                val jsonObject = JSONObject(sb.toString())
                val iterator = jsonObject.keys()

                while (iterator.hasNext()) {
                    val key = iterator.next()
                    val value = jsonObject.getString(key)
                    val level = Level.valueOf(value)

                    config[key] = level
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }

        /**
         * Helper method that converts a byte array to a hex string.
         *
         * @param buffer The byte array
         * @return The hex string
         */
        fun toHexString(buffer: ByteArray?): String {
            if (buffer == null) {
                return "<null>"
            }

            return toHexString(buffer, 0, buffer.size)
        }

        /**
         * Helper method that converts a byte array to a hex string.
         *
         * @param buffer The byte array
         * @param start  The start position
         * @param len    The number of bytes to convert
         * @return The hex string
         */
        fun toHexString(buffer: ByteArray?, start: Int, len: Int): String {
            var len = len
            if (buffer == null) {
                return "<null>"
            }

            if (start + len > buffer.size) {
                return "<invalid>"
            }

            val stringBuilder = StringBuilder(buffer.size * 3 - 1)
            val byteBuffer = ByteBuffer.wrap(buffer)
            byteBuffer.position(start)

            while (len > 0) {
                stringBuilder.append(String.format("%02x", byteBuffer.get()))
                len--
                if (len > 0) {
                    stringBuilder.append(" ")
                }
            }

            return stringBuilder.toString()
        }

    }
}