//
// ScanFragment.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.activity.view.device


import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.teva.respiratoryapp.R
import com.teva.respiratoryapp.activity.controls.BarcodeScanner
import com.teva.respiratoryapp.activity.viewmodel.device.ScanViewModel
import com.teva.respiratoryapp.databinding.ScanIntroFragmentBinding
import com.teva.respiratoryapp.models.ApplicationSettings
import com.teva.respiratoryapp.mvvmframework.ui.BaseFragment
import com.teva.respiratoryapp.mvvmframework.ui.PermissionChecker
import com.teva.common.messages.PermissionUpdateMessage
import com.teva.common.services.analytics.enumerations.AnalyticsScreen
import com.teva.common.utilities.MessageHandler
import com.teva.common.utilities.Messenger
import com.teva.respiratoryapp.services.alert.AlertButton
import com.teva.respiratoryapp.services.alert.SystemAlertManager

import org.greenrobot.eventbus.Subscribe


/**
 * Fragment class for the Device List screen.
 */
class ScanFragment : BaseFragment<ScanIntroFragmentBinding, ScanViewModel>(R.layout.scan_fragment), ScanViewModel.ScanControl, BarcodeScanner.BarcodeListener, MessageHandler.MessageListener {

    private var permissionChecker: PermissionChecker? = null
    private var hasCameraPermission: Boolean = false
    private var scanEnabled: Boolean = false

    private var barcodeScanner: BarcodeScanner? = null

    private val messageHandler: MessageHandler = MessageHandler(this)
    private var instructionView1: View? = null
    private var instructionView2: View? = null

    init {
        screen = AnalyticsScreen.ScanInhaler()
    }

    /**
     * Implemented by derived fragments to configure the properties of the fragment.
     */
    override fun configureFragment() {
        super.configureFragment()

        toolbarTitle = localizationService!!.getString(R.string.addDeviceScanInhalerTitle_text)
    }

    /**
     * Sets the ViewModel for the fragment.
     *
     * @param fragmentArguments The saved fragment instance state
     */
    override fun inject(fragmentArguments: Bundle?) {
        viewModel = ScanViewModel(dependencyProvider!!)
    }

    /**
     * PermissionUpdateMessage handler that is called when the app permissions are updated.
     */
    @Subscribe
    fun onPermissionUpdateMessage(message: PermissionUpdateMessage) {
        if (message.hasPermission(Manifest.permission.CAMERA)) {
            hasCameraPermission = true
            updateScannerState()
        }
    }

    /**
     * Updates the state of the scanner based on the current request state of the ViewModel.
     */
    @Throws(SecurityException::class)
    private fun updateScannerState() {
        if (!barcodeScanner!!.isScanning && scanEnabled && hasCameraPermission) {
            startCameraPreview()
        } else if (barcodeScanner!!.isScanning && !scanEnabled) {
            stopCameraPreview()
        }
    }

    /**
     * ScanControl interface method called by the ViewModel to enable barcode scanning.
     */
    override fun enableScanning() {
        scanEnabled = true
        updateScannerState()
    }

    /**
     * ScanControl interface method called by the ViewModel to disable barcode scanning.
     */
    override fun disableScanning() {
        scanEnabled = false
        updateScannerState()
    }


    /**
     * Android lifecycle method called when the fragment is no longer interacting
     * with the user.
     */
    override fun onPause() {
        super.onPause()
        stopCameraPreview()

        messageHandler.removeMessages(FORWARD_TRANSITION)
        messageHandler.removeMessages(REVERSE_TRANSITION)
    }

    /**
     * Android lifecycle method called when the fragment begins interacting
     * with the user.
     */
    @Throws(SecurityException::class)
    override fun onResume() {
        super.onResume()

        hasCameraPermission = permissionChecker!!.checkPermissions(Manifest.permission.CAMERA)
        if (!hasCameraPermission) {
            if (permissionChecker!!.shouldShowRationale(Manifest.permission.CAMERA)) {
                showCameraPermissionRationale()
            } else {
                val applicationSettings = dependencyProvider!!.resolve<ApplicationSettings>()

                if (!applicationSettings.cameraPermissionRequested) {
                    permissionChecker!!.requestPermissions(arrayOf(Manifest.permission.CAMERA), object : PermissionChecker.PermissionCallback{
                        override fun onPermissionRequestResult(granted: Boolean) {
                            applicationSettings.cameraPermissionRequested = true
                        }
                    })
                }
            }
        }

        updateScannerState()

        messageHandler.sendEmptyMessageDelayed(FORWARD_TRANSITION, TRANSITION_INTERVAL.toLong())
    }

    /**
     * Displays a dialog explaining why the app wants the camera permissions.
     */
    private fun showCameraPermissionRationale() {
        val systemAlertManager = dependencyProvider!!.resolve<SystemAlertManager>()
        systemAlertManager.showQuery(
                messageId = R.string.consentToAccessCameraContent_text,
                titleId = R.string.consentToAccessCameraTitle_text,
                primaryButtonTextId = R.string.ok_text,
                secondaryButtonTextId = R.string.cancel_text,
                onClick = { alertButton ->
                    if (alertButton === AlertButton.PRIMARY) {
                        permissionChecker!!.requestPermissions(Manifest.permission.CAMERA)
                    }
                })
    }

    /**
     * Android lifecycle method called when the fragment is displayed on the screen.
     */
    override fun onStart() {
        super.onStart()
        viewModel!!.setScanControl(this)

        val messenger = dependencyProvider!!.resolve<Messenger>()
        messenger.subscribe(this)
    }

    /**
     * Android lifecycle method called when the fragment is no longer visible
     * to the user.
     */
    override fun onStop() {
        super.onStop()
        viewModel!!.setScanControl(null)

        barcodeScanner!!.stop()

        val messenger = dependencyProvider!!.resolve<Messenger>()
        messenger.unsubscribeToAll(this)
    }

    /**
     * This method creates and returns the view hierarchy associated with the fragment.

     * @param inflater           The view inflater for the fragment.
     * *
     * @param container          The container that the view will be added to.
     * *
     * @param savedInstanceState The saved state of the fragment.
     * *
     * @return The main view for the fragment.
     */
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        permissionChecker = dependencyProvider!!.resolve()

        barcodeScanner = view.findViewById(R.id.scanner)
        barcodeScanner!!.setBarcodeListener(this)

        instructionView1 = view.findViewById(R.id.instruction1)
        instructionView2 = view.findViewById(R.id.instruction2)
        instructionView2!!.visibility = View.INVISIBLE

        return view
    }

    /**
     * This method stops the camera preview.
     */
    private fun stopCameraPreview() {
        if (barcodeScanner!!.isScanning) {
            barcodeScanner!!.stop()
        }
    }

    /**
     * This method starts the camera preview.
     */
    private fun startCameraPreview() {
        if (!barcodeScanner!!.isScanning) {
            barcodeScanner!!.start()
        }
    }

    /**
     * Receives a scanned barcode.

     * @param barcodeValue The barcode text.
     */
    override fun onBarcodeScanned(barcodeValue: String) {
        viewModel!!.onQRCode(barcodeValue)
    }

    /**
     * Method called when a message is received by the MessageHandler.

     * @param message The id of the message.
     */
    override fun onMessage(message: Int) {
        if (message == FORWARD_TRANSITION) {
            transitionBetweenViews(instructionView1!!, instructionView2!!)
            messageHandler.sendEmptyMessageDelayed(REVERSE_TRANSITION, TRANSITION_INTERVAL.toLong())
        } else {
            transitionBetweenViews(instructionView2!!, instructionView1!!)
            messageHandler.sendEmptyMessageDelayed(FORWARD_TRANSITION, TRANSITION_INTERVAL.toLong())
        }
    }

    /**
     * This method transitions from the first view to the second.
     * @param fromView - the view to transition from.
     * *
     * @param toView - the view to transition to.
     */
    private fun transitionBetweenViews(fromView: View, toView: View) {
        // Set the content view to 0% opacity but visible, so that it is visible
        // (but fully transparent) during the animation.
        toView.alpha = 0f
        toView.visibility = View.VISIBLE

        // Animate the content view to 100% opacity, and clear any animation
        // listener set on the view.
        toView.animate()
                .alpha(1f)
                .setDuration(TRANSITION_DURATION.toLong())
                .setListener(null)

        // Animate the loading view to 0% opacity. After the animation ends,
        // set its visibility to GONE as an optimization step (it won't
        // participate in layout passes, etc.)
        fromView.animate()
                .alpha(0f)
                .setDuration(TRANSITION_DURATION.toLong())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        fromView.visibility = View.GONE
                    }
                })
    }

    companion object {
        private val FORWARD_TRANSITION = 1
        private val REVERSE_TRANSITION = 2
        private val TRANSITION_INTERVAL = 5000
        private val TRANSITION_DURATION = 500
    }
}
