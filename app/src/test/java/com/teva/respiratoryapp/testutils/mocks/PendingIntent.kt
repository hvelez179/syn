//
// PendingIntent.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package android.app

import android.content.Context
import android.content.Intent

/**
 * This class is a mock implementation of the android PendingIntent class.
 */
open class PendingIntent {

    var flags: Int = 0
    var requestCode: Int = 0
    var intent: Intent? = null
    var context: Context? = null

    fun cancel() {

    }

    companion object {
        val FLAG_CANCEL_CURRENT = 1 shl 28

        /**
         * This method mocks the getBroadcast method used while setting and retrieving alarms.
         * While setting the alarm, we store the alarm ID and return a mock pending intent.
         * While retrieving the alarm, we return the mock pending intent only if the alarm
         * ID matches the one that was used while setting the alarm.
         */
        @JvmStatic
        fun getBroadcast(context: android.content.Context,
                         requestCode: Int, intent: Intent, flags: Int): PendingIntent {
            val pendingIntent = PendingIntent()
            pendingIntent.context = context
            pendingIntent.requestCode = requestCode
            pendingIntent.intent = intent
            pendingIntent.flags = flags

            return pendingIntent
        }
    }
}
