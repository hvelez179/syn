//
// Notification.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package android.app

import android.content.Context
import android.net.Uri

/**
 * Shadow class for Notification and Notification.Builder
 */

open class Notification {
    var contentText: CharSequence? = null
    var contentTitle: CharSequence? = null
    var smallIcon: Int = 0
    var contentIntent: PendingIntent? = null
    var sound: Uri? = null

    class Builder(context: Context) {
        internal var notification: Notification? = null

        init {
            notification = Notification()
        }

        fun setContentText(contentText: CharSequence): Builder {
            notification!!.contentText = contentText
            return this
        }

        fun setContentTitle(contentTitle: CharSequence): Builder {
            notification!!.contentTitle = contentTitle
            return this
        }

        fun setSmallIcon(smallIcon: Int): Builder {
            notification!!.smallIcon = smallIcon
            return this
        }

        fun setContentIntent(intent: PendingIntent): Builder {
            notification!!.contentIntent = intent
            return this
        }

        fun setSound(soundUri: Uri?): Builder {
            notification!!.sound = soundUri ?: Uri.EMPTY
            return this
        }

        fun build(): Notification? {
            return notification
        }
    }
}
