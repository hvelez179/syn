//
// BlurExtensions.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.mvvmframework.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.view.View

/**
 * Creates a blurred bitmap for the view.
 *
 * @param scaledWidth The width of the scaled image used as the blur source.
 * @param radius The blur radius
 * @return A blurred image of the view.
 */
fun View.blur(scaledWidth: Int = 200, radius: Float = 5f): Bitmap?
{
    if (measuredWidth == 0 || measuredHeight == 0) {
        return null
    }

    val scaledHeight = (scaledWidth * (measuredHeight.toFloat() / measuredWidth)).toInt()

    val srcBitmap = Bitmap.createBitmap(scaledWidth, scaledHeight, Bitmap.Config.ARGB_8888)
    val blurBitmap = Bitmap.createBitmap(scaledWidth, scaledHeight, Bitmap.Config.ARGB_8888)

    val canvas = Canvas(srcBitmap)
    val scale = scaledWidth.toFloat() / measuredWidth
    canvas.scale(scale, scale)

    draw(canvas)

    val renderScript = RenderScript.create(context)
    val intrinsic = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript))

    val tmpIn: Allocation = Allocation.createFromBitmap(renderScript, srcBitmap)
    val tmpOut: Allocation = Allocation.createFromBitmap(renderScript, blurBitmap)

    intrinsic.setInput(tmpIn)
    intrinsic.setRadius(radius)
    intrinsic.forEach(tmpOut)
    tmpOut.copyTo(blurBitmap)

    return blurBitmap
}

/**
 * Creates a blurred bitmap from the source bitmap.
 *
 * @param context The application or view context
 * @param radius The blur radius
 * @return A blurred image of the source bitmap.
 */
fun Bitmap.blur(context: Context, radius: Float = 10f): Bitmap {
    val blurBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

    val renderScript = RenderScript.create(context)
    val intrinsic = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript))

    val tmpIn: Allocation = Allocation.createFromBitmap(renderScript, this)
    val tmpOut: Allocation = Allocation.createFromBitmap(renderScript, blurBitmap)

    intrinsic.setInput(tmpIn)
    intrinsic.setRadius(radius)
    intrinsic.forEach(tmpOut)
    tmpOut.copyTo(blurBitmap)

    return blurBitmap
}
