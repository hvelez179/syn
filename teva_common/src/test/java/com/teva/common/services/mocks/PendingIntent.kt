//
// PendingIntent.java
// teva_common
//
// Copyright (c) 2017 Teva. All rights reserved.
//

package android.app

import org.mockito.Mockito.mock

/**
 * This class is a mock implementation of the android PendingIntent class.
 */
class PendingIntent {

    fun cancel() {

    }

    companion object {

        internal var pendingIntentMock = PendingIntent()
        internal var alarmId: String? = null
        private val FLAG_NO_CREATE = 1 shl 29

        /**
         * This method mocks the getBroadcast method used while setting and retrieving alarms.
         * While setting the alarm, we store the alarm ID and return a mock pending intent.
         * While retrieving the alarm, we return the mock pending intent only if the alarm
         * ID matches the one that was used while setting the alarm.
         */
        @JvmStatic
        fun getBroadcast(context: android.content.Context,
                         requestCode: Int, intent: android.content.Intent, flags: Int): PendingIntent? {
            if (flags == FLAG_NO_CREATE) {
                return if (intent.getAction() == alarmId) pendingIntentMock else null
            } else {
                alarmId = intent.getAction()
                return pendingIntentMock
            }
        }
    }
}
