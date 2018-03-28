//
// NotificationPresenterImplTest.java
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.services

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Parcelable
import com.nhaarman.mockito_kotlin.*
import com.teva.utilities.services.DependencyProvider
import com.teva.common.utilities.LocalizationService
import com.teva.notifications.services.notification.NotificationDataKey
import com.teva.notifications.services.notification.NotificationInfo
import com.teva.respiratoryapp.R
import com.teva.respiratoryapp.activity.DashboardActivity
import com.teva.respiratoryapp.activity.view.popup.DsaPopup
import com.teva.respiratoryapp.mocks.MockedLocalizationService
import com.teva.respiratoryapp.models.notification.NotificationCategories
import com.teva.respiratoryapp.models.notification.NotificationCategory
import com.teva.respiratoryapp.models.notification.NotificationType
import com.teva.respiratoryapp.testutils.NotificationDataMatcher
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.*

/**
 * Tests for the NotificationPresenterImpl class.
 */
class NotificationPresenterImplTest {

    private lateinit var androidNotificationManager: NotificationManager
    private lateinit var dependencyProvider: DependencyProvider
    private lateinit var context: Context

    @Before
    @Throws(Exception::class)
    fun setup() {
        dependencyProvider = DependencyProvider()

        context = mock()
        dependencyProvider.register(Context::class, context)

        val localizationService = MockedLocalizationService()
        localizationService.add(R.string.app_name, "respiratory app");
        localizationService.add(SHOW_BOTH_BODY_TEXT_ID, SHOW_BOTH_BODY_TEXT)
        localizationService.add(SHOW_FOREGROUND_BODY_TEXT_ID, SHOW_FOREGROUND_BODY_TEXT)
        localizationService.add(SHOW_BACKGROUND_BODY_TEXT_ID, SHOW_BACKGROUND_BODY_TEXT)

        dependencyProvider.register(LocalizationService::class, localizationService)

        androidNotificationManager = mock()
        dependencyProvider.register(NotificationManager::class, androidNotificationManager)

        val categories = NotificationCategories.categories as MutableMap
        categories.clear()
        categories[SHOW_BOTH_CATEGORY] =
                NotificationCategory(
                        SHOW_BOTH_CATEGORY,
                        NotificationType.ASK_USER_FEELING,
                        DsaPopup::class,
                        bodyStringId = SHOW_BOTH_BODY_TEXT_ID)

        categories[SHOW_FOREGROUND_CATEGORY] =
                NotificationCategory(
                        SHOW_FOREGROUND_CATEGORY,
                        NotificationType.ASK_USER_FEELING,
                        DsaPopup::class,
                        showWhenFromNotification = false,
                        bodyStringId = SHOW_FOREGROUND_BODY_TEXT_ID)

        categories[SHOW_BACKGROUND_CATEGORY] =
                NotificationCategory(
                        SHOW_BACKGROUND_CATEGORY,
                        NotificationType.ASK_USER_FEELING,
                        DsaPopup::class,
                        showWhenInForeground = false,
                        bodyStringId = SHOW_BACKGROUND_BODY_TEXT_ID)

    }

    @After
    fun tearDown() {
        // restore the NotificationCategories
        val categories = NotificationCategories.categories as MutableMap<String, NotificationCategory>
        categories.clear()

        NotificationCategories.createCategories().associateTo(categories) { Pair(it.categoryId, it)}
    }

    /**
     * Test that the DashboardActivity is directly called instead of producing a notification
     * if the app is in the foreground.
     */
    @Test
    @Throws(Exception::class)
    fun testThatDisplayNotificationSendsForegroundEnabledNotificationToDashboardInForeground() {
        // define expectations
        val expectedAction = "com.teva.respiratoryapp.NOTIFICATION.notification_id"
        val expectedFlags = Intent.FLAG_ACTIVITY_NEW_TASK
        val expectedFromNotification = false

        // configure input
        val otherDataKey = "other_data"
        val notificationId = "notification_id"
        val dataMap = HashMap<String, Any>()
        dataMap.put(NotificationDataKey.NOTIFICATION_ID, notificationId)
        dataMap.put(otherDataKey, 5)
        val notificationData = NotificationInfo(SHOW_BOTH_CATEGORY, dataMap)

        // execute test
        val notificationPresenter = NotificationPresenterImpl(dependencyProvider)
        notificationPresenter.setInForeground(true)
        notificationPresenter.displayNotification(notificationData)

        // verify results
        val intentArgumentCaptor = argumentCaptor<Intent>()

        // verify that Context.startActivity() was called.
        verify(context).startActivity(intentArgumentCaptor.capture())

        // verify that the correct intent was passed to Context.startActivity();
        val intent = intentArgumentCaptor.lastValue
        assertEquals(expectedAction, intent.getAction())
        assertEquals(expectedFlags.toLong(), intent.flags.toLong())
        assertEquals(expectedFromNotification, intent.getBooleanExtra(NotificationPresenterImpl.FROM_NOTIFICATION_EXTRA, true))

        val dataMatcher = NotificationDataMatcher.matchesNotificationData(notificationData)
        assertTrue(dataMatcher.matches(intent.getParcelableExtra<Parcelable>(NotificationPresenterImpl.NOTIFICATION_EXTRA)))
    }

    /**
     * Test that the DashboardActivity is not called when the app is in the foreground and
     * the NotificationCategory disables foreground notification.
     */
    @Test
    @Throws(Exception::class)
    fun testThatDisplayNotificationDoesNotSendsForegroundDisabledNotificationToDashboardInForeground() {
        // configure input
        val otherDataKey = "other_data"
        val notificationId = "notification_id"

        val dataMap = HashMap<String, Any>()
        dataMap.put(NotificationDataKey.NOTIFICATION_ID, notificationId)
        dataMap.put(otherDataKey, 5)
        val notificationData = NotificationInfo(SHOW_BACKGROUND_CATEGORY, dataMap)

        // execute test
        val notificationPresenter = NotificationPresenterImpl(dependencyProvider)
        notificationPresenter.setInForeground(true)
        notificationPresenter.displayNotification(notificationData)

        // verify that Context.startActivity() is not called.
        verify(context, never()).startActivity(any())
    }

    /**
     * Verify that a notification is displayed when the app is in the background.
     */
    @Test
    @Throws(Exception::class)
    fun testThatDisplayNotificationCreatesAndroidNotificationWhenInBackground() {
        // define expectations
        val expectedAction = "com.teva.respiratoryapp.NOTIFICATION.notification_id"
        val expectedIntentFlags = 0
        val expectedPendingIntentFlags = PendingIntent.FLAG_CANCEL_CURRENT

        // configure input
        val otherDataKey = "other_data"
        val notificationId = "notification_id"
        val dataMap = HashMap<String, Any>()
        dataMap.put(NotificationDataKey.NOTIFICATION_ID, notificationId)
        dataMap.put(otherDataKey, 5)
        val notificationData = NotificationInfo(SHOW_BOTH_CATEGORY, dataMap)

        // execute test
        val notificationPresenter = NotificationPresenterImpl(dependencyProvider)
        notificationPresenter.setInForeground(false)
        notificationPresenter.displayNotification(notificationData)

        // verify results
        val notificationArgumentCaptor = argumentCaptor<Notification>()

        // verify that NotificationManager.notify() was called.
        verify(androidNotificationManager).notify(eq(notificationId), eq(0), notificationArgumentCaptor.capture())

        // verify that the correct intent was passed to the notification;
        val notification = notificationArgumentCaptor.lastValue
        assertEquals(SHOW_BOTH_BODY_TEXT, notification.contentText)

        assertEquals(expectedPendingIntentFlags, notification.contentIntent!!.flags)
        val intent = notification.contentIntent!!.intent
        assertEquals(expectedAction, intent!!.getAction())
        assertEquals(expectedIntentFlags.toLong(), intent!!.flags.toLong())

        val dataMatcher = NotificationDataMatcher.matchesNotificationData(notificationData)
        assertTrue(dataMatcher.matches(intent.getParcelableExtra(NotificationPresenterImpl.NOTIFICATION_EXTRA)))
    }

    /**
     * Tests the reception of a intent from the NotificationManager
     * sent when a user clicks on it.
     */
    @Test
    @Throws(Exception::class)
    fun testThatNotificationReceivedFromAndroidIsSentToDashboard() {
        // Create an Intent to send to the receiver.
        val notificationId = "notification_id"
        val dataMap = HashMap<String, Any>()
        dataMap.put(NotificationDataKey.NOTIFICATION_ID, notificationId)
        val notificationData = NotificationInfo(SHOW_BOTH_CATEGORY, dataMap)

        val notificationIntent = Intent(context, NotificationPresenterImpl.Receiver::class.java)
        notificationIntent.setAction(NotificationPresenterImpl.NOTIFICATION_ACTION + "." + notificationId)
        notificationIntent.putExtra(NotificationPresenterImpl.NOTIFICATION_EXTRA, notificationData)

        // Create the objects under test
        val notificationPresenter = NotificationPresenterImpl(dependencyProvider)
        val receiver = NotificationPresenterImpl.Receiver()

        // Send the intent to the receiver.
        receiver.onReceive(context, notificationIntent)

        // Verify that Context.startActivity() is called with an intent
        // for the DashboardActivity.
        val expectedAction = NotificationPresenterImpl.NOTIFICATION_ACTION + "." + notificationId
        val expectedClass = DashboardActivity::class.java
        val expectedFlags = Intent.FLAG_ACTIVITY_NEW_TASK

        val intentArgumentCaptor = argumentCaptor<Intent>()
        verify(context).startActivity(intentArgumentCaptor.capture())

        val dashboardIntent = intentArgumentCaptor.lastValue
        assertEquals(expectedClass, dashboardIntent.cls)
        assertEquals(expectedAction, dashboardIntent.getAction())
        assertEquals(expectedFlags.toLong(), dashboardIntent.flags.toLong())

        val dataMatcher = NotificationDataMatcher.matchesNotificationData(notificationData)
        assertTrue(dataMatcher.matches(dashboardIntent.getParcelableExtra<Parcelable>(NotificationPresenterImpl.NOTIFICATION_EXTRA)))
    }

    /**
     * Tests the reception of a malformed intent from the NotificationManager.
     * Verify that nothing is forwarded to the DashboardActivity.
     */
    @Test
    @Throws(Exception::class)
    fun testThatMalformedNotificationIntentReceivedFromAndroidIsIgnored() {
        // Create an Intent to send to the receiver.
        // Don't put in the NotificationData.
        val notificationId = "notification_id"
        val notificationIntent = Intent(context, NotificationPresenterImpl.Receiver::class.java)
        notificationIntent.setAction(NotificationPresenterImpl.NOTIFICATION_ACTION + "." + notificationId)

        // Create the objects under test
        val notificationPresenter = NotificationPresenterImpl(dependencyProvider)
        val receiver = NotificationPresenterImpl.Receiver()

        // Send the intent to the receiver.
        receiver.onReceive(context, notificationIntent)

        // Verify that Context.startActivity() is not called
        verify(context, never()).startActivity(any())
    }

    companion object {
        private val SHOW_BOTH_CATEGORY = "show_both"
        private val SHOW_FOREGROUND_CATEGORY = "show_foreground"
        private val SHOW_BACKGROUND_CATEGORY = "show_background"

        private val SHOW_BOTH_BODY_TEXT_ID = 1
        private val SHOW_FOREGROUND_BODY_TEXT_ID = 2
        private val SHOW_BACKGROUND_BODY_TEXT_ID = 3

        private val SHOW_BOTH_BODY_TEXT = "show_both body text"
        private val SHOW_FOREGROUND_BODY_TEXT = "show_foreground body text"
        private val SHOW_BACKGROUND_BODY_TEXT = "show_background body text"
    }
}