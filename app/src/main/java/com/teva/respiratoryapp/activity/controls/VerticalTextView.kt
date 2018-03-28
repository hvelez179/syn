//
// VerticalTextView.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.activity.controls

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.Gravity
import android.widget.TextView

/**
 * This class represents a text view which draws text from bottom to top.
 */
class VerticalTextView(context: Context, attrs: AttributeSet) : TextView(context, attrs) {
    internal val topDown: Boolean

    init {
        val gravity = gravity
        if (Gravity.isVertical(gravity) && gravity and Gravity.VERTICAL_GRAVITY_MASK == Gravity.BOTTOM) {
            setGravity(gravity and Gravity.HORIZONTAL_GRAVITY_MASK or Gravity.TOP)
            topDown = false
        } else
            topDown = true
    }

    /**
     * Measures the control.
     *
     * @param widthMeasureSpec The desired width specification
     * @param heightMeasureSpec The desired height specification
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(heightMeasureSpec, widthMeasureSpec)
        setMeasuredDimension(measuredHeight, measuredWidth)
    }

    /**
     * Renders the control
     *
     * @param canvas The canvas on which to render the control
     */
    override fun onDraw(canvas: Canvas) {
        val textPaint = paint
        textPaint.color = currentTextColor
        textPaint.drawableState = drawableState

        canvas.save()

        if (topDown) {
            canvas.translate(0f, height.toFloat())
            canvas.rotate(-90f)
        } else {
            canvas.translate(width.toFloat(), 0f)
            canvas.rotate(90f)
        }


        canvas.translate(compoundPaddingLeft.toFloat(), extendedPaddingTop.toFloat())

        layout.draw(canvas)
        canvas.restore()
    }
}
