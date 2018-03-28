//
// LoadingIndicator.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.activity.controls


import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.drawable.AnimationDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.teva.respiratoryapp.R

/**
 * Custom control that implements a loading_indicator indicator.
 *
 * @param context The activity that created the control.
 * @param attrs The control attributes from the layout resource.
 * @param defStyleAttr The default style attribute id.
 * @param defStyleRes The default style resource id.
 */
class LoadingIndicator @JvmOverloads constructor(context: Context,
                                                 attrs: AttributeSet? = null,
                                                 defStyleAttr: Int = 0,
                                                 defStyleRes: Int = 0)
    : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    private val view: View
    private val graphic: View
    private val label: TextView
    private val revealBehavior: SweepRevealTextViewBehavior
    private val animationDrawable: AnimationDrawable?
    private var isShowing = false

    private var animatorSet: AnimatorSet? = null

    init {
        view = LayoutInflater.from(context).inflate(R.layout.loading_indicator, this, false)
        graphic = view.findViewById(R.id.graphic)
        label = view.findViewById(R.id.label)
        revealBehavior = SweepRevealTextViewBehavior(label, resources.getDimension(R.dimen.sweep_fade_width))
        animationDrawable = graphic.background as? AnimationDrawable

        view.alpha = 0f
        addView(view)

    }

    /**
     * Starts the animation to reveal the indicator.
     */
    fun show() {
        if (!isShowing) {
            isShowing = true

            graphic.alpha = 1f
            label.alpha = 1f
            revealBehavior.fraction = 0f
            animationDrawable?.selectDrawable(0)
            animationDrawable?.setVisible(false, true);

            val fadeInAnimator = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f)
            fadeInAnimator.duration = fadeInDuration

            val labelRevealAnimator = ObjectAnimator.ofFloat(revealBehavior, "fraction", 0f, 1f)
            labelRevealAnimator.duration = labelRevealDuration
            labelRevealAnimator.startDelay = postFadeInDelay

            labelRevealAnimator.addListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(p0: Animator?) {
                }

                override fun onAnimationEnd(p0: Animator?) {
                }

                override fun onAnimationCancel(p0: Animator?) {
                }

                override fun onAnimationStart(p0: Animator?) {
                    animationDrawable?.start()
                    animationDrawable?.setVisible(true, true);
                }

            })

            animatorSet = AnimatorSet()
            animatorSet?.playTogether(fadeInAnimator, labelRevealAnimator)
            animatorSet?.start()
        }
    }

    /**
     * Starts the animation to hide the indicator.
     */
    fun hide() {
        if (isShowing) {
            isShowing = false

            val fadeOutAnimator = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f)
            fadeOutAnimator.duration = fadeOutDuration
            fadeOutAnimator.startDelay = fadeOutOffset

            val graphicFadeOutAnimator = ObjectAnimator.ofFloat(graphic, "alpha", 1f, 0f)
            graphicFadeOutAnimator.duration = internalFadeOutDuration
            val labelFadeOutAnimator = ObjectAnimator.ofFloat(label, "alpha", 1f, 0f)
            labelFadeOutAnimator.duration = internalFadeOutDuration


            animatorSet = AnimatorSet()
            animatorSet?.playTogether(fadeOutAnimator, graphicFadeOutAnimator, labelFadeOutAnimator)
            animatorSet?.addListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(p0: Animator?) {
                }

                override fun onAnimationCancel(p0: Animator?) {
                }

                override fun onAnimationStart(p0: Animator?) {
                }

                override fun onAnimationEnd(p0: Animator?) {
                    animationDrawable?.setVisible(false, true)
                    animationDrawable?.stop()
                }
            })

            animatorSet?.start()
        }
    }

    companion object {
        val fadeInDuration: Long = 250
        val postFadeInDelay: Long = 500
        val labelRevealDuration: Long = 750
        val internalFadeOutDuration: Long = 500
        val fadeOutDuration: Long = 250
        val fadeOutOffset: Long = 500
    }
}