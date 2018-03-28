//
// AppService.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.support.annotation.RequiresApi
import com.teva.common.utilities.*

import com.teva.respiratoryapp.R
import com.teva.respiratoryapp.activity.DashboardActivity
import com.teva.devices.messages.DeviceConnectedMessage
import com.teva.devices.messages.DeviceDisconnectedMessage
import com.teva.devices.model.DeviceManager
import com.teva.devices.model.DeviceQuery
import com.teva.location.services.LocationService
import com.teva.utilities.services.DependencyProvider
import com.teva.utilities.utilities.Logger

import org.greenrobot.eventbus.Subscribe

import com.teva.utilities.utilities.Logger.Level.VERBOSE

/**
 * An Android service that controls the lifetime of the app services.
 */
class AppService : Service() {

    private val dependencyProvider: DependencyProvider = DependencyProvider.default

    /**
     * Return the communication channel to the service.
     *
     * @param intent The Intent that was used to bind to this service
     * @return An IBinder through which clients can call on to the service
     */
    override fun onBind(intent: Intent): IBinder? {
        logger.log(VERBOSE, "onBind")

        return Binder()
    }

    /**
     * Called by the system every time a client explicitly starts the service by calling startService(Intent).
     *
     * @param intent  The Intent supplied to startService(Intent)
     * @param flags   Additional data about this start request
     * @param startId A unique integer representing this specific request to start
     * @return One of the constants associated with the START_CONTINUATION_MASK bits
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        logger.log(VERBOSE, "onStartCommand")
        return START_STICKY
    }

    /**
     * Called by the system when the service is first created.
     */
    override fun onCreate() {
        logger.log(VERBOSE, "onCreate")
        super.onCreate()

        updateForegroundState()

        startServices()
    }

    /**
     * Called by the system to notify a Service that it is no longer used and is being removed.
     */
    override fun onDestroy() {
        logger.log(VERBOSE, "onDestroy")
        super.onDestroy()

        stopServices()

        stopForeground(true)
    }

    /**
     * Updates the foreground service persistent notification.
     */
    private fun updateForegroundState() {
        // retrieve the number of connected inhalers

        DataTask<Unit, Int>("AppSerivce_updateForegroundState")
                .inBackground {
                    dependencyProvider.resolve<DeviceQuery>().getConnectedDeviceCount()
                }
                .onResult { deviceCount ->
                    // update the foreground notification.
                    val notificationIntent = Intent(this@AppService, DashboardActivity::class.java)
                    val pendingIntent = PendingIntent.getActivity(this@AppService, 0, notificationIntent, 0)

                    val message: String
                    if (deviceCount == 0) {
                        message = getString(R.string.service_notification_message_zero_text)
                    } else if (deviceCount == 1) {
                        message = getString(R.string.service_notification_message_one_text)
                    } else {
                        message = getString(R.string.service_notification_message_many_text, deviceCount)
                    }

                    val bigTextStyle = Notification.BigTextStyle()
                            .setBigContentTitle(getText(R.string.service_notification_title_text))
                            .bigText(message)

                    val notification: Notification =
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                Notification.Builder(this@AppService, createNotificationChannel())
                                        .setContentTitle(getText(R.string.service_notification_title_text))
                                        .setContentText(message)
                                        .setSmallIcon(R.drawable.ic_notification)
                                        .setContentIntent(pendingIntent)
                                        .setStyle(bigTextStyle)
                                        .build()


                            } else {
                                Notification.Builder(this@AppService)
                                        .setContentTitle(getText(R.string.service_notification_title_text))
                                        .setContentText(message)
                                        .setSmallIcon(R.drawable.ic_notification)
                                        .setPriority(Notification.PRIORITY_MIN)
                                        .setContentIntent(pendingIntent)
                                        .setStyle(bigTextStyle)
                                        .build()
                            }

                    startForeground(ONGOING_NOTIFICATION_ID, notification)
                }
                .execute()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(): String{
        val channelId = "respiratoryapp_service"
        val channelName = "RespiratoryApp Service"
        val channel = NotificationChannel(channelId,
                channelName, NotificationManager.IMPORTANCE_DEFAULT)
        channel.lockscreenVisibility = Notification.VISIBILITY_SECRET
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(channel)
        return channelId
    }

    /**
     * Message handler for the DeviceConnectedMessage.
     * Updates the message in the foreground notification.
     * @param message The message received.
     */
    @Suppress("UNUSED_PARAMETER")
    @Subscribe
    fun onDeviceConnectedMessage(message: DeviceConnectedMessage) {
        updateForegroundState()
    }

    /**
     * Message handler for the DeviceDisconnectedMessage.
     * Updates the message in the foreground notification.
     * @param message The message received.
     */
    @Suppress("UNUSED_PARAMETER")
    @Subscribe
    fun onDeviceDisconnectedMessage(message: DeviceDisconnectedMessage) {
        updateForegroundState()
    }

    /**
     * Starts the app servces.
     */
    private fun startServices() {
        logger.log(VERBOSE, "startServices")

        dependencyProvider.resolve<LocationService>().startService()
        dependencyProvider.resolve<DeviceManager>().start()

        dependencyProvider.resolve<Messenger>().subscribe(this)
    }

    /**
     * Stops the app services.
     */
    private fun stopServices() {
        logger.log(VERBOSE, "stopServices")

        dependencyProvider.resolve<LocationService>().stopService()
        dependencyProvider.resolve<DeviceManager>().stop()

        dependencyProvider.resolve<Messenger>().unsubscribeToAll(this)
    }

    companion object {
        private val logger = Logger(AppService::class)

        private val ONGOING_NOTIFICATION_ID = 4321
    }
}

