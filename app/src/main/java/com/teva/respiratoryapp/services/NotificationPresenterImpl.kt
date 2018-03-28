//
// NotificationPresenterImpl.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.services

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.teva.utilities.services.DependencyProvider
import com.teva.common.utilities.LocalizationService
import com.teva.utilities.utilities.Logger
import com.teva.utilities.utilities.Logger.Level.DEBUG
import com.teva.utilities.utilities.Logger.Level.VERBOSE
import com.teva.notifications.services.NotificationPresenter
import com.teva.notifications.services.notification.NotificationDataKey
import com.teva.notifications.services.notification.NotificationInfo
import com.teva.respiratoryapp.R
import com.teva.respiratoryapp.activity.DashboardActivity
import com.teva.respiratoryapp.models.notification.NotificationCategories
import android.media.RingtoneManager



/**
 * Class that creates Android Notification objects from a NotificationData object.
 *
 * @param dependencyProvider The dependency injection mechanism.
 */
class NotificationPresenterImpl(private val dependencyProvider: DependencyProvider) : NotificationPresenter {

    private var inForeground: Boolean = false

    init {
        Receiver.notificationPresenter = this
    }

    /**
     * Creates an Android notification object for the specified NotificationData.
     *
     * @param notificationInfo The NotificationData containing the description of the notification.
     */
    override fun displayNotification(notificationInfo: NotificationInfo) {
        logger.log(VERBOSE, "displayNotification")

        val context = dependencyProvider.resolve<Context>()

        val data = notificationInfo.notificationData
        val categoryId = notificationInfo.categoryId
        val notificationId = data[NotificationDataKey.NOTIFICATION_ID] as String

        val notificationCategory = NotificationCategories.findCategory(categoryId)

        if (inForeground) {
            // The app is in the foreground, so just display the popup if enabled for this notification.
            if (notificationCategory.showWhenInForeground) {
                sendToDashboard(notificationInfo, false)
            }
        } else {
            // The app is in the background, so send the notification to Android
            val notificationIntent = Intent(context, Receiver::class.java)
            notificationIntent.action = NOTIFICATION_ACTION + "." + notificationId
            notificationIntent.putExtra(NOTIFICATION_EXTRA, notificationInfo)

            val tappedPendingIntent = PendingIntent.getBroadcast(context,
                    NOTIFICATION_REQUEST_CODE, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT)

            val localizationService = dependencyProvider.resolve<LocalizationService>()
            val dataMap = notificationInfo.notificationData
            var headerStringId = notificationCategory.headerStringId

            if (headerStringId == 0) {
                headerStringId = R.string.app_name
            }

            val contentTitle = localizationService.getString(headerStringId, dataMap)
            val contentText = localizationService.getString(notificationCategory.notificationStringId ?: notificationCategory.bodyStringId, dataMap)
            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val notification = Notification.Builder(context)
                    .setContentText(contentText)
                    .setContentTitle(contentTitle)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentIntent(tappedPendingIntent)
                    .setSound(soundUri)
                    .build()

            logger.log(VERBOSE, "posting notification: " + notificationId)
            val notificationManager = dependencyProvider.resolve<NotificationManager>()
            notificationManager.notify(notificationId, 0, notification)
        }
    }

    /**
     * Processes a notification intent received from Android.
     *
     * @param intent The intent to process.
     * @return True if the intent was a notification intent, false otherwise.
     */
    private fun processIntent(intent: Intent): Boolean {
        var result = false

        val notificationInfo = intent.getParcelableExtra<NotificationInfo>(NOTIFICATION_EXTRA)
        if (notificationInfo != null) {
            result = true

            val data = notificationInfo.notificationData
            val notificationId = data[NotificationDataKey.NOTIFICATION_ID] as String

            // Clear the notification
            val notificationManager = dependencyProvider.resolve<NotificationManager>()
            notificationManager.cancel(notificationId, 0)

            // relay the intent to the Dashboard.
            sendToDashboard(notificationInfo, true)
        }

        return result
    }

    /**
     * This method sends the notification to the DashboardActivity.
     *
     * @param notificationInfo The notification to send.
     * @param fromNotification A value indicating whether the source was a notification that the
     * *                         user clicked on.
     */
    private fun sendToDashboard(notificationInfo: NotificationInfo, fromNotification: Boolean) {
        val data = notificationInfo.notificationData
        val notificationId = data[NotificationDataKey.NOTIFICATION_ID] as String

        logger.log(DEBUG, "sendToDashboard: " + notificationId)

        val context = dependencyProvider.resolve<Context>()

        val dashboardIntent = Intent(context, DashboardActivity::class.java)
        dashboardIntent.action = NOTIFICATION_ACTION + "." + notificationId
        dashboardIntent.putExtra(NOTIFICATION_EXTRA, notificationInfo)
        dashboardIntent.putExtra(FROM_NOTIFICATION_EXTRA, fromNotification)
        dashboardIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(dashboardIntent)
    }

    /**
     * Sets a value indicating whether the app is in the foreground.
     *
     * @param inForeground True if the app is in the foreground, false otherwise.
     */
    override fun setInForeground(inForeground: Boolean) {
        this.inForeground = inForeground
    }

    /**
     * This class is the BroadcastReceiver that receives messages from Android when
     * the user clicks on a notification.
     */
    class Receiver : BroadcastReceiver() {

        /**
         * This method is called when the user taps or swipes a notification.
         *
         * @param context The Context in which the receiver is running.
         * @param intent  The Intent being received.
         */
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action.startsWith(NOTIFICATION_ACTION)) {
                notificationPresenter!!.processIntent(intent)
            }
        }

        companion object {
            internal var notificationPresenter: NotificationPresenterImpl? = null
        }
    }

    companion object {
        private val logger = Logger(NotificationPresenterImpl::class)

        private val NOTIFICATION_REQUEST_CODE = 1
        val NOTIFICATION_ACTION = "com.teva.respiratoryapp.NOTIFICATION"
        val NOTIFICATION_EXTRA = "NotificationExtra"
        val FROM_NOTIFICATION_EXTRA = "FromNotificationExtra"
    }
}
