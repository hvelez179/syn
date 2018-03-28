//
// SyncManagerImpl.kt
// teva_cloud
//
// Copyright © 2017 Teva. All rights reserved.
//

package com.teva.cloud.models.sync

import com.teva.cloud.dataentities.UserProfile
import com.teva.cloud.enumerations.SyncManagerState.*
import com.teva.cloud.extensions.toGMTString
import com.teva.cloud.messages.CloudSyncCompleteMessage
import com.teva.cloud.models.CloudConstants
import com.teva.cloud.models.CloudManagerNotificationId
import com.teva.cloud.models.CloudSessionState
import com.teva.cloud.models.userprofile.UserProfileManager
import com.teva.cloud.services.CloudObjectContainer
import com.teva.cloud.services.sync.DHPSyncCloudService
import com.teva.cloud.services.sync.SyncCloudService
import com.teva.common.services.TimeService
import com.teva.common.utilities.DBExecutor
import com.teva.utilities.services.DependencyProvider
import com.teva.utilities.utilities.Logger
import com.teva.utilities.utilities.Logger.Level.*
import com.teva.common.utilities.Messenger
import com.teva.devices.dataquery.DeviceDataQuery
import com.teva.devices.dataquery.InhaleEventDataQuery
import com.teva.devices.entities.Device
import com.teva.devices.entities.InhaleEvent
import com.teva.devices.model.DeviceManager
import com.teva.devices.model.DeviceManagerNotificationId
import com.teva.devices.model.DeviceQuery
import com.teva.environment.models.DailyEnvironmentalReminderManager
import com.teva.environment.models.EnvironmentNotificationId
import com.teva.medication.dataquery.PrescriptionDataQuery
import com.teva.medication.entities.Prescription
import com.teva.notifications.dataquery.ReminderDataQuery
import com.teva.notifications.entities.ReminderSetting
import com.teva.notifications.models.NotificationManager
import com.teva.userfeedback.dataquery.DailyUserFeelingDataQuery
import com.teva.userfeedback.entities.DailyUserFeeling
import com.teva.userfeedback.model.DSANotificationId
import com.teva.userfeedback.model.DailyAssessmentReminderManager
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import com.teva.analysis.messages.*
import com.teva.cloud.enumerations.CloudActivity
import com.teva.cloud.models.CloudConstants.acceptableServerTimeDifferenceForUpload
import com.teva.cloud.models.programmanagement.CareProgramManager
import com.teva.common.messages.SystemMonitorMessage
import java.util.ArrayList

/**
 * This class implements the SyncManager interface for syncing the App data.
 */
class SyncManagerImpl : SyncManager {

    // Internal properties

    /**
     * The current state of the sync manager.
     */
    private var syncManagerState = IDLE

    override val hasSynced: Boolean
        get() = !syncService.isFirstSync

    /**
     * This property is the Logger used to log to the console.
     */
    private val logger = Logger("SyncManagerImpl")

    // Private properties

    /**
     * This property is the messenger used for publishing messages.
     */
    private val messenger = DependencyProvider.default.resolve<Messenger>()

    /**
     * This property is the user profile manager used to retrieve user profiles.
     */
    private val userProfileManager = DependencyProvider.default.resolve<UserProfileManager>()

    /**
     * This property is the care program manager used to retrieve user profiles.
     */
    private val careProgramManager = DependencyProvider.default.resolve<CareProgramManager>()

    /**
     * This property is used to get the current time for the app.
     */
    private var timeService = DependencyProvider.default.resolve<TimeService>()

    /**
    This property stores the data to be uploaded to the cloud.
     */
    private var uploadData: CloudObjectContainer = CloudObjectContainer()

    /**
     * This property indicates whether a download has been executed for the current sync.
     */
    private var hasDownloaded: Boolean = false

    /**
     * This property indicates whether prescriptions and devices have been downloaded during the first sync.
     */
    private var hasDownloadedPrescriptionsAndDevices: Boolean = false

    /**
     * This is start time for calculating timing for syncing.
     */
    private var syncStartTime: Instant? = null

    /**
     * This property indicates the number of inhale events synced that needed to be monitored.
     */
    private val monitoredInhaleEventCount = 100

    /**
     * The service used to upload and download data.
     */
    private var syncService: SyncCloudService = DHPSyncCloudService()

    init {
        syncService.didDownload = this::downloadCompleted
        syncService.didUpload = this::uploadCompleted
    }

    /**
     * This function is invoked by the sync service when upload is completed.
     */
    internal fun uploadCompleted(success: Boolean) {
        if (success) {
            if (uploadData.inhaleEvents.size > monitoredInhaleEventCount) {
                messenger.post(SystemMonitorMessage(CloudActivity.MoreThan100InhalesSynced()))
            }
            continueSync()
        } else {
            resetUploadedChangedFlags()
            idle(false)
        }
    }

    /**
     * This function is invoked by the sync service when download is completed.
     */
    internal fun downloadCompleted(success: Boolean, data: CloudObjectContainer, moreDataToDownloadExists: Boolean, didProcessDownloadedData: (() -> Unit)?) {
        hasDownloaded = true

        if (success) {
            mergeIntoDatabase(data)
            didProcessDownloadedData?.invoke()

            // More data exists, continue downloading.
            if (moreDataToDownloadExists) {
                hasDownloadedPrescriptionsAndDevices = true
                hasDownloaded = false
            } else if (syncService.isFirstSync) {
                // Sync twice on first sync.
                hasDownloaded = false
                syncService.isFirstSync = false
            }

            if (data.inhaleEvents.size > monitoredInhaleEventCount) {
                messenger.post(SystemMonitorMessage(CloudActivity.MoreThan100InhalesSynced()))
            }

            continueSync()
        } else {
            idle(false)
        }
    }

    /**
     * This method enters the idle state for the CloudManager. If sync fails, this method check whether a notification should be raised indicating that there was no successful sync for 14 days.
     * @param syncSucceeded: Indicates whether cloud sync succeeded.
     */
    private fun idle(syncSucceeded: Boolean) {
        var timingInterval: Duration? = null
        syncStartTime?.let { syncStart ->
            timingInterval = Duration.between(syncStart, Instant.now())
            syncStartTime = null
        }

        messenger.post(SystemMonitorMessage(CloudActivity.CloudSync(syncSucceeded, timingInterval)))

        logger.log(VERBOSE, "idle(); syncSucceeded: $syncSucceeded")
        hasDownloaded = false

        // Failed to get Server Time as part of the sync process.
        // Check if need to raise the NoCloudSyncFor14Days notification.
        updateNoCloudSyncFor14DaysNotification(syncSucceeded)

        syncManagerState = IDLE
    }

    // Private methods

    /**
     * This method disables the NoCloudSyncFor14Days notification upon successful sync,
     * otherwise posts the NoCloudSyncFor14Days notification if there has not been a
     * successful sync in at least 14 days.
     * @param syncSuccess: This flag indicates whether the last sync was sucessful.
     */
    private fun updateNoCloudSyncFor14DaysNotification(syncSuccess: Boolean) {
        val now = timeService.now()

        if (syncSuccess) {
            CloudSessionState.shared.lastSuccessfulSyncDate = now
        } else {

            val lastSuccessfulSyncDate = CloudSessionState.shared.lastSuccessfulSyncDate

            if ( lastSuccessfulSyncDate == null) {
                // First time tried to sync, and failed. Set both success and fail times to now.
                CloudSessionState.shared.lastSuccessfulSyncDate = now
                CloudSessionState.shared.lastFailedSyncDate = now

                // No need to check if diff of success and fail dates > 14 days since dates are the same.
                return
            }

            // If difference between previous lastFailedSyncDate and lastSuccessfulSyncDate >= 14, notification was already raised.
            val maxDaysSinceSuccessfulSync = 14

            val previousLastFailedSyncDate = CloudSessionState.shared.lastFailedSyncDate
            CloudSessionState.shared.lastFailedSyncDate = now

            if (previousLastFailedSyncDate != null) {

                // There was a previous failure; If >= 14 days from successful sync,
                // then notification was already raised; prevent re-raising.
                if (previousLastFailedSyncDate.isAfter(lastSuccessfulSyncDate) &&
                        Duration.between(lastSuccessfulSyncDate, previousLastFailedSyncDate).toDays() >= maxDaysSinceSuccessfulSync) {
                    return
                }
            }

            // Notification was not already raised. Check if it needs to be raised.

            // Since daysBetween() returns absolute difference,
            // if now <= lastSuccessfulSyncDate, no need to proceed.
            if(!now.isAfter(lastSuccessfulSyncDate)) {
                return
            }

            // If it has been 14 days or more since last successful sync, raise the NoCloudSyncFor14Days notification.
            if (Duration.between(lastSuccessfulSyncDate, now).toDays() >= maxDaysSinceSuccessfulSync) {
                raiseNoCloudSyncFor14DaysNotification()
            }
        }
    }

    /**
     * This method raises the NoCloudSyncFor14Days immediately. It sets flag indicating that the notification was raised.
     */
    private fun raiseNoCloudSyncFor14DaysNotification() {
        val notificationManager = DependencyProvider.default.resolve<NotificationManager>()
        val categoryId = CloudManagerNotificationId.NO_CLOUD_SYNC_FOR_14_DAYS
        notificationManager.setNotification(categoryId, HashMap())
    }

    /**
     * This method begins the Cloud sync process.
     */
    override fun sync() {
        logger.log(INFO, "sync()")

        if(syncManagerState != IDLE) {
            return
        }

        syncStartTime = timeService.now()
        syncManagerState = SYNCING
        continueSync()
    }

    /**
     * This method continues the Cloud sync process through the state machine.
     */
    private fun continueSync() {

        logger.log(INFO, "continueSync()")

        try {
            applyServerTimeOffset(true)
        } catch (e: Exception) {
            logger.log(ERROR, "exception applying server offset", e)
        }

        // Upload if there is data,
        if (uploadData.hasData()) {
            logger.log(INFO, "uploading ${uploadData.objectCountString()})")
            syncManagerState = UPLOADING
            syncService.uploadAsync(uploadData)
        } else if (syncService.isFirstSync && !hasDownloadedPrescriptionsAndDevices) {
            syncManagerState = DOWNLOADING
            syncService.downloadPrescriptionsAndDevicesAsync()
        } else if (!hasDownloaded) {
            syncManagerState = DOWNLOADING
            syncService.downloadAsync()
        } else {
            messenger.post(CloudSyncCompleteMessage())
            idle(true)
        }
    }


    /**
     * This method retrieves the list of changed objects from the database, provided they are not in the future, based on the server time.
     * It also applies the current server time offset to changed objects that do not have an offset applied.
     *
     * @param uploadChangedObjects - are the changed objects being gathered for upload. If set to false, this method only checks for serverTimeOffsets.
     */
    override fun applyServerTimeOffset(uploadChangedObjects: Boolean) {

        var uploadObject: Boolean
        if (uploadChangedObjects) {
            uploadData.removeAllData()
        }

        val offset = CloudSessionState.shared.serverTimeOffset ?: CloudConstants.unknownOffsetValue
        val serverTime = CloudSessionState.shared.serverTime?.plus(
                Duration.between(syncStartTime, timeService.now()
                        .plusSeconds(CloudConstants.acceptableServerTimeDifferenceForUpload)))
                ?: timeService.now()

        DBExecutor.doWork(Runnable{

        // Get Prescriptions
        val prescriptionQuery = DependencyProvider.default.resolve<PrescriptionDataQuery>()
        val prescriptions = prescriptionQuery.getAllChanged()
        for (prescription in prescriptions) {
            if (prescription.serverTimeOffset == null) {
                prescription.serverTimeOffset = offset
            }

            uploadObject = uploadChangedObjects && !(prescription.changeTime?.isAfter(serverTime) ?: true)
            prescriptionQuery.resetChangedFlag(prescription, !uploadObject)
            if (uploadObject) {
                uploadData.prescriptions.add(prescription)
            }
        }

        // Get Devices
        val deviceQuery = DependencyProvider.default.resolve<DeviceDataQuery>()
        val devices = deviceQuery.getAllChanged()
        for (device in devices) {
            if (device.serverTimeOffset == null) {
                device.serverTimeOffset = offset
            }

            uploadObject = uploadChangedObjects && !(device.changeTime?.isAfter(serverTime) ?: true)
            deviceQuery.resetChangedFlag(device, !uploadObject)
            if (uploadObject) {
                uploadData.devices.add(device)
            }
        }

        // Get InhaleEvents
        val inhaleEventQuery  = DependencyProvider.default.resolve<InhaleEventDataQuery>()
        val events = inhaleEventQuery.getAllChanged()
        for (event in events) {
            if (event.serverTimeOffset == null) {
                event.serverTimeOffset = offset
            }

            uploadObject = uploadChangedObjects && !(event.changeTime?.isAfter(serverTime) ?: true)
            inhaleEventQuery.resetChangedFlag(event, !uploadObject)
            if (uploadObject) {
                uploadData.inhaleEvents.add(event)
            }
        }

        // Get DailyUserFeeling
        val dailyUserFeelingQuery = DependencyProvider.default.resolve<DailyUserFeelingDataQuery>()
        val dsaObjects = dailyUserFeelingQuery.getAllChanged()
        for (dsa in dsaObjects) {
            if (dsa.serverTimeOffset == null) {
                dsa.serverTimeOffset = offset
            }

            uploadObject = uploadChangedObjects && !(dsa.changeTime?.isAfter(serverTime) ?: true)
            dailyUserFeelingQuery.resetChangedFlag(dsa, !uploadObject)
            if (uploadObject) {
                uploadData.dsas.add(dsa)
            }
        }

        // Get ReminderSettings if this is NOT the first sync, to avoid overwriting cloud settings
        if (!syncService.isFirstSync) {
            val reminderDataQuery = DependencyProvider.default.resolve<ReminderDataQuery>()
            val reminderSettings = reminderDataQuery.getAll().filter { it.name != DeviceManagerNotificationId.INHALATIONS_FEEDBACK_TURN_OFF_GOOD_INHALATION_NOTIFICATION }

            // If any settings change, all must be uploaded
            if (reminderSettings.any { it.hasChanged }) {

                for (setting in reminderSettings) {
                    if (setting.serverTimeOffset == null) {
                        setting.serverTimeOffset = offset
                        reminderDataQuery.resetChangedFlag(setting, true)
                    }

                    if (uploadChangedObjects && !(setting.changeTime?.isAfter(serverTime) ?: true)) {
                        reminderDataQuery.resetChangedFlag(setting, false)
                        uploadData.settings.add(setting)
                    } else {
                        uploadData.settings.clear()
                        break
                    }
                }
            }
        }

        // Get changed profiles
        val profiles = userProfileManager.getAllChangedProfiles()
        for (profile in profiles) {
            if (profile.serverTimeOffset == null) {
                profile.serverTimeOffset = offset
            }

            uploadObject = uploadChangedObjects && !(profile.changeTime?.isAfter(serverTime) ?: true)
            userProfileManager.update(profile, !uploadObject)
            if (uploadObject) {
                uploadData.profiles.add(profile)
            }
        }
        })
    }

    /**
     * This method resets the hasChanged flags to true in the objects that failed to upload.
     */
    private fun resetUploadedChangedFlags() {

        logger.log(VERBOSE, "resetUploadedChangedFlags()")

        val prescriptionQuery = DependencyProvider.default.resolve<PrescriptionDataQuery>()
        val deviceQuery = DependencyProvider.default.resolve<DeviceDataQuery>()
        val inhaleEventQuery = DependencyProvider.default.resolve<InhaleEventDataQuery>()
        val dsaQuery = DependencyProvider.default.resolve<DailyUserFeelingDataQuery>()
        val reminderDataQuery = DependencyProvider.default.resolve<ReminderDataQuery>()

        DBExecutor.doWork(Runnable {

            for (prescription in uploadData.prescriptions) {
                prescriptionQuery.resetChangedFlag(prescription, true)
            }

            for (device in uploadData.devices) {
                deviceQuery.resetChangedFlag(device, true)
            }

            for (inhaleEvent in uploadData.inhaleEvents) {
                inhaleEventQuery.resetChangedFlag(inhaleEvent, true)
            }

            for (dsa in uploadData.dsas) {
                dsaQuery.resetChangedFlag(dsa, true)
            }

            for (setting in uploadData.settings) {
                reminderDataQuery.resetChangedFlag(setting, true)
            }

            for (profile in uploadData.profiles) {
                userProfileManager.update(profile, true)
            }
        })
    }

    /**
     * This method finds a Prescription object that matches the specified prescription drugUID and time.
     */
    private fun getMatchingPrescription(query: PrescriptionDataQuery, drugUID: String, gmtString: String): Prescription? {
        val prescriptions= query.getAll()
        return prescriptions.firstOrNull { it.medication?.drugUID == drugUID && it.prescriptionDate?.toGMTString(false) == gmtString }
    }

    /**
     * This method merges downloaded Prescription objects into the database.
     */
    private fun mergePrescriptionsIntoDatabase(prescriptions: List<Prescription>) {
        logger.log(INFO, "mergePrescriptionsIntoDatabase - ${prescriptions.size} prescription(s)")

        val query = DependencyProvider.default.resolve<PrescriptionDataQuery>()

        for (prescription in prescriptions) {

            val sourceTime = prescription.prescriptionDate!!.toGMTString(false)
            val existingPrescription = getMatchingPrescription(query, prescription.medication!!.drugUID, sourceTime)
            if (existingPrescription != null) {
                // make sure the cloud object is newer than the existing object
                if (prescription.changeTime?.isAfter(existingPrescription.changeTime ?: Instant.ofEpochSecond(0)) == true) {
                    logger.log(VERBOSE, "update prescription; prescriptionDate: ${prescription.prescriptionDate}")
                    query.update(prescription, false)
                }
            } else {
                logger.log(VERBOSE, "insert prescription; prescriptionDate: ${prescription.prescriptionDate}")
                query.insert(prescription, false)
            }
        }
    }

    /**
     * This method merges downloaded Device objects into the database.
     */
    private fun mergeDevicesIntoDatabase(devices: List<Device>) {

        logger.log(INFO, "mergeDevicesIntoDatabase: ${devices.size} device(s);")
        val query = DependencyProvider.default.resolve<DeviceQuery>()

        var devicesAdded = false
        for (device in devices) {

            val existingDevice = query.get(device.serialNumber)
            if (existingDevice != null) {
                // make sure the cloud object is newer than the existing object
                if (device.changeTime?.isAfter(existingDevice.changeTime) == true) {
                    logger.log(VERBOSE, "update device; device serial number: ${device.serialNumber}")
                    query.update(device, false)
                }
            } else {
                logger.log(VERBOSE, "insert device; device serial number: ${device.serialNumber}")
                query.insert(device, false)
                devicesAdded = true
            }
        }

        if (devicesAdded) {
            // Update the list of devices to scan for.
            val deviceManager = DependencyProvider.default.resolve<DeviceManager>()
            deviceManager.restart()
        }
    }

    /**
     * This method merges downloaded InhaleEvent objects into the database.
     */
    private fun mergeInhaleEventsIntoDatabase(inhaleEvents: List<InhaleEvent>) {

        logger.log(INFO, "mergeInhaleEventsIntoDatabase: ${inhaleEvents.size} inhale event(s);")
        val query = DependencyProvider.default.resolve<InhaleEventDataQuery>()
        val deviceQuery = DependencyProvider.default.resolve<DeviceDataQuery>()

        for (event in inhaleEvents) {
            val device = deviceQuery.get(event.deviceSerialNumber)
            if (device != null) {

                val existingEvent = query.get(event.eventUID, device)
                if (existingEvent != null) {

                    // make sure the cloud object is newer than the existing object
                    // openTime is what we care about.  Change time can vary from the openTime
                    // on inhaleEvents.  Inhale events should be immutable, but there can
                    // be variation between openTime that is calculated by different phones
                    // based on the delta delivered from the inhaler.
                    if (event.eventTime?.isAfter(existingEvent.eventTime) == true) {
                        logger.log(VERBOSE, "update inhale event; inhale event ID: ${event.uniqueId}")
                        query.update(event, false)
                    }
                } else {
                    logger.log(VERBOSE, "insert inhale event; inhale event ID: ${event.uniqueId}")
                    query.insert(event, false)
                }
            } else {
                logger.log(ERROR, "Didn't find the device when merging an inhale event.")
            }
        }
    }

    /**
     * This method merges downloaded DSA objects into the database.
     */
    private fun mergeDSAsIntoDatabase(dsas: List<DailyUserFeeling>) {

        logger.log(INFO, "mergeDSAsIntoDatabase: ${dsas.size} dsa(s);")
        val query = DependencyProvider.default.resolve<DailyUserFeelingDataQuery>()

        for (dsa in dsas) {

            val existingDSA = if (dsa.date != null) query.get(dsa.date!!) else null
            if (existingDSA != null) {

                // make sure the cloud object is newer than the existing object
                if (dsa.changeTime?.isAfter(existingDSA.changeTime) == true) {
                    logger.log(INFO, "update DSA; DSA date: ${dsa.date}")
                    query.update(dsa, false)
                }
            } else {
                logger.log(INFO, "insert DSA; DSA date: ${dsa.date}")
                query.insert(dsa, false)
            }
        }
    }

    /**
     * This method merges downloaded ReminderSetting objects into the database.
     */
    private fun mergeReminderSettingsIntoDatabase(settings: List<ReminderSetting>) {

        logger.log(INFO, "mergeReminderSettingsIntoDatabase: ${settings.size} setting(s);")
        val query = DependencyProvider.default.resolve<ReminderDataQuery>()

        for (setting in settings) {
            when (setting.name) {
                DSANotificationId.DSA_REMINDER -> {
                    val dailyAssessmentReminderManager: DailyAssessmentReminderManager = DependencyProvider.default.resolve()
                    val existingSetting = query.get(setting.name ?: "")

                    if (existingSetting != null) {
                        if (setting.changeTime?.isAfter(existingSetting.changeTime) == true || syncService.isFirstSync) {
                            logger.log(VERBOSE, "update setting; name: ${setting.name}, enabled: ${setting.isEnabled}")

                            existingSetting.isEnabled = setting.isEnabled
                            existingSetting.changeTime = setting.changeTime

                            // Manager cancels/schedules the notification, updates the database, and sets flag changed flag to true
                            dailyAssessmentReminderManager.enableReminder(existingSetting.isEnabled)
                            query.resetChangedFlag(existingSetting, false)
                        }
                    } else if (!setting.isEnabled) {
                        logger.log(VERBOSE, "insert setting; name: ${setting.name}, enabled: ${setting.isEnabled}")

                        // If DSA reminder is disabled via another device, create the notication.
                        // Cannot always call start() here because if enabled, the reminder should not appear before the walkthrough.
                        dailyAssessmentReminderManager.start(false)

                        val dsaSetting = query.get(setting.name ?: "")
                        if (dsaSetting != null) {
                            query.resetChangedFlag(dsaSetting, false)
                        }
                    }
                }
                EnvironmentNotificationId.DailyEnvironmentalReminder -> {
                    val dailyEnvironmentalReminderManager: DailyEnvironmentalReminderManager = DependencyProvider.default.resolve()
                    val existingSetting = query.get(setting.name ?: "")

                    if (existingSetting != null ) {

                        if (existingSetting.changeTime == null || setting.changeTime?.isAfter(existingSetting.changeTime) == true || syncService.isFirstSync) {
                            logger.log(VERBOSE, "update setting; name: ${setting.name}, enabled: ${setting.isEnabled}")

                            existingSetting.isEnabled = setting.isEnabled
                            existingSetting.changeTime = setting.changeTime

                            // Manager cancels/schedules the notification, updates the database, and sets flag changed flag to true
                            dailyEnvironmentalReminderManager.enableReminder(existingSetting.isEnabled)
                            query.resetChangedFlag(existingSetting, false)
                        }

                    } else if (!setting.isEnabled) {
                        logger.log(VERBOSE, "insert setting; name: ${setting.name}, enabled: ${setting.isEnabled}")

                        // If DSA reminder is disabled via another device, create the notication.
                        // Cannot always call start() here because if enabled, the reminder should not appear before the walkthrough.
                        dailyEnvironmentalReminderManager.start(false)
                        val environmentSetting = query.get(setting.name ?: "")

                        if (environmentSetting != null) {
                            query.resetChangedFlag(environmentSetting, false)
                        }
                    }
                }
            }
        }
    }

    /**
     * This method merges downloaded UserProfile objects into the database.
     */
    private fun mergeProfilesIntoDatabase(profiles: List<UserProfile>) {

        logger.log(INFO, "mergeProfilesIntoDatabase; ${profiles.size} Profile(s)")
        val existingAccountOwnerProfile = userProfileManager.getAccountOwner() ?: return
        val profile = profiles.firstOrNull { it.isAccountOwner == true &&  it.dateOfBirth != existingAccountOwnerProfile.dateOfBirth}

        if(profile != null) {
            // On first sync, if the DOB is different, update the DHP; otherwise, update locally.
            if (syncService.isFirstSync) {
                userProfileManager.update(existingAccountOwnerProfile, true)
            } else {
                existingAccountOwnerProfile.dateOfBirth = profile.dateOfBirth
                userProfileManager.update(existingAccountOwnerProfile, false)
            }
        }

        val existingActiveProfile = userProfileManager.getActive()
        val activeProfile = profiles.firstOrNull { it.profileId == existingActiveProfile?.profileId }
        if (activeProfile != null) {
            if (profile?.changeTime!! > existingActiveProfile?.changeTime!!.plusSeconds(acceptableServerTimeDifferenceForUpload)){
                careProgramManager.refreshUserProgramListAsync()
            }
        }

    }

    /**
     * This method merges the downloaded objects into the database.
     */
    private fun mergeIntoDatabase(data: CloudObjectContainer) {
        logger.log(VERBOSE, "mergeIntoDatabase()")

        if(!data.prescriptions.isEmpty()) {
            mergePrescriptionsIntoDatabase(data.prescriptions)
        }

        if(!data.devices.isEmpty()) {
            mergeDevicesIntoDatabase(data.devices)
        }

        if(!data.inhaleEvents.isEmpty()) {
            mergeInhaleEventsIntoDatabase(data.inhaleEvents)
        }

        if(!data.dsas.isEmpty()) {
            mergeDSAsIntoDatabase(data.dsas)
        }

        if(!data.settings.isEmpty()) {
            mergeReminderSettingsIntoDatabase(data.settings)
        }

        if (!data.profiles.isEmpty()) {
            mergeProfilesIntoDatabase(data.profiles)
        }
        messenger.post(UpdateAnalysisDataMessage(ArrayList<Any>()))
    }
}