//
// DailyReportDecoration.kt
// app
//
// Copyright © 2018 Teva. All rights reserved
//

package com.teva.respiratoryapp.activity.controls

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.support.v7.widget.RecyclerView

/**
 * RecyclerView decoration class for the Daily Report.
 * Draws a vertical line between the inhaler status
 */
class DailyReportDecoration(
        strokeColor: Int,
        strokeWidth: Float,
        private val xOffset: Float,
        private val yOffset: Float) : RecyclerView.ItemDecoration() {

    private val paint: Paint = Paint()
    private var childBounds = Rect()

    init {
        paint.color = strokeColor
        paint.strokeWidth = strokeWidth
    }

    /**
     * Draw any appropriate decorations into the Canvas supplied to the RecyclerView.
     * Any content drawn by this method will be drawn after the item views are drawn
     * and will thus appear over the views.
     *
     * @param canvas Canvas to draw into
     * @param parent RecyclerView this ItemDecoration is drawing into
     * @param state  The current state of recyclerView.
     */
    override fun onDraw(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State?) {
        super.onDraw(canvas, parent, state)

        val layoutManager = parent.layoutManager

        val childCount = parent.childCount
        if (childCount > 1) {

            layoutManager.getDecoratedBoundsWithMargins(parent.getChildAt(0), childBounds)
            val y1 = childBounds.top + yOffset

            layoutManager.getDecoratedBoundsWithMargins(parent.getChildAt(childCount - 1), childBounds)
            val y2 = childBounds.top + yOffset

            canvas.drawLine(xOffset, y1, xOffset, y2, paint)
        }
    }
}
