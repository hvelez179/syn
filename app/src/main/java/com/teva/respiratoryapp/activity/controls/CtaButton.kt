///
//
// CtaButton.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//
///

package com.teva.respiratoryapp.activity.controls

import android.animation.AnimatorInflater
import android.content.Context
import android.graphics.Outline
import android.util.AttributeSet
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.Button
import com.teva.respiratoryapp.R

/**
 * A custom button for the CTA button style that sets the state list animator and
 * the shadow outline provider.
 */
class CtaButton @JvmOverloads constructor(context: Context,
                                          attrs: AttributeSet? = null,
                                          defStyleAttr: Int = R.styleable.CustomTheme_ctaButtonStyle,
                                          defStyleRes: Int = R.style.cta_button)
    : Button(context, attrs, defStyleAttr, defStyleRes) {

    init {
        val buttonColor: String?
        val outline: String?

        val typedArray = context.obtainStyledAttributes(
                attrs, R.styleable.CtaButton, defStyleAttr, defStyleRes)
        try {
            buttonColor = if (typedArray.hasValue(R.styleable.CtaButton_animator)) {
                typedArray.getString(R.styleable.CtaButton_animator)
            } else {
                null
            }

            outline = if (typedArray.hasValue(R.styleable.CtaButton_outline)) {
                typedArray.getString(R.styleable.CtaButton_outline)
            } else {
                null
            }

        } finally {
            typedArray.recycle()
        }

        if (!isInEditMode) {

            stateListAnimator = when (buttonColor) {
                WHITE_ANIMATION -> AnimatorInflater.loadStateListAnimator(context, R.animator.cta_button_white_animator)
                BLUE_ANIMATION -> AnimatorInflater.loadStateListAnimator(context, R.animator.cta_button_blue_animator)
                else -> null
            }

            outlineProvider = when (outline) {
                CAPSULE_OUTLINE -> CapsuleOutlineProvider()
                else -> null
            }
        } else {
            stateListAnimator = null
        }
    }

    /**
     * An outline provider used to display a shadow that is narrower than the button.
     */
    class CapsuleOutlineProvider : ViewOutlineProvider() {

        /**
         * Creates an outline for the view.
         * @param view The view to provide an outline for.
         * @param outline The outline object to be defined.
         */
        override fun getOutline(view: View, outline: Outline) {
            val width = view.measuredWidth
            val height = view.measuredHeight
            val padding = view.resources.getDimensionPixelOffset(R.dimen.cta_shadow_padding)
            outline.setRoundRect(padding, 0, width - padding, height, height.toFloat())
        }
    }

    companion object {
        val CAPSULE_OUTLINE = "capsule"
        val BLUE_ANIMATION = "blue"
        val WHITE_ANIMATION = "white"
        val RED_ANIMATION = "red"
        val GREEN_ANIMATION = "green"

    }

}