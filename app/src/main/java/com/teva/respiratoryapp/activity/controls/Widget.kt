//
// Widget.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.activity.controls


import android.content.Context
import android.support.annotation.AttrRes
import android.support.annotation.StyleRes
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.teva.respiratoryapp.R

/**
 * A control with a Critical state
 */
class Widget @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, @AttrRes defStyleAttr: Int = 0, @StyleRes defStyleRes: Int = 0) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    /**
     * A value indicating whether the widget should be displayed in the critical state.
     */
    var isCritical: Boolean = false
        set(value) {
            if (field != value) {
                field = value

                refreshDrawableState()
            }
        }

    init {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.Widget, 0, 0)
        try {
            val critical = ta.getBoolean(R.styleable.Widget_critical, false)

            isCritical = critical
        } finally {
            ta.recycle()
        }
    }

    /**
     * Updates the drawable states of the control.
     *
     * @param extraSpace The number of drawable state space needed by the base class
     * @return The drawable state of the control.
     */
    override fun onCreateDrawableState(extraSpace: Int): IntArray {
        if (isCritical) {
            val drawableState = super.onCreateDrawableState(extraSpace + CRITICAL_STATE_SET.size)

            View.mergeDrawableStates(drawableState, CRITICAL_STATE_SET)
            return drawableState
        } else {
            return super.onCreateDrawableState(extraSpace)
        }
    }

    companion object {
        private val CRITICAL_STATE_SET = intArrayOf(R.attr.critical)
    }
}
