//
// VASManagerImpl.kt
// teva_userfeedback
//
// Copyright (c) 2017 Teva. All rights reserved.
//

package com.teva.userfeedback.model

import android.util.Log
import com.teva.common.services.TimeService
import com.teva.common.services.UpdateTimeMessage
import com.teva.utilities.services.DependencyProvider
import com.teva.common.utilities.Messenger
import com.teva.notifications.entities.ReminderSetting
import com.teva.notifications.enumerations.RepeatType
import com.teva.notifications.models.NotificationManager
import com.teva.userfeedback.dataquery.DailyUserFeelingDataQuery
import com.teva.userfeedback.entities.DailyUserFeeling
import com.teva.userfeedback.enumerations.UserFeeling

import org.greenrobot.eventbus.Subscribe
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime

import java.util.HashMap


/**
 * This class implements the UserFeelingManager and DailyAssessmentReminderManager interfaces
 * to support setting and retrieving UserFeeling data and managing daily self assessment reminders.
 *
 * @param dependencyProvider -  the dependency injection mechanism
 */
class DSAManagerImpl(private val dependencyProvider: DependencyProvider)
    : DailyAssessmentReminderManager, UserFeelingManager {

    private val notificationId = DSANotificationId.DSA_REMINDER
    private val notificationManager: NotificationManager = dependencyProvider.resolve<NotificationManager>()
    private val dailyUserFeelingDataQuery: DailyUserFeelingDataQuery = dependencyProvider.resolve<DailyUserFeelingDataQuery>()
    private val defaultReminderTime = LocalTime.of(8, 0)
    private val timeService: TimeService = dependencyProvider.resolve<TimeService>()

    init {
        dependencyProvider.resolve<Messenger>().subscribe(this)

        if (!notificationManager.hasReminderSetting(notificationId)) {
            enableReminder(true)
        }
    }

    /**
     * The reminder setting.
     */
    override val reminderSetting: ReminderSetting?
        get() {
            return notificationManager.getReminderSettingByName(notificationId)
        }


    /**
     * This method enables the specified reminder setting.
     *
     * @param reminder - the reminder to be enabled.
     */
    private fun enableReminder(reminder: ReminderSetting) {
        val vasSetForToday = dailyUserFeelingDataQuery.get(timeService.today()) != null
        notificationManager.setNotification(notificationId, HashMap<String, Any>(), reminder, vasSetForToday)
    }

    /**
     * This method starts the reminder manager.
     */
    override fun start() {
        start(true)
    }

    /**
     * It checks if there is a reminder setting for VAS and if none exists, create with a default of enabled.
     */
    override fun start(enabled: Boolean) {
        if (!notificationManager.hasReminderSetting(notificationId)) {
            enableReminder(enabled)
        }
    }

    /**
     * Handler for the UpdateTimeMessage that is sent whenever the hypertime settings are changed.
     */
    @Suppress("UNUSED_PARAMETER")
    @Subscribe
    fun onUpdateTimeMessage(message: UpdateTimeMessage) {
        val reminderSetting = notificationManager.getReminderSettingByName(notificationId)
        val enableReminder = reminderSetting == null || reminderSetting.isEnabled
        enableReminder(enableReminder)
    }

    /**
     * This method configures the reminder notification and saves the setting to the database.
     *
     * @param enabled - This parameter indicates whether the reminder is enabled.
     */
    override fun enableReminder(enabled: Boolean) {
        val reminder = createReminder(enabled, defaultReminderTime)
        enableReminder(reminder)
    }

    /**
     * This method saves a UserFeeling for the date passed in. It updates the local database. It overwrites a previously stored value.
     *
     * @param userFeeling - The user feeling corresponding to the timestamp passed in.
     * @param timeStamp   - The date/time that the user sets user feeling.
     */
    override fun saveUserFeeling(userFeeling: UserFeeling, timeStamp: Instant) {
        val dailyUserFeeling = DailyUserFeeling(timeStamp, userFeeling)
        Log.w("save data","dailyUserFeeling date==>"+dailyUserFeeling.date)
        Log.w("save data","dailyUserFeeling userFeeling==>"+dailyUserFeeling.userFeeling)

        dailyUserFeelingDataQuery.insertOrUpdate(dailyUserFeeling, true)

        // User already set the user feeling, cancel any scheduled reminders for today and reschedule for tomorrow.
        // Note: setNotification() cancels existing notifications if ID already exists in the scheduled notifications.
        var reminder: ReminderSetting? = notificationManager.getReminderSettingByName(notificationId)

        if (reminder == null) {
            reminder = createReminder(true, null)
        }

        notificationManager.setNotification(notificationId, HashMap<String, Any>(), reminder, true)
    }

    /**
     * This method returns a UserFeeling for the date passed in, if it exists, otherwise Unknown.
     *
     * @param date - The date to get the UserFeeling.
     * @return - Returns a UserFeeling for the date passed in, if it exists, otherwise Unknown.
     */
    override fun getUserFeelingAtDate(date: LocalDate): DailyUserFeeling {
        val dailyUserFeeling = dailyUserFeelingDataQuery.get(date)

        return dailyUserFeeling ?: DailyUserFeeling(null, UserFeeling.UNKNOWN)
    }

    /**
     * This method returns an array of DailyUserFeeling objects starting from the date passed in, to the current date. DailyUserFeeling objects contains a timestamp and UserFeeling.
     *
     * @param fromDate - The starting date.
     * @param toDate   - The ending date.
     * @return - Returns the history of DailyUserFeeling for the dates passed in.
     */
    override fun getUserFeelingHistoryFromDate(fromDate: LocalDate, toDate: LocalDate): Map<LocalDate, DailyUserFeeling?> {
        return dailyUserFeelingDataQuery.get(fromDate, toDate)
    }

    /**
     * This method creates a reminder setting with the specified values.
     *
     * @param enabled      - indicates if the reminder should be enabled.
     * @param reminderTime - the time at which the reminder should be displayed.
     * @return - returns the created reminder setting.
     */
    private fun createReminder(enabled: Boolean, reminderTime: LocalTime?): ReminderSetting {
        return ReminderSetting(enabled, notificationId, RepeatType.ONCE_PER_DAY, reminderTime)
    }
}
