//
//
// BlurImage.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//
//

package com.teva.respiratoryapp.mvvmframework.controls

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.renderscript.*
import android.support.v4.view.ViewCompat
import android.util.AttributeSet
import android.util.Size
import android.view.View
import com.teva.respiratoryapp.R
import com.teva.respiratoryapp.mvvmframework.utils.toBitmap

/**
 * This class provides blur functionality used in transition animations.
 *
 * @param context      The Context the view is running in, through which it can
 *                     access the current theme, resources, etc.
 * @param attrs        The attributes of the XML tag that is inflating the view.
 * @param defStyleAttr An attribute in the current theme that contains a
 *                     reference to a style resource that supplies default values for
 *                     the view. Can be 0 to not look for defaults.
 * @param defStyleRes  A resource identifier of a style resource that
 *                     supplies default values for the view, used only if
 *                     defStyleAttr is 0 or can not be found in the theme. Can be 0
 *                     to not look for defaults.
 */
class BlurImage @JvmOverloads constructor(context: Context,
                                         attrs: AttributeSet? = null,
                                         defStyleAttr: Int = 0,
                                         defStyleRes: Int = 0) : View(context, attrs, defStyleAttr, defStyleRes) {

    private val paint: Paint
    private var scaledSrc: Bitmap? = null
    private var blurBitmap: Bitmap? = null
    private var blurSrcSize: Size = Size(1,1)
    private var tmpIn: Allocation? = null
    private var tmpOut: Allocation? = null
    private var renderScript: RenderScript? = null
    private var theIntrinsic: ScriptIntrinsicBlur? = null

    private val maximum_radius = 25f
    private val srcRect: Rect = Rect()
    private val destRect: Rect = Rect()

    /**
     * A value indicating the level of blurring. 0 for no blur, 1 for maximum blur.
     */
    var blur: Float = 0f
        set(value) {
            field = value
            invalidate()
        }

    /**
     * The drawable image to blur.
     */
    var blurSrc: Drawable? = null
        set(value) {
            field = value
            createBlurSources()
            invalidate()
        }

    /**
     * The distance to shift the blurred image.
     */
    var blurOffset: Int = 0
        set(value) {
            field = value
            invalidate()
        }

    init {
        paint = Paint()

        if (!isInEditMode) {
            renderScript = RenderScript.create(context)
            theIntrinsic = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript))
        }

        val typedArray = context.obtainStyledAttributes(
                attrs, R.styleable.BlurImage, defStyleAttr, defStyleRes)

        try {
            blurSrc = typedArray.getDrawable(R.styleable.BlurImage_blurSrc)
            blur = typedArray.getFloat(R.styleable.BlurImage_blur, 0f)
            blurOffset = typedArray.getDimensionPixelOffset(R.styleable.BlurImage_blurOffset, 0)
        } finally {
            typedArray.recycle()
        }

    }

    /**
     * Implement this to do your drawing.
     *
     * @param canvas the canvas on which the background will be drawn
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val radius = maximum_radius * blur
        val isRTL = ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL
        val offsetDir = if (isRTL) 1 else -1

        if (blurSrc != null) {
            val aspect = measuredWidth.toFloat() / measuredHeight
            val srcAspect = if (scaledSrc != null) { blurSrcSize.width.toFloat() / blurSrcSize.height } else { aspect }

            if (srcAspect > aspect) {
                // width constrained,
                // so scale to match height and crop width
                var scaledHeight = measuredHeight
                var scaledWidth = (measuredHeight * srcAspect).toInt()
                val diff = scaledWidth - measuredWidth
                if (diff < blurOffset) {
                    scaledWidth = measuredWidth + blurOffset
                    scaledHeight = (scaledWidth / srcAspect).toInt()
                }

                val widthOffset = ((scaledWidth - measuredWidth) / 2) + (blurOffset/2) * offsetDir
                val heightOffset = (scaledHeight - measuredHeight) / 2

                destRect.set(-widthOffset, -heightOffset, scaledWidth - widthOffset, scaledHeight - heightOffset)
            } else {
                // height constrained
                // so scale to match width and crop height
                val scaledWidth = measuredWidth + blurOffset
                val scaledHeight = (scaledWidth / srcAspect).toInt()

                val widthOffset = if (isRTL) 0 else -blurOffset
                val heightOffset = scaledHeight - measuredHeight

                destRect.set(widthOffset, -heightOffset, scaledWidth + widthOffset, measuredHeight)
            }


            if (radius > 0f && !isInEditMode) {
                srcRect.set(0, 0, scaledSrc!!.width, scaledSrc!!.height)
                theIntrinsic?.setRadius(radius)
                theIntrinsic?.forEach(tmpOut)
                tmpOut!!.copyTo(blurBitmap)

                var offset = (blurOffset * blur).toInt()
                if (isRTL) {
                    offset = -offset
                }
                destRect.left += offset
                destRect.right += offset

                canvas.drawBitmap(blurBitmap, srcRect, destRect, paint)
            } else {
                blurSrc?.setBounds(destRect.left, destRect.top, destRect.right, destRect.bottom)
                blurSrc?.draw(canvas)
            }
        }
    }

    /**
     * Initializes the fields used when blurring the source image.
     */
    private fun createBlurSources() {
        val srcBitmap = blurSrc?.toBitmap()
        if (srcBitmap != null) {
            blurSrcSize = Size(srcBitmap.width, srcBitmap.height)
            scaledSrc = Bitmap.createScaledBitmap(srcBitmap, 300, 600, true)
            blurBitmap = Bitmap.createBitmap(scaledSrc)
            if (!isInEditMode) {
                tmpIn = Allocation.createFromBitmap(renderScript, scaledSrc)
                tmpOut = Allocation.createFromBitmap(renderScript, blurBitmap)
                theIntrinsic?.setInput(tmpIn)
            }
        } else {
            scaledSrc = null
            blurBitmap = null
            tmpIn = null
            tmpOut = null
            theIntrinsic?.setInput(null)
            blurSrcSize = Size(1,1)
        }
    }
}
