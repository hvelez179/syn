//
// HeaderDecoration.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.mvvmframework.controls

import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.support.v7.widget.RecyclerView
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.teva.respiratoryapp.BR

import com.teva.respiratoryapp.R
import com.teva.respiratoryapp.mvvmframework.ui.HeaderAdapter

/**
 * This class is a RecyclerView sticky header decoration.
 *
 * @param headerAdapter The header / viewmodel adapter.
 * @param headerLayoutId The layout id to use for the header.
 * @param headerBackgroundId The drawable id of the header background.
 */
class HeaderDecoration<Binding : ViewDataBinding, ViewModel>(
        private val headerAdapter: HeaderAdapter<ViewModel>,
        private val headerLayoutId: Int,
        headerBackgroundColor: Int,
        private val shadowDrawable: Drawable,
        private val shadowHeight: Float
    ) : RecyclerView.ItemDecoration() {

    private val backgroundPaint: Paint = Paint()
    private val headerCache = SparseArray<Binding>()
    private val shadowRect = Rect()

    init {
        backgroundPaint.color = headerBackgroundColor
    }

    /**
     * Draw any appropriate decorations into the Canvas supplied to the RecyclerView.
     * Any content drawn by this method will be drawn after the item views are drawn
     * and will thus appear over the views.
     *
     * @param canvas Canvas to draw into
     * @param parent RecyclerView this ItemDecoration is drawing into
     * @param state  The current state of RecyclerView.
     */
    override fun onDrawOver(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State?) {
        val count = parent.childCount
        var previousId = -1

        for (layoutPos in 0 until count) {
            val child = parent.getChildAt(layoutPos)
            val adapterPos = parent.getChildAdapterPosition(child)

            if (adapterPos != RecyclerView.NO_POSITION) {
                val headerId = headerAdapter.getHeaderId(adapterPos)

                if (headerId != previousId) {
                    previousId = headerId

                    val headerView = getHeaderBinding(parent, adapterPos).root

                    canvas.save()

                    val left = child.left
                    val (top, hasShadow) = getHeaderTop(parent, child, headerView, adapterPos, layoutPos)
                    canvas.translate(left.toFloat(), top.toFloat())

                    headerView.translationX = left.toFloat()
                    headerView.translationY = top.toFloat()
                    headerView.draw(canvas)

                    if (hasShadow) {
                        val shadowTop = top + headerView.measuredHeight
                        shadowRect.set(0, shadowTop,
                                parent.measuredHeight, shadowTop + shadowHeight.toInt())
                        shadowDrawable.bounds = shadowRect
                        shadowDrawable.draw(canvas)
                    }

                    canvas.restore()
                }
            }
        }
    }

    /**
     * Draw any appropriate decorations into the Canvas supplied to the RecyclerView.
     * Any content drawn by this method will be drawn before the item views are drawn,
     * and will thus appear underneath the views.
     *
     * @param canvas      Canvas to draw into
     * @param parent RecyclerView this ItemDecoration is drawing into
     * @param state  The current state of RecyclerView
     */
    override fun onDraw(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State?) {
        val count = parent.childCount
        var previousId = -1

        for (layoutPos in 0 until count) {
            val child = parent.getChildAt(layoutPos)
            val adapterPos = parent.getChildAdapterPosition(child)

            if (adapterPos != RecyclerView.NO_POSITION) {
                val headerId = headerAdapter.getHeaderId(adapterPos)

                if (headerId != previousId) {
                    previousId = headerId

                    val headerView = getHeaderBinding(parent, adapterPos).root

                    val left = child.left
                    val (top, _) = getHeaderTop(parent, child, headerView, adapterPos, layoutPos)

                    canvas.drawRect(left.toFloat(), top.toFloat(), parent.measuredWidth.toFloat(), (top + headerView.measuredHeight).toFloat(), backgroundPaint)
                }
            }
        }
    }

    /**
     * Tuple class used to return results from getHeaderTop()
     */
    private data class HeaderTopResult(
            val offset: Int,
            val hasShadow: Boolean)

    /**
     * This method computes the Y position of a header.
     *
     * @param parent     The parent RecyclerView
     * @param child      The child view that the header is associated with
     * @param headerView The header view
     * @param adapterPos The position of the item in the adapter list
     * @param layoutPos  The position of the item in the container's children
     * @return The Y position of the header
     */
    private fun getHeaderTop(parent: RecyclerView, child: View, headerView: View, adapterPos: Int,
                             layoutPos: Int): HeaderTopResult {
        val headerHeight = headerView.height
        var top = child.y.toInt() - headerHeight

        var hasShadow = top < 0

        top = Math.max(0, top)

        if (layoutPos == 0) {

            // this is the first item, so check to see if it's being pushed off.
            val count = parent.childCount
            val currentHeaderId = headerAdapter.getHeaderId(adapterPos)

            for (nextIndex in 1 until count) {
                val nextAdapterPos = parent.getChildAdapterPosition(parent.getChildAt(nextIndex))
                if (nextAdapterPos != RecyclerView.NO_POSITION) {
                    val nextHeaderId = headerAdapter.getHeaderId(nextAdapterPos)
                    if (nextHeaderId != currentHeaderId) {
                        val nextView = parent.getChildAt(nextIndex)
                        val nextHeader = getHeaderBinding(parent, nextAdapterPos).root

                        val offset = nextView.y.toInt() - (headerHeight + nextHeader.height)

                        top = Math.min(0, offset)
                        if (top < 0) {
                            hasShadow = false
                        }
                        break
                    }
                }
            }

        }

        return HeaderTopResult(top, hasShadow)
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
        val position = parent.getChildAdapterPosition(view)
        var height = 0

        if (position != RecyclerView.NO_POSITION && (position == 0 || headerAdapter.getHeaderId(position - 1) != headerAdapter
                .getHeaderId(position))) {
            val binding = getHeaderBinding(parent, position)
            height = binding.root.height
        }

        outRect.set(0, height, 0, 0)
    }

    /**
     * Gets a data binding object for a header from the cache or creates a new one.
     *
     * @param parent   The parent RecyclerView
     * @param position The adapter position of the item
     * @return A data binding for the header.
     */
    private fun getHeaderBinding(parent: RecyclerView, position: Int): Binding {
        val headerId = headerAdapter.getHeaderId(position)

        var binding: Binding? = headerCache.get(headerId)
        if (binding == null) {
            // create a binding and view for the header.
            val inflater = LayoutInflater.from(parent.context)
            binding = DataBindingUtil.inflate<Binding>(inflater, headerLayoutId, parent, false)

            val headerViewModel = headerAdapter.getHeaderViewModel(position)
            binding!!.setVariable(BR.viewmodel, headerViewModel)
            binding.executePendingBindings()

            // create the ViewHolder and store in cache.
            headerCache.put(headerId, binding)

            val headerView = binding.root

            val offset = parent.resources.getDimensionPixelOffset(R.dimen.recycler_divider_margin)

            // measure and layout header view
            val widthSpec = View.MeasureSpec.makeMeasureSpec(parent.measuredWidth, View
                    .MeasureSpec.EXACTLY)
            val heightSpec = View.MeasureSpec.makeMeasureSpec(parent.measuredHeight, View
                    .MeasureSpec.UNSPECIFIED)

            val childWidth = ViewGroup.getChildMeasureSpec(widthSpec,
                    parent.paddingLeft + parent.paddingRight + offset, headerView
                    .layoutParams.width)
            val childHeight = ViewGroup.getChildMeasureSpec(heightSpec,
                    parent.paddingTop + parent.paddingBottom, headerView
                    .layoutParams.height)

            headerView.measure(childWidth, childHeight)
            headerView.layout(0, 0, headerView.measuredWidth, headerView.measuredHeight)
        }

        return binding
    }
}
