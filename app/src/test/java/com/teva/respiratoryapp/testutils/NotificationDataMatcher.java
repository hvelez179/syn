//
// NotificationDataMatcher.java
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.testutils;

import com.teva.notifications.services.notification.NotificationInfo;
import com.teva.notifications.services.notification.RecurringScheduledNotificationInfo;
import com.teva.notifications.services.notification.ScheduledNotificationInfo;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import java.util.Objects;

/**
 * This class defines methods for comparing different notification data objects.
 * This class is used in unit tests.
 */

public class NotificationDataMatcher {
    /**
     * Matcher for NotificationData
     *
     * @param expectedNotificationInfo The NotificationData to be matched against.
     * @return An implementation of the Hamcrest Matcher interface for matching NotificationData
     */
    public static Matcher<NotificationInfo> matchesNotificationData(final NotificationInfo expectedNotificationInfo) {
        return new BaseMatcher<NotificationInfo>() {
            @Override
            public boolean matches(final Object object) {
                NotificationInfo actualNotificationInfo = (NotificationInfo) object;

                MapMatcher matcher = new MapMatcher(expectedNotificationInfo.getNotificationData());

                return matcher.matches(actualNotificationInfo.getNotificationData()) &&
                        Objects.equals(expectedNotificationInfo.getCategoryId(), actualNotificationInfo.getCategoryId()) &&
                        Objects.equals(expectedNotificationInfo.getNotificationCategory(), actualNotificationInfo.getNotificationCategory());
            }

            @Override
            public void describeTo(final Description description) {
                description.appendText("NotificationData fields should match");
            }
        };
    }

    /**
     * Matcher for ScheduledNotification
     *
     * @param expectedScheduledNotificationInfo The ScheduledNotification to be matched against.
     * @return An implementation of the Hamcrest Matcher interface for matching ScheduledNotification
     */
    public static Matcher<ScheduledNotificationInfo> matchesScheduledNotification(final ScheduledNotificationInfo expectedScheduledNotificationInfo) {
        return new BaseMatcher<ScheduledNotificationInfo>() {
            @Override
            public boolean matches(final Object object) {
                ScheduledNotificationInfo actualScheduledNotificationInfo = (ScheduledNotificationInfo) object;

                MapMatcher matcher = new MapMatcher(expectedScheduledNotificationInfo.getNotificationData());

                return matcher.matches(actualScheduledNotificationInfo.getNotificationData()) &&
                        Objects.equals(expectedScheduledNotificationInfo.getCategoryId(), actualScheduledNotificationInfo.getCategoryId()) &&
                        Objects.equals(expectedScheduledNotificationInfo.getNotificationCategory(), actualScheduledNotificationInfo.getNotificationCategory()) &&
                        Objects.equals(expectedScheduledNotificationInfo.getFireDateApplicationTime(), actualScheduledNotificationInfo.getFireDateApplicationTime());

            }

            @Override
            public void describeTo(final Description description) {
                description.appendText("ScheduledNotification fields should match");
            }
        };
    }

    /**
     * Matcher for RecurringScheduledNotification
     *
     * @param expectedRecurringScheduledNotificationInfo The RecurringScheduledNotification to be matched against.
     * @return An implementation of the Hamcrest Matcher interface for matching RecurringScheduledNotification
     */
    public static Matcher<RecurringScheduledNotificationInfo> matchesRecurringScheduledNotification(final RecurringScheduledNotificationInfo expectedRecurringScheduledNotificationInfo) {
        return new BaseMatcher<RecurringScheduledNotificationInfo>() {
            @Override
            public boolean matches(final Object object) {
                RecurringScheduledNotificationInfo actualRecurringScheduledNotificationInfo = (RecurringScheduledNotificationInfo) object;

                MapMatcher matcher = new MapMatcher(expectedRecurringScheduledNotificationInfo.getNotificationData());

                return matcher.matches(actualRecurringScheduledNotificationInfo.getNotificationData()) &&
                        Objects.equals(expectedRecurringScheduledNotificationInfo.getCategoryId(), actualRecurringScheduledNotificationInfo.getCategoryId()) &&
                        Objects.equals(expectedRecurringScheduledNotificationInfo.getNotificationCategory(), actualRecurringScheduledNotificationInfo.getNotificationCategory()) &&
                        Objects.equals(expectedRecurringScheduledNotificationInfo.getFireDateApplicationTime(), actualRecurringScheduledNotificationInfo.getFireDateApplicationTime()) &&
                        expectedRecurringScheduledNotificationInfo.getRepeatType() == actualRecurringScheduledNotificationInfo.getRepeatType();

            }

            @Override
            public void describeTo(final Description description) {
                description.appendText("RecurringScheduledNotification fields should match");
            }
        };
    }
}
