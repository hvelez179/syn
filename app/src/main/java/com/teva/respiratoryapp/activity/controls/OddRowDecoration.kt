//
// OddRowDecoration.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.activity.controls


import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.support.v7.widget.RecyclerView

/**
 * This class is a RecyclerView ItemDecoration that draws dividers between
 * items in a RecyclerView.
 *
 * @param divider The drawable used to draw the divider
 * @param marginDimension The d
 */
class OddRowDecoration(oddRowBackgroundColor: Int)
    : RecyclerView.ItemDecoration() {

    private val oddRowPaint: Paint = Paint()
    private var childBounds = Rect()

    init {
        oddRowPaint.color = oddRowBackgroundColor
    }

    /**
     * Draw any appropriate decorations into the Canvas supplied to the RecyclerView.
     * Any content drawn by this method will be drawn after the item views are drawn
     * and will thus appear over the views.
     *
     * @param canvas Canvas to draw into
     * @param parent RecyclerView this ItemDecoration is drawing into
     * @param state  The current state of ecyclerView.
     */
    override fun onDraw(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State?) {
        super.onDraw(canvas, parent, state)

        val layoutManager = parent.layoutManager

        val left = parent.paddingLeft
        val right = parent.width - parent.paddingRight

        val childCount = parent.childCount
        for (i in 1 until childCount step 2) {
            val child = parent.getChildAt(i)
            layoutManager.getDecoratedBoundsWithMargins(child, childBounds)
            canvas.drawRect(childBounds, oddRowPaint)
        }
    }


}
