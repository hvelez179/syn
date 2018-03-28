//
// EnvironmentMonitorImpl.kt
// teva_environment
//
// Copyright (c) 2017 Teva. All rights reserved.
//

package com.teva.environment.models

import android.location.Location
import android.os.Parcelable
import com.teva.common.services.AlarmService
import com.teva.common.services.AlarmServiceCallback
import com.teva.common.services.TimeService
import com.teva.common.services.UpdateTimeMessage
import com.teva.utilities.services.DependencyProvider
import com.teva.utilities.utilities.Logger
import com.teva.common.utilities.Messenger
import com.teva.environment.entities.EnvironmentInfo
import com.teva.environment.messages.CheckLocationAndUpdateEnvironmentMessage
import com.teva.environment.messages.EnvironmentUpdatedMessage
import com.teva.environment.messages.UpdateEnvironmentMessage
import com.teva.environment.services.EnvironmentService
import com.teva.environment.services.EnvironmentServiceDelegate
import com.teva.location.models.LocationManager
import com.teva.location.services.LocationCallback
import com.teva.location.services.LocationInfo
import com.teva.notifications.entities.ReminderSetting
import com.teva.notifications.enumerations.RepeatType
import com.teva.notifications.models.NotificationManager
import com.teva.notifications.services.notification.NotificationDataKey
import org.greenrobot.eventbus.Subscribe
import org.threeten.bp.LocalTime
import java.util.*

/**
 * This class is responsible for retrieving the environment information.
 */
class EnvironmentMonitorImpl(private val dependencyProvider: DependencyProvider,
                             private val environmentService: EnvironmentService)
    : EnvironmentMonitor,
        EnvironmentServiceDelegate,
        AlarmServiceCallback,
        DailyEnvironmentalReminderManager {

    private val logger = Logger(EnvironmentMonitorImpl::class)

    /**
     * This property returns the cached environment data.
     * This property returns null if the data is not available, due to either no internet connection, or failure to get current location (i.e., location services not available).
     * The current EnvironmentInfo could become null, if internet connection or location services are subsequently lost, and attempt to update data fails due to data expiration.
     * The individual environment properties, e.g., AirQualityInfo, PollenInfo, WeatherInfo could be null if the corresponding request fails (e.g., HTTP error)
     * or could be marked as invalid if there was a problem parsing the corresponding web service response.
     */
    override var currentEnvironmentInfo: EnvironmentInfo? = null
        private set

    private var environmentInfoRequested = false
    private var environmentRetrievalInProgress = false
    private var currentLocationName = "Unknown Location"
    private var previousLocation: Location? = null

    private val dailyReminderTime = LocalTime.of(8, 30)

    private val notificationId: String = EnvironmentNotificationId.DailyEnvironmentalReminder

    init {
        dependencyProvider.resolve<AlarmService>().register(ALARM_SERVICE_ID, this)
        dependencyProvider.resolve<Messenger>().subscribe(this)
        environmentService.setEnvironmentServiceDelegate(this)

        val notificationManager = dependencyProvider.resolve<NotificationManager>()
        val reminderSetting = notificationManager.getReminderSettingByName(notificationId)
        val enableReminder = reminderSetting?.isEnabled ?: true
        enableReminder(enableReminder)


    }

    /**
     * This method is invoked after the environment information retrieval succeeded or failed.
     *
     * @param environmentInfo This parameter contains the requested environment information.
     *                        If the request fails, the value is null.
     */
    override fun getEnvironmentComplete(environmentInfo: EnvironmentInfo?) {
        environmentRetrievalInProgress = false
        currentEnvironmentInfo = environmentInfo

        val alarmService: AlarmService = dependencyProvider.resolve()

        val currentTime = dependencyProvider.resolve<TimeService>().now()

        if (currentEnvironmentInfo != null  && !(currentEnvironmentInfo!!.airQualityInfo == null && currentEnvironmentInfo!!.pollenInfo == null && currentEnvironmentInfo!!.weatherInfo == null)) {
            if (currentEnvironmentInfo!!.weatherInfo != null) {
                currentEnvironmentInfo!!.weatherInfo!!.locationFull = currentLocationName
            }

            val expirationDate = currentEnvironmentInfo!!.expirationDate

            if (currentTime.isAfter(expirationDate!!)) {
                logger.log(Logger.Level.INFO, "Environment data already expired. Setting retry time to " + currentTime.plusSeconds(ENVIRONMENT_RETRY_INTERVAL_IN_SECONDS.toLong()).toString())
                alarmService.setAlarm(ENVIRONMENT_UPDATE_ALARM_ID,
                        currentTime.plusSeconds(ENVIRONMENT_RETRY_INTERVAL_IN_SECONDS.toLong()), null)
            } else {
                logger.log(Logger.Level.INFO, "Environment data valid. Setting retry time to " + expirationDate.toString())
                alarmService.setAlarm(ENVIRONMENT_UPDATE_ALARM_ID,
                        expirationDate, null)
            }

        } else {
            logger.log(Logger.Level.INFO, "Environment data could not be retrieved. Setting retry time to " + currentTime.plusSeconds(ENVIRONMENT_RETRY_INTERVAL_IN_SECONDS.toLong()).toString())
            alarmService.setAlarm(ENVIRONMENT_UPDATE_ALARM_ID,
                    currentTime.plusSeconds(ENVIRONMENT_RETRY_INTERVAL_IN_SECONDS.toLong()), null)
        }

        dependencyProvider.resolve<Messenger>().publish(EnvironmentUpdatedMessage())

        if (environmentInfoRequested) {
            getEnvironmentAsync()
        }
    }

    /**
     * This method retrieves the current location and starts a request for retrieving
     * the environment information for the current location.
     */
    private fun getEnvironmentAsync(updateOnlyIfLocationChanges:Boolean = false) {
        val locationManager = dependencyProvider.resolve(LocationManager::class.java)
        val currentLocationInfo = locationManager.currentLocation

        if (currentLocationInfo == null ) {
            getEnvironmentComplete(null)
            previousLocation = currentLocationInfo
        } else {

            if(updateOnlyIfLocationChanges && !locationsDifferSignificantly(currentLocationInfo,previousLocation)) {
                logger.log(Logger.Level.INFO, "Location did not change.")
                return
            }

            logger.log(Logger.Level.INFO, "Locations - Old: ${previousLocation?.latitude}, ${previousLocation?.longitude}   New: ${currentLocationInfo.latitude}, ${currentLocationInfo.longitude}")
            previousLocation = currentLocationInfo

            locationManager.reverseLocationLookup(
                    currentLocationInfo.latitude,
                    currentLocationInfo.longitude,
                    object : LocationCallback {
                        override fun locationLookupCompleted(locationInfo: LocationInfo?) {
                            if (locationInfo == null) {
                                getEnvironmentComplete(null)
                            } else {
                                currentLocationName = locationInfo.locality

                                if (!environmentRetrievalInProgress) {

                                    environmentInfoRequested = false
                                    environmentRetrievalInProgress = true

                                    environmentService.getEnvironmentAsync(locationInfo)
                                } else {
                                    // request already in progress
                                    environmentInfoRequested = true
                                }
                            }
                        }
                    })
        }
    }

    /**
     * This function compares two locations to check if there is a significant
     * difference between them. Difference up to the tenths place is significant
     * as this can create a differnce of about 1.1 km so we compare the values
     * up to the tenths place.
     */
    private fun locationsDifferSignificantly(location1: Location?,  location2: Location?): Boolean {
        if(location1 == null || location2 == null) {
            return true
        }

        val location1Latitude = (location1.latitude * 100).toInt()
        val location1Longitude = (location1.longitude * 100).toInt()
        val location2Latitude = (location2.latitude * 100).toInt()
        val location2Longitude = (location2.longitude * 100).toInt()

        if(location1Latitude == location2Latitude && location1Longitude == location2Longitude) {
            return false
        }

        return true
    }

    /**
     * This method handles the alarm indicating that the envrionment data needs to be updated.
     *
     * @param id   - The alarm id.
     * @param data - The alarm data.
     */
    override fun onAlarm(id: String, data: Parcelable?) {
        if (id == ENVIRONMENT_UPDATE_ALARM_ID) {
            getEnvironmentAsync()
        }
    }

    /**
     * Handler for the UpdateEnvironmentMessage. Retrieves the environment information again.
     */
    @Subscribe
    fun onUpdateEnvironmentMessage(message: UpdateEnvironmentMessage) {
        currentEnvironmentInfo = null
        cancelPendingEnvironmentRequest()
        getEnvironmentAsync()
    }

    /**
     * Handler for the CheckLocationAndUpdateEnvironmentMessage.
     */
    @Subscribe
    fun onCheckLocationAndUpdateEnvironmentMessage(message: CheckLocationAndUpdateEnvironmentMessage) {
        getEnvironmentAsync(true)
    }

    /**
     * Handler for the UpdateTimeMessage that is sent whenever the hypertime settings are changed.
     */
    @Subscribe
    fun onUpdateTimeMessage(message: UpdateTimeMessage) {
        val notificationManager: NotificationManager = dependencyProvider.resolve()
        val reminderSetting = notificationManager.getReminderSettingByName(notificationId)
        val enableReminder = reminderSetting == null || reminderSetting.isEnabled
        enableReminder(enableReminder)
    }

    /**
     * This method cancels scheduled environment update requests.
     */
    private fun cancelPendingEnvironmentRequest() {
        val alarmService: AlarmService = dependencyProvider.resolve()
        alarmService.cancelAlarm(ENVIRONMENT_UPDATE_ALARM_ID)
    }

    /**
     * This method returns the daily environmental reminder.
     *
     * @return - the daily environmental reminder.
     */
    override val reminderSetting: ReminderSetting?
        get() {
            val notificationManager: NotificationManager = dependencyProvider.resolve()
            return notificationManager.getReminderSettingByName(notificationId)
        }

    /**
     * This method enables or disables the daily environment reminder.
     *
     * @param enabled      - This parameter indicates whether the reminder is to be enabled or disabled.
     */
    override fun enableReminder(enabled: Boolean) {
        val notificationManager: NotificationManager = dependencyProvider.resolve()
        val reminderSetting = ReminderSetting(enabled, notificationId, RepeatType.ONCE_PER_DAY, dailyReminderTime)
        val notificationInfo = HashMap<String, Any>()
        notificationInfo.put(NotificationDataKey.NOTIFICATION_ID, notificationId)
        notificationManager.setNotification(notificationId, notificationInfo, reminderSetting, false)
    }

    /**
     * This method starts the reminder manager.
     */
    override fun start() {
        start(true)
    }

    /**
     * This method checks if there is a reminder setting for DailyEnvironmentalReminder and
     * if none exists, create with a default of enabled.
     */
    override fun start(enabled: Boolean) {
        val notificationManager: NotificationManager = dependencyProvider.resolve()
        if (!notificationManager.hasReminderSetting(notificationId)) {
            enableReminder(enabled)
        }
    }

    companion object {

        private val ENVIRONMENT_RETRY_INTERVAL_IN_SECONDS = 300

        private val ALARM_SERVICE_ID = EnvironmentMonitorImpl::class.java.canonicalName + ".Alarm"

        private val RETRIEVE_ENVIRONMENT_NOTIFICATION_ID = "RetrieveEnvironmentNotification"

        private val ENVIRONMENT_UPDATE_ALARM_ID = ALARM_SERVICE_ID + RETRIEVE_ENVIRONMENT_NOTIFICATION_ID
    }
}
