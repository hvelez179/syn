//
// ScanViewModel.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.activity.viewmodel.device

import android.content.Context
import android.content.Context.TELEPHONY_SERVICE
import android.content.Intent
import android.databinding.Bindable
import android.net.Uri
import android.os.Handler
import com.teva.common.utilities.Messenger
import android.telephony.TelephonyManager
import com.teva.common.utilities.DataTask
import com.teva.utilities.services.DependencyProvider
import com.teva.utilities.utilities.Logger.Level.INFO
import com.teva.utilities.utilities.Logger.Level.VERBOSE
import com.teva.common.utilities.MessageHandler
import com.teva.common.utilities.SystemMonitorActivity
import com.teva.devices.model.DeviceQuery
import com.teva.devices.model.QRCode
import com.teva.devices.model.QRCodeParser
import com.teva.medication.dataquery.MedicationDataQuery
import com.teva.medication.entities.Product
import com.teva.respiratoryapp.BR
import com.teva.respiratoryapp.BuildConfig
import com.teva.respiratoryapp.R
import com.teva.respiratoryapp.activity.model.InhalerRegistrationCommonState
import com.teva.respiratoryapp.activity.viewmodel.dashboard.ContactSupportViewModel
import com.teva.respiratoryapp.common.AppSystemMonitorActivity
import com.teva.respiratoryapp.mvvmframework.ui.FragmentViewModel
import com.teva.respiratoryapp.services.alert.AlertButton
import com.teva.respiratoryapp.services.alert.SystemAlertManager

/**
 * ViewModel for the Scan Device screen
 * @param dependencyProvider Dependency Injection object.
 */
class ScanViewModel(dependencyProvider: DependencyProvider)
    : FragmentViewModel(dependencyProvider), MessageHandler.MessageListener {

    private var scanControl: ScanControl? = null
    private var isNewDevice: Boolean = false
    private val messageHandler: MessageHandler = MessageHandler(this)
    private val handler = Handler()

    // This flag indicates that a qr code is being processed.
    // It is used to disallow manual entry.
    private var qrCodeBeingProcessed: Boolean = false

    // This flag indicates that manual entry has been triggered.
    // It is used to disallow qr code processing.
    private var manualEntryInProgress: Boolean = false

    /**
     * A value indicating whether the "Got It!" message is visible
     */
    @get:Bindable
    var isGotItVisible: Boolean = false
        set(gotItVisible) {
            field = gotItVisible
            notifyPropertyChanged(BR.gotItVisible)
        }

    /**
     * Sets an instance of the ScanControl interface used by the ViewModel to start and
     * stop barcode scanning.
     *
     * @param scanControl
     */
    fun setScanControl(scanControl: ScanControl?) {
        this.scanControl = scanControl
    }

    /**
     * Method called by the BaseFragment when the fragment's onPause() lifecycle method is called.
     */
    override fun onPause() {
        super.onPause()
        scanControl?.disableScanning()
    }

    /**
     * Method called by the BaseFragment when the fragment's onResume() lifecycle method is called.
     */
    override fun onResume() {
        // when the view model is resumed, reset the flags indicating
        // QR code processing or manual entry is in progress.
        qrCodeBeingProcessed = false
        manualEntryInProgress = false
        super.onResume()
        scanControl?.enableScanning()
    }

    /**
     * Method called by the fragment when a QR Code is scanned.
     *
     * @param qrCodeString The QR Code text.
     */
    @Synchronized fun onQRCode(qrCodeString: String) {
        if(manualEntryInProgress) {
            logger.log(INFO, "Not processing QR code as manual entry is in progress.")
            return
        }

        logger.log(VERBOSE, "Checking: " + qrCodeString)

        val parser = dependencyProvider.resolve<QRCodeParser>()
        val qrCode = parser.parse(qrCodeString)

        if (qrCode != null) {
            validateProductID(qrCode)
        } else {
            // invalid QR Code
            postAnalyticsMessage(AppSystemMonitorActivity.InvalidQRCodeScanned())

            val systemAlertManager = dependencyProvider.resolve<SystemAlertManager>()
            systemAlertManager.showAlert(
                    messageId = R.string.addDeviceInvalidQRCode_text,
                    onClick = { scanControl?.enableScanning() })
        }
    }

    private fun validateProductID(qrCode: QRCode) {
        var product: Product? = null
        DataTask<Unit, Boolean>("ScanViewModel_ValidateProductIDTask")
                .inBackground {
                    val query = dependencyProvider.resolve<MedicationDataQuery>()
                    val medications = query.getAll()
                    var validProductID = false
                    for(medication in medications) {
                        val matchingProduct = medication.products.firstOrNull {it.productId == (qrCode.productID ?: "")}
                        if( matchingProduct != null) {
                            product = matchingProduct
                            validProductID = true
                            break
                        }
                    }
                    validProductID
                }
                .onResult { result ->
                    handler.post({
                        processQRCodeAfterValidatingProductID(qrCode, product, result!!)
                    })
                }
                .execute()
    }

    private fun processQRCodeAfterValidatingProductID(qrCode: QRCode, product: Product?, prouductIDValid: Boolean) {
        if(prouductIDValid) {
            checkQRCode(qrCode, product!!)
        } else {
            val systemAlertManager = dependencyProvider.resolve<SystemAlertManager>()
            val context = dependencyProvider.resolve<Context>()

            val telephonyManager = context.getSystemService(TELEPHONY_SERVICE) as TelephonyManager?

            if(telephonyManager != null) {
                systemAlertManager.showAlert(
                        messageId = R.string.addDeviceIncompatibleInhaler_text,
                        titleId = R.string.callCenter_text,
                        primaryButtonTextId =  R.string.call_text,
                        secondaryButtonTextId = R.string.cancel_text,
                        onClick = {  button ->
                            if (button == AlertButton.PRIMARY) {
                                val number = BuildConfig.SUPPORT_NUMBER
                                val callIntent = Intent(Intent.ACTION_DIAL)
                                callIntent.data = Uri.parse("tel:" + number)
                                dependencyProvider.resolve<Context>().startActivity(callIntent)
                            } else {
                                scanControl?.enableScanning()
                            }})

            } else {
                systemAlertManager.showAlert(
                        messageId = R.string.addDeviceIncompatibleInhaler_text,
                        primaryButtonTextId = R.string.ok_text,
                        secondaryButtonTextId =  R.string.cancel_text,
                        onClick = { button ->
                            if (button == AlertButton.PRIMARY) {
                                scanControl?.enableScanning()
                            }
                        })
            }
        }
    }

    /**
     * Click handler for the Trouble Scanning button.
     */
    @Synchronized fun onTroubleScanning() {
       dependencyProvider.resolve<ScanEvents>().onTroubleScanning()
    }

    fun onContactSupportClicked() {
        dependencyProvider.resolve<ContactSupportViewModel.Events>().onContactSupport()
    }

    /**
     * check to see if the device already exists, and if it doesn't, then save
     * the serial number and authentication key and move to the next screen.
     */
    private fun checkQRCode(qrCode: QRCode, product: Product) {
        // Set the flag indicating that qr code is being processed.
        // The flag gets reset if qr code matches an existing inhaler and
        // scanning is re-enabled or if onResume is called on the view model.
        qrCodeBeingProcessed = true
        DataTask<Unit, QRCodeTaskResult>("ScanViewModel_CheckQRCodeTask")
                .inBackground {
                    val query = dependencyProvider.resolve<DeviceQuery>()
                    val device = query.get(qrCode.serialNumber)

                    when {
                    // First time device was scanned
                        device == null -> QRCodeTaskResult.NEW

                    // Device is already an active device
                        device.isActive -> QRCodeTaskResult.ALREADY_ACTIVE

                    // Device was previously deleted
                        else -> {
                            query.undoMarkAsDeleted(device)
                            postAnalyticsMessage(AppSystemMonitorActivity.ExistingInactiveInhalerReactivated())
                            dependencyProvider.resolve<InhalerRegistrationCommonState>().mode = InhalerRegistrationCommonState.Mode.Reactivate
                            QRCodeTaskResult.REACTIVATE
                        }
                    }
                }
                .onResult { result ->
                    if (result == QRCodeTaskResult.ALREADY_ACTIVE) {
                        postAnalyticsMessage(AppSystemMonitorActivity.ExistingActiveInhalerQRCodeScanned())
                        // Inhaler already registered
                        val systemAlertManager = dependencyProvider.resolve<SystemAlertManager>()
                        systemAlertManager.showAlert(
                                messageId = R.string.addDeviceDeviceExists_text,
                                primaryButtonTextId = R.string.tryAgain_text,
                                onClick = { scanControl?.enableScanning() })
                        qrCodeBeingProcessed = false
                    } else {
                        // this is a new device or a previously deleted device,
                        // so save serial number and authentication code then move to the next screen.
                        val deviceState = dependencyProvider.resolve<InhalerRegistrationCommonState>()

                        deviceState.serialNumber = qrCode.serialNumber
                        deviceState.authenticationKey = qrCode.authenticationKey
                        deviceState.doseCount = product.initialDoseCount
                        deviceState.remainingDoseCount = product.initialDoseCount

                        isNewDevice = result == QRCodeTaskResult.NEW
                        isGotItVisible = true

                        messageHandler.sendEmptyMessageDelayed(QRCODE_DONE_MESSAGE, QRCODE_DONE_MESSAGE_DELAY)
                    }
                }
                .execute()
    }

    /**
     * Method called when a message is received by the MessageHandler.

     * @param message The id of the message.
     */
    override fun onMessage(message: Int) {
        if (message == QRCODE_DONE_MESSAGE) {
            dependencyProvider.resolve<ScanEvents>().onQRCodeScanned(isNewDevice)
        }
    }

    private fun postAnalyticsMessage(activity: SystemMonitorActivity) {
        dependencyProvider.resolve<Messenger>().post(activity)
    }

    /**
     * Enumeration used by the CheckQRCodeTask to return it's result.
     */
    private enum class QRCodeTaskResult {
        NEW,
        REACTIVATE,
        ALREADY_ACTIVE
    }

    /**
     * Interface implemented by the fragment to provide the ViewModel control over
     * the starting and stopping of barcode scanning.
     */
    interface ScanControl {
        /**
         * Start the scanner.
         */
        fun enableScanning()

        /**
         * Stop the scanner.
         */
        fun disableScanning()
    }

    /**
     * Events produced by the ViewModel to request actions by the Activity.
     */
    interface ScanEvents {
        /**
         * Indicates that the QR Code has been successfully scanned
         */
        fun onQRCodeScanned(isNewDevice: Boolean)

        /**
         * Requests to contact customer support.
         */
        fun onTroubleScanning()
    }

    companion object {
        private val QRCODE_DONE_MESSAGE = 1
        private val QRCODE_DONE_MESSAGE_DELAY: Long = 1000 // milliseconds
    }
}
