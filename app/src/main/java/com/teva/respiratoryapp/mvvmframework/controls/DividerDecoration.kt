//
// DividerDecoration.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.mvvmframework.controls


import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.support.v7.widget.RecyclerView
import android.view.View

/**
 * This class is a RecyclerView ItemDecoration that draws dividers between
 * items in a RecyclerView.
 *
 * @param divider The drawable used to draw the divider
 * @param marginDimension The d
 */
class DividerDecoration(private val divider: Drawable,
                        private val marginDimension: Int = 0,
                        private val showDividerAtTop: Boolean = false,
                        private val showDividerAtBottom: Boolean = false)
    : RecyclerView.ItemDecoration() {

    /**
     * Draw any appropriate decorations into the Canvas supplied to the RecyclerView.
     * Any content drawn by this method will be drawn after the item views are drawn
     * and will thus appear over the views.
     *
     * @param canvas Canvas to draw into
     * @param parent RecyclerView this ItemDecoration is drawing into
     * @param state  The current state of ecyclerView.
     */
    override fun onDrawOver(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State?) {
        super.onDrawOver(canvas, parent, state)

        val layoutManager = parent.layoutManager

        var margin = 0
        if (marginDimension != 0) {
            margin = parent.resources.getDimension(marginDimension).toInt()
        }

        val left = margin + parent.paddingLeft
        val right = parent.width - (margin + parent.paddingRight)

        val childCount = parent.childCount
        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)
            var bottom = layoutManager.getDecoratedBottom(child)
            var top = bottom - divider.intrinsicHeight
            divider.setBounds(left, top, right, bottom)
            divider.draw(canvas)

            // draw top divider for first item.
            if (i == 0 && showDividerAtTop) {
                top = layoutManager.getDecoratedTop(child)
                bottom = top + divider.intrinsicHeight

                divider.setBounds(left, top, right, bottom)
                divider.draw(canvas)
            }
        }
    }

    /**
     * Retrieve any offsets for the given item. Each field of `outRect` specifies
     * the number of pixels that the item view should be inset by, similar to padding or margin.
     * The default implementation sets the bounds of outRect to 0 and returns.
     *
     * @param outRect Rect to receive the output.
     * @param view    The child view to decorate
     * @param parent  RecyclerView this ItemDecoration is decorating
     * @param state   The current state of RecyclerView.
     */
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State?) {
        outRect.set(0, 0, 0, divider.intrinsicHeight)
    }
}
