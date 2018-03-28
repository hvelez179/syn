//
// InputField
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.activity.controls

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.support.annotation.AttrRes
import android.support.annotation.StyleRes
import android.support.v4.view.ViewCompat
import android.text.InputType
import android.text.TextPaint
import android.text.method.DigitsKeyListener
import android.text.method.KeyListener
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import com.teva.respiratoryapp.R

/**
 * Custom EditText class used for text input fields
 */
class InputField @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        @AttrRes defStyleAttr: Int = R.styleable.CustomTheme_inputFieldStyle,
        @StyleRes defStyleRes: Int = R.style.input_field_style)
    : EditText(context, attrs, defStyleAttr, defStyleRes) {

    /**
     * Indicates whether the hint should be shown when the InputField is not empty.
     */
    var showPatternHint = false

    private val textRect = Rect()
    private val hintPaint = TextPaint()

    /**
     * A value indicating whether the widget should be displayed in the inError state.
     */
    var isInError: Boolean = false
        set(value) {
            if (field != value) {
                field = value

                refreshDrawableState()
            }
        }

    init {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.InputField, 0, 0)
        try {
            isInError = ta.getBoolean(R.styleable.InputField_inError, false)
        } finally {
            ta.recycle()
        }
    }

    /**
     * Event handler for touch events.
     *
     * @param event The touch event that occurred.
     */
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if(event?.action == MotionEvent.ACTION_UP) {
            val hotspotWidth = resources.getDimension(R.dimen.clear_text_field_hotspot_width)
            val isRTL = ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL
            val x = event.x
            if ((!isRTL && (x > width - hotspotWidth)) || (isRTL && (x < hotspotWidth))) {
                setText("")
            }
        }

        return super.onTouchEvent(event)
    }

    /**
     * Updates the drawable states of the control.
     *
     * @param extraSpace The number of drawable state space needed by the base class
     * @return The drawable state of the control.
     */
    override fun onCreateDrawableState(extraSpace: Int): IntArray {
        val states = when {
            isInError && !text.isNullOrEmpty() -> BOTH_STATE_SET
            isInError -> ERROR_STATE_SET
            !text.isNullOrEmpty() -> CAN_CLEAR_STATE_SET
            else -> null
        }

        if (states != null) {
            val drawableState = super.onCreateDrawableState(extraSpace + states.size)

            View.mergeDrawableStates(drawableState, states)
            return drawableState
        } else {
            return super.onCreateDrawableState(extraSpace)
        }
    }

    /**
     * Called when the text changes.
     *
     * @param text The new text
     * @param start The index where the text was changed
     * @param lengthBefore The length of the changed text before the change.
     * @param lengthAfter The length of the changed text after the change.
     */
    override fun onTextChanged(text: CharSequence?, start: Int, lengthBefore: Int, lengthAfter: Int) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter)

        refreshDrawableState()
    }

    /**
     * Draw any foreground content for this view.

     *
     * Foreground content may consist of scroll bars, a [foreground][.setForeground]
     * drawable or other view-specific decorations. The foreground is drawn on top of the
     * primary view content.

     * @param canvas canvas to draw into
     */
    override fun onDrawForeground(canvas: Canvas?) {
        super.onDrawForeground(canvas)

        if ( showPatternHint &&
                !text.isNullOrEmpty() &&
                !hint.isNullOrEmpty() &&
                (text.length < hint.length)) {
            val str = hint.substring(text.length, hint.length)

            hintPaint.set(paint)
            hintPaint.color = currentHintTextColor

            // getTextBounds doesn't measure trailing whitespace, so measure a '|'
            // character, append it to the string when measuring and then subtract it's size.
            hintPaint.getTextBounds(TRAILING_WHITESPACE_MARKER, 0, 1, textRect);
            val widthMarker = textRect.width()
            hintPaint.getTextBounds(text.toString() + TRAILING_WHITESPACE_MARKER,
                    0, text.length+1, textRect)

            val isRTL = ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL
            if (isRTL) {
                // TODO: Is this correct?
                canvas?.drawText(str,
                        (textRect.left + widthMarker).toFloat(),
                        baseline.toFloat(), hintPaint)
            } else {
                canvas?.drawText(str,
                        (textRect.right - widthMarker).toFloat(),
                        baseline.toFloat(), hintPaint)
            }
        }
    }

    companion object {
        private val ERROR_STATE_SET = intArrayOf(R.attr.inError)
        private val CAN_CLEAR_STATE_SET = intArrayOf(R.attr.canClear)
        private val BOTH_STATE_SET = intArrayOf(R.attr.inError, R.attr.canClear)
        private val TRAILING_WHITESPACE_MARKER = "|"
    }
}
