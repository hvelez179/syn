//
// ShadeBar.kt
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

class ShadeBar @JvmOverloads constructor(context: Context,
                                         attrs: AttributeSet? = null,
                                         @AttrRes defStyleAttr: Int = 0,
                                         @StyleRes defStyleRes: Int = 0) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {
    var isOpen: Boolean = false
        set(open) {
            field = open

            visibility = if (this.isOpen) View.VISIBLE else View.GONE
        }

    init {

        val ta = context.obtainStyledAttributes(attrs, R.styleable.ShadeBar, 0, 0)
        try {
            val open = ta.getBoolean(R.styleable.ShadeBar_open, true)

            isOpen = open
        } finally {
            ta.recycle()
        }
    }
}
