//
// SweepRevealTextViewBehavior.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.activity.controls

import android.graphics.LinearGradient
import android.graphics.Shader
import android.widget.TextView

/**
 * Behavior class that implements a sweep alpha and can be animated to reveal a text view.
 *
 * @param textView The text view to control.
 * @param fadeWidth The width of the fade region.
 */
class SweepRevealTextViewBehavior(private val textView: TextView,
                                  private val fadeWidth: Float) {
    /**
     * The fraction that the textview is revealed from left to right.
     */
    var fraction: Float = 0f
        set(value) {
            field = value
            updateGradient()
        }

    init {
        updateGradient()
    }

    /**
     * Updates the text view's gradient.
     */
    private fun updateGradient() {
        val x = ((textView.measuredWidth + fadeWidth) * fraction) - fadeWidth
        val color1 =  textView.textColors.defaultColor
        val color2 = color1 and 0xffffff
        textView.paint.shader = LinearGradient(x, 0f, x + fadeWidth, 0f,
                color1, color2, Shader.TileMode.CLAMP)
        textView.invalidate()
    }

}