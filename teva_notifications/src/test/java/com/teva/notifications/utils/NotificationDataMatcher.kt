//
// NotificationDataMatcher.kt
// teva_notifications
//
// Copyright (c) 2017 Teva. All rights reserved.
//

package com.teva.notifications.utils

import com.teva.notifications.services.notification.NotificationInfo
import com.teva.notifications.services.notification.RecurringScheduledNotificationInfo
import com.teva.notifications.services.notification.ScheduledNotificationInfo

import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher

import java.util.Objects

/**
 * This class defines methods for comparing different notification data objects.
 * This class is used in unit tests.
 */

object NotificationDataMatcher {
    /**
     * Matcher for NotificationData

     * @param expectedNotificationInfo The NotificationData to be matched against.
     * *
     * @return An implementation of the Hamcrest Matcher interface for matching NotificationData
     */
    @JvmStatic
    fun matchesNotificationData(expectedNotificationInfo: NotificationInfo): Matcher<NotificationInfo> {
        return object : BaseMatcher<NotificationInfo>() {
            override fun matches(`object`: Any): Boolean {
                val actualNotificationInfo = `object` as NotificationInfo

                val matcher = MapMatcher(expectedNotificationInfo.notificationData)

                return matcher.matches(actualNotificationInfo.notificationData) &&
                        expectedNotificationInfo.categoryId == actualNotificationInfo.categoryId &&
                        expectedNotificationInfo.notificationCategory == actualNotificationInfo.notificationCategory

            }

            override fun describeTo(description: Description) {
                description.appendText("NotificationData fields should match")
            }
        }
    }

    /**
     * Matcher for ScheduledNotification

     * @param expectedScheduledNotificationInfo The ScheduledNotification to be matched against.
     * *
     * @return An implementation of the Hamcrest Matcher interface for matching ScheduledNotification
     */
    @JvmStatic
    fun matchesScheduledNotification(expectedScheduledNotificationInfo: ScheduledNotificationInfo): Matcher<ScheduledNotificationInfo> {
        return object : BaseMatcher<ScheduledNotificationInfo>() {
            override fun matches(`object`: Any): Boolean {
                val actualScheduledNotificationInfo = `object` as ScheduledNotificationInfo

                val matcher = MapMatcher(expectedScheduledNotificationInfo.notificationData)

                return matcher.matches(actualScheduledNotificationInfo.notificationData) &&
                        expectedScheduledNotificationInfo.categoryId == actualScheduledNotificationInfo.categoryId &&
                        expectedScheduledNotificationInfo.notificationCategory == actualScheduledNotificationInfo.notificationCategory &&
                        expectedScheduledNotificationInfo.fireDateApplicationTime == actualScheduledNotificationInfo.fireDateApplicationTime

            }

            override fun describeTo(description: Description) {
                description.appendText("ScheduledNotification fields should match")
            }
        }
    }

    /**
     * Matcher for RecurringScheduledNotification

     * @param expectedRecurringScheduledNotificationInfo The RecurringScheduledNotification to be matched against.
     * *
     * @return An implementation of the Hamcrest Matcher interface for matching RecurringScheduledNotification
     */
    @JvmStatic
    fun matchesRecurringScheduledNotification(expectedRecurringScheduledNotificationInfo: RecurringScheduledNotificationInfo): Matcher<RecurringScheduledNotificationInfo> {
        return object : BaseMatcher<RecurringScheduledNotificationInfo>() {
            override fun matches(`object`: Any): Boolean {
                val actualRecurringScheduledNotificationInfo = `object` as RecurringScheduledNotificationInfo

                val matcher = MapMatcher(expectedRecurringScheduledNotificationInfo.notificationData)

                return matcher.matches(actualRecurringScheduledNotificationInfo.notificationData) &&
                        expectedRecurringScheduledNotificationInfo.categoryId == actualRecurringScheduledNotificationInfo.categoryId &&
                        expectedRecurringScheduledNotificationInfo.notificationCategory == actualRecurringScheduledNotificationInfo.notificationCategory &&
                        expectedRecurringScheduledNotificationInfo.fireDateApplicationTime == actualRecurringScheduledNotificationInfo.fireDateApplicationTime &&
                        expectedRecurringScheduledNotificationInfo.repeatType === actualRecurringScheduledNotificationInfo.repeatType
            }

            override fun describeTo(description: Description) {
                description.appendText("RecurringScheduledNotification fields should match")
            }
        }
    }
}
