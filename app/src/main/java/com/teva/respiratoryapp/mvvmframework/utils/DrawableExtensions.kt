//
//
// DrawableExtensions.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//
//

package com.teva.respiratoryapp.mvvmframework.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.opengl.ETC1.getHeight
import android.opengl.ETC1.getWidth
import android.provider.MediaStore.Images.Media.getBitmap
import android.graphics.drawable.BitmapDrawable



fun Drawable.toBitmap(): Bitmap {
    if (this is BitmapDrawable) {
        if (bitmap != null) {
            return bitmap
        }
    }

    val result = if (intrinsicWidth <= 0 || intrinsicHeight <= 0) {
        Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
    } else {
        Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
    }

    val canvas = Canvas(result)
    setBounds(0, 0, canvas.width, canvas.height)
    draw(canvas)
    return result
}