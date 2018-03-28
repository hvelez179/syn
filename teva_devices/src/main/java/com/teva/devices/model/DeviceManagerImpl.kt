//
// DeviceManagerImpl.kt
// teva_devices
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.devices.model

import android.content.SharedPreferences
import android.support.annotation.UiThread
import com.teva.common.messages.AppForegroundMessage
import com.teva.common.services.TimeService
import com.teva.common.utilities.*
import com.teva.utilities.utilities.Logger.Level.*
import com.teva.devices.DeviceManagerStringReplacementKey
import com.teva.devices.dataquery.ConnectionMetaDataQuery
import com.teva.devices.dataquery.DeviceDataQuery
import com.teva.devices.dataquery.InhaleEventDataQuery
import com.teva.devices.entities.ConnectionMeta
import com.teva.devices.entities.Device
import com.teva.devices.entities.InhaleEvent
import com.teva.devices.messages.DeviceConnectedMessage
import com.teva.devices.messages.DeviceDisconnectedMessage
import com.teva.devices.messages.DeviceUpdatedMessage
import com.teva.devices.messages.UpdateDeviceMessage
import com.teva.devices.service.*
import com.teva.notifications.enumerations.RepeatType
import com.teva.notifications.models.NotificationManager
import com.teva.utilities.services.DependencyProvider
import com.teva.utilities.utilities.Logger
import org.greenrobot.eventbus.Subscribe
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.temporal.ChronoUnit
import java.util.*
import kotlin.collections.ArrayList

/**
 * Manages searching for and connecting to inhalers.
 * Updates the database when new data is retrieved from an inhaler.
 */
@UiThread
internal class DeviceManagerImpl(private val dependencyProvider: DependencyProvider,
                                 deviceDataQuery: DeviceDataQuery)
    : DeviceManager, MessageHandler.MessageListener, DeviceServiceCallback {

    private val logger = Logger(DeviceManagerImpl::class)

    val deviceQuery: DeviceQuery = DeviceQueryImpl(deviceDataQuery)

    private val timeService: TimeService = dependencyProvider.resolve()

    private val connectedDevices: MutableMap<String, Duration> = HashMap()
    private val lock = Any()
    private val connectivityCheckLock = Any()

    private val service: DeviceService = dependencyProvider.resolve()

    private val handler: MessageHandler = MessageHandler(this)

    private val inhaleEventDataQuery: InhaleEventDataQuery = dependencyProvider.resolve()
    private val connectionMetaQuery: ConnectionMetaDataQuery = dependencyProvider.resolve()

    private val messenger: Messenger = dependencyProvider.resolve()

    private var isStarted: Boolean = false

    private var nextNoConnectivityNotificationTime: Long = 0

    init {
        service.setCallback(this)

        val sharedPreferences: SharedPreferences = dependencyProvider.resolve()
        nextNoConnectivityNotificationTime = sharedPreferences.getLong(NEXT_NO_CONNECTIVITY_NOTIFICATION_TIME, 0)
    }


    /**
     * Pushes a list of ConnectionInfo for the current active devices into the service.
     */
    private fun updateServiceDeviceList() {
        if (isStarted) {
            DataTask<Unit, List<ConnectionInfo>>("DeviceManager_UpdateServiceDeviceList")
                    .inBackground {
                        val connectionInfoList = ArrayList<ConnectionInfo>()

                        for (device in deviceQuery.getAllActive()) {
                            val connectionInfo = ConnectionInfo()
                            connectionInfo.serialNumber = device.serialNumber
                            connectionInfo.authenticationKey = device.authenticationKey
                            connectionInfo.lastRecordId = device.lastRecordId
                            connectionInfo.protocolType = ProtocolType.Inhaler

                            connectionInfoList.add(connectionInfo)
                        }

                        logger.log(DEBUG, "updateServiceDeviceList(): " + connectionInfoList.size + " devices")

                        return@inBackground connectionInfoList
                    }
                    .onResult { result ->
                        result?.let { service.setConnectionInfo(it) }
                    }
                    .execute()
        }
    }

    /**
     * Starts the DeviceManager processing
     */
    override fun start() {
        isStarted = true
        service.start()
        messenger.subscribe(this)

        updateServiceDeviceList()
    }

    /**
     * Shuts down the DeviceManager processing
     */
    override fun stop() {
        service.stop()
        isStarted = false
        messenger.unsubscribeToAll(this)
    }

    /**
     * Forces the DeviceManager to stop and start again.
     * This might be useful to someone who wishes to update the ConnectionInfoList.
     * This method disconnects any connected MedicationDispensers.
     */
    override fun restart() {
        stop()
        start()
    }

    /**
     * Message handler for the AppForegroundMessage that indicates when the application
     * is in the foreground state.
     */
    @Subscribe
    fun onAppForeground(message: AppForegroundMessage) {
        service.setInForeground(message.inForeground)
    }

    @Subscribe
    fun onUpdateDeviceMessage(message: UpdateDeviceMessage) {

        val today = timeService.today()

        val connectionMetaList = connectedDevices.keys.map { ConnectionMeta(today, it) }

        DataTask<Unit, Unit>("DeviceManagerImpl_onUpdateDeviceMessage")
                .inBackground {
                    for (connectionMeta in connectionMetaList) {
                        connectionMetaQuery.insertOrUpdate(connectionMeta, true)
                    }
                }
                .execute()
    }

    /**
     * Returns true if the bluetooth radio is on, false otherwise
     */
    override val isBluetoothEnabled: Boolean
        get() = service.isBluetoothEnabled

    /**
     * Callback from the MessageHandler that receives messages for other threads.
     *
     * @param message The message that occurred.
     */
    override fun onMessage(message: Int) {
        if (message == DEVICE_UPDATED_MESSAGE_ID) {
            updateServiceDeviceList()

            // when the device is updated (inserted, deleted, marked inactive etc.)
            // check if the no connectivity notification needs to be scheduled or cleared.
            checkAndScheduleNoConnectivityNotification()
        }
    }

    /**
     * Indicates that a device has connected.
     *
     * @param connectionInfo The device that was connected.
     */
    override fun onConnected(connectionInfo: ConnectionInfo) {
        logger.log(VERBOSE, "onConnected: " + connectionInfo.serialNumber)

        var showConnectionNotification = false

        updateDeviceInBackground(connectionInfo.serialNumber) { device, _ ->
            synchronized(lock) {
                val lastConnection = device.lastConnection
                val duration: Duration
                if(lastConnection == null) {
                    showConnectionNotification = true
                    duration = Duration.ofMillis(0)
                } else {
                    duration = Duration.between(timeService.now(), device.lastConnection)
                }

                connectedDevices.put(device.serialNumber, duration)
            }
        }.onResult { result ->
            checkAndScheduleNoConnectivityNotification()

            if (result != null) {

                if(showConnectionNotification) {
                    notifyConnection(result)
                }
                messenger.post(DeviceConnectedMessage(result))
            }
        }.execute()
    }

    /**
     * Indicates that a device has disconnected.
     *
     * @param connectionInfo The device that was disconnected.
     */
    override fun onDisconnected(connectionInfo: ConnectionInfo) {
        logger.log(VERBOSE, "onDisconnected: " + connectionInfo.serialNumber)

        updateDeviceInBackground(connectionInfo.serialNumber) { device, _ ->
            synchronized(lock) {
                connectedDevices.remove(device.serialNumber)
            }
        }.onResult { result ->
            messenger.post(DeviceDisconnectedMessage(result!!))
            checkAndScheduleNoConnectivityNotification()
        }.execute()
    }

    /**
     * Indicates that device attributes or inhale events have been updated.
     *
     * @param connectionInfo The device that was updated.
     * @param deviceInfo     The updated DeviceInfo.
     * @param events         The new events downloaded from the device.
     */
    override fun onUpdated(connectionInfo: ConnectionInfo,
                           deviceInfo: DeviceInfo,
                           events: List<InhaleEventInfo>) {
        logger.log(INFO, "onUpdated: ${connectionInfo.serialNumber} dosesTaken: ${deviceInfo.dosesTaken} events.size(): ${events.size}")

        updateDeviceInBackground(connectionInfo.serialNumber) { device, eventCount ->
            deviceInfo.updateDevice(device)

            var inhaleEventCount = if(device.remainingDoseCount > 0) device.dosesTaken else eventCount

            // insert only if the event is not already in the database.
            val inhaleEventList: MutableList<InhaleEvent> = ArrayList()
            for(event in events) {
                if(null == inhaleEventDataQuery.get(event.eventUID, device) && inhaleEventCount <= device.doseCount + device.medication!!.emptyDoseCountThreshold ) {
                    inhaleEventList.add(event.toInhaleEvent(device.serialNumber, device.medication!!.drugUID))
                }
                inhaleEventCount++
            }

            // generate ConnectionMeta records for each event
            for (it in inhaleEventList) {
                val metadata = ConnectionMeta(it.localEventTime.toLocalDate(), device.serialNumber)
                connectionMetaQuery.insertOrUpdate(metadata, false)
            }

            logger.log(VERBOSE, "onUpdated: inserting " + inhaleEventList.size + " records")
            inhaleEventDataQuery.insert(inhaleEventList, true)

            messenger.post(DeviceUpdatedMessage(device, inhaleEventList))
        }.execute()
    }

    private inner class DeviceQueryImpl(private val deviceDataQuery: DeviceDataQuery) : DeviceQuery {

        private fun postDeviceUpdatedMessage() {
            handler.removeMessages(DEVICE_UPDATED_MESSAGE_ID)
            handler.sendEmptyMessageDelayed(DEVICE_UPDATED_MESSAGE_ID, DEVICE_UPDATED_MESSAGE_DELAY.toLong())
        }

        /**
         * Gets a value indicating whether the database contains device objects.
         */
        override fun getHasDevices(): Boolean {
            return deviceDataQuery.hasData()
        }

        /**
         * Gets the number of connected devices.
         */
        override fun getConnectedDeviceCount(): Int {
            return synchronized(lock) { connectedDevices.size }
        }

        /**
         * Marks a device as inactive in the database.
         */
        override fun removeDevice(device: Device) {
            deviceDataQuery.markAsDeleted(device)

            // post a message to the handler that will update
            // the connection info list.
            postDeviceUpdatedMessage()
        }

        /**
         * Marks a previous inactive device as active.
         */
        override fun undoRemoveDevice(device: Device) {
            deviceDataQuery.undoMarkAsDeleted(device)

            // post a message to the handler that will update
            // the connection info list.
            postDeviceUpdatedMessage()
        }


        /**
         * Inserts this object in the data store and mark as changed if specified.
         */
        override fun insert(model: Device, changed: Boolean) {
            deviceDataQuery.insert(model, changed)
            postDeviceUpdatedMessage()
        }

        /**
         * Inserts the objects in the data store and mark as changed if specified.
         */
        override fun insert(modelObjects: List<Device>, changed: Boolean) {
            deviceDataQuery.insert(modelObjects, changed)
            postDeviceUpdatedMessage()
        }

        /**
         * Updates the item in the data store that matches the given object and
         * mark as changed if specified.
         */
        override fun update(model: Device, changed: Boolean) {
            deviceDataQuery.update(model, changed)
            postDeviceUpdatedMessage()
        }

        /**
         * Updates the objects in the data store that matches the given object
         * and mark as changed if specified.
         */
        override fun update(modelObjects: List<Device>, changed: Boolean) {
            deviceDataQuery.update(modelObjects, changed)
            postDeviceUpdatedMessage()
        }

        /**
         * Inserts the given object in the data store. If it already exists, update instead.
         * Mark the object as changed if specified.
         */
        override fun insertOrUpdate(model: Device, changed: Boolean) {
            if (deviceDataQuery.get(model.serialNumber) == null) {
                model.lastConnection = timeService.now()
            }
            deviceDataQuery.insertOrUpdate(model, changed)
            postDeviceUpdatedMessage()
        }

        /**
         * Deletes the object from the data store.
         */
        override fun delete(model: Device) {

            deviceDataQuery.delete(model)
            postDeviceUpdatedMessage()
        }

        /**
         * Resets the changed flag and do no pubilsh a model changed message.
         *
         * @param model The device to update
         */
        override fun resetChangedFlag(model: Device, changed: Boolean) {
            deviceDataQuery.resetChangedFlag(model, changed)
        }

        /**
         * Fetches all the items in the data store.
         */
        override fun getAll(): List<Device> {
            val deviceList = deviceDataQuery.getAll()
            augmentDeviceList(deviceList)
            return deviceList
        }

        /**
         * Checks if there are data in the data store.
         */
        override fun hasData(): Boolean {
            return deviceDataQuery.hasData()
        }

        /**
         * Gets the last connected active controller.
         */
        override fun lastConnectedActiveController(): Device? {
            return deviceDataQuery.lastConnectedActiveController()
        }

        /**
         * Gets the last connected active reliever.
         */
        override fun lastConnectedActiveReliever(): Device? {
            return deviceDataQuery.lastConnectedActiveReliever()
        }

        /**
         * Gets the device with the given serial number.
         */
        override operator fun get(serialNumber: String): Device? {
            val device = deviceDataQuery.get(serialNumber)
            if (device != null) {
                augmentDevice(device)
            }

            return device
        }

        /**
         * Gets all the data that are flagged as changed.
         */
        override fun getAllChanged(): List<Device> {
            val deviceList = deviceDataQuery.getAllChanged()
            augmentDeviceList(deviceList)
            return deviceList
        }

        /**
         * Gets all active devices.
         */
        override fun getAllActive(): List<Device> {
            val deviceList = deviceDataQuery.getAllActive()
            augmentDeviceList(deviceList)
            return deviceList
        }

        /**
         * Returns true if the there are any active devices.
         */
        override fun hasActiveDevices(): Boolean {
            return deviceDataQuery.hasActiveDevices()
        }

        /**
         * Marks the device as deleted. This will de-activate and disconnect with the device.
         */
        override fun markAsDeleted(device: Device) {
            device.isActive = false
            device.lastConnection = null
            deviceDataQuery.update(device, true)
            postDeviceUpdatedMessage()
        }

        /**
         * Restore a previously de-activated device. Marks device.isActive = true
         */
        override fun undoMarkAsDeleted(device: Device) {
            device.isActive = true
            deviceDataQuery.update(device, true)
            postDeviceUpdatedMessage()
        }

        /**
         * Get a device by nickname.
         *
         * @param nickname The nickname to search for.
         */
        override fun has(nickname: String): Boolean {
            return deviceDataQuery.has(nickname)
        }

        /**
         * Adds connection related information to a device object.
         */
        private fun augmentDevice(device: Device) {
            synchronized(lock) {
                if (connectedDevices.containsKey(device.serialNumber)) {
                    device.isConnected = true
                    device.disconnectedTimeSpan = connectedDevices[device.serialNumber]
                } else {
                    device.isConnected = false
                    if (device.lastConnection != null) {
                        device.disconnectedTimeSpan = Duration.between(device.lastConnection!!, timeService.now())
                    }
                }
            }
        }


        /**
         * Adds connection related information to the device objects in a list.
         */
        private fun augmentDeviceList(deviceList: List<Device>) {
            for (device in deviceList) {
                augmentDevice(device)
            }
        }
    }

    private fun updateDeviceInBackground(serialNumber: String, updateFunc: (Device, Int) -> Unit)
            : DataTask<Unit, Device> {
        val task = DataTask<Unit, Device>("DeviceManager_DeviceUpdateTask")
                .inBackground {
                    val device = deviceQuery.get(serialNumber)

                    if (device != null) {
                        val eventCount = inhaleEventDataQuery.getCount(device.serialNumber)
                        updateFunc(device, eventCount)

                        if(device.isActive) {
                            device.lastConnection = timeService.now()
                        }

                        deviceQuery.update(device, true)

                        connectionMetaQuery.insertOrUpdate(
                                ConnectionMeta(timeService.today(), device.serialNumber), false)
                    }

                    return@inBackground device
                }

        return task
    }

    /**
     * This method checks if there are any active devices which are disconnected
     * and schedules a notification to be displayed if device remains disconnected
     * for a week. If all active devices are connected, this method clears any
     * previously scheduled notifications.
     */
    private fun checkAndScheduleNoConnectivityNotification() {

        DataTask<Unit, List<Device>>("DeviceManagerImpl_noConnectivityMessage")
                .inBackground {
                    synchronized(connectivityCheckLock) {
                        deviceQuery.getAllActive()
                    }
                }
                .onResult { result ->
                    // check if there were any disconnected devices
                    val allActiveDevicesConnected = result!!.none { !it.isConnected }

                    // get the oldest disconnect time
                    val oldestDisconnectTime: Instant? =
                            result.filter { !it.isConnected && it.lastConnection != null }
                                    .map { it.lastConnection!! }.min()



                    if (allActiveDevicesConnected) {
                        clearConnectivityNotification()
                    } else if(oldestDisconnectTime != null){
                        scheduleNoConnectivityNotification(oldestDisconnectTime)
                    }
                }
                .execute()
    }

    /**
     * Schedules the No Connectivity notification
     */
    private fun scheduleNoConnectivityNotification(oldestDisconnectTime: Instant) {
        val notificationManager: NotificationManager = dependencyProvider.resolve()
        val sharedPreferences: SharedPreferences = dependencyProvider.resolve()
        val sharedPreferencesEditor = sharedPreferences.edit()

        val proposedNotificationTime = oldestDisconnectTime.plusSeconds((DAYS_PER_WEEK * SECONDS_PER_DAY).toLong())

        val newNotificationTime: Instant
        val now = timeService.now()

        if (nextNoConnectivityNotificationTime == 0L) {
            // if there are no scheduled notifications, schedule the notification
            // one week from the disconnect time.
            newNotificationTime = proposedNotificationTime
        } else {

            var scheduledNotificationTime = Instant.ofEpochMilli(nextNoConnectivityNotificationTime)

            // Calculate the next notification time by incrementing the
            // previous scheduled time by multiples of 7 days till the next
            // notification time is beyond the current time.
            while (scheduledNotificationTime.isBefore(now)) {
                scheduledNotificationTime = scheduledNotificationTime.plusSeconds((DAYS_PER_WEEK * SECONDS_PER_DAY).toLong())
            }

            // compare the two calculated notification times and pick the latest.
            newNotificationTime = if (scheduledNotificationTime.isAfter(proposedNotificationTime))
                scheduledNotificationTime
            else
                proposedNotificationTime
        }

        // Scheduling notification requires number of days after which
        // the notification is to be scheduled. Calculate the number of days.
        val nextNotificationTimeInCurrentTimeZone = ZonedDateTime.ofInstant(newNotificationTime, ZoneId.systemDefault())
        val currentTime = ZonedDateTime.ofInstant(now, ZoneId.systemDefault())
        // calculate number of days based on the number of minutes and add one day if this number
        // is not an exact multiple of the number of minutes per day.
        val notificationMinutesFromNow = currentTime.until(nextNotificationTimeInCurrentTimeZone, ChronoUnit.MINUTES)
        val notificationDaysFromNow = (notificationMinutesFromNow / MINUTES_PER_DAY + if (notificationMinutesFromNow % MINUTES_PER_DAY > 0) 1 else 0).toInt()
        val newNotificationLocalTime = nextNotificationTimeInCurrentTimeZone.toLocalTime()

        // schedule the next notification and store the time in the shared preferences.
        notificationManager.setNotification(DeviceManagerNotificationId.CONNECTIVITY_DISCONNECTED_7_DAYS_OR_MORE, HashMap<String, Any>(), notificationDaysFromNow, newNotificationLocalTime, RepeatType.ONCE_PER_WEEK)
        nextNoConnectivityNotificationTime = newNotificationTime.toEpochMilli()
        sharedPreferencesEditor.putLong(NEXT_NO_CONNECTIVITY_NOTIFICATION_TIME, nextNoConnectivityNotificationTime)
        sharedPreferencesEditor.apply()
    }

    /**
     * Clears the No Connectivity notification
     */
    private fun clearConnectivityNotification() {
        val notificationManager: NotificationManager = dependencyProvider.resolve()
        val sharedPreferences: SharedPreferences = dependencyProvider.resolve()
        val sharedPreferencesEditor = sharedPreferences.edit()

        // if all active devices are connected, clean up any
        // scheduled notifications.
        notificationManager.disableNotification(DeviceManagerNotificationId.CONNECTIVITY_DISCONNECTED_7_DAYS_OR_MORE)
        nextNoConnectivityNotificationTime = 0
        sharedPreferencesEditor.putLong(NEXT_NO_CONNECTIVITY_NOTIFICATION_TIME, 0)
        sharedPreferencesEditor.apply()
    }

    /**
     * This method sets the device connection notification.
     *
     * @param device - the device being connected.
     */
    private fun notifyConnection(device: Device) {
        // First time connected.
        val notificationData = hashMapOf(
                DeviceManagerStringReplacementKey.MEDICATION_NAME to device.medication!!.brandName!!,
                DeviceManagerInhaleEventKey.DEVICE_ID to device.serialNumber,
                DeviceManagerStringReplacementKey.NAME to device.nickname)

        dependencyProvider.resolve<NotificationManager>().setNotification(DeviceManagerNotificationId.CONNECTIVITY_NOW_CONNECTED, notificationData)
    }

    companion object {
        private val DAYS_PER_WEEK = 7
        private val HOURS_PER_DAY = 24
        private val MINUTES_PER_HOUR = 60
        private val SECONDS_PER_MINUTE = 60
        private val MINUTES_PER_DAY = MINUTES_PER_HOUR * HOURS_PER_DAY
        private val SECONDS_PER_HOUR = SECONDS_PER_MINUTE * MINUTES_PER_HOUR
        private val SECONDS_PER_DAY = SECONDS_PER_HOUR * HOURS_PER_DAY

        private val DEVICE_UPDATED_MESSAGE_ID = 2
        private val DEVICE_UPDATED_MESSAGE_DELAY = 250
        private val NEXT_NO_CONNECTIVITY_NOTIFICATION_TIME = "NextNoConnectivityNotificationTime"
    }
}
