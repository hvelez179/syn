//
// MedicationDispenserImpl.kt
// teva_devices
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.devices.service

import android.bluetooth.*
import android.content.Context
import android.support.annotation.BinderThread
import android.support.annotation.MainThread
import com.teva.common.services.RunModes
import com.teva.common.services.TimeService
import com.teva.utilities.services.DependencyProvider
import com.teva.utilities.utilities.Logger
import com.teva.utilities.utilities.Logger.Level.*
import com.teva.common.utilities.MessageHandler
import com.teva.common.utilities.ubyte
import com.teva.common.utilities.ushort
import org.jetbrains.annotations.NonNls
import org.threeten.bp.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.Charset
import java.security.GeneralSecurityException
import java.security.SecureRandom
import java.util.*
import android.bluetooth.BluetoothGatt
import com.teva.common.messages.SystemMonitorMessage
import com.teva.common.utilities.Messenger
import com.teva.devices.enumerations.DeviceActivity


/**
 * This class handles the authentication with the inhaler as well as the reading of inhale events.
 */
class MedicationDispenserImpl
@MainThread
@Throws(Exception::class)
constructor(private val dependencyProvider: DependencyProvider,
            override val connectionInfo: ConnectionInfo,
            val bluetoothDevice: BluetoothDevice) : MedicationDispenser, MessageHandler.MessageListener {

    private val timeService: TimeService = dependencyProvider.resolve()

    private var aesCryptor: AESCryptor? = null

    private var hasSimulatorService: Boolean = false

    var simulatorTimePending: Boolean = false
    var racpPending: Boolean = false

    private var serviceMap: MutableMap<UUID, BluetoothGattService>? = null
    private val debugId: Int

    private val mainHandler: MessageHandler = MessageHandler(this)
    private var medicationDispenserCallback: MedicationDispenserCallback? = null

    private var isDisconnecting: Boolean = false

    private var writePending: Boolean = false
    private val pendingChanges: MutableList<PendingChange> = ArrayList()

    // these fields are accessed by unit test so must be package local, not private
    var bluetoothGatt: BluetoothGatt? = null

    val deviceInfo: DeviceInfo = DeviceInfo()
    private val inhaleEventList: MutableList<InhaleEventInfo> = ArrayList()

    private var authenticationRand1: ByteArray? = null

    private var authenticationRand2: ByteArray? = null

    private var nonce: ByteArray? = null

    var gattCallback: BluetoothGattCallback = GattCallback()

    var state: State = State.CONNECT

    /**
     * Used to retry the connection for a maximum time.
     * Must use the real OS time for this, not the TimeService.
     */
    val connectionTime: Instant = Instant.now()

    private var authenticationStartTime: Instant? = null

    enum class State {
        CONNECT,
        CONNECTING,
        DISCOVER_SERVICES,
        VERIFYING_SERIAL_NUMBER,
        SET_NOTIFY_AUTHENTICATION,
        AUTHENTICATION_1,
        AUTHENTICATION_2,
        READING_MANUFACTURER_NAME,
        READING_HARDWARE_REVISION,
        READING_SOFTWARE_REVISION,
        READING_DEVICE_STATE,
        SET_NOTIFY_DEVICE_STATE,
        SET_NOTIFY_INHALE_EVENT_STATE,
        SET_NOTIFY_RACP_STATE,
        SET_NOTIFY_SIMULATOR_STATE,
        IDLE,
        TIME_SYNC,
        WRITE_NONCE,
        DELAY_RACP_START,
        STARTING_RACP,
        WAITING_FOR_RACP
    }

    private enum class Action {
        CONNECTED,
        SERVICES_DISCOVERED,
        READ,
        WRITE,
        WRITE_DESCRIPTOR,
        CHANGED,
        DELAY
    }

    private inner class PendingChange {
        internal var serviceUuid: UUID? = null
        internal var characteristicUuid: UUID? = null
        internal var value: ByteArray? = null
    }

    init {
        logger.log(VERBOSE, "Constructor")
        debugId = nextDebugId++

        deviceInfo.lastRecordId = connectionInfo.lastRecordId

        try {
            logger.log(VERBOSE, "authentication key: " + connectionInfo.authenticationKey)
            aesCryptor = AESCryptor(connectionInfo.authenticationKey)
        } catch (e: GeneralSecurityException) {
            throw Exception("Invalid authentication key", e)
        }

    }

    /**
     * Requests that the MedicationDispenser connect to the physical device.
     */
    override fun connect() {
        logger.log(INFO, "Connecting to " + connectionInfo.serialNumber)

        state = State.CONNECT
        processState(null, null, null, null)
    }

    /**
     * Requests that the MedicationDispenser disconnect from the physical device.
     */
    override fun disconnect() {
        logger.log(INFO, "Disconnecting from " + connectionInfo.serialNumber)

        if (!isDisconnecting) {
            isDisconnecting = true

            if (bluetoothGatt != null) {
                bluetoothGatt!!.close()
                medicationDispenserCallback?.onDisconnected(connectionInfo)
            }
        }
    }

    /**
     * Sets the callback interface used to relay events from the physical device.
     */
    @MainThread
    override fun setCallback(callback: MedicationDispenserCallback) {
        this.medicationDispenserCallback = callback
    }

    /**
     * Searches for the service with the specified UUID.  Allows derived class to
     * overload this method to add search logic.
     */
    private fun getService(serviceId: UUID): BluetoothGattService? {

        if (serviceMap == null) {
            serviceMap = HashMap<UUID, BluetoothGattService>()

            for (service in bluetoothGatt!!.services) {
                val uuid = service.uuid

                if (uuid == DEVICE_INFORMATION_SERVICE_UUID && service.getCharacteristic(SERIAL_NUMBER_UUID) == null) {
                    continue
                }

                serviceMap!!.put(uuid, service)
            }
        }

        return serviceMap!![serviceId]
    }

    /**
     * Gets a characteristic object from a GATT service.
     *
     * @param serviceId        The id of the GATT service
     * @param characteristicId The id of the characteristic
     * @return The BluetoothGattCharacteristic object.
     */
    private fun getCharacteristic(serviceId: UUID, characteristicId: UUID): BluetoothGattCharacteristic? {
        var characteristic: BluetoothGattCharacteristic? = null

        val service = getService(serviceId)

        if (service != null) {
            characteristic = service.getCharacteristic(characteristicId)
        }

        return characteristic
    }

    /**
     * Method called when an error has occured.
     *
     * @param reason A description of the reason for the error.
     */
    private fun onError(@NonNls reason: String) {
        logger.log(ERROR, debugId.toString() + " onError() - state: " + state + " reason: " + reason)
        mainHandler.post { disconnect() }
        mainHandler.post { medicationDispenserCallback?.onDisconnected(connectionInfo) }
    }

    /**
     * Initiates a read from the Bluetooth device.
     *
     * @param serviceUUID        The id of the Bluetooth GATT service.
     * @param characteristicUUID The id of the Bluetooth GATT characteristic.
     */
    private fun readCharacteristic(serviceUUID: UUID, characteristicUUID: UUID) {
        val service = getService(serviceUUID)
        if (service != null) {
            val characteristic = service.getCharacteristic(characteristicUUID)
            if (characteristic != null) {
                if (!bluetoothGatt!!.readCharacteristic(characteristic)) {
                    onError("readCharacteristic($serviceUUID, $characteristicUUID) failed")
                }
            } else {
                onError("Characteristic not found: " + characteristicUUID)
            }

        } else {
            onError("Service not found: " + serviceUUID)
        }
    }

    /**
     * Initiates a write to the Bluetooth device.
     *
     * @param serviceUUID        The id of the Bluetooth GATT service.
     * @param characteristicUUID The id of the Bluetooth GATT characteristic.
     * @param value              The value to write.
     */
    private fun writeCharacteristic(serviceUUID: UUID, characteristicUUID: UUID, value: ByteArray) {
        val service = getService(serviceUUID)
        if (service != null) {
            val characteristic = service.getCharacteristic(characteristicUUID)
            if (characteristic != null) {
                // Notified characteristic changes that occur due to a write
                // will come before the onCharacteristicWrite() callback.
                // So we queue up the deferred change notifications and
                // send them once the write completed notification is sent.
                writePending = true

                characteristic.value = value
                if (!bluetoothGatt!!.writeCharacteristic(characteristic)) {
                    onError("writeCharacteristic($serviceUUID, $characteristicUUID) failed")
                }
            } else {
                onError("Characteristic not found: " + characteristicUUID)
            }

        } else {
            onError("Service not found: " + serviceUUID)
        }
    }

    /**
     * Initiates a descriptor write to the Bluetooth device to enable characteristic notifications.
     *
     * @param serviceUUID        The id of the Bluetooth GATT service.
     * @param characteristicUUID The id of the Bluetooth GATT characteristic.
     * @param isIndication       Indicates whether indications or notifications should be enabled.
     */
    private fun setNotifyCharacteristic(serviceUUID: UUID, characteristicUUID: UUID, isIndication: Boolean) {
        val characteristic = getCharacteristic(serviceUUID, characteristicUUID)

        if (characteristic != null && bluetoothGatt!!.setCharacteristicNotification(characteristic, true)) {
            val descriptor = characteristic.getDescriptor(NOTIFICATION_DESCRIPTOR_UUID)
            if (isIndication) {
                descriptor.value = BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
            } else {
                descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            }

            val succeeded = bluetoothGatt!!.writeDescriptor(descriptor)
            if (!succeeded) {
                onError("writeDescriptor failed: serviceId=$serviceUUID characteristicId=$characteristicUUID")
            }
        } else {
            onError("Characteristic not found: " + characteristicUUID)
        }
    }

    /**
     * Creates an array of random bytes.
     *
     * @param length The length of the array
     * @return An array of random bytes
     */
    private fun createRandomBuffer(length: Int): ByteArray {
        val random = SecureRandom()

        val buffer = ByteArray(length)
        random.nextBytes(buffer)

        return buffer
    }

    // State machine methods

    /**
     * State machine method for the Bluetooth communication.
     *
     * @param action             The Bluetooth action that occurred
     * @param serviceUuid        The UUID of the service, or null if not applicable.
     * @param characteristicUuid The UUID of the characteristic, or null if not applicable.
     * @param value              The value of the read or change notification, or null if not applicable.
     */
    @MainThread
    private fun processState(action: Action?, serviceUuid: UUID?, characteristicUuid: UUID?, value: ByteArray?) {
        logger.log(VERBOSE, "processState() - state: " + state + " action: " + action + " serviceUuid: " + serviceUuid + " characteristicUuid: " + characteristicUuid + " value: " + Logger.toHexString(value))

        try {
            // check for asynchronous Device State or Simulator notifications that
            // can happen at any time and don't affect the current state of the state machine
            // unless we are in the idle state.
            val isDeviceStateNotification = action == Action.CHANGED
                    && serviceUuid == INHALER_SERVICE_UUID
                    && characteristicUuid == DEVICE_STATE_UUID

            val isSimulatorTimeNotification = action == Action.CHANGED
                    && serviceUuid == SIMULATOR_SERVICE_UUID
                    && characteristicUuid == SIMULATOR_TIME_UUID

            if (isDeviceStateNotification) {
                if (!parseDeviceInfo(value)) {
                    onError("Received malformed DeviceInfo notification")
                }
            } else if (isSimulatorTimeNotification) {
                val runModes = RunModes.fromRawValue(value!![0].toInt())
                timeService.initializeTimeService(runModes)
                simulatorTimePending = true
            } else {
                when (state) {
                // Initial state that starts the connection process
                    MedicationDispenserImpl.State.CONNECT -> {
                        // connect to the device.
                        state = State.CONNECTING
                        bluetoothDevice.connectGatt(dependencyProvider.resolve<Context>(), false, gattCallback)
                    }

                // Connect completed
                    MedicationDispenserImpl.State.CONNECTING -> {
                        if (action != Action.CONNECTED) {
                            onError("Wrong action")
                        } else {
                            logger.log(INFO, "Connected to " + connectionInfo.serialNumber)

                            // discover the services provided by the device.
                            state = State.DISCOVER_SERVICES
                            bluetoothGatt!!.discoverServices()
                        }
                    }

                // discoverServices() completed
                    MedicationDispenserImpl.State.DISCOVER_SERVICES -> {
                        if (action != Action.SERVICES_DISCOVERED) {
                            onError("Wrong action")
                        } else {
                            // check for the simulator service
                            for (service in bluetoothGatt!!.services) {
                                if (service.uuid == SIMULATOR_SERVICE_UUID) {
                                    hasSimulatorService = true
                                    simulatorTimePending = true
                                }
                            }

                            // read the serial number to verify with the expected serial number
                            state = State.VERIFYING_SERIAL_NUMBER
                            readCharacteristic(DEVICE_INFORMATION_SERVICE_UUID, SERIAL_NUMBER_UUID)
                        }
                    }

                // Serial number read complete.
                    MedicationDispenserImpl.State.VERIFYING_SERIAL_NUMBER -> {
                        if (serviceUuid != DEVICE_INFORMATION_SERVICE_UUID
                                || characteristicUuid != SERIAL_NUMBER_UUID
                                || action != Action.READ) {
                            onError("Wrong service, characteristic, or action")
                        } else {
                            // verify the serial number matches the expected one.
                            val stringValue = String(value!!, ASCII_CHARSET)
                            if (connectionInfo.serialNumber == stringValue) {
                                authenticationStartTime = timeService.now()
                                // setup the authentication characteristic for notifications
                                state = State.SET_NOTIFY_AUTHENTICATION
                                setNotifyCharacteristic(INHALER_SERVICE_UUID, AUTHENTICATION_KEY_UUID, true)
                            } else {
                                onError("Wrong serial number")
                            }
                        }
                    }

                // Setting up the authentication characteristic for notifications
                    MedicationDispenserImpl.State.SET_NOTIFY_AUTHENTICATION -> {
                        if (serviceUuid != INHALER_SERVICE_UUID
                                || characteristicUuid != AUTHENTICATION_KEY_UUID
                                || action != Action.WRITE_DESCRIPTOR) {
                            onError("Wrong service, characteristic, or action")
                        } else {
                            // start authentication by sending a random 8 byte buffer.
                            state = State.AUTHENTICATION_1
                            authenticationRand1 = createRandomBuffer(HALF_AUTHENTICATION_LENGTH)
                            logger.log(VERBOSE, "authenticationRand1: " + Logger.toHexString(authenticationRand1))

                            writeCharacteristic(INHALER_SERVICE_UUID, AUTHENTICATION_KEY_UUID, authenticationRand1!!)
                        }
                    }

                // 1st authentication response received.
                    MedicationDispenserImpl.State.AUTHENTICATION_1 -> {
                        if (serviceUuid != INHALER_SERVICE_UUID
                                || characteristicUuid != AUTHENTICATION_KEY_UUID
                                || action != Action.WRITE && action != Action.CHANGED) {
                            onError("Wrong service, characteristic, or action")
                        } else if (action == Action.CHANGED) {
                            // advance to the next state on the CHANGED action

                            if (value == null || value.size != HALF_AUTHENTICATION_LENGTH) {
                                onError("Incorrect value length")
                            } else {
                                // save the 8 byte authentication response
                                authenticationRand2 = value
                                logger.log(VERBOSE, "authenticationRand2: " + Logger.toHexString(authenticationRand2))
                            }

                            // create a buffer combining the two 8 byte buffers and
                            // send it to the device.
                            val buf = ByteArray(AUTHENTICATION_LENGTH)
                            val byteBuffer = ByteBuffer.wrap(buf)
                            byteBuffer.put(authenticationRand1)
                            byteBuffer.put(authenticationRand2)

                            logger.log(VERBOSE, "combined authentication buffer: " + Logger.toHexString(buf))

                            val encryptedBuf: ByteArray
                            try {
                                encryptedBuf = aesCryptor!!.encrypt(buf)
                                logger.log(VERBOSE,
                                        "encrypted authentication buffer: " + Logger.toHexString(encryptedBuf))

                                state = State.AUTHENTICATION_2
                                writeCharacteristic(INHALER_SERVICE_UUID, AUTHENTICATION_KEY_UUID, encryptedBuf)
                            } catch (e: GeneralSecurityException) {
                                onError("Exception during authentication handshake")
                            }

                        }
                    }

                // waiting for the 2nd authentication response.
                    MedicationDispenserImpl.State.AUTHENTICATION_2 -> {
                        if (serviceUuid != INHALER_SERVICE_UUID
                                || characteristicUuid != AUTHENTICATION_KEY_UUID
                                || action != Action.WRITE && action != Action.CHANGED) {
                            onError("Wrong service, characteristic, or action")
                        } else if (action == Action.CHANGED) {
                            // advance to the next state on the CHANGED action

                            if (value == null || value.size != AUTHENTICATION_LENGTH) {
                                onError("Incorrect value length")
                            } else {
                                // verify the response matches expectations.
                                val expectedBytes = ByteBuffer.allocate(AUTHENTICATION_LENGTH)
                                expectedBytes.put(authenticationRand2)
                                expectedBytes.put(authenticationRand1)

                                try {
                                    val decryptedValue = aesCryptor!!.decrypt(value)

                                    if (!Arrays.equals(expectedBytes.array(), decryptedValue)) {
                                        //Todo - check how authentication timeout can be detected.
                                        dependencyProvider.resolve<Messenger>().post(SystemMonitorMessage(DeviceActivity.AuthenticationTimeout()))
                                        onError("processAuthenticationState() invalid state")
                                    } else {
                                        val authenticationDuration = Duration.between(authenticationStartTime, timeService.now())
                                        dependencyProvider.resolve<Messenger>().post(SystemMonitorMessage(DeviceActivity.Authentication(authenticationDuration)))
                                        // mark the device as connected after the authentication succeeds.
                                        mainHandler.post {
                                            medicationDispenserCallback?.onConnected(connectionInfo)
                                        }
                                    }
                                } catch (e: GeneralSecurityException) {
                                    onError("Exception during authentication handshake")
                                    return
                                }

                                // If this is the first sync, then read the device information.
                                if (connectionInfo.lastRecordId == 0) {
                                    state = State.READING_MANUFACTURER_NAME
                                    readCharacteristic(DEVICE_INFORMATION_SERVICE_UUID, MANUFACTURER_NAME_UUID)
                                } else {
                                    state = State.READING_DEVICE_STATE
                                    readCharacteristic(INHALER_SERVICE_UUID, DEVICE_STATE_UUID)
                                }
                            }
                        }
                    }

                // Received the manufacturer name
                    MedicationDispenserImpl.State.READING_MANUFACTURER_NAME -> {
                        if (serviceUuid != DEVICE_INFORMATION_SERVICE_UUID
                                || characteristicUuid != MANUFACTURER_NAME_UUID
                                || action != Action.READ) {
                            onError("Wrong service, characteristic, or action")
                        } else {
                            deviceInfo.manufacturerName = String(value!!, ASCII_CHARSET)

                            // read the next device information field
                            state = State.READING_HARDWARE_REVISION
                            readCharacteristic(DEVICE_INFORMATION_SERVICE_UUID, HARDWARE_REVISION_UUID)
                        }
                    }

                // Received the hardware revision
                    MedicationDispenserImpl.State.READING_HARDWARE_REVISION -> {
                        if (serviceUuid != DEVICE_INFORMATION_SERVICE_UUID
                                || characteristicUuid != HARDWARE_REVISION_UUID
                                || action != Action.READ) {
                            onError("Wrong service, characteristic, or action")
                        } else {
                            deviceInfo.hardwareRevision = String(value!!, ASCII_CHARSET)

                            // read the next device information field
                            state = State.READING_SOFTWARE_REVISION
                            readCharacteristic(DEVICE_INFORMATION_SERVICE_UUID, SOFTWARE_REVISION_UUID)
                        }
                    }

                // Received the software revision
                    MedicationDispenserImpl.State.READING_SOFTWARE_REVISION -> {
                        if (serviceUuid != DEVICE_INFORMATION_SERVICE_UUID
                                || characteristicUuid != SOFTWARE_REVISION_UUID
                                || action != Action.READ) {
                            onError("Wrong service, characteristic, or action")
                        } else {
                            deviceInfo.softwareRevision = String(value!!, ASCII_CHARSET)

                            // read the device state
                            state = State.READING_DEVICE_STATE
                            readCharacteristic(INHALER_SERVICE_UUID, DEVICE_STATE_UUID)
                        }
                    }

                // Received the device state
                    MedicationDispenserImpl.State.READING_DEVICE_STATE -> {
                        if (serviceUuid != INHALER_SERVICE_UUID
                                || characteristicUuid != DEVICE_STATE_UUID
                                || action != Action.READ) {
                            onError("Wrong service, characteristic, or action")
                        } else if (parseDeviceInfo(value)) {
                            state = State.SET_NOTIFY_DEVICE_STATE
                            setNotifyCharacteristic(INHALER_SERVICE_UUID, DEVICE_STATE_UUID, false)
                        }
                    }

                // Completed setting up notification for Device State characteristic.
                    MedicationDispenserImpl.State.SET_NOTIFY_DEVICE_STATE -> {
                        if (serviceUuid != INHALER_SERVICE_UUID
                                || characteristicUuid != DEVICE_STATE_UUID
                                || action != Action.WRITE_DESCRIPTOR) {
                            onError("Wrong service, characteristic, or action")
                        } else {
                            state = State.SET_NOTIFY_INHALE_EVENT_STATE
                            setNotifyCharacteristic(INHALER_SERVICE_UUID, INHALE_EVENT_UUID, false)
                        }
                    }

                // Completed setting up notification for Inhale Event characteristic.
                    MedicationDispenserImpl.State.SET_NOTIFY_INHALE_EVENT_STATE -> {
                        if (serviceUuid != INHALER_SERVICE_UUID
                                || characteristicUuid != INHALE_EVENT_UUID
                                || action != Action.WRITE_DESCRIPTOR) {
                            onError("Wrong service, characteristic, or action")
                        } else {
                            state = State.SET_NOTIFY_RACP_STATE
                            setNotifyCharacteristic(INHALER_SERVICE_UUID, RACP_UUID, true)
                        }
                    }

                // Completed setting up notification for RACP.
                    MedicationDispenserImpl.State.SET_NOTIFY_RACP_STATE -> {
                        if (serviceUuid != INHALER_SERVICE_UUID
                                || characteristicUuid != RACP_UUID
                                || action != Action.WRITE_DESCRIPTOR) {
                            onError("Wrong service, characteristic, or action")
                        } else {
                            if (hasSimulatorService) {
                                state = State.SET_NOTIFY_SIMULATOR_STATE
                                setNotifyCharacteristic(SIMULATOR_SERVICE_UUID, SIMULATOR_TIME_UUID, false)
                            } else {
                                state = State.IDLE
                            }
                        }
                    }

                    MedicationDispenserImpl.State.SET_NOTIFY_SIMULATOR_STATE -> {
                        if (serviceUuid != SIMULATOR_SERVICE_UUID
                                || characteristicUuid != SIMULATOR_TIME_UUID
                                || action != Action.WRITE_DESCRIPTOR) {
                            onError("Wrong service, characteristic, or action")
                        } else {
                            state = State.IDLE
                        }
                    }

                // Complete writing the simulator time
                    MedicationDispenserImpl.State.TIME_SYNC -> {
                        if (serviceUuid != SIMULATOR_SERVICE_UUID
                                || characteristicUuid != SIMULATOR_TIME_UUID
                                || action != Action.WRITE) {
                            onError("Wrong service, characteristic, or action")
                        } else {
                            state = State.IDLE
                        }
                    }

                // Completed writing the nonce
                    MedicationDispenserImpl.State.WRITE_NONCE -> {
                        if (serviceUuid != INHALER_SERVICE_UUID
                                || characteristicUuid != AUTHENTICATION_KEY_UUID
                                || action != Action.WRITE) {
                            onError("Wrong service, characteristic, or action")
                        } else {
                            // The inhaler requires a delay between writing the nonce and
                            // writing the RACP command.
                            state = State.DELAY_RACP_START
                            mainHandler.sendEmptyMessageDelayed(DELAY_ACTION_MESSAGE, RACP_DELAY.toLong())
                        }
                    }

                // Delay between writing the nonce and writing the start command has
                // completed.
                    MedicationDispenserImpl.State.DELAY_RACP_START -> {
                        if (action != Action.DELAY) {
                            onError("Wrong action")
                        } else {
                            state = State.STARTING_RACP

                            val commandBuffer = ByteArray(RACP_COMMAND_LENGTH)
                            val command = ByteBuffer.wrap(commandBuffer)
                            command.order(ByteOrder.LITTLE_ENDIAN)

                            // Build an RACP command buffer to retrieve all of the
                            // records with an index greater than or equal to
                            // the last record id seen by the Asthma App.
                            command.put(RACP_READ_OPCODE.toByte())
                            command.put(RACP_GT_EQ_OPERATOR.toByte())
                            command.put(RACP_FILTER_BY_INDEX.toByte())
                            command.putShort(deviceInfo.lastRecordId.toShort())

                            writeCharacteristic(INHALER_SERVICE_UUID, RACP_UUID, commandBuffer)
                        }
                    }

                // RACP command write complete.
                    MedicationDispenserImpl.State.STARTING_RACP -> {
                        if (serviceUuid != INHALER_SERVICE_UUID
                                || characteristicUuid != RACP_UUID
                                || action != Action.WRITE) {
                            onError("Wrong service, characteristic, or action")
                        } else {
                            state = State.WAITING_FOR_RACP
                        }
                    }

                // Completed writing the start RACP command
                    MedicationDispenserImpl.State.WAITING_FOR_RACP -> {
                        if (serviceUuid != INHALER_SERVICE_UUID
                                || characteristicUuid != RACP_UUID && characteristicUuid != INHALE_EVENT_UUID
                                || action != Action.CHANGED) {
                            onError("Wrong service, characteristic, or action")
                        } else {
                            if (characteristicUuid == INHALE_EVENT_UUID) {
                                if (value == null || !parseInhaleEvent(value)) {
                                    onError("Error parsing inhale event")
                                }
                            } else if (characteristicUuid == RACP_UUID) {
                                if (value == null || parseRacp(value)) {
                                    state = State.IDLE
                                } else {
                                    onError("Error parsing RACP response")
                                }
                            }
                        }
                    }

                    else -> {
                        logger.log(WARN, "Unexpected state $state")
                    }
                }
            }

            // If we're in the idle state, check to see if we need to retrieve inhale events
            // or sync the time with the inhaler simulator.
            if (state == State.IDLE) {
                if (racpPending) {
                    logger.log(VERBOSE, "starting RACP")
                    // start the RACP process by writing a new nonce
                    racpPending = false
                    nonce = createRandomBuffer(NONCE_LENGTH)
                    state = State.WRITE_NONCE
                    writeCharacteristic(INHALER_SERVICE_UUID, AUTHENTICATION_KEY_UUID, nonce!!)
                } else if (simulatorTimePending) {
                    logger.log(VERBOSE, "syncing simulator time")
                    val buf = createSimulatorTimeBuffer()
                    simulatorTimePending = false
                    state = State.TIME_SYNC
                    writeCharacteristic(SIMULATOR_SERVICE_UUID, SIMULATOR_TIME_UUID, buf)
                }
            }
        } catch (ex: Exception) {
            logger.logException(ERROR, "Exception occurred during onProcessState()", ex)
            onError("Exception occurred during onProcessState()")
        }

    }

    /**
     * Parses the InhaleEvent characteristic value.
     *
     * @param value The byte array received from the Bluetooth device.
     * @return True if the value is valid, false otherwise
     */
    private fun parseInhaleEvent(value: ByteArray): Boolean {
        val decryptedValue: ByteArray
        try {
            decryptedValue = aesCryptor!!.decryptWithKeyStream(value, nonce!!)
        } catch (e: GeneralSecurityException) {
            logger.logException(ERROR, "failed to decrypt inhale event", e)

            return false
        }

        logger.log(VERBOSE, "parseInhaleEvent: " + Logger.toHexString(decryptedValue))

        if (Crc16Ccitt.compute(decryptedValue, decryptedValue.size) != 0) {
            logger.log(ERROR, "Inhale event failed CRC check.")
            return false
        }

        val buffer = ByteBuffer.wrap(decryptedValue)
        buffer.order(ByteOrder.LITTLE_ENDIAN)
        val info = InhaleEventInfo()

        info.eventUID = buffer.ushort
        info.eventTime = timeService.getTimeFromRealTimeInterval(-buffer.int)
        info.inhaleStartOffset = buffer.ushort * TENTHS_OF_SECOND_TO_MILLISECOND_MULTIPLIER

        val zonedTime = ZonedDateTime.ofInstant(info.eventTime!!, ZoneId.systemDefault())
        val zoneOffset = ZoneOffset.from(zonedTime)
        info.timezoneOffsetMinutes = zoneOffset.totalSeconds / SECONDS_PER_MINUTES

        info.inhaleDuration = buffer.ushort
        info.inhalePeak = buffer.ushort
        info.inhalePeakOffset = buffer.ushort
        info.inhaleVolume = buffer.ushort
        info.status = buffer.ubyte
        info.isValidInhale = info.status and INVALID_EVENT_STATUS_MASK == 0

        inhaleEventList.add(info)

        if (info.eventUID > deviceInfo.lastRecordId) {
            deviceInfo.lastRecordId = info.eventUID
        }

        logger.log(DEBUG, "Received InhaleEvent: " + info.eventUID)

        return true
    }

    /**
     * Parses the RACP characteristic value.
     *
     * @param value The byte array received from the Bluetooth device.
     * @return True if the value is valid, false otherwise
     */
    private fun parseRacp(value: ByteArray): Boolean {
        var result = false
        logger.log(VERBOSE, "parseRacp: " + Logger.toHexString(value))

        if (value.size == 4) {
            val buffer = ByteBuffer.wrap(value)
            buffer.order(ByteOrder.LITTLE_ENDIAN)
            val opCode = buffer.ubyte
            val operand = buffer.ubyte
            val requestOpCode = buffer.ubyte
            val responseCode = buffer.ubyte

            if (opCode != RACP_RESPONSE_CODE || operand != RACP_NULL || requestOpCode != RACP_SUCCESS) {
                logger.log(ERROR, "Unexpected RACP message: " + Logger.toHexString(value))
            } else if (responseCode != RACP_SUCCESS && responseCode != RACP_NO_RECORDS_FOUND) {
                logger.log(ERROR, "Received RACP error response: " + Logger.toHexString(value))
            } else {
                result = true
            }
        } else {
            logger.log(ERROR, "Unexpected RACP length: " + Logger.toHexString(value))
        }

        reportUpdated()

        return result
    }

    /**
     * Reports changes to the device and inhale events ot the client.
     */
    private fun reportUpdated() {
        logger.log(INFO, "Updated state and inhale events for " + connectionInfo.serialNumber)

        val deviceInfoCopy = deviceInfo.copy()
        val inhaleEventListCopy = ArrayList(inhaleEventList)
        inhaleEventList.clear()

        mainHandler.post {
            medicationDispenserCallback?.onUpdated(connectionInfo, deviceInfoCopy, inhaleEventListCopy)
        }
    }

    /**
     * Callback for the MessageHandler used to receive the delay messages.
     *
     * @param message The message id.
     */
    override fun onMessage(message: Int) {
        if (message == DELAY_ACTION_MESSAGE) {
            processState(Action.DELAY, null, null, null)
        }
    }

    /**
     * Creates a byte array containing the current TimeService settings.
     *
     * @return A byte array containing the current TimeService settings.
     */
    private fun createSimulatorTimeBuffer(): ByteArray {
        // Not using the TimeService here on purpose
        // The difference between the real now and the reference time is
        // what is sent to the simulator.
        val referenceTimeMillisecondsDelta = Instant.now().toEpochMilli() - timeService.referenceTime!!.toEpochMilli()
        val referenceHypertimeMilliseconds = timeService.referenceHypertime!!.toEpochMilli()

        val buf = ByteArray(SIMULATOR_TIME_BUFFER_LENGTH)
        val buffer = ByteBuffer.wrap(buf)
        buffer.order(ByteOrder.LITTLE_ENDIAN)
        buffer.putLong(referenceTimeMillisecondsDelta)
        buffer.putLong(referenceHypertimeMilliseconds)
        buffer.put(timeService.timeMode!!.rawValue.toByte())

        return buf
    }

    /**
     * Parses the DeviceState characteristic value.
     *
     * @param value The byte array received from the Bluetooth device.
     * @return True if the value is valid, false otherwise
     */
    private fun parseDeviceInfo(value: ByteArray?): Boolean {
        if (value == null || value.size != DEVICE_INFO_LENGTH) {
            onError("Incorrect DeviceInfo length")
            return false
        }

        val buffer = ByteBuffer.wrap(value)
        buffer.order(ByteOrder.LITTLE_ENDIAN)
        buffer.position(DOSES_TAKEN_POSITION)
        val newDosesTaken = buffer.ushort

        logger.log(DEBUG, "parseDeviceInfo(): newDosesTaken=" + newDosesTaken)

        if (deviceInfo.dosesTaken != newDosesTaken) {
            deviceInfo.dosesTaken = newDosesTaken
            racpPending = true
        }

        return true
    }

    /**
     * Callback class to receive events from the Bluetooth device.
     */
    @BinderThread
    private inner class GattCallback : BluetoothGattCallback() {
        /**
         * Callback from the BluetoothGatt object that occurs when a device connects or disconnects.
         */
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)

            logger.log(INFO, "$debugId onConnectionStateChange: ${bluetoothDevice.address} ${connectionInfo.serialNumber} ${status} ${newState}")

            bluetoothGatt = gatt
            dependencyProvider.register(bluetoothGatt!!)

            val duration = Duration.between(connectionTime, Instant.now())
            logger.log(DEBUG, "$debugId connection duration: $duration")

            mainHandler.post {
                var reconnecting = false

                if (state == State.CONNECTING && status == GATT_ERROR) {
                    // The connection likely timed out, so we will retry until we've exceed a maximum time.
                    if (duration < MAXIMUM_CONNECTION_DURATION) {
                        // try again
                        reconnecting = true

                        bluetoothGatt?.close()
                        bluetoothDevice.connectGatt(dependencyProvider.resolve(Context::class.java), false, gattCallback)

                    } else {
                        bluetoothGatt?.close()
                    }
                } else {
                    if (newState == BluetoothGatt.STATE_CONNECTED) {
                        if (!isDisconnecting) {
                            processState(Action.CONNECTED, null, null, null)
                        } else {
                            // requested to disconnect before completing connection so now that we are connected,
                            // we should disconnect
                            bluetoothGatt!!.disconnect()
                        }
                    } else {
                        bluetoothGatt!!.close()
                    }
                }

                // delay disconnect notification
                if (newState == BluetoothGatt.STATE_DISCONNECTED && !reconnecting) {
                    mainHandler.postDelayed({
                        medicationDispenserCallback?.onDisconnected(connectionInfo)
                    }, DISCONNECT_NOTIFICATION_DELAY.toLong())
                }
            }

        }

        /**
         * Callback from the BluetoothGatt object that occurs when a service discovery completes.
         */
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            super.onServicesDiscovered(gatt, status)
            logger.log(VERBOSE, debugId.toString() + " onServicesDiscovered")

            if (logger.isEnabled(DEBUG)) {
                for (service in gatt.services) {
                    logger.log(DEBUG, "$debugId Service ${service.uuid}")
                    for (characteristic in service.characteristics) {
                        logger.log(DEBUG, "    ${characteristic.uuid}")
                    }
                }
            }

            mainHandler.post {
                if (state == MedicationDispenserImpl.State.DISCOVER_SERVICES) {
                    processState(Action.SERVICES_DISCOVERED, null, null, null)
                }
            }
        }

        /**
         * Callback from the BluetoothGatt object that occurs when a characteristic read completes.
         */
        override fun onCharacteristicRead(gatt: BluetoothGatt,
                                          characteristic: BluetoothGattCharacteristic,
                                          status: Int) {
            super.onCharacteristicRead(gatt, characteristic, status)
            logger.log(VERBOSE, "$debugId onCharacteristicRead status = $status")

            val serviceUuid = characteristic.service.uuid
            val characteristicUuid = characteristic.uuid
            val value = characteristic.value

            mainHandler.post { processState(Action.READ, serviceUuid, characteristicUuid, value) }
        }

        /**
         * Callback from the BluetoothGatt object that occurs when a characteristic changed
         * notification occurs.
         */
        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            super.onCharacteristicChanged(gatt, characteristic)
            logger.log(VERBOSE, debugId.toString() + " onCharacteristicChanged")

            val serviceUuid = characteristic.service.uuid
            val characteristicUuid = characteristic.uuid
            val value = characteristic.value
            val queueChange = writePending

            mainHandler.post {
                // Notified characteristic changes that occur due to a write
                // will come before the onCharacteristicWrite() callback.
                // So we queue up the deferred change notifications and
                // send them once the write completed notification is sent.
                if (queueChange) {
                    logger.log(VERBOSE, "queueing change notification")
                    val pendingChange = PendingChange()
                    pendingChange.serviceUuid = serviceUuid
                    pendingChange.characteristicUuid = characteristicUuid
                    pendingChange.value = value
                    pendingChanges.add(pendingChange)
                } else {
                    processState(Action.CHANGED, serviceUuid, characteristicUuid, value)
                }
            }
        }

        /**
         * Callback from the BluetoothGatt object that occurs when a descriptor write completes.
         */
        override fun onDescriptorWrite(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int) {
            super.onDescriptorWrite(gatt, descriptor, status)
            logger.log(VERBOSE, debugId.toString() + " onDescriptorWrite " + status)

            val serviceUuid = descriptor.characteristic.service.uuid
            val characteristicUuid = descriptor.characteristic.uuid

            mainHandler.post { processState(Action.WRITE_DESCRIPTOR, serviceUuid, characteristicUuid, null) }
        }

        /**
         * Callback from the BluetoothGatt object that occurs when a characteristic write completes.
         */
        override fun onCharacteristicWrite(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            logger.log(VERBOSE, debugId.toString() + " onCharacteristicWrite " + status)

            writePending = false

            val serviceUuid = characteristic.service.uuid
            val characteristicUuid = characteristic.uuid

            mainHandler.post {
                processState(Action.WRITE, serviceUuid, characteristicUuid, null)

                // Notified characteristic changes that occur due to a write
                // will come before the onCharacteristicWrite() callback.
                // So we queue up the deferred change notifications and
                // send them once the write completed notification is sent.
                if (pendingChanges.size > 0) {
                    for (change in pendingChanges) {
                        processState(Action.CHANGED, change.serviceUuid, change.characteristicUuid, change.value)
                    }

                    pendingChanges.clear()
                }
            }
        }
    }

    companion object {

        private val logger = Logger(MedicationDispenserImpl::class)

        private val MAXIMUM_CONNECTION_DURATION = Duration.ofSeconds(120)

        private val ASCII_CHARSET = Charset.forName("US-ASCII")

        private val INHALER_SERVICE_UUID = UUID.fromString("f429de80-c342-11e4-9da5-0002a5d5c51b")
        private val INHALE_EVENT_UUID = UUID.fromString("f429de81-c342-11e4-9da5-0002a5d5c51b")
        private val DEVICE_STATE_UUID = UUID.fromString("f429de83-c342-11e4-9da5-0002a5d5c51b")
        private val RACP_UUID = UUID.fromString("00002a52-0000-1000-8000-00805f9b34fb")
        private val AUTHENTICATION_KEY_UUID = UUID.fromString("f429de86-c342-11e4-9da5-0002a5d5c51b")

        private val SIMULATOR_SERVICE_UUID = UUID.fromString("BAAFEA22-4135-43ED-94BC-B81FC0399369")
        private val SIMULATOR_TIME_UUID = UUID.fromString("DE1E5C4F-0E96-4B38-B746-C54131EFFC1E")

        private val DEVICE_INFORMATION_SERVICE_UUID = UUID.fromString("0000180a-0000-1000-8000-00805f9b34fb")
        private val MANUFACTURER_NAME_UUID = UUID.fromString("00002a29-0000-1000-8000-00805f9b34fb")
        private val SOFTWARE_REVISION_UUID = UUID.fromString("00002a28-0000-1000-8000-00805f9b34fb")
        private val HARDWARE_REVISION_UUID = UUID.fromString("00002a27-0000-1000-8000-00805f9b34fb")
        private val SERIAL_NUMBER_UUID = UUID.fromString("00002a25-0000-1000-8000-00805f9b34fb")

        private val NOTIFICATION_DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
        private val DELAY_ACTION_MESSAGE = 1
        private val DISCONNECT_NOTIFICATION_DELAY = 2500

        private val INVALID_EVENT_STATUS_MASK = 0x7A

        private val RACP_DELAY = 500

        private val DEVICE_INFO_LENGTH = 6

        private val AUTHENTICATION_LENGTH = 16
        private val HALF_AUTHENTICATION_LENGTH = 8

        private val NONCE_LENGTH = 16
        private val DOSES_TAKEN_POSITION = 3
        private val RACP_READ_OPCODE = 1
        private val RACP_GT_EQ_OPERATOR = 3
        private val RACP_FILTER_BY_INDEX = 1
        private val RACP_COMMAND_LENGTH = 5
        private val TENTHS_OF_SECOND_TO_MILLISECOND_MULTIPLIER = 100
        private val RACP_RESPONSE_CODE = 6
        private val RACP_SUCCESS = 1
        private val RACP_NULL = 0
        private val RACP_NO_RECORDS_FOUND = 6
        private val SIMULATOR_TIME_BUFFER_LENGTH = 17
        private val SECONDS_PER_MINUTES = 60

        private val GATT_ERROR = 133

        private var nextDebugId = 0
    }
}
