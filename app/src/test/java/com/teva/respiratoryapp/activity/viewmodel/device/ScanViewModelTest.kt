///
// ScanViewModelTest.java
// app
//
// Copyright © 2017 Teva. All rights reserved
///

package com.teva.respiratoryapp.activity.viewmodel.device

import android.content.Context
import com.nhaarman.mockito_kotlin.*
import com.teva.utilities.services.DependencyProvider
import com.teva.common.utilities.LocalizationService
import com.teva.common.utilities.Messenger
import com.teva.devices.entities.Device
import com.teva.devices.model.DeviceQuery
import com.teva.devices.model.QRCode
import com.teva.devices.model.QRCodeParser
import com.teva.medication.dataquery.MedicationDataQuery
import com.teva.medication.entities.Medication
import com.teva.medication.entities.Product
import com.teva.respiratoryapp.R
import com.teva.respiratoryapp.activity.model.InhalerRegistrationCommonState
import com.teva.respiratoryapp.mocks.MockedLocalizationService
import com.teva.respiratoryapp.services.alert.AlertButton
import com.teva.respiratoryapp.services.alert.SystemAlertManager
import com.teva.respiratoryapp.testutils.BaseTest
import com.teva.respiratoryapp.testutils.mocks.HandlerHelper

import org.junit.Before
import org.junit.Test
import org.threeten.bp.Duration

class ScanViewModelTest : BaseTest() {

    private lateinit var commonState: InhalerRegistrationCommonState
    private lateinit var messenger: Messenger
    private lateinit var device: Device
    private lateinit var deviceQuery: DeviceQuery
    private lateinit var events: ScanViewModel.ScanEvents
    private lateinit var scanControl: ScanViewModel.ScanControl
    private lateinit var systemAlertManager: SystemAlertManager
    private lateinit var qrCodeParser: QRCodeParser

    private lateinit var dependencyProvider: DependencyProvider
    private lateinit var medicationdataQuery: MedicationDataQuery

    @Before
    @Throws(Exception::class)
    fun setUp() {
        dependencyProvider = DependencyProvider.default

        messenger = mock()
        dependencyProvider.register(Messenger::class, messenger)

        val medication = Medication()
        medication.brandName = BRANDNAME
        device = Device()
        device.serialNumber = ACTIVE_SERIAL_NUMBER
        device.nickname = NICKNAME
        device.medication = medication
        device.disconnectedTimeSpan = Duration.ZERO

        val product1 = Product("745750", "", 200, 6)
        val product2 = Product("745750", "AAA030", 30, 6)
        medication.products = listOf(product1, product2)

        deviceQuery = mock()
        whenever(deviceQuery.get(ACTIVE_SERIAL_NUMBER)).thenReturn(device)
        dependencyProvider.register(DeviceQuery::class, deviceQuery)

        medicationdataQuery = mock()
        whenever(medicationdataQuery.getAll()).thenReturn(listOf(medication))
        dependencyProvider.register(MedicationDataQuery::class, medicationdataQuery)

        // initialize localization service
        val localizationService = MockedLocalizationService()
        dependencyProvider.register(LocalizationService::class, localizationService)

        systemAlertManager = mock()
        dependencyProvider.register(SystemAlertManager::class, systemAlertManager)

        events = mock()
        dependencyProvider.register(ScanViewModel.ScanEvents::class, events)

        scanControl = mock()

        // initialize common state
        commonState = mock()
        dependencyProvider.register(InhalerRegistrationCommonState::class, commonState)

        qrCodeParser = mock()
        whenever(qrCodeParser.base64ToAuthenticationKey(any())).thenReturn("0000")
        dependencyProvider.register(QRCodeParser::class, qrCodeParser)

        val context: Context = mock()
        whenever(context.getSystemService(any())).thenReturn(null)
        dependencyProvider.register(Context::class, context)
    }

    @Test
    fun testOnResumeEnablesScanning() {
        val viewModel = ScanViewModel(dependencyProvider)

        viewModel.setScanControl(scanControl)
        viewModel.onResume()

        verify(scanControl).enableScanning()
    }

    @Test
    fun testOnPauseDisablesScanning() {
        val viewModel = ScanViewModel(dependencyProvider)

        viewModel.setScanControl(scanControl)
        viewModel.onPause()

        verify(scanControl).disableScanning()
    }

    @Test
    fun testAlertDisplayedForInvalidQRCodeAndScanningReEnabled() {
        val viewModel = ScanViewModel(dependencyProvider)

        whenever(qrCodeParser.parse(BAD_QRCODE)).thenReturn(null)
        viewModel.setScanControl(scanControl)
        viewModel.onQRCode(BAD_QRCODE)

        HandlerHelper.loopHandler()

        val callbackArgumentCaptor = argumentCaptor<(AlertButton)->Unit>()
        verify(systemAlertManager).showAlert(
                id = isNull(),
                message = isNull(),
                messageId = eq(R.string.addDeviceInvalidQRCode_text),
                title = isNull(),
                titleId = isNull(),
                primaryButtonTextId = eq(R.string.ok_text),
                secondaryButtonTextId = isNull(),
                onClick = callbackArgumentCaptor.capture(),
                onClickClose = isNull(),
                imageTextId = isNull(),
                imageId = isNull(),
                onImageClick = isNull())

        // simulate an OK click on the alert.
        val callback = callbackArgumentCaptor.lastValue
        callback(AlertButton.PRIMARY)

        verify(scanControl).enableScanning()
    }

    @Test
    fun testAlertDisplayedForIncompatibleQRCodeAndScanningReEnabled() {
        val viewModel = ScanViewModel(dependencyProvider)
        val qrCode = QRCode(PRODUCT_ID, ACTIVE_SERIAL_NUMBER, AUTHENTICATION_CODE)
        whenever(qrCodeParser.parse(INCOMPATIBLE_QRCODE)).thenReturn(qrCode)
        viewModel.setScanControl(scanControl)
        viewModel.onQRCode(INCOMPATIBLE_QRCODE)

        HandlerHelper.loopHandler()

        val callbackArgumentCaptor = argumentCaptor<(AlertButton)->Unit>()
        verify(systemAlertManager)?.showAlert(
                id = isNull(),
                message = isNull(),
                messageId = eq(R.string.addDeviceIncompatibleInhaler_text),
                title = isNull(),
                titleId = isNull(),
                primaryButtonTextId = eq(R.string.ok_text),
                secondaryButtonTextId = eq(R.string.cancel_text),
                onClick = callbackArgumentCaptor.capture(),
                onClickClose = isNull(),
                imageTextId = isNull(),
                imageId = isNull(),
                onImageClick = isNull())

        // simulate an OK click on the alert.
        val callback = callbackArgumentCaptor.lastValue
        callback(AlertButton.PRIMARY)

        verify(scanControl).enableScanning()
    }

    @Test
    fun testAlertDisplayedWhenActiveInhalerScanned() {
        val viewModel = ScanViewModel(dependencyProvider)

        val qrCode = QRCode(null, ACTIVE_SERIAL_NUMBER, AUTHENTICATION_CODE)
        whenever(qrCodeParser.parse(ACTIVE_QRCODE)).thenReturn(qrCode)

        viewModel.setScanControl(scanControl)
        viewModel.onQRCode(ACTIVE_QRCODE)

        HandlerHelper.loopHandler()

        val callbackArgumentCaptor = argumentCaptor<(AlertButton)->Unit>()
        verify(systemAlertManager)?.showAlert(
                id = isNull(),
                message = isNull(),
                messageId = eq(R.string.addDeviceDeviceExists_text),
                title = isNull(),
                titleId = isNull(),
                primaryButtonTextId = eq(R.string.tryAgain_text),
                secondaryButtonTextId = isNull(),
                onClick = callbackArgumentCaptor.capture(),
                onClickClose = isNull(),
                imageTextId = isNull(),
                imageId = isNull(),
                onImageClick = isNull())

        // simulate an OK click on the alert.
        val callback = callbackArgumentCaptor.lastValue
        callback(AlertButton.PRIMARY)

        verify(scanControl).enableScanning()
    }

    @Test
    fun testQrCodeSavedAndOnQRCodeScannedCalledForValidQRCode() {
        val viewModel = ScanViewModel(dependencyProvider)

        val qrCode = QRCode(null, NEW_SERIAL_NUMBER, AUTHENTICATION_CODE)
        whenever(qrCodeParser.parse(NEW_QRCODE)).thenReturn(qrCode)

        viewModel.setScanControl(scanControl)
        viewModel.onQRCode(NEW_QRCODE)

        HandlerHelper.loopHandler()
        HandlerHelper.loopHandler()

        verify(commonState).serialNumber = NEW_SERIAL_NUMBER
        verify(commonState).authenticationKey = AUTHENTICATION_CODE

        verify(events).onQRCodeScanned(true)
    }

    @Test
    fun testQrCodeSavedAndOnQRCodeScannedCalledForValidQRCodeWithProductID() {
        val viewModel = ScanViewModel(dependencyProvider)

        val qrCode = QRCode(VALID_PRODUCT_ID, NEW_SERIAL_NUMBER, AUTHENTICATION_CODE)
        whenever(qrCodeParser.parse(NEW_QRCODE)).thenReturn(qrCode)

        viewModel.setScanControl(scanControl)
        viewModel.onQRCode(NEW_QRCODE)

        HandlerHelper.loopHandler()
        HandlerHelper.loopHandler()

        verify(commonState).serialNumber = NEW_SERIAL_NUMBER
        verify(commonState).authenticationKey = AUTHENTICATION_CODE

        verify(events).onQRCodeScanned(true)
    }

    companion object {
        private val NEW_QRCODE = "http://tevapharm.com?s=DlLbDG0.&a=NjE1MTk2Mjk0"
        private val ACTIVE_QRCODE = "http://tevapharm.com?s=DlLbDG4.&a=NjE1MTk2Mjk0"
        private val BAD_QRCODE = "http://tevapharm.com?s=DlLbDG8.&w=NjE1MTk2Mjk0"
        private val INCOMPATIBLE_QRCODE = "http://tevapharm.com?p=t1234I&s=DlLbDG4.&a=NjE1MTk2Mjk0"

        private val AUTHENTICATION_CODE = "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF"

        private val VALID_PRODUCT_ID = "AAA030"
        private val PRODUCT_ID = "t1234I"
        private val NEW_SERIAL_NUMBER = "61519629421"
        private val ACTIVE_SERIAL_NUMBER = "61519629422"
        private val NICKNAME = "nickname"
        private val BRANDNAME = "brandname"

        private val INVALID_QR_CODE = "invalid_qr_code"
        private val INCOMPATIBLE_QR_CODE = "incompatible_qr_code"
        private val INHALER_ALREADY_REGISTERED = "inhaler_already_registered"
        private val TEVA_SUPPORT = "teva_support"
    }

}