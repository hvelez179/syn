//
// InhalationDataMonitor.kt
// teva_analysis
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.analysis.model.datamonitors

import com.teva.analysis.entities.SummaryInfo
import com.teva.analysis.enumerations.RelieverUsage
import com.teva.analysis.enumerations.SummaryTextId
import com.teva.analysis.extensions.enumerations.InhalationEffort
import com.teva.analysis.extensions.inhalationEffort
import com.teva.analysis.messages.HistoryUpdatedMessage
import com.teva.analysis.model.AnalyzedDataProvider
import com.teva.common.services.TimeService
import com.teva.common.utilities.DataTask
import com.teva.utilities.services.DependencyProvider
import com.teva.utilities.utilities.Logger
import com.teva.utilities.utilities.Logger.Level.WARN
import com.teva.common.utilities.Messenger
import com.teva.devices.DeviceManagerStringReplacementKey
import com.teva.devices.dataquery.DeviceDataQuery
import com.teva.devices.dataquery.InhaleEventDataQuery
import com.teva.devices.entities.Device
import com.teva.devices.entities.InhaleEvent
import com.teva.devices.enumerations.InhaleStatus
import com.teva.devices.enumerations.SystemErrorCode
import com.teva.devices.model.DeviceManagerInhaleEventKey
import com.teva.devices.model.DeviceManagerNotificationId
import com.teva.medication.dataquery.MedicationDataQuery
import com.teva.medication.entities.Medication
import com.teva.notifications.entities.ReminderSetting
import com.teva.notifications.models.NotificationManager
import org.greenrobot.eventbus.Subscribe
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import java.util.*

/**
 * This class is intended to monitor inhaler usage.
 *
 * @param dependencyProvider - the dependency injection mechanism.
 */
class InhalationDataMonitor(private val dependencyProvider: DependencyProvider) {
    private val logger = Logger(InhalationDataMonitor::class)

    /**
     * This property contains the maximum inhale event age, in minutes,
     * to allow a corresponding inhalation feedback notification to be raised.
     * If the inhale event occurred >= this time interval, then the corresponding
     * notification is not raised.
     */
    private val NOTIFICATION_TIME_WINDOW_MINUTES = 15

    init {

        if (!dependencyProvider.resolve<NotificationManager>()
                .hasReminderSetting(DeviceManagerNotificationId.INHALATIONS_FEEDBACK_TURN_OFF_GOOD_INHALATION_NOTIFICATION)) {
            enableGoodInhalationNotification()
        }

        dependencyProvider.resolve<Messenger>().subscribe(this)
    }

    /**
     * This method enables good inhalation notifications.
     */
    private fun enableGoodInhalationNotification() {
        val reminderSetting = ReminderSetting()

        reminderSetting.isEnabled = true
        reminderSetting.name = DeviceManagerNotificationId.INHALATIONS_FEEDBACK_TURN_OFF_GOOD_INHALATION_NOTIFICATION

        dependencyProvider.resolve<NotificationManager>().saveReminderSettingByName(reminderSetting)
    }

    /**
     * This method is invoked when a HistoryUpdatedMessage is triggered.

     * @param historyUpdatedMessage - the HistoryUpdatedMessage.
     */
    @Subscribe
    fun onHistoryUpdated(historyUpdatedMessage: HistoryUpdatedMessage) {
        // Perform inhalation checks only if InhaleEvents have changed.
        if (historyUpdatedMessage.containsObjectsOfType<InhaleEvent>()) {

            DataTask<Unit, Device>("InhalationDataMonitor_onHistoryUpdated")
                    .inBackground {
                        val inhaleEvents = dependencyProvider.resolve<InhaleEventDataQuery>().getLast(1)
                        var device: Device? = null
                        if (inhaleEvents.isNotEmpty()) {
                            val lastInhaleEvent = inhaleEvents[0]
                            if (historyUpdatedMessage.contains(lastInhaleEvent)) {
                                val query = dependencyProvider.resolve<DeviceDataQuery>()
                                device = query.get(lastInhaleEvent.deviceSerialNumber)
                            }
                        }

                        return@inBackground device
                    }
                    .onResult { result ->
                        provideFeedback(false, result)
                    }
                    .execute()
        } else {
            provideFeedback(true, null)
        }
    }

    /**
     * This method provides feedback for inhalation events.
     *
     * @param overUseOnly - this flag indicates if only overuse feedback needs to be provided.
     * @param device      - the device for which inhalation event was received.
     */
    private fun provideFeedback(overUseOnly: Boolean, device: Device?) {
        val EVENTS_FOR_MULTIPLE_SUBOPTIMAL_INHALATIONS = 12

        DataTask<Unit, FeedbackData>("InhalationDataMonitor_provideFeedback")
                .inBackground {
                    val lastInhaleEvents = dependencyProvider.resolve<InhaleEventDataQuery>().getLast(EVENTS_FOR_MULTIPLE_SUBOPTIMAL_INHALATIONS)
                    val lastInhaleOnlyEvents = dependencyProvider.resolve<InhaleEventDataQuery>().getLast(EVENTS_FOR_MULTIPLE_SUBOPTIMAL_INHALATIONS, InhaleStatus.SystemErrorStatuses)
                    val reminderSetting = dependencyProvider.resolve<NotificationManager>().getReminderSettingByName(DeviceManagerNotificationId.INHALATIONS_FEEDBACK_TURN_OFF_GOOD_INHALATION_NOTIFICATION)

                    return@inBackground FeedbackData(
                            drugUIDToMedicationMap,
                            lastInhaleEvents,
                            lastInhaleOnlyEvents,
                            overUseOnly,
                            reminderSetting?.isEnabled ?: true,
                            device)
                }
                .onResult { result ->
                    if (result != null) {
                        if (overUseOnly) {
                            provideOveruseFeedback(false, result)
                        } else {
                            provideInhalationsFeedback(result)
                        }
                    }
                }
                .execute()
    }

    /**
     * This method returns a map of the medication drugUID and the corresponding medication.
     *
     * @return - a map of the medication drugUID and the corresponding medication.
     */
    private val drugUIDToMedicationMap: Map<String, Medication>
        get() {
            val medicationDataQuery = dependencyProvider.resolve<MedicationDataQuery>()
            val medications = medicationDataQuery.getAll()

            return medications.associateBy { it.drugUID }
        }

    /**
     * This method provides overuse feedback.
     *
     * @param notify       - this flag indicates if inhalation event notification needs to be sent.
     * @param feedbackData - the data used for determining the feedback to be provided.
     */
    private fun provideOveruseFeedback(notify: Boolean, feedbackData: FeedbackData) {
        val today = dependencyProvider.resolve<TimeService>().today()

        if (didOveruseInhaler(today)) {
            sendNotificationAndUpdateOverUseSummary(notify, feedbackData)
        } else {
            dependencyProvider.resolve<SummaryMessageQueue>()
                    .removeMessage(SummaryInfo(SummaryTextId.OVERUSE, null))
        }
    }

    /**
     * This method checks if the inhaler was overused on the specified day.
     *
     * @param date - the day on which the inhaler usage needs to be checked.
     * @return - true if the inhaler was overused, else false.
     */
    private fun didOveruseInhaler(date: LocalDate): Boolean {

        val history = dependencyProvider.resolve<AnalyzedDataProvider>().getHistory(date, date)

        return history.firstOrNull()?.relieverUsage === RelieverUsage.HIGH
    }

    /**
     * This method sends the inhalation notification and updates summary with overuse message.
     *
     * @param notify - this flag indicates if notification has to be sent.
     */
    private fun sendNotificationAndUpdateOverUseSummary(notify: Boolean, feedbackData: FeedbackData) {

        val lastInhaleEvent = feedbackData.lastInhaleEvents.firstOrNull()
        if (lastInhaleEvent != null) {

            val medicationName = feedbackData.medicationMap[lastInhaleEvent.drugUID]?.brandName
            if (medicationName != null) {

                if (notify) {
                    val notificationData = hashMapOf(
                        DeviceManagerStringReplacementKey.MEDICATION_NAME to medicationName,
                        DeviceManagerInhaleEventKey.INHALE_EVENT_UNIQUE_ID to lastInhaleEvent.uniqueId)

                    setNotificationIfInhaleEventOccurredRecently(DeviceManagerNotificationId.DEVICE_OVERUSE, notificationData, lastInhaleEvent.eventTime)
                }

                val messageParameters = hashMapOf<String, Any>("MedicationName" to medicationName)
                val summaryMessage = SummaryInfo(SummaryTextId.OVERUSE, messageParameters)

                dependencyProvider.resolve<SummaryMessageQueue>().addMessage(summaryMessage)
            } else {

                logger.log(WARN, "Medication ${lastInhaleEvent.drugUID} " +
                        "not found when creating Overuse Summary")
            }
        }
    }


    /**
     * This method provides inhalation feedback.
     *
     * @param feedbackData - the data used for determining the feedback to be provided.
     */
    private fun provideInhalationsFeedback(feedbackData: FeedbackData) {

        if (feedbackData.lastInhaleEvents.isNotEmpty()) {
            val mostRecentInhaleEvent = feedbackData.lastInhaleEvents[0]

            if (feedbackData.device != null) {
                // Check for notifications and monitor usage.
                checkAndNotifyForInhalationsFeedback(feedbackData)
            }

            // Prevent checking for Overuse if last inhalation effort was a SystemError.
            val effort = mostRecentInhaleEvent.inhalationEffort
            if (effort !== InhalationEffort.SYSTEM_ERROR) {
                provideOveruseFeedback(true, feedbackData)
            }
        }
    }

    /**
     * This method checks the most recent inhalation for issues and provides feedback.
     *
     * @param feedbackData - the data used for determining the feedback to be provided.
     */
    private fun checkAndNotifyForInhalationsFeedback(feedbackData: FeedbackData) {

        if(checkAndNotifyForSystemError(feedbackData)) {
            return
        }

        if (checkAndNotifyForAcceptableInhalation(feedbackData)) {
            return
        }


        if (checkAndNotifyForSuboptimalInhalation(
                feedbackData,
                InhalationEffort.NO_INHALATION,
                DeviceManagerNotificationId.INHALATIONS_FEEDBACK_NO_INHALATION)) {
            return
        }

        if (checkAndNotifyForSuboptimalInhalation(
                feedbackData,
                InhalationEffort.EXHALATION,
                DeviceManagerNotificationId.INHALATIONS_FEEDBACK_EXHALATION)) {
            return
        }

        if (checkAndNotifyForSuboptimalInhalation(
                feedbackData,
                InhalationEffort.ERROR,
                DeviceManagerNotificationId.INHALATIONS_FEEDBACK_HIGH_INHALATION)) {
            return
        }

    }

    /**
     * This method checks if the most recent inhalation was acceptable.
     *
     * @param feedbackData - the data used for determining the feedback to be provided.
     * @return - true if the inhalation was acceptable, else false.
     */
    private fun checkAndNotifyForAcceptableInhalation(feedbackData: FeedbackData): Boolean {
        val maxConsecutiveSameInhaleEventEffort = 10
        val lastInhaleEvent = feedbackData.lastInhaleEvents[0]
        val inhalationEffort = lastInhaleEvent.inhalationEffort

        if (inhalationEffort.isAcceptable) {
            // Check if Good Inhalation notification is enabled.
            if (feedbackData.goodInhalationFeedbackSettingEnabled) {
                // Check for consecutive inhalation efforts that are either Low or Good.
                val secondaryNotificationId: String
                val normalNotificationId: String
                if (inhalationEffort === InhalationEffort.GOOD_INHALATION) {
                    normalNotificationId = DeviceManagerNotificationId.INHALATIONS_FEEDBACK_GOOD_INHALATION
                    secondaryNotificationId = DeviceManagerNotificationId.INHALATIONS_FEEDBACK_TURN_OFF_GOOD_INHALATION_NOTIFICATION
                } else {
                    // LowInhalation (Note: Unit test verifies that isAcceptable() is only Good, or Low).
                    normalNotificationId = DeviceManagerNotificationId.INHALATIONS_FEEDBACK_LOW_INHALATION
                    secondaryNotificationId = DeviceManagerNotificationId.INHALATIONS_FEEDBACK_TURN_OFF_GOOD_INHALATION_NOTIFICATION_WITH_TIP
                }

                checkAndNotifyConsecutiveAcceptableInhalation(
                        feedbackData.lastInhaleEvents, maxConsecutiveSameInhaleEventEffort,
                        normalNotificationId, secondaryNotificationId)
            }
            return true
        }
        return false
    }

    /**
     * This method checks and notifies if there are a specified number of consecutive inhalation events with acceptable inhalation effort.
     * If not, it sets a notification specified by the normalNotificationId; otherwise by the secondaryNotificationId.
     *
     * @param inhaleEvents                  - This parameter is a list of reverse chronological inhale events.
     * @param maxNumberOfConsecutiveEfforts - This parameter is the threshold of consecutive InhalationEffort values.  Once it equals or exceeds this number, the secondary notification is used.
     * @param normalNotificationId          - This parameter is the notification used when number of specified InhalationEffort values has not been reached.
     * @param secondaryNotificationId       - This parameter is the notification used when number of specified InhalationEffort values has been reached or exceeded.
     */
    private fun checkAndNotifyConsecutiveAcceptableInhalation(inhaleEvents: List<InhaleEvent>, maxNumberOfConsecutiveEfforts: Int, normalNotificationId: String, secondaryNotificationId: String) {

        var numExpectedInhalations = 0
        if (inhaleEvents.size >= maxNumberOfConsecutiveEfforts) {

            // Look at inhalation efforts of the last (chronologically) maxNumberOfConsecutiveEfforts events.
            // Since the list is in reverse chronological order, check the first ones.
            numExpectedInhalations = inhaleEvents.takeWhile { it.inhalationEffort.isAcceptable }.count()
        }

        if (inhaleEvents.isNotEmpty()) {

            val lastEvent = inhaleEvents[0]
            val notificationData = HashMap<String, Any>()
            notificationData.put(DeviceManagerInhaleEventKey.INHALE_EVENT_UNIQUE_ID, lastEvent.uniqueId)

            if (numExpectedInhalations < maxNumberOfConsecutiveEfforts) {
                setNotificationIfInhaleEventOccurredRecently(normalNotificationId,
                        notificationData, lastEvent.eventTime)
            } else {
                setNotificationIfInhaleEventOccurredRecently(secondaryNotificationId,
                        notificationData, lastEvent.eventTime)
            }
        }
    }

    /**
     * This method checks if the most recent inhalation was suboptimal.
     *
     * @param feedbackData           - the data to be used for determining the feedback to be provided.
     * @param targetInhalationEffort - the target inhalation effort that the recent inhalation is being verified for.
     * @param notificationId         - the notification id for the notification to be provided.
     * @return - true if the recent inhalation effort matches the target inhalation effort.
     */
    private fun checkAndNotifyForSuboptimalInhalation(feedbackData: FeedbackData, targetInhalationEffort: InhalationEffort, notificationId: String): Boolean {

        val lastInhaleEvent = feedbackData.lastInhaleEvents[0]
        val inhalationEffort = lastInhaleEvent.inhalationEffort
        if (inhalationEffort == targetInhalationEffort) {

            // Re-enable GoodInhalation notifications after not getting Good effort.
            enableGoodInhalationNotification()

            checkAndNotifyForTooManySuboptimalUses(feedbackData)

            val notificationData = HashMap<String, Any>()
            notificationData.put(DeviceManagerInhaleEventKey.INHALE_EVENT_UNIQUE_ID, lastInhaleEvent.uniqueId)
            setNotificationIfInhaleEventOccurredRecently(notificationId, notificationData, lastInhaleEvent.eventTime)

            return true
        }

        return false
    }

    /**
     * This method checks if there have been too many suboptimal inhalations in the recent past
     * and sends a notification if true.
     *
     * @param feedbackData - the data used to check for too many suboptimal inhalations.
     * @return - true if many suboptimal inhalations were found, else false.
     */
    private fun checkAndNotifyForTooManySuboptimalUses(feedbackData: FeedbackData): Boolean {
        val maxEventsToCheckForUnsuccessfulInhalationEvents = 12
        val maxUnsuccessfulInhaleEvents = 6

        if (feedbackData.lastInhaleOnlyEvents.size < maxUnsuccessfulInhaleEvents) {
            return false
        }

        // Count suboptimal (i.e., unsuccessful) inhalations.
        val lastInhaleEvent = feedbackData.lastInhaleOnlyEvents[0]

        val numSuboptimalInhaleEvents =
                feedbackData.lastInhaleOnlyEvents.count { it.inhalationEffort.isUnsuccessful }

        if (numSuboptimalInhaleEvents >= maxUnsuccessfulInhaleEvents) {
            val notificationData = hashMapOf(
                    DeviceManagerStringReplacementKey.MAX_EVENTS_TO_CHECK_FOR_UNSUCCESSFUL_INHALATION_EVENTS to maxEventsToCheckForUnsuccessfulInhalationEvents,
                    DeviceManagerStringReplacementKey.MAX_UNSUCCESSFUL_INHALE_EVENTS to maxUnsuccessfulInhaleEvents,
                    DeviceManagerInhaleEventKey.INHALE_EVENT_UNIQUE_ID to lastInhaleEvent.uniqueId)

            val drugUID = feedbackData.device?.medication?.drugUID
            val brandName = feedbackData.medicationMap[drugUID]?.brandName
            if (brandName != null) {
                notificationData[DeviceManagerStringReplacementKey.MEDICATION_NAME] = brandName
            }

            setNotificationIfInhaleEventOccurredRecently(
                    DeviceManagerNotificationId.INHALATIONS_FEEDBACK_SUBOPTIMAL_INHALATIONS,
                    notificationData,
                    lastInhaleEvent.eventTime)

            return true
        }

        return false
    }

    /**
     * This method sets the notification for an inhale event if it has occurred
     * recently(within the past 15 minutes).
     *
     * @param categoryId       - the category id for the notification.
     * @param notificationData - the notification data.
     * @param eventTime        - the time at which the inhalation event occurred.
     */
    private fun setNotificationIfInhaleEventOccurredRecently(categoryId: String, notificationData: Map<String, Any>, eventTime: Instant?) {
        if (eventTime != null) {
            val now = dependencyProvider.resolve<TimeService>().now()
            val timeInterval = Duration.between(eventTime, now)
            if (timeInterval.toMinutes() < NOTIFICATION_TIME_WINDOW_MINUTES) {
                dependencyProvider.resolve<NotificationManager>().setNotification(categoryId, notificationData)
            }
        }
    }

    /**
     * This method checks the last inhale event for system error and
     * sends a notification if there is a system error.
     *
     * @param feedbackData - the data used for determining the feedback to be provided.
     * @return - true if the last inhale event had a system error, else false.
     */
    private fun checkAndNotifyForSystemError(feedbackData: FeedbackData): Boolean {
        val lastInhaleEvent = feedbackData.lastInhaleEvents[0]
        val effort = lastInhaleEvent.inhalationEffort
        if (effort === InhalationEffort.SYSTEM_ERROR) {
            val issues = lastInhaleEvent.issues
            val systemErrorCodes = InhaleStatus.getSystemErrorCodes(issues)

            // Build error code string.
            var errorCodes = ""
            for (systemErrorCode in systemErrorCodes) {
                errorCodes = updateErrorCodeString(errorCodes, systemErrorCode)
            }

            val notificationData = hashMapOf(
                    DeviceManagerStringReplacementKey.SYSTEM_ERROR_CODE to errorCodes,
                    DeviceManagerInhaleEventKey.INHALE_EVENT_UNIQUE_ID to lastInhaleEvent.uniqueId)

            setNotificationIfInhaleEventOccurredRecently(DeviceManagerNotificationId.SYSTEM_ERROR_DETECTED, notificationData, lastInhaleEvent.eventTime)

            return true
        }

        return false
    }

    /**
     * This method appends error codes with a delimiter.
     *
     * @param curErrorCodeString - This parameter is the error code string to append the new error code.
     * @param errorCode          - This parameter is the new error code to append.
     */
    private fun updateErrorCodeString(curErrorCodeString: String, errorCode: SystemErrorCode): String {

        var updatedErrorCodeString = curErrorCodeString

        if (updatedErrorCodeString.isNotBlank()) {
            updatedErrorCodeString += ", "
        }

        updatedErrorCodeString += errorCode.code

        return updatedErrorCodeString
    }

    /**
     * This class holds the data used for providing the inhalation feedback.
     * Instead of making multiple asynchronous calls while checking for each
     * inhalation type (good, suboptimal, system error etc.), the necessary
     * information is stored n this class and used as required.
     *
     * @property medicationMap a mapping between the drugUID and medication.
     * @property lastInhaleEvents he recent inhalation events used for checking for successive
     *           good or suboptimal inhalations.
     * @property overUseFeedbackOnly flag to indicate if only overuse feedback needs to be
     *           provided(no recent inhalation events).
     * @property goodInhalationFeedbackSettingEnabled flag to indicate if feedback needs to be provided for good inhalations.
     * @property device The device to which the feedback corresponds.
     */
    private inner class FeedbackData(
            var medicationMap: Map<String, Medication>,
            var lastInhaleEvents: List<InhaleEvent>,
            var lastInhaleOnlyEvents: List<InhaleEvent>,
            var overUseFeedbackOnly: Boolean,
            var goodInhalationFeedbackSettingEnabled: Boolean,
            var device: Device?)

}
