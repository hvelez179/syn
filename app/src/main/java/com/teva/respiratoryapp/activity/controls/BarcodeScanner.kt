//
// BarcodeScanner.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.activity.controls

import android.content.Context
import android.content.res.Configuration
import android.graphics.Rect
import android.hardware.Camera
import android.support.annotation.BinderThread
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.ViewGroup
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.MultiProcessor
import com.google.android.gms.vision.Tracker
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.teva.utilities.utilities.Logger
import com.teva.utilities.utilities.Logger.Level.DEBUG
import com.teva.utilities.utilities.Logger.Level.ERROR
import java.io.IOException
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit


/**
 * View control that scans for barcodes
 *
 * @param context - the context.
 * @param attrs   - the attribute set.
 */
class BarcodeScanner(context: Context, attrs: AttributeSet) : ViewGroup(context, attrs) {
    private val surfaceView: SurfaceView
    private var startRequested: Boolean = false
    private var surfaceAvailable: Boolean = false
    private var cameraSource: CameraSource? = null

    private var listener: BarcodeListener? = null
    private val visiblePreviewBounds: Rect
    private var barcodeFound: Boolean = false
    private val autoFocusExecutor = ScheduledThreadPoolExecutor(1)
    /**
     * Gets a value indicating whether the barcode scanner is scanning.
     */
    var isScanning: Boolean = false
        private set

    init {
        startRequested = false
        surfaceAvailable = false
        visiblePreviewBounds = Rect()

        surfaceView = SurfaceView(context)

        // The setAutoFocusEnabled method in the start method below sets
        // the auto focus mode. However, if that does not work, override
        // the focus mode when the user clicks on the screen.
        surfaceView.setOnClickListener {
            cameraSource?.let { cameraSource ->
                setCameraFocusModeIfNotCurrentlyAutoFocusing(cameraSource, Camera.Parameters.FOCUS_MODE_MACRO)
            }
        }

        surfaceView.holder.addCallback(SurfaceCallback())
        addView(surfaceView)

    }

    /**
     * Sets a listener to receive the detected barcodes.

     * @param listener The listener object to be called when a barcode is detected.
     */
    fun setBarcodeListener(listener: BarcodeListener) {
        this.listener = listener
    }

    /**
     * Starts the barcode scanner.
     */
    fun start() {
        // initialize barcode detector
        val tracker = object : Tracker<Barcode>() {
            override fun onUpdate(detections: Detector.Detections<Barcode>?, barcode: Barcode?) {
                if (barcode != null) {
                    onBarcode(barcode)
                }
            }
        }

        val multiprocessorFactory = MultiProcessor.Factory<Barcode> { tracker }

        val barcodeMultiProcessor = MultiProcessor.Builder(multiprocessorFactory).build()

        val barcodeDetector = BarcodeDetector.Builder(context).build()
        barcodeDetector.setProcessor(
                barcodeMultiProcessor)

        cameraSource = CameraSource.Builder(context, barcodeDetector)
                .setAutoFocusEnabled(true)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedPreviewSize(REQUESTED_PREVIEW_WIDTH, REQUESTED_PREVIEW_HEIGHT)
                .setRequestedFps(CAMERA_FRAMES_PER_SECOND)
                .build()

        startRequested = true
        barcodeFound = false

        isScanning = true

        startIfReady()
    }

    /**
     * Stops capturing images.
     */
    fun stopCapturing() {
        isScanning = false
        if (cameraSource != null) {
            cameraSource!!.stop()
        }
    }

    /**
     * Stops the barcode scanner.
     */
    fun stop() {
        isScanning = false
        if (cameraSource != null) {
            cameraSource!!.stop()
            cameraSource!!.release()
            cameraSource = null
        }
    }

    /**
     * Lays out the children of the ViewGroup.

     * @param changed Indicates whether the layout dimensions have changed
     * *
     * @param left    The left edge of the ViewGroup
     * *
     * @param top     The top edge of the ViewGroup
     * *
     * @param right   The right edge of the ViewGroup
     * *
     * @param bottom  The bottom edge of the ViewGroup
     */
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        val layoutWidth = measuredWidth
        val layoutHeight = measuredHeight

        // default values if preview size is not available.
        var width = layoutWidth
        var height = layoutHeight

        if (cameraSource != null) {
            val size = cameraSource!!.previewSize
            if (size != null) {
                width = size.width
                height = size.height
            }
        }

        // Swap width and height sizes when in portrait, since it will be rotated 90 degrees
        if (isPortraitMode) {
            val tmp = width

            width = height
            height = tmp
        }

        // Computes height and width for potentially doing fit width.
        var childWidth = layoutWidth
        var childHeight = (height.toFloat() / width.toFloat() * childWidth).toInt()

        // If height is too short using fit width, does fit height instead.
        if (childHeight < layoutHeight) {
            childHeight = layoutHeight
            childWidth = (layoutHeight.toFloat() / height.toFloat() * width).toInt()
        }

        val xOffset = (childWidth - layoutWidth) / 2
        val yOffset = (childHeight - layoutHeight) / 2

        for (i in 0..childCount - 1) {
            getChildAt(i).layout(-xOffset, -yOffset, childWidth - xOffset, childHeight - yOffset)
        }

        val scale = width.toFloat() / childWidth

        visiblePreviewBounds.set(
                (xOffset * scale).toInt(),
                (yOffset * scale).toInt(),
                ((childWidth - xOffset) * scale).toInt(),
                ((childHeight - yOffset) * scale).toInt())

        startIfReady()
    }

    /**
     * Called when a barcode is detected.

     * @param barcode The barcode object containing the barcode components.
     */
    @BinderThread
    private fun onBarcode(barcode: Barcode) {
        // notify only if we haven't already found a barcode since starting
        if (!barcodeFound) {
            // notify only if the barcode was detected within the visible bounds.
            val barcodeBounds = barcode.boundingBox
            if (visiblePreviewBounds.contains(barcodeBounds)) {
                barcodeFound = true

                logger.log(DEBUG, "Found " + barcode.rawValue)

                post {
                    // stop scanning
                    stopCapturing()

                    // notify client
                    if (listener != null) {
                        listener!!.onBarcodeScanned(barcode.rawValue)
                    }
                }
            }
        }
    }

    /**
     * This method checks if the display is in portrait mode.

     * @return - true if the display is in portrait mode else false.
     */
    private val isPortraitMode: Boolean
        get() {
            val orientation = context.resources.configuration.orientation
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                return false
            }
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                return true
            }

            logger.log(DEBUG, "isPortraitMode returning false by default")
            return false
        }

    /**
     * This method starts the camera to receive preview frames.

     */
    private fun startIfReady() {
        if (startRequested && surfaceAvailable) {
            try {
                cameraSource!!.start(surfaceView.holder)
                startRequested = false
                val declaredFields = CameraSource::class.java.declaredFields

                for (field in declaredFields) {
                    if (field.type == Camera::class.java) {
                        field.isAccessible = true
                        try {
                            val camera = field.get(cameraSource) as Camera?
                            if (camera != null) {
                                autoFocusExecutor.schedule({
                                    val params: Camera.Parameters = camera!!.parameters
                                    if (params.getSupportedFocusModes()
                                            .contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                                        params.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE
                                    } else if (params.getSupportedFocusModes()
                                            .contains(Camera.Parameters.FOCUS_MODE_MACRO)) {
                                        params.focusMode = Camera.Parameters.FOCUS_MODE_MACRO
                                    }
                                    camera?.parameters = params
                                }, 1000, TimeUnit.MILLISECONDS)
                            }
                        }
                        catch (e: Exception) {
                            logger.log(ERROR, "Error auto focusing camera", e)
                        }
                    }
                }

            } catch (exception: SecurityException) {
                // There's nothing to do in response to this exception.
                // The user will see that the camera didn't start and can
                // use the manual registration screens.
                logger.log(ERROR, "Error starting Camera to scan inhaler.", exception)
            } catch (exception: IOException) {
                logger.log(ERROR, "Error starting Camera to scan inhaler.", exception)
            }

            requestLayout()
        }
    }

    /**
     * This interface needs to be implemented by classes that need to be notified when a barcode is detected.
     */
    interface BarcodeListener {
        /**
         * Receives a scanned barcode.

         * @param barcodeValue The barcode text.
         */
        fun onBarcodeScanned(barcodeValue: String)
    }

    /**
     * This class implements the SurfaceHolder.Callback interface which is used
     * for passing the image data from camera hardware ro the application.
     */
    private inner class SurfaceCallback : SurfaceHolder.Callback {
        /**
         * This is called immediately after the surface is first created.

         * @param holder The SurfaceHolder whose surface is being created.
         */
        override fun surfaceCreated(holder: SurfaceHolder) {
            surfaceAvailable = true

            startIfReady()
        }

        /**
         * This is called immediately before a surface is being destroyed.

         * @param holder The SurfaceHolder whose surface is being destroyed.
         */
        override fun surfaceDestroyed(holder: SurfaceHolder) {
            surfaceAvailable = false
        }

        /**
         * This is called immediately after any structural changes (format or
         * size) have been made to the surface.

         * @param holder The SurfaceHolder whose surface has changed.
         * *
         * @param format The new PixelFormat of the surface.
         * *
         * @param width The new width of the surface.
         * *
         * @param height The new height of the surface.
         */
        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}
    }

    companion object {
        val REQUESTED_PREVIEW_WIDTH = 1600
        val REQUESTED_PREVIEW_HEIGHT = 1200
        private val logger = Logger(BarcodeScanner::class)
        private val CAMERA_FRAMES_PER_SECOND = 15.0f

        /**
         * This method sets the specified focus mode for the camera source if the
         * camera source is not already in one of the auto focus modes.
         * @param cameraSource - the camera source.
         * *
         * @param focusMode - the focus mode to be set.
         */
        private fun setCameraFocusModeIfNotCurrentlyAutoFocusing(cameraSource: CameraSource, focusMode: String) {
            val declaredFields = CameraSource::class.java.declaredFields

            for (field in declaredFields) {
                if (field.type == Camera::class.java) {
                    field.isAccessible = true
                    try {
                        val camera = field.get(cameraSource) as Camera?
                        if (camera != null) {
                            val params = camera.parameters
                            val currentFocusMode = params.focusMode

                            // if the camera is already in an auto focus mode, do not override
                            // it as this actually seems to cancel the auto focus in most devices.
                            // if it is not in one of the required modes, set the specified mode.
                            if (currentFocusMode != Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO &&
                                    currentFocusMode != Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE &&
                                    currentFocusMode != Camera.Parameters.FOCUS_MODE_MACRO) {
                                params.focusMode = focusMode
                                camera.parameters = params
                            }
                        }

                    } catch (e: Exception) {
                        logger.log(ERROR, "Error auto focusing camera", e)
                    }

                    break
                }
            }
        }
    }

}
