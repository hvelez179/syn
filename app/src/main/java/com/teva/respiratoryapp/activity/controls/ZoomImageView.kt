//
// ZoomImageView.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.activity.controls

import android.content.Context
import android.graphics.Matrix
import android.support.v7.widget.AppCompatImageView
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.widget.ImageView

import com.teva.respiratoryapp.R

/**
 * ImageView control that supports pinch zoom and pan
 */
class ZoomImageView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : AppCompatImageView(context, attrs) {

    private var zoom: Float = 0f
    private var xOrigin: Float = 0f
    private var yOrigin: Float = 0f

    private var minZoom: Float = 0f
    private var maxZoom: Float = 0f

    private val horzFullViewPadding: Float = resources.getDimension(R.dimen.ifu_full_view_horz_padding)
    private val vertFullViewPadding: Float = resources.getDimension(R.dimen.ifu_full_view_vert_padding)

    private val viewMatrix: Matrix = Matrix()

    private var isFirstScale = true

    private val scaleGestureDetector: ScaleGestureDetector
    private val gestureDetector: GestureDetector

    init {
        scaleType = ImageView.ScaleType.MATRIX

        scaleGestureDetector = ScaleGestureDetector(
                context, ScaleGestureListener())

        gestureDetector = GestureDetector(context, GestureListener())

        post { resetMatrix() }
    }

    /**
     * Called when the control surface is touched.
     *
     * @param event The touch event
     * @return True if the event was handled, false otherwise
     */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (zoom > minZoom) {
            parent.requestDisallowInterceptTouchEvent(true)
        }

        gestureDetector.onTouchEvent(event)

        scaleGestureDetector.onTouchEvent(event)

        return true
    }

    /**
     * Gesture listener class used to detect scroll and double tap operations.
     */
    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {

        /**
         * Called when a scroll gesture is detected.
         *
         * @param e1 The first down motion event that started the scroll.
         * @param e2 The current move motion event.
         * @param distanceX The X distance that has been scrolled since the last onScroll() call.
         * @param distanceY The Y distance that has been scrolled since the last onScroll() call.
         * @return True if the event was handled, false otherwise.
         */
        override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {

            if (zoom > minZoom + MIN_ZOOM_THRESHOLD) {
                xOrigin -= distanceX
                yOrigin -= distanceY
                applyMatrix()
                return true
            }

            return false
        }

        /**
         * Called for events that occur after a double tap gesture is detected on the second
         * down touch event until the up touch event.
         *
         * @param e The motion event that occurred
         * @return true if handled.
         */
        override fun onDoubleTapEvent(e: MotionEvent): Boolean {

            // only change the zoom on the up touch event if a quick scale operation is not occurring
            if (e.action == MotionEvent.ACTION_UP && !scaleGestureDetector.isInProgress) {
                if (zoom < minZoom + MIN_ZOOM_THRESHOLD) {
                    scale(2f, e.x, e.y)
                } else {
                    resetMatrix()
                }
            }

            return true
        }
    }

    /**
     * Gesture listener class used to detect pinch zoom gestures.
     */
    private inner class ScaleGestureListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {

            val result = !isFirstScale

            isFirstScale = false

            return result
        }

        override fun onScaleEnd(detector: ScaleGestureDetector) {

            super.onScaleEnd(detector)
        }

        /**
         * Called when a pinch zoom gesture is detected.
         *
         * @param detector The gesture detector.
         * @return True if the event is handled, false otherwise
         */
        override fun onScale(detector: ScaleGestureDetector): Boolean {

            val factor = detector.scaleFactor
            val focusX = detector.focusX
            val focusY = detector.focusY

            scale(factor, focusX, focusY)

            return true
        }

    }

    /**
     * Scale the view around a specified point.
     *
     * @param factor The amount to scale by.
     * @param focusX The X center of the scaling.
     * @param focusY The Y center of the scaling.
     */
    private fun scale(factor: Float, focusX: Float, focusY: Float) {
        val dx = (focusX - xOrigin) / zoom
        val dy = (focusY - yOrigin) / zoom

        zoom *= factor
        if (zoom > maxZoom) {
            zoom = maxZoom
        } else if (zoom < minZoom) {
            zoom = minZoom
        }

        xOrigin = focusX - dx * zoom
        yOrigin = focusY - dy * zoom

        applyMatrix()
    }

    /**
     * Resets the display matrix that scales the image to fit the view.
     */
    private fun resetMatrix() {
        val drawable = drawable

        val imageWidth = drawable.intrinsicWidth
        val imageHeight = drawable.intrinsicHeight

        val viewWidth = measuredWidth - (2 * horzFullViewPadding).toInt()
        val viewHeight = measuredHeight - (2 * vertFullViewPadding).toInt()

        val aspectImage = imageWidth.toFloat() / imageHeight
        val aspectView = viewWidth.toFloat() / viewHeight

        if (aspectImage > aspectView) {
            // width constrained
            zoom = viewWidth.toFloat() / imageWidth
        } else {
            // height constrained
            zoom = viewHeight.toFloat() / imageHeight
        }

        minZoom = zoom
        maxZoom = minZoom * 3

        val scaledWidth = zoom * imageWidth
        val scaledHeight = zoom * imageHeight

        xOrigin = (viewWidth - scaledWidth) / 2
        yOrigin = (viewWidth - scaledHeight) / 2

        applyMatrix()
    }

    /**
     * Builds and applies a matrix from the zoom and translate parameters.
     */
    private fun applyMatrix() {

        val drawable = drawable

        val imageWidth = drawable.intrinsicWidth
        val imageHeight = drawable.intrinsicHeight

        val viewWidth = measuredWidth - (2 * horzFullViewPadding).toInt()
        val viewHeight = measuredHeight - (2 * vertFullViewPadding).toInt()

        val zoomedWidth = imageWidth * zoom
        val zoomedHeight = imageHeight * zoom

        val fullViewWidth = imageWidth * minZoom
        val maxX = horzFullViewPadding + Math.max(0f, (viewWidth - fullViewWidth) / 2f)
        val minX = maxX + fullViewWidth - zoomedWidth


        val fullViewHeight = imageHeight * minZoom
        val maxY = vertFullViewPadding + Math.max(0f, (viewHeight - fullViewHeight) / 2f)
        val minY = maxY + fullViewHeight - zoomedHeight

        if (xOrigin < minX) {
            xOrigin = minX
        } else if (xOrigin > maxX) {
            xOrigin = maxX
        }

        if (yOrigin < minY) {
            yOrigin = minY
        } else if (yOrigin > maxY) {
            yOrigin = maxY
        }

        viewMatrix.setScale(zoom, zoom)
        viewMatrix.postTranslate(xOrigin, yOrigin)

        imageMatrix = viewMatrix
    }

    companion object {
        private val MIN_ZOOM_THRESHOLD = 0.001f
    }
}
