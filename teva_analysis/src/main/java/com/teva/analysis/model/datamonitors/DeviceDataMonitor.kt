//
// DeviceDataMonitor.kt
// teva_analysis
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.analysis.model.datamonitors

import com.teva.analysis.entities.SummaryInfo
import com.teva.analysis.enumerations.SummaryTextId
import com.teva.analysis.messages.UpdateAnalysisDataMessage
import com.teva.utilities.services.DependencyProvider
import com.teva.common.utilities.Messenger
import com.teva.devices.DeviceManagerStringReplacementKey
import com.teva.devices.entities.Device
import com.teva.devices.entities.InhaleEvent
import com.teva.devices.messages.DeviceUpdatedMessage
import com.teva.devices.model.DeviceManagerInhaleEventKey
import com.teva.devices.model.DeviceManagerNotificationId
import com.teva.devices.model.DeviceQuery
import com.teva.notifications.models.NotificationManager

import org.greenrobot.eventbus.Subscribe

/**
 * This class monitors device updates and publishes related notifications and messages.
 */
class DeviceDataMonitor(private val dependencyProvider: DependencyProvider) {
    val summaryMessageQueue = dependencyProvider.resolve<SummaryMessageQueue>()

    init {
        dependencyProvider.resolve<Messenger>().subscribe(this)
    }

    /**
     * This method is the handler for the DeviceUpdatedMessage.
     *
     * @param deviceUpdatedMessage - the DeviceUpdatedMessage.
     */
    @Subscribe
    fun onDeviceUpdated(deviceUpdatedMessage: DeviceUpdatedMessage) {
        val device = deviceUpdatedMessage.device

        if (device.isConnected && device.isNearEmpty) {
            val lastInhaleEvent = deviceUpdatedMessage.inhaleEvents.lastOrNull()

            notifyLowDoseCount(device, lastInhaleEvent)
        }
    }

    /**
     * This method is the handler for the UpdateAnalysisDataMessage.
     *
     * @param updateAnalysisDataMessage - the UpdateAnalysisDataMessage.
     */
    @Suppress("UNUSED_PARAMETER")
    @Subscribe
    fun onUpdateAnalysisData(updateAnalysisDataMessage: UpdateAnalysisDataMessage) {
        checkForSummaryMessages()
    }

    /**
     * This method verifies if the device has low dose count and sends a notification.
     *
     * @param device          - the device.
     * @param lastInhaleEvent - the last inhale event received from the device.
     */
    private fun notifyLowDoseCount(device: Device, lastInhaleEvent: InhaleEvent?) {
        val notificationDataMap = hashMapOf<String, Any>(
                DeviceManagerInhaleEventKey.DEVICE_ID to device.serialNumber,
                DeviceManagerStringReplacementKey.NAME to device.nickname)

        if (lastInhaleEvent != null) {
            notificationDataMap[DeviceManagerInhaleEventKey.INHALE_EVENT_UNIQUE_ID] = lastInhaleEvent.uniqueId
        }

        dependencyProvider.resolve<NotificationManager>().setNotification(
                DeviceManagerNotificationId.DEVICE_NEAR_EMPTY, notificationDataMap)
    }

    /**
     * This method adds new summary messages based on device status.
     */
    private fun checkForSummaryMessages() {
        val devices = dependencyProvider.resolve<DeviceQuery>().getAllActive()

        if (devices.isEmpty()) {
            // Set dashboard message if there are no active inhalers.
            summaryMessageQueue.addMessage(SummaryInfo(SummaryTextId.NO_INHALERS, null))
        } else {

            summaryMessageQueue.removeMessage(SummaryInfo(SummaryTextId.NO_INHALERS, null))
            summaryMessageQueue.removeMessage(SummaryInfo(SummaryTextId.EMPTY_INHALER, null))

            // Check for any inhalers that are near empty.
            val device = devices.firstOrNull { it.isNearEmpty }
            if (device != null) {
                // Set dashboard message that inhaler is near empty.
                val messageData = hashMapOf<String, Any>("Name" to device.nickname)
                val emptyInhalerSummaryInfo = SummaryInfo(SummaryTextId.EMPTY_INHALER, messageData)
                summaryMessageQueue.addMessage(emptyInhalerSummaryInfo)
            }
        }
    }
}
