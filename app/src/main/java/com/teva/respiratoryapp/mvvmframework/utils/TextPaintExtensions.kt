//
// TextPaintExtensions.kt
// app
//
// Copyright © 2018 Teva. All rights reserved
//

package com.teva.respiratoryapp.mvvmframework.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.text.TextPaint

/**
 * Loads a style resource with [styleResourceId] from the resources of [context] and
 * applies the text appearance styles to a TextPaint object.
 */
@SuppressLint("WrongConstant")
fun TextPaint.applyAppearance(context: Context, styleResourceId: Int) {
    val styleable = arrayOf(
            android.R.attr.textSize,
            android.R.attr.textColor,
            android.R.attr.fontFamily,
            android.R.attr.textStyle)

    styleable.sort()

    val theme = context.theme
    val appearance = theme.obtainStyledAttributes(styleResourceId, styleable.toIntArray())
    if (appearance != null) {
        try {
            var fontFamily: String? = null
            var textStyle = 0

            for(index in 0 until appearance.indexCount) {
                val attrIndex = appearance.getIndex(index)
                val attr = styleable[attrIndex]
                when (attr) {
                    android.R.attr.textColor -> color = appearance.getColor(attrIndex, 0)
                    android.R.attr.textSize -> textSize = appearance.getDimension(attrIndex, 0f)
                    android.R.attr.fontFamily -> fontFamily = appearance.getString(attrIndex)
                    android.R.attr.textStyle -> textStyle = appearance.getInt(attrIndex, 0)
                }
            }

            typeface = Typeface.create(fontFamily, textStyle)
        } finally {
            appearance.recycle()
        }
    }
}
