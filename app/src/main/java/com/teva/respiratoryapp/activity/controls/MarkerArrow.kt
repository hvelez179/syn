package com.teva.respiratoryapp.activity.controls

import android.content.Context
import android.databinding.BindingAdapter
import android.graphics.Outline
import android.graphics.Paint
import android.graphics.Path
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.PathShape
import android.util.AttributeSet
import android.view.View
import android.view.ViewOutlineProvider
import com.teva.respiratoryapp.R


/**
 * Custom control to display a marker arrow that is capable of casting a shadow.
 */
class MarkerArrow @JvmOverloads constructor(context: Context,
                                           attrs: AttributeSet? = null,
                                           defStyleAttr: Int = 0,
                                            defStyleRes: Int = 0)
    : View(context, attrs, defStyleAttr, defStyleRes) {

    /**
     * Indicates whether the arrow is pointing up or down.
     */
    var isUp: Boolean = false
        set(value) {
            field = value
            invalidateOutline()
        }

    private var outlinePath: Path? = null
    private val shapeDrawable = ShapeDrawable()

    init {
        val typedArray = context.obtainStyledAttributes(
                attrs, R.styleable.MarkerArrow, defStyleAttr, defStyleRes)

        try {
            shapeDrawable.paint.color =
                    typedArray.getColor(R.styleable.MarkerArrow_color, 0xffffffff.toInt())
            isUp = typedArray.getBoolean(R.styleable.MarkerArrow_isUp, false)
        } finally {
            typedArray.recycle()
        }

        shapeDrawable.paint.style = Paint.Style.FILL

        outlineProvider = OutlineProvider()
        background = shapeDrawable
    }

    /**
     * Called by the OS when the size of the view changes.
     * @param newWidth The new width of the view.
     * @param newHeight The new height of the view.
     * @param oldWidth The old width of the view.
     * @param oldHeight The old height of the view.
     */
    override fun onSizeChanged(newWidth: Int, newHeight: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(newWidth, newHeight, oldWidth, oldHeight)

        // Rebuild the path used to display an arrow.
        val height = newHeight.toFloat()
        val width = newWidth.toFloat()
        val halfWidth = width/2f

        val path = Path()
        if (isUp) {
            path.moveTo(0f, height)
            path.lineTo(halfWidth, 0f)
            path.lineTo(width, height)
            path.close()
        } else {
            path.moveTo(0f, 0f)
            path.lineTo(width, 0f)
            path.lineTo(halfWidth, height)
            path.close()
        }

        shapeDrawable.shape = PathShape(path, width, height)

        outlinePath = path
        invalidateOutline()
    }

    /**
     * This class provides a triangle outline for the marker arrow's shadow.
     */
    inner class OutlineProvider : ViewOutlineProvider() {
        /**
         * Called to get the provider to populate the Outline.
         * @param view The view building the outline.
         * @param outline The empty outline to be populated.
         */
        override fun getOutline(view: View?, outline: Outline?) {

            outlinePath?.let { outline?.setConvexPath(it) }
        }
    }

    companion object {
        /**
         * Custom binding to convert a boolean value to a visibility.
         *
         * @param view The View to modify.
         * @param isVisible The boolean value to convert.
         */
        @BindingAdapter("visibility")
        @JvmStatic
        fun setVisibility(view: MarkerArrow, isVisible: Boolean) {
            view.visibility = if (isVisible) View.VISIBLE else View.GONE
        }

    }
}