package com.teva.respiratoryapp.models


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Parcelable
import com.teva.analysis.messages.UpdateAnalysisDataMessage
import com.teva.cloud.messages.SyncCloudMessage
import com.teva.common.messages.AppForegroundMessage
import com.teva.common.messages.SystemMonitorMessage
import com.teva.common.services.*
import com.teva.common.services.analytics.AnalyticsService
import com.teva.utilities.services.DependencyProvider
import com.teva.utilities.utilities.Logger
import com.teva.utilities.utilities.Logger.Level.DEBUG
import com.teva.utilities.utilities.Logger.Level.INFO
import com.teva.common.utilities.Messenger
import com.teva.devices.entities.ConnectionMeta
import com.teva.devices.entities.Device
import com.teva.devices.entities.InhaleEvent
import com.teva.devices.messages.DeviceUpdatedMessage
import com.teva.devices.messages.UpdateDeviceMessage
import com.teva.medication.entities.Prescription
import com.teva.notifications.entities.ReminderSetting
import com.teva.respiratoryapp.common.AppSystemMonitorActivity
import com.teva.respiratoryapp.common.messages.ModelUpdatedMessage
import com.teva.respiratoryapp.models.engagementbooster.UpdateEngagementBoosterMessage
import com.teva.userfeedback.entities.DailyUserFeeling
import org.greenrobot.eventbus.Subscribe
import org.threeten.bp.LocalTime
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import java.util.*

/**
 * This class coordinates and redirects messages between the packages and the UI.
 *
 * @param dependencyProvider The dependency injection mechanism
 */
class SystemManager(private val dependencyProvider: DependencyProvider) : AlarmServiceCallback {
    private val messenger: Messenger = dependencyProvider.resolve()
    private val analytics: AnalyticsService = dependencyProvider.resolve()

    init {

        messenger.subscribe(this)

        dependencyProvider.resolve<AlarmService>().register(DAY_CHANGE_ALARM_ID, this)

        updateDayChangeAlarm()

        Receiver.systemManager = this

        onDayChange()
    }

    /**
     * Message handler for the ModelUpdatedMessage.
     */
    @Subscribe
    fun onModelUpdated(message: ModelUpdatedMessage) {
        logger.log(DEBUG, "onModelUpdated")
        if (message.containsObjectsOfType(DailyUserFeeling::class.java, ConnectionMeta::class.java, Device::class.java)) {
            messenger.post(UpdateAnalysisDataMessage(message.objectsUpdated))
        }

        if (message.containsObjectsOfType(DailyUserFeeling::class.java, Device::class.java, InhaleEvent::class.java, Prescription::class.java, ReminderSetting::class.java)) {
            messenger.publish(SyncCloudMessage())
        }
    }

    /**
     * Message handler for the DeviceUpdatedMessage.
     */
    @Subscribe
    fun onDeviceUpdated(message: DeviceUpdatedMessage) {
        logger.log(DEBUG, "onDeviceUpdated")

        // if there was an inhale event, update engagement booster.
        val inhaleEvents = message.inhaleEvents
        if (inhaleEvents.isNotEmpty()) {
            messenger.post(UpdateEngagementBoosterMessage())
        }
    }

    @Subscribe
    fun reportToAnalytics(message: SystemMonitorMessage) {
        val activity = message.activity
        if (activity.timing != null) {
            analytics.timing(activity.activityName, activity.source, activity.timing!!)
        }

        if (!activity.timingOnly) {
            analytics.event(activity.activityName, activity.source, activity.activityLabel)
        }
    }

    /**
     * Message handler for the UpdateTimeMessage.

     * @param time The current time.
     */
    @Suppress("UNUSED_PARAMETER")
    @Subscribe
    fun onTimeUpdated(time: UpdateTimeMessage) {
        updateDayChangeAlarm()
        messenger.post(UpdateEngagementBoosterMessage())
    }

    /**
     * Message handler for the AppForegroundMessage
     * @param message - the AppForegroundMessage.
     */
    @Suppress("UNUSED_PARAMETER")
    @Subscribe
    fun onAppForeground(message: AppForegroundMessage) {
        messenger.post(UpdateDeviceMessage())

        // if device foreground status changed, update engagement booster.
        messenger.post(UpdateEngagementBoosterMessage())

        messenger.post(SyncCloudMessage())

        if(message.inForeground) {
            messenger.post(SystemMonitorMessage(AppSystemMonitorActivity.Foregrounded()))
        } else {
            messenger.post(SystemMonitorMessage(AppSystemMonitorActivity.Backgrounded()))
        }
    }

    /**
     * Reschedules the day change alarm for the next day if the TimeService is in a hyper time.
     */
    private fun updateDayChangeAlarm() {
        logger.log(INFO, "updateDayChangeAlarm()")

        val alarmService = dependencyProvider.resolve<AlarmService>()
        val timeService = dependencyProvider.resolve<TimeService>()
        if (timeService.timeMode === RunModes.REALTIME) {
            alarmService.cancelAlarm(DAY_CHANGE_ALARM_ID)
        } else {
            val tomorrow = timeService.today().plusDays(1)
            val midnight = ZonedDateTime.of(
                    tomorrow,
                    LocalTime.of(0, 0),
                    dependencyProvider.resolve<ZoneId>())
            val wakeupTime = timeService.getRealTimeFromDate(midnight.toInstant())

            alarmService.setAlarm(DAY_CHANGE_ALARM_ID, wakeupTime, null)
        }
    }

    /**
     * Notifies a client that an alarm has expired.

     * @param id   The alarm id.
     * *
     * @param data The alarm data.
     */
    override fun onAlarm(id: String, data: Parcelable?) {
        if (id == DAY_CHANGE_ALARM_ID) {
            onDayChange()

            updateDayChangeAlarm()
        }
    }

    /**
     * Called when the hypertime day change alarm expires or when the ACTION_DATE_CHANGED
     * Intent is broadcast by Android.
     */
    private fun onDayChange() {
        logger.log(INFO, "onDayChange()")

        messenger.post(UpdateDeviceMessage())
        messenger.post(UpdateAnalysisDataMessage(ArrayList<Any>()))
    }

    /**
     * This BroadcastReceiver listens ACTION_DATE_CHANGED Intents from Android and
     * triggers the actions that occur on a date change.
     */
    class Receiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_DATE_CHANGED) {
                systemManager?.onDayChange()
            }
        }

        companion object {
            var systemManager: SystemManager? = null
        }
    }

    companion object {
        private val logger = Logger(SystemManager::class)
        private val DAY_CHANGE_ALARM_ID = SystemManager::class.java.canonicalName + ".DayChange"
    }
}
