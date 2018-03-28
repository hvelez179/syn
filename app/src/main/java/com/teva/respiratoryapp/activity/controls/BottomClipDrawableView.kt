//
// BottomClipDrawableView.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.activity.controls

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View

import com.teva.respiratoryapp.R

/**
 * Custom view class that draws a bitmap centered horizontally and aligned to the top.
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
class BottomClipDrawableView @JvmOverloads constructor(context: Context,
                                                       attrs: AttributeSet? = null,
                                                       defStyleAttr: Int = 0,
                                                       defStyleRes: Int = 0) : View(context, attrs, defStyleAttr, defStyleRes) {

    private var drawable: Drawable? = null

    init {

        val ta = context.obtainStyledAttributes(
                attrs, R.styleable.BottomClipDrawableView, defStyleAttr, defStyleRes)

        try {
            drawable = ta.getDrawable(R.styleable.BottomClipDrawableView_src)
        } finally {
            ta.recycle()
        }
    }

    /**
     * Implement this to do your drawing.
     *
     * @param canvas the canvas on which the background will be drawn
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        var drawableToRender = drawable
        if (drawableToRender == null) {
            drawableToRender = background
        }

        if (drawableToRender != null) {
            val width = measuredWidth
            var height = measuredHeight
            val intrinsicWidth = drawableToRender.intrinsicWidth
            val intrinsicHeight = drawableToRender.intrinsicHeight

            if (intrinsicHeight != 0 && intrinsicWidth != 0) {
                height = (width * (intrinsicHeight.toFloat() / intrinsicWidth)).toInt()
            }

            drawableToRender.setBounds(0, 0, width, height)

            drawableToRender.draw(canvas)
        }
    }
}
