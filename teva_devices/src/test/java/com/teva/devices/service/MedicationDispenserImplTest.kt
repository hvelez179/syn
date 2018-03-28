//
// MedicationDispenserImplTest.java
// teva_devices
//
// Copyright (c) 2017 Teva. All rights reserved.
//

package com.teva.devices.service

import android.bluetooth.*
import android.content.Context
import com.nhaarman.mockito_kotlin.*
import com.teva.common.services.RunModes
import com.teva.common.services.TimeService
import com.teva.utilities.services.DependencyProvider
import com.teva.common.utilities.Messenger
import com.teva.devices.mocks.HandlerHelper
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.MockitoAnnotations
import org.mockito.stubbing.Answer
import org.threeten.bp.Instant
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.Charset
import java.util.*

class MedicationDispenserImplTest {

    private var timeService: TimeService = mock()
    private var context: Context = mock()
    private var bluetoothDevice: BluetoothDevice = mock()
    private var bluetoothGatt: BluetoothGatt = mock()
    private var bluetoothGattCallback: BluetoothGattCallback = mock()
    private var callback: MedicationDispenserCallback = mock()

    private var inhalerService: BluetoothGattService = mock()
    private var inhaleEventCharacteristic: BluetoothGattCharacteristic = mock()
    private var deviceStateCharacteristic: BluetoothGattCharacteristic = mock()
    private var racpCharacteristic: BluetoothGattCharacteristic = mock()
    private var authenticationKeyCharacteristic: BluetoothGattCharacteristic = mock()

    private var deviceInfoService: BluetoothGattService = mock()
    private var manufacturerNameCharacteristic: BluetoothGattCharacteristic = mock()
    private var hardwareRevisionCharacteristic: BluetoothGattCharacteristic = mock()
    private var softwareRevisiionCharacteristic: BluetoothGattCharacteristic = mock()
    private var serialNumberCharacteristic: BluetoothGattCharacteristic = mock()

    private var authenticationKeyDescriptor: BluetoothGattDescriptor = mock()
    private var deviceStateDescriptor: BluetoothGattDescriptor = mock()
    private var inhaleEventDescriptor: BluetoothGattDescriptor = mock()
    private var racpDescriptor: BluetoothGattDescriptor = mock()

    private var connectionInfo = ConnectionInfo(SERIAL_NUMBER, AUTHENTICATION_KEY, ProtocolType.Inhaler, 0)
    private var dependencyProvider: DependencyProvider = DependencyProvider.default
    private val messenger: Messenger = mock()

    /**
     * Setups up the mocks before every test.
     */
    @Before
    @Throws(IllegalAccessException::class)
    fun setup() {
        DependencyProvider.default.unregisterAll()
        HandlerHelper.clearQueue()

        MockitoAnnotations.initMocks(this)

        createMocks()
        createGattMocks()

        whenever(bluetoothGatt.setCharacteristicNotification(any(), any())).thenReturn(true)

        dependencyProvider.register(Context::class, context)

        dependencyProvider.register(TimeService::class, timeService)
        dependencyProvider.register(Messenger::class, messenger)
    }

    /**
     * Creates mocks for the common objects used by the tests
     */
    private fun createMocks() {
        whenever(timeService.timeMode).thenReturn(RunModes.REALTIME)
        whenever(timeService.referenceHypertime).thenReturn(CurrentTime)
        whenever(timeService.referenceTime).thenReturn(CurrentTime)
        whenever(timeService.getTimeFromRealTimeInterval(any())).thenAnswer { invocation -> CurrentTime.plusSeconds((invocation.arguments[0] as Int).toLong()) }

        whenever(bluetoothDevice.address).thenReturn("12:34:56:78:90:12")

        createGattMocks()
    }

    /**
     * Creates a mock of a BluetoothGattCharacteristic.
     *
     * @param uuid The UUID of the characteristic.
     * @return A mocked BluetoothGattCharacteristic.
     */
    private fun mockGattCharacteristic(uuid: UUID): BluetoothGattCharacteristic {
        val characteristic: BluetoothGattCharacteristic = mock()
        whenever(characteristic.uuid).thenReturn(uuid)

        return characteristic
    }

    /**
     * Creates a mock of a BluetoothGattService.
     *
     * @param uuid The UUID of the service
     * @param characteristics The characteristics owned by the service.
     * @return A mocked BluetoothGattService.
     */
    private fun mockGattService(uuid: UUID,
                                vararg characteristics: BluetoothGattCharacteristic): BluetoothGattService {
        val service: BluetoothGattService = mock()

        whenever(service.uuid).thenReturn(uuid)
        whenever(service.characteristics).thenAnswer { Arrays.asList(*characteristics) }

        whenever(service.getCharacteristic(any())).thenAnswer(Answer<BluetoothGattCharacteristic> { invocation ->
            for (characteristic in characteristics) {
                val uuid = invocation.arguments[0] as UUID
                if (characteristic.uuid == uuid) {
                    return@Answer characteristic
                }
            }
            return@Answer null
        })

        for (characteristic in characteristics) {
            whenever(characteristic.service).thenReturn(service)
        }

        return service
    }

    /**
     * This method sets up mocks for the services and characteristics
     * returned by the BluetoothGatt.
     */
    private fun createGattMocks() {
        manufacturerNameCharacteristic = mockGattCharacteristic(MANUFACTURER_NAME_UUID)
        hardwareRevisionCharacteristic = mockGattCharacteristic(HARDWARE_REVISION_UUID)
        softwareRevisiionCharacteristic = mockGattCharacteristic(SOFTWARE_REVISION_UUID)
        serialNumberCharacteristic = mockGattCharacteristic(SERIAL_NUMBER_UUID)
        deviceInfoService = mockGattService(DEVICE_INFORMATION_SERVICE_UUID,
                manufacturerNameCharacteristic,
                hardwareRevisionCharacteristic,
                softwareRevisiionCharacteristic,
                serialNumberCharacteristic)

        inhaleEventCharacteristic = mockGattCharacteristic(INHALE_EVENT_UUID)
        deviceStateCharacteristic = mockGattCharacteristic(DEVICE_STATE_UUID)
        racpCharacteristic = mockGattCharacteristic(RACP_UUID)
        authenticationKeyCharacteristic = mockGattCharacteristic(AUTHENTICATION_KEY_UUID)
        inhalerService = mockGattService(INHALER_SERVICE_UUID,
                inhaleEventCharacteristic,
                deviceStateCharacteristic,
                racpCharacteristic,
                authenticationKeyCharacteristic)


        whenever(serialNumberCharacteristic.value)
                .thenReturn(SERIAL_NUMBER.toByteArray(ASCII_CHARSET))

        whenever(authenticationKeyCharacteristic.getDescriptor(NOTIFICATION_DESCRIPTOR_UUID))
                .thenReturn(authenticationKeyDescriptor)
        whenever(authenticationKeyDescriptor.characteristic)
                .thenReturn(authenticationKeyCharacteristic)

        whenever(deviceStateCharacteristic.getDescriptor(NOTIFICATION_DESCRIPTOR_UUID))
                .thenReturn(deviceStateDescriptor)
        whenever(deviceStateDescriptor.characteristic).thenReturn(deviceStateCharacteristic)

        whenever(inhaleEventCharacteristic.getDescriptor(NOTIFICATION_DESCRIPTOR_UUID))
                .thenReturn(inhaleEventDescriptor)
        whenever(inhaleEventDescriptor.characteristic).thenReturn(inhaleEventCharacteristic)

        whenever(racpCharacteristic.getDescriptor(NOTIFICATION_DESCRIPTOR_UUID))
                .thenReturn(racpDescriptor)
        whenever(racpDescriptor.characteristic).thenReturn(racpCharacteristic)

        whenever(bluetoothGatt.services).thenAnswer {
            val list = ArrayList<BluetoothGattService>()
            list.add(deviceInfoService)
            list.add(inhalerService)
            list
        }

        whenever(bluetoothGatt.getService(INHALER_SERVICE_UUID)).thenReturn(inhalerService)
        whenever(bluetoothGatt.getService(DEVICE_INFORMATION_SERVICE_UUID))
                .thenReturn(deviceInfoService)
    }

    /**
     * Test that the MedicationDispenser calls the correct BluetoothDevice and BluetoothGatt methods
     * to perform the initial connection steps up through verifying the serial number.
     */
    @Test
    @Throws(Exception::class)
    fun testConnectToInhalerAndVerifySerialNumber() {
        // setup test

        whenever(bluetoothDevice.connectGatt(any(), eq(false), any())).thenAnswer { invocation ->
            bluetoothGattCallback = invocation.arguments[2] as BluetoothGattCallback
            bluetoothGattCallback.onConnectionStateChange(bluetoothGatt,
                    BluetoothGatt.GATT_SUCCESS, BluetoothProfile.STATE_CONNECTED)
            bluetoothGatt
        }

        whenever(bluetoothGatt.discoverServices()).thenAnswer {
            bluetoothGattCallback.onServicesDiscovered(bluetoothGatt,
                    BluetoothGatt.GATT_SUCCESS)
            true
        }

        whenever(bluetoothGatt.readCharacteristic(serialNumberCharacteristic))
                .thenAnswer {
                    bluetoothGattCallback.onCharacteristicRead(bluetoothGatt,
                            serialNumberCharacteristic, BluetoothGatt.GATT_SUCCESS)
                    true
                }

        whenever(bluetoothGatt.writeDescriptor(any())).thenReturn(true)

        // execute test
        val medicationDispenser = MedicationDispenserImpl(dependencyProvider, connectionInfo, bluetoothDevice)

        medicationDispenser.connect()

        // run the test handler's looper.
        HandlerHelper.loopHandler()

        // verify expectations

        // verify we are in the expected state
        assertEquals(MedicationDispenserImpl.State.SET_NOTIFY_AUTHENTICATION,
                medicationDispenser.state)

        // verify that disconnect was not called (indicating a failure)
        verify(bluetoothGatt, never()).disconnect()

        // verify that the expected gatt methods are called.
        verify(bluetoothDevice).connectGatt(eq(context), eq(false), any())
        verify(bluetoothGatt).discoverServices()
        verify(bluetoothGatt).readCharacteristic(serialNumberCharacteristic)
        verify(bluetoothGatt).setCharacteristicNotification(authenticationKeyCharacteristic, true)
        verify(bluetoothGatt).writeDescriptor(authenticationKeyDescriptor)
        verify(authenticationKeyDescriptor).value = BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
    }

    /**
     * Test the authentication handshake between the app and the inhaler.
     */
    @Test
    @Throws(Exception::class)
    fun testAuthenticateWithInhalerStep() {
        // setup the test
        whenever(timeService.now()).thenReturn(Instant.ofEpochMilli(1516637002212))
        whenever(bluetoothGatt.writeCharacteristic(any())).thenReturn(true)
        whenever(bluetoothGatt.readCharacteristic(any())).thenReturn(true)

        val aesCryptor = AESCryptor(AUTHENTICATION_KEY)

        val medicationDispenser = MedicationDispenserImpl(dependencyProvider, connectionInfo, bluetoothDevice)
        val authenticationStartTimeMember = medicationDispenser.javaClass.getDeclaredField("authenticationStartTime")
        authenticationStartTimeMember.isAccessible = true
        authenticationStartTimeMember.set(medicationDispenser, Instant.ofEpochMilli(1516637000212))

        medicationDispenser.bluetoothGatt = bluetoothGatt
        medicationDispenser.state = MedicationDispenserImpl.State.SET_NOTIFY_AUTHENTICATION
        bluetoothGattCallback = medicationDispenser.gattCallback

        // run the test

        // Signal the completion of setting up the authentication key notification
        bluetoothGattCallback.onDescriptorWrite(bluetoothGatt, authenticationKeyDescriptor,
                BluetoothGatt.GATT_SUCCESS)
        HandlerHelper.loopHandler()

        // verify we are in the right state
        assertEquals(MedicationDispenserImpl.State.AUTHENTICATION_1, medicationDispenser.state)

        val byteArrayCaptor = argumentCaptor<ByteArray>()

        // verify and get the 8 byte value that was written to the authentication key
        verify(bluetoothGatt, times(1)).writeCharacteristic(authenticationKeyCharacteristic)
        verify(authenticationKeyCharacteristic, times(1)).value = byteArrayCaptor.capture()
        val authenticationBuffer1 = byteArrayCaptor.lastValue
        assertEquals(8, authenticationBuffer1.size.toLong())


        // signal the reception of 8 bytes from the inhaler
        val authenticationBuffer2 = byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8)

        whenever(authenticationKeyCharacteristic.value).thenReturn(authenticationBuffer2)
        bluetoothGattCallback.onCharacteristicChanged(bluetoothGatt,
                authenticationKeyCharacteristic)

        // write ack comes after changed notification caused by write
        bluetoothGattCallback.onCharacteristicWrite(bluetoothGatt, authenticationKeyCharacteristic,
                BluetoothGatt.GATT)
        HandlerHelper.loopHandler()

        assertEquals(MedicationDispenserImpl.State.AUTHENTICATION_2, medicationDispenser.state)

        // verify the correct 16 byte buffer is sent to inhaler
        verify(bluetoothGatt, times(2)).writeCharacteristic(authenticationKeyCharacteristic)
        verify(authenticationKeyCharacteristic, times(2)).value = byteArrayCaptor.capture()
        val authenticationBuffer3 = byteArrayCaptor.lastValue
        assertEquals(16, authenticationBuffer3.size.toLong())

        var expectedBuffer = ByteArray(16)
        var byteBuffer = ByteBuffer.wrap(expectedBuffer)
        byteBuffer.put(authenticationBuffer1)
        byteBuffer.put(authenticationBuffer2)
        expectedBuffer = aesCryptor.encrypt(expectedBuffer)
        assertArrayEquals(expectedBuffer, authenticationBuffer3)

        // signal authentication response from inhaler
        var responseBuffer = ByteArray(16)
        byteBuffer = ByteBuffer.wrap(responseBuffer)
        byteBuffer.put(authenticationBuffer2)
        byteBuffer.put(authenticationBuffer1)
        responseBuffer = aesCryptor.encrypt(responseBuffer)

        whenever(authenticationKeyCharacteristic.value).thenReturn(responseBuffer)
        bluetoothGattCallback.onCharacteristicChanged(bluetoothGatt,
                authenticationKeyCharacteristic)

        // write ack comes after changed notification caused by write
        bluetoothGattCallback.onCharacteristicWrite(bluetoothGatt, authenticationKeyCharacteristic,
                BluetoothGatt.GATT)
        HandlerHelper.loopHandler()

        // verify current state and that a read of the Manufacturer Name was initiated.
        assertEquals(MedicationDispenserImpl.State.READING_MANUFACTURER_NAME,
                medicationDispenser.state)
        verify(bluetoothGatt).readCharacteristic(manufacturerNameCharacteristic)

        // reset to the last state and try again with lastRecordId > 0
        connectionInfo.lastRecordId = 1
        medicationDispenser.state = MedicationDispenserImpl.State.AUTHENTICATION_2

        whenever(authenticationKeyCharacteristic.value).thenReturn(responseBuffer)
        bluetoothGattCallback.onCharacteristicChanged(bluetoothGatt,
                authenticationKeyCharacteristic)
        HandlerHelper.loopHandler()

        // verify current state and that a read of the Device State was initiated.
        assertEquals(MedicationDispenserImpl.State.READING_DEVICE_STATE, medicationDispenser.state)
        verify(bluetoothGatt).readCharacteristic(deviceStateCharacteristic)
    }

    /**
     * Test the stage of the connection where the device information is read.
     */
    @Test
    @Throws(Exception::class)
    fun testReadDeviceInformation() {
        val expectedManufacturerName = "ManufacturerName"
        val expectedHardwareRevision = "HardwareVersion"
        val expectedSoftwareRevision = "SoftwareRevision"
        val expectedDoseCount: Short = 567

        // setup the test
        whenever(bluetoothGatt.writeCharacteristic(any()))
                .thenReturn(true)
        whenever(bluetoothGatt.readCharacteristic(any()))
                .thenReturn(true)
        whenever(bluetoothGatt.writeDescriptor(any())).thenReturn(true)

        val medicationDispenser = MedicationDispenserImpl(dependencyProvider, connectionInfo, bluetoothDevice)

        medicationDispenser.bluetoothGatt = bluetoothGatt
        medicationDispenser.state = MedicationDispenserImpl.State.READING_MANUFACTURER_NAME
        bluetoothGattCallback = medicationDispenser.gattCallback

        // Signal the completion reading the Maufacturer Name
        whenever(manufacturerNameCharacteristic.value)
                .thenReturn(expectedManufacturerName.toByteArray(ASCII_CHARSET))
        bluetoothGattCallback.onCharacteristicRead(bluetoothGatt, manufacturerNameCharacteristic,
                BluetoothGatt.GATT_SUCCESS)
        HandlerHelper.loopHandler()

        assertEquals(MedicationDispenserImpl.State.READING_HARDWARE_REVISION,
                medicationDispenser.state)
        assertEquals(expectedManufacturerName, medicationDispenser.deviceInfo!!.manufacturerName)
        verify(bluetoothGatt).readCharacteristic(hardwareRevisionCharacteristic)

        // Signal the completion reading the Hardware Revision
        whenever(hardwareRevisionCharacteristic.value)
                .thenReturn(expectedHardwareRevision.toByteArray(ASCII_CHARSET))
        bluetoothGattCallback.onCharacteristicRead(bluetoothGatt, hardwareRevisionCharacteristic,
                BluetoothGatt.GATT_SUCCESS)
        HandlerHelper.loopHandler()

        assertEquals(MedicationDispenserImpl.State.READING_SOFTWARE_REVISION,
                medicationDispenser.state)
        assertEquals(expectedHardwareRevision, medicationDispenser.deviceInfo!!.hardwareRevision)
        verify(bluetoothGatt).readCharacteristic(softwareRevisiionCharacteristic)

        // Signal the completion reading the Software Revision
        whenever(softwareRevisiionCharacteristic.value)
                .thenReturn(expectedSoftwareRevision.toByteArray(ASCII_CHARSET))
        bluetoothGattCallback.onCharacteristicRead(bluetoothGatt, softwareRevisiionCharacteristic,
                BluetoothGatt.GATT_SUCCESS)
        HandlerHelper.loopHandler()

        assertEquals(MedicationDispenserImpl.State.READING_DEVICE_STATE, medicationDispenser.state)
        assertEquals(expectedSoftwareRevision, medicationDispenser.deviceInfo!!.softwareRevision)
        verify(bluetoothGatt).readCharacteristic(deviceStateCharacteristic)

        // Signal the completion reading the Software Revision
        val deviceStateBuffer = ByteBuffer.allocate(6)
        deviceStateBuffer.order(ByteOrder.LITTLE_ENDIAN)
        deviceStateBuffer.put(0.toByte())
        deviceStateBuffer.put(0.toByte())
        deviceStateBuffer.put(0.toByte())
        deviceStateBuffer.putShort(expectedDoseCount)
        deviceStateBuffer.put(0.toByte())

        whenever(deviceStateCharacteristic.value).thenReturn(deviceStateBuffer.array())
        bluetoothGattCallback.onCharacteristicRead(bluetoothGatt, deviceStateCharacteristic,
                BluetoothGatt.GATT_SUCCESS)
        HandlerHelper.loopHandler()

        assertEquals(MedicationDispenserImpl.State.SET_NOTIFY_DEVICE_STATE,
                medicationDispenser.state)
        assertEquals(expectedDoseCount.toLong(), medicationDispenser.deviceInfo!!.dosesTaken.toLong())
        assertTrue(medicationDispenser.racpPending)

        verify(bluetoothGatt).writeDescriptor(deviceStateDescriptor)
        verify(deviceStateDescriptor).value = BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
    }

    /**
     * Tests the stage of the connection process were the characteristics
     * are set up for notifications.
     */
    @Test
    @Throws(Exception::class)
    fun testSetupNotifications() {
        whenever(bluetoothGatt.writeDescriptor(any()))
                .thenAnswer { invocation ->
                    val descriptor = invocation.arguments[0] as BluetoothGattDescriptor
                    bluetoothGattCallback.onDescriptorWrite(bluetoothGatt, descriptor,
                            BluetoothGatt.GATT_SUCCESS)
                    true
                }

        val medicationDispenser = MedicationDispenserImpl(dependencyProvider, connectionInfo, bluetoothDevice)

        medicationDispenser.bluetoothGatt = bluetoothGatt
        medicationDispenser.state = MedicationDispenserImpl.State.SET_NOTIFY_DEVICE_STATE

        bluetoothGattCallback = medicationDispenser.gattCallback

        medicationDispenser.simulatorTimePending = false
        medicationDispenser.racpPending = false

        // Signal the completion reading the Maufacturer Name
        bluetoothGattCallback.onDescriptorWrite(bluetoothGatt, deviceStateDescriptor,
                BluetoothGatt.GATT_SUCCESS)

        HandlerHelper.loopHandler()

        assertEquals(MedicationDispenserImpl.State.IDLE, medicationDispenser.state)

        verify(bluetoothGatt).writeDescriptor(inhaleEventDescriptor)
        verify(inhaleEventDescriptor).value = BluetoothGattDescriptor.ENABLE_INDICATION_VALUE

        verify(bluetoothGatt).writeDescriptor(racpDescriptor)
        verify(racpDescriptor).value = BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
    }

    /**
     * Creates a buffer representing an inhale event.
     */
    private fun createInhaleEventBuffer(eventUID: Int,
                                        timeOffset: Int,
                                        inhaleStartOffset: Int,
                                        inhaleDuration: Int,
                                        inhalePeak: Int,
                                        inhalePeakOffset: Int,
                                        inhaleVolume: Int,
                                        status: Int): ByteArray {
        val buffer = ByteArray(19)
        val byteBuffer = ByteBuffer.wrap(buffer)
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN)

        byteBuffer.putShort(eventUID.toShort())
        byteBuffer.putInt(timeOffset)
        byteBuffer.putShort(inhaleStartOffset.toShort())
        byteBuffer.putShort(inhaleDuration.toShort())
        byteBuffer.putShort(inhalePeak.toShort())
        byteBuffer.putShort(inhalePeakOffset.toShort())
        byteBuffer.putShort(inhaleVolume.toShort())
        byteBuffer.put(status.toByte())

        byteBuffer.put(Crc16Ccitt.getBytes(buffer, 17))

        return buffer
    }

    /**
     * Verifies that an InhaleEventInfo object matches the expected data.
     */
    private fun verifyInhaleEventInfo(info: InhaleEventInfo,
                                      eventUID: Int,
                                      timeOffset: Int,
                                      inhaleStartOffset: Int,
                                      inhaleDuration: Int,
                                      inhalePeak: Int,
                                      inhalePeakOffset: Int,
                                      inhaleVolume: Int,
                                      status: Int) {
        var result = true

        result = result and (info.eventUID == eventUID)
        result = result and (info.inhaleStartOffset == inhaleStartOffset * 100)
        result = result and (info.inhaleDuration == inhaleDuration)
        result = result and (info.inhalePeak == inhalePeak)
        result = result and (info.inhalePeakOffset == inhalePeakOffset)
        result = result and (info.inhaleVolume == inhaleVolume)
        result = result and (info.status == status)

        result = result and (info.eventTime == CurrentTime.plusSeconds(timeOffset.toLong()))

        assertTrue(result)
    }

    /**
     * Tests that a DeviceState change notification initiates an RACP transfer of the
     * inhale events.  Also verifies the RACP process.
     */
    @Test
    @Throws(Exception::class)
    fun testDeviceStateNotification() {
        whenever(bluetoothGatt.writeCharacteristic(any()))
                .thenAnswer { invocation ->
                    val characteristic = invocation.arguments[0] as BluetoothGattCharacteristic
                    bluetoothGattCallback.onCharacteristicWrite(bluetoothGatt, characteristic,
                            BluetoothGatt.GATT_SUCCESS)
                    true
                }

        val medicationDispenser = MedicationDispenserImpl(dependencyProvider, connectionInfo, bluetoothDevice)

        medicationDispenser.setCallback(callback)

        connectionInfo.lastRecordId = 2
        medicationDispenser.deviceInfo!!.lastRecordId = 1
        medicationDispenser.deviceInfo!!.dosesTaken = 2

        medicationDispenser.bluetoothGatt = bluetoothGatt
        medicationDispenser.state = MedicationDispenserImpl.State.IDLE
        bluetoothGattCallback = medicationDispenser.gattCallback

        // Signal a device state change
        val deviceStateBuffer = ByteBuffer.allocate(6)
        deviceStateBuffer.order(ByteOrder.LITTLE_ENDIAN)
        deviceStateBuffer.put(0.toByte())
        deviceStateBuffer.put(0.toByte())
        deviceStateBuffer.put(0.toByte())
        deviceStateBuffer.putShort(5.toShort())
        deviceStateBuffer.put(0.toByte())

        whenever(deviceStateCharacteristic.value).thenReturn(deviceStateBuffer.array())
        bluetoothGattCallback.onCharacteristicChanged(bluetoothGatt, deviceStateCharacteristic)

        HandlerHelper.loopHandler()

        val byteArrayCaptor = argumentCaptor<ByteArray>()

        verify(bluetoothGatt).writeCharacteristic(authenticationKeyCharacteristic)
        verify(authenticationKeyCharacteristic).value = byteArrayCaptor.capture()
        var nonce = byteArrayCaptor.lastValue
        nonce = Arrays.copyOf(nonce, nonce.size)

        assertEquals(16, nonce.size.toLong())

        HandlerHelper.loopHandler()

        verify(bluetoothGatt).writeCharacteristic(racpCharacteristic)
        verify(racpCharacteristic).value = byteArrayCaptor.capture()
        val command = byteArrayCaptor.lastValue

        val expectedCommand = ByteBuffer.allocate(5)
        expectedCommand.order(ByteOrder.LITTLE_ENDIAN)
        expectedCommand.put(1.toByte())
        expectedCommand.put(3.toByte())
        expectedCommand.put(1.toByte())
        expectedCommand.putShort(1.toShort())

        assertArrayEquals(expectedCommand.array(), command)

        var inhaleEvent1 = createInhaleEventBuffer(1, -10, 1001, 1002, 1003, 1004, 1005, 0)
        var inhaleEvent2 = createInhaleEventBuffer(2, -10, 1002, 1003, 1004, 1005, 1006, 0xaa)
        var inhaleEvent3 = createInhaleEventBuffer(3, -10, 1003, 1004, 1005, 1006, 1007, 0x55)
        var inhaleEvent4 = createInhaleEventBuffer(4, -10, 1004, 1005, 1006, 1007, 1008, 0)

        val aesCryptor = AESCryptor(AUTHENTICATION_KEY)
        // decryptWithKeyStream() actually does the same thing as encrypting.
        inhaleEvent1 = aesCryptor.decryptWithKeyStream(inhaleEvent1, nonce)
        inhaleEvent2 = aesCryptor.decryptWithKeyStream(inhaleEvent2, nonce)
        inhaleEvent3 = aesCryptor.decryptWithKeyStream(inhaleEvent3, nonce)
        inhaleEvent4 = aesCryptor.decryptWithKeyStream(inhaleEvent4, nonce)

        whenever(inhaleEventCharacteristic.value).thenReturn(inhaleEvent1)
        bluetoothGattCallback.onCharacteristicChanged(bluetoothGatt, inhaleEventCharacteristic)

        whenever(inhaleEventCharacteristic.value).thenReturn(inhaleEvent2)
        bluetoothGattCallback.onCharacteristicChanged(bluetoothGatt, inhaleEventCharacteristic)

        whenever(inhaleEventCharacteristic.value).thenReturn(inhaleEvent3)
        bluetoothGattCallback.onCharacteristicChanged(bluetoothGatt, inhaleEventCharacteristic)

        whenever(inhaleEventCharacteristic.value).thenReturn(inhaleEvent4)
        bluetoothGattCallback.onCharacteristicChanged(bluetoothGatt, inhaleEventCharacteristic)

        val racpResponse = ByteBuffer.allocate(4)
        racpResponse.order(ByteOrder.LITTLE_ENDIAN)
        racpResponse.put(6.toByte())
        racpResponse.put(0.toByte())
        racpResponse.put(1.toByte())
        racpResponse.put(1.toByte())

        whenever(racpCharacteristic.value).thenReturn(racpResponse.array())
        bluetoothGattCallback.onCharacteristicChanged(bluetoothGatt, racpCharacteristic)

        HandlerHelper.loopHandler()

        val inhaleEventCaptor = argumentCaptor<List<InhaleEventInfo>>()

        val deviceInfoCaptor = argumentCaptor<DeviceInfo>()
        verify(callback).onUpdated(eq(connectionInfo), deviceInfoCaptor.capture(),
                inhaleEventCaptor.capture())

        val deviceInfo = deviceInfoCaptor.lastValue
        val inhaleEvents = inhaleEventCaptor.lastValue

        assertEquals(5, deviceInfo.dosesTaken.toLong())
        assertEquals(4, deviceInfo.lastRecordId.toLong())

        assertEquals(4, inhaleEvents.size.toLong())

        verifyInhaleEventInfo(inhaleEvents[0], 1, 10, 1001, 1002, 1003, 1004, 1005, 0)
        verifyInhaleEventInfo(inhaleEvents[1], 2, 10, 1002, 1003, 1004, 1005, 1006, 0xaa)
        verifyInhaleEventInfo(inhaleEvents[2], 3, 10, 1003, 1004, 1005, 1006, 1007, 0x55)
        verifyInhaleEventInfo(inhaleEvents[3], 4, 10, 1004, 1005, 1006, 1007, 1008, 0)

    }

    /**
     * Tests that a disconnect event from the inhaler propagates to the client of the
     * MedicationDispenserImpl.
     */
    @Test
    @Throws(Exception::class)
    fun TestDisconnect() {
        val medicationDispenser = MedicationDispenserImpl(dependencyProvider, connectionInfo, bluetoothDevice)

        medicationDispenser.setCallback(callback)
        bluetoothGattCallback = medicationDispenser.gattCallback
        medicationDispenser.state = MedicationDispenserImpl.State.IDLE

        bluetoothGattCallback.onConnectionStateChange(bluetoothGatt, BluetoothGatt.GATT_SUCCESS,
                BluetoothProfile.STATE_DISCONNECTED)

        HandlerHelper.loopHandler(2)

        verify(callback).onDisconnected(eq(connectionInfo))
    }


    /**
     * Tests that the MedicationDispenser disconnects from the bluetooth device
     * if the readCharacteristic() method returns a failure.
     */
    @Test
    @Throws(Exception::class)
    fun TestReadFailure() {
        // setup the test
        whenever(bluetoothGatt.readCharacteristic(any())).thenReturn(false)

        val medicationDispenser = MedicationDispenserImpl(dependencyProvider, connectionInfo, bluetoothDevice)

        medicationDispenser.bluetoothGatt = bluetoothGatt
        medicationDispenser.state = MedicationDispenserImpl.State.READING_MANUFACTURER_NAME
        bluetoothGattCallback = medicationDispenser.gattCallback

        val manufacturerName = "ManufacturerName"
        whenever(manufacturerNameCharacteristic.value)
                .thenReturn(manufacturerName.toByteArray(ASCII_CHARSET))

        // Signal the completion reading the Maufacturer Name which will cause the next
        // read to be started.  This read will fail and should cause a disconnect.
        bluetoothGattCallback.onCharacteristicRead(bluetoothGatt, manufacturerNameCharacteristic,
                BluetoothGatt.GATT_SUCCESS)
        HandlerHelper.loopHandler()

        // verify that disconnect was called in response to the error.
        verify(bluetoothGatt).close()
    }

    /**
     * Tests that the MedicationDispenser disconnects from the bluetooth device
     * if the writeCharacteristic() method returns a failure.
     */
    @Test
    @Throws(Exception::class)
    fun TestWriteFailure() {
        // setup the writeCharacteristic() method to return false to indicate an error.
        whenever(bluetoothGatt.writeCharacteristic(any())).thenReturn(false)

        val medicationDispenser = MedicationDispenserImpl(dependencyProvider, connectionInfo, bluetoothDevice)

        medicationDispenser.bluetoothGatt = bluetoothGatt
        medicationDispenser.state = MedicationDispenserImpl.State.SET_NOTIFY_AUTHENTICATION
        bluetoothGattCallback = medicationDispenser.gattCallback

        // run the test

        // Signal the completion of setting up the authentication key notification
        bluetoothGattCallback.onDescriptorWrite(bluetoothGatt, authenticationKeyDescriptor, BluetoothGatt.GATT_SUCCESS)
        HandlerHelper.loopHandler()

        // verify that disconnect was called in response to the error.
        verify(bluetoothGatt).close()
    }

    /**
     * Tests that the MedicationDispenser notifies the DeviceService of a disconnect
     * if the bluetooth device disconnects during the connection process.
     */
    @Test
    @Throws(Exception::class)
    fun TestRandomDisconnectWhileConnecting() {
        // Set up the MedicationDispenser to be in the Reading Manufacturer state
        val medicationDispenser = MedicationDispenserImpl(dependencyProvider, connectionInfo, bluetoothDevice)

        medicationDispenser.setCallback(callback)
        bluetoothGattCallback = medicationDispenser.gattCallback
        medicationDispenser.state = MedicationDispenserImpl.State.READING_MANUFACTURER_NAME

        // indicate that a disconnect occurred.
        bluetoothGattCallback.onConnectionStateChange(bluetoothGatt, BluetoothGatt.GATT_SUCCESS,
                BluetoothProfile.STATE_DISCONNECTED)

        HandlerHelper.loopHandler(2)

        // Verify that the DeviceService is notified of the disconnect.
        verify(callback).onDisconnected(eq(connectionInfo))
    }

    /**
     * Test that the MedicationDispenser disconnects from the bluetooth device
     * if an incorrect authentication key is used by the inhaler.
     */
    @Test
    @Throws(Exception::class)
    fun TestIncorrectAuthenticationKey() {
        // setup the test
        whenever(bluetoothGatt.writeCharacteristic(any()))
                .thenReturn(true)
        whenever(bluetoothGatt.readCharacteristic(any()))
                .thenReturn(true)

        val aesCryptor = AESCryptor(AUTHENTICATION_KEY)
        val wrongAesCryptor = AESCryptor(WRONG_AUTHENTICATION_KEY)

        val medicationDispenser = MedicationDispenserImpl(dependencyProvider, connectionInfo, bluetoothDevice)

        medicationDispenser.bluetoothGatt = bluetoothGatt
        medicationDispenser.state = MedicationDispenserImpl.State.SET_NOTIFY_AUTHENTICATION
        bluetoothGattCallback = medicationDispenser.gattCallback

        // run the test

        // Signal the completion of setting up the authentication key notification
        bluetoothGattCallback.onDescriptorWrite(bluetoothGatt, authenticationKeyDescriptor,
                BluetoothGatt.GATT_SUCCESS)
        HandlerHelper.loopHandler()

        // verify we are in the right state
        assertEquals(MedicationDispenserImpl.State.AUTHENTICATION_1, medicationDispenser.state)

        val byteArrayCaptor = argumentCaptor<ByteArray>()

        // verify and get the 8 byte value that was written to the authentication key
        verify(bluetoothGatt, times(1)).writeCharacteristic(authenticationKeyCharacteristic)
        verify(authenticationKeyCharacteristic, times(1)).value = byteArrayCaptor.capture()
        val authenticationBuffer1 = byteArrayCaptor.lastValue
        assertEquals(8, authenticationBuffer1.size.toLong())


        // signal the reception of 8 bytes from the inhaler
        val authenticationBuffer2 = byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8)

        whenever(authenticationKeyCharacteristic.value).thenReturn(authenticationBuffer2)
        bluetoothGattCallback.onCharacteristicChanged(bluetoothGatt,
                authenticationKeyCharacteristic)

        // write ack comes after changed notification caused by write
        bluetoothGattCallback.onCharacteristicWrite(bluetoothGatt, authenticationKeyCharacteristic,
                BluetoothGatt.GATT)
        HandlerHelper.loopHandler()

        assertEquals(MedicationDispenserImpl.State.AUTHENTICATION_2, medicationDispenser.state)

        // verify the correct 16 byte buffer is sent to inhaler
        verify(bluetoothGatt, times(2)).writeCharacteristic(authenticationKeyCharacteristic)
        verify(authenticationKeyCharacteristic, times(2)).value = byteArrayCaptor.capture()
        val authenticationBuffer3 = byteArrayCaptor.lastValue
        assertEquals(16, authenticationBuffer3.size.toLong())

        var expectedBuffer = ByteArray(16)
        var byteBuffer = ByteBuffer.wrap(expectedBuffer)
        byteBuffer.put(authenticationBuffer1)
        byteBuffer.put(authenticationBuffer2)
        expectedBuffer = aesCryptor.encrypt(expectedBuffer)
        assertArrayEquals(expectedBuffer, authenticationBuffer3)

        // signal authentication response from inhaler
        var responseBuffer = ByteArray(16)
        byteBuffer = ByteBuffer.wrap(responseBuffer)
        byteBuffer.put(authenticationBuffer2)
        byteBuffer.put(authenticationBuffer1)
        responseBuffer = wrongAesCryptor.encrypt(responseBuffer)

        whenever(authenticationKeyCharacteristic.value).thenReturn(responseBuffer)
        bluetoothGattCallback.onCharacteristicChanged(bluetoothGatt,
                authenticationKeyCharacteristic)

        // write ack comes after changed notification caused by write
        bluetoothGattCallback.onCharacteristicWrite(bluetoothGatt, authenticationKeyCharacteristic,
                BluetoothGatt.GATT)
        HandlerHelper.loopHandler()

        // verify that disconnect was called in response to the error.
        verify(bluetoothGatt).close()
    }

    @Test
    @Throws(Exception::class)
    fun TestInvalidDeviceStateBuffer() {
        whenever(bluetoothGatt.writeCharacteristic(any()))
                .thenAnswer { invocation ->
                    val characteristic = invocation.arguments[0] as BluetoothGattCharacteristic
                    bluetoothGattCallback.onCharacteristicWrite(bluetoothGatt, characteristic,
                            BluetoothGatt.GATT_SUCCESS)
                    true
                }

        val medicationDispenser = MedicationDispenserImpl(dependencyProvider, connectionInfo, bluetoothDevice)

        medicationDispenser.setCallback(callback)

        connectionInfo.lastRecordId = 2
        medicationDispenser.deviceInfo!!.lastRecordId = 1
        medicationDispenser.deviceInfo!!.dosesTaken = 2

        medicationDispenser.bluetoothGatt = bluetoothGatt
        medicationDispenser.state = MedicationDispenserImpl.State.IDLE
        bluetoothGattCallback = medicationDispenser.gattCallback

        // Create a device state buffer that is too short.
        val deviceStateBuffer = ByteBuffer.allocate(2)
        deviceStateBuffer.order(ByteOrder.LITTLE_ENDIAN)
        deviceStateBuffer.put(0.toByte())
        deviceStateBuffer.put(0.toByte())

        whenever(deviceStateCharacteristic.value).thenReturn(deviceStateBuffer.array())
        bluetoothGattCallback.onCharacteristicChanged(bluetoothGatt, deviceStateCharacteristic)

        HandlerHelper.loopHandler()

        // verify that disconnect was called in response to the error.
        verify(bluetoothGatt).close()
    }

    /**
     * Tests that a invalid inhale event buffer created by using the wrong
     * authentication key is rejected and the bluetooth device is disconnected.
     */
    @Test
    @Throws(Exception::class)
    fun TestInvalidInhaleEventBuffer() {
        whenever(bluetoothGatt.writeCharacteristic(any()))
                .thenAnswer { invocation ->
                    val characteristic = invocation.arguments[0] as BluetoothGattCharacteristic
                    bluetoothGattCallback.onCharacteristicWrite(bluetoothGatt, characteristic,
                            BluetoothGatt.GATT_SUCCESS)
                    true
                }

        val medicationDispenser = MedicationDispenserImpl(dependencyProvider, connectionInfo, bluetoothDevice)

        medicationDispenser.setCallback(callback)

        connectionInfo.lastRecordId = 2
        medicationDispenser.deviceInfo!!.lastRecordId = 1
        medicationDispenser.deviceInfo!!.dosesTaken = 2

        medicationDispenser.bluetoothGatt = bluetoothGatt
        medicationDispenser.state = MedicationDispenserImpl.State.IDLE
        bluetoothGattCallback = medicationDispenser.gattCallback

        // Signal a device state change
        val deviceStateBuffer = ByteBuffer.allocate(6)
        deviceStateBuffer.order(ByteOrder.LITTLE_ENDIAN)
        deviceStateBuffer.put(0.toByte())
        deviceStateBuffer.put(0.toByte())
        deviceStateBuffer.put(0.toByte())
        deviceStateBuffer.putShort(5.toShort())
        deviceStateBuffer.put(0.toByte())

        whenever(deviceStateCharacteristic.value).thenReturn(deviceStateBuffer.array())
        bluetoothGattCallback.onCharacteristicChanged(bluetoothGatt, deviceStateCharacteristic)

        HandlerHelper.loopHandler()

        val byteArrayCaptor = argumentCaptor<ByteArray>()

        verify(bluetoothGatt).writeCharacteristic(authenticationKeyCharacteristic)
        verify(authenticationKeyCharacteristic).value = byteArrayCaptor.capture()
        var nonce = byteArrayCaptor.lastValue
        nonce = Arrays.copyOf(nonce, nonce.size)

        assertEquals(16, nonce.size.toLong())

        HandlerHelper.loopHandler()

        verify(bluetoothGatt).writeCharacteristic(racpCharacteristic)
        verify(racpCharacteristic).value = byteArrayCaptor.capture()
        val command = byteArrayCaptor.lastValue

        val expectedCommand = ByteBuffer.allocate(5)
        expectedCommand.order(ByteOrder.LITTLE_ENDIAN)
        expectedCommand.put(1.toByte())
        expectedCommand.put(3.toByte())
        expectedCommand.put(1.toByte())
        expectedCommand.putShort(1.toShort())

        assertArrayEquals(expectedCommand.array(), command)

        var inhaleEvent1 = createInhaleEventBuffer(1, 10, 1001, 1002, 1003, 1004, 1005, 0)

        val aesCryptor = AESCryptor(WRONG_AUTHENTICATION_KEY)
        // decryptWithKeyStream() actually does the same thing as encrypting.
        inhaleEvent1 = aesCryptor.decryptWithKeyStream(inhaleEvent1, nonce)

        whenever(inhaleEventCharacteristic.value).thenReturn(inhaleEvent1)
        bluetoothGattCallback.onCharacteristicChanged(bluetoothGatt, inhaleEventCharacteristic)

        HandlerHelper.loopHandler()

        // verify that disconnect was called in response to the error.
        verify(bluetoothGatt).close()
    }

    /**
     * Test that an RACP error results in the disconnecting from the bluetooth device.
     */
    @Test
    @Throws(Exception::class)
    fun TestRACPErrorReported() {
        whenever(bluetoothGatt.writeCharacteristic(any()))
                .thenAnswer { invocation ->
                    val characteristic = invocation.arguments[0] as BluetoothGattCharacteristic
                    bluetoothGattCallback.onCharacteristicWrite(bluetoothGatt, characteristic,
                            BluetoothGatt.GATT_SUCCESS)
                    true
                }

        val medicationDispenser = MedicationDispenserImpl(dependencyProvider, connectionInfo, bluetoothDevice)

        medicationDispenser.setCallback(callback)

        connectionInfo.lastRecordId = 2
        medicationDispenser.deviceInfo!!.lastRecordId = 1
        medicationDispenser.deviceInfo!!.dosesTaken = 2

        medicationDispenser.bluetoothGatt = bluetoothGatt
        medicationDispenser.state = MedicationDispenserImpl.State.IDLE
        bluetoothGattCallback = medicationDispenser.gattCallback

        // Signal a device state change
        val deviceStateBuffer = ByteBuffer.allocate(6)
        deviceStateBuffer.order(ByteOrder.LITTLE_ENDIAN)
        deviceStateBuffer.put(0.toByte())
        deviceStateBuffer.put(0.toByte())
        deviceStateBuffer.put(0.toByte())
        deviceStateBuffer.putShort(5.toShort())
        deviceStateBuffer.put(0.toByte())

        whenever(deviceStateCharacteristic.value).thenReturn(deviceStateBuffer.array())
        bluetoothGattCallback.onCharacteristicChanged(bluetoothGatt, deviceStateCharacteristic)

        HandlerHelper.loopHandler()

        val byteArrayCaptor = argumentCaptor<ByteArray>()

        verify(bluetoothGatt).writeCharacteristic(authenticationKeyCharacteristic)
        verify(authenticationKeyCharacteristic).value = byteArrayCaptor.capture()
        var nonce = byteArrayCaptor.lastValue
        nonce = Arrays.copyOf(nonce, nonce.size)

        assertEquals(16, nonce.size.toLong())

        HandlerHelper.loopHandler()

        verify(bluetoothGatt).writeCharacteristic(racpCharacteristic)
        verify(racpCharacteristic).value = byteArrayCaptor.capture()
        val command = byteArrayCaptor.lastValue

        val expectedCommand = ByteBuffer.allocate(5)
        expectedCommand.order(ByteOrder.LITTLE_ENDIAN)
        expectedCommand.put(1.toByte())
        expectedCommand.put(3.toByte())
        expectedCommand.put(1.toByte())
        expectedCommand.putShort(1.toShort())

        assertArrayEquals(expectedCommand.array(), command)

        val racpResponse = ByteBuffer.allocate(4)
        racpResponse.order(ByteOrder.LITTLE_ENDIAN)
        racpResponse.put(6.toByte())
        racpResponse.put(0.toByte())
        racpResponse.put(1.toByte())
        racpResponse.put(9.toByte())

        whenever(racpCharacteristic.value).thenReturn(racpResponse.array())
        bluetoothGattCallback.onCharacteristicChanged(bluetoothGatt, racpCharacteristic)

        HandlerHelper.loopHandler()

        // verify that disconnect was called in response to the error.
        verify(bluetoothGatt).close()
    }

    companion object {
        private val ASCII_CHARSET = Charset.forName("US-ASCII")

        private val INHALER_SERVICE_UUID = UUID.fromString("f429de80-c342-11e4-9da5-0002a5d5c51b")
        private val INHALE_EVENT_UUID = UUID.fromString("f429de81-c342-11e4-9da5-0002a5d5c51b")
        private val DEVICE_STATE_UUID = UUID.fromString("f429de83-c342-11e4-9da5-0002a5d5c51b")
        private val RACP_UUID = UUID.fromString("00002a52-0000-1000-8000-00805f9b34fb")
        private val AUTHENTICATION_KEY_UUID = UUID.fromString("f429de86-c342-11e4-9da5-0002a5d5c51b")

        private val DEVICE_INFORMATION_SERVICE_UUID = UUID.fromString("0000180a-0000-1000-8000-00805f9b34fb")
        private val MANUFACTURER_NAME_UUID = UUID.fromString("00002a29-0000-1000-8000-00805f9b34fb")
        private val SOFTWARE_REVISION_UUID = UUID.fromString("00002a28-0000-1000-8000-00805f9b34fb")
        private val HARDWARE_REVISION_UUID = UUID.fromString("00002a27-0000-1000-8000-00805f9b34fb")
        private val SERIAL_NUMBER_UUID = UUID.fromString("00002a25-0000-1000-8000-00805f9b34fb")

        private val NOTIFICATION_DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

        private val SERIAL_NUMBER = "12345678901"
        private val AUTHENTICATION_KEY = "1234567890123456"
        private val WRONG_AUTHENTICATION_KEY = "4567890123456123"
        private val CurrentTime = Instant.ofEpochMilli(1483965361)
    }
}