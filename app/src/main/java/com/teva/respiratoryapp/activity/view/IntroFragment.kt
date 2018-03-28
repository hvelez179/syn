//
//
// IntroFragment.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//
//

package com.teva.respiratoryapp.activity.view

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.databinding.ViewDataBinding
import android.os.Bundle
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import com.teva.common.services.analytics.enumerations.AnalyticsScreen
import com.teva.respiratoryapp.R
import com.teva.respiratoryapp.activity.viewmodel.dashboard.IntroViewModel
import com.teva.respiratoryapp.databinding.IntroFragmentBinding
import com.teva.respiratoryapp.databinding.IntroPage1Binding
import com.teva.respiratoryapp.databinding.IntroPage2Binding
import com.teva.respiratoryapp.databinding.IntroPage3Binding
import com.teva.respiratoryapp.mvvmframework.controls.BlurImage
import com.teva.respiratoryapp.mvvmframework.controls.InsetsForwarder
import com.teva.respiratoryapp.mvvmframework.controls.attachInsetsForwarder
import com.teva.respiratoryapp.mvvmframework.ui.BaseFragment

/**
 * This class represents the Value Proposition Intro screen.
 */
class IntroFragment : BaseFragment<IntroFragmentBinding, IntroViewModel>(R.layout.intro_fragment) {

    private var viewPager: ViewPager? = null
    private lateinit var pages: ArrayList<ViewDataBinding>
    private var animate = false

    init {
        screen = AnalyticsScreen.Introduction()
    }

    /**
     * Android lifecycle method called to create the fragment's view
     *
     * @param inflater           The view inflater for the fragment.
     * @param container          The container that the view will be added to.
     * @param savedInstanceState The saved state of the fragment.
     * @return The view for the fragment.
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val rootView = super.onCreateView(inflater, container, savedInstanceState)

        viewPager = rootView.findViewById(R.id.view_pager)

        viewPager?.attachInsetsForwarder()

        pages = arrayListOf(
                IntroPage1Binding.inflate(inflater, viewPager, false),
                IntroPage2Binding.inflate(inflater, viewPager, false),
                IntroPage3Binding.inflate(inflater, viewPager, false))

        viewPager?.adapter = IntroAdapter()

        if (animate) {
            // first load, not reconstituted from saved instance.
            performSplashTransition(rootView)
        } else {
            bypassSplashTransition(rootView)
        }

        return rootView
    }

    /**
     * Implemented by derived fragments to configure the properties of the fragment.
     */
    override fun configureFragment() {
        super.configureFragment()

        setSaveViewModelState(true)
    }

    /**
     * Android lifecycle method called when the fragment is created.
     *
     * @param savedInstanceState The saved state of the fragment.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        animate = savedInstanceState == null
    }

    /**
     * Displays the view animations that provide the splash screen transition.
     */
    private fun performSplashTransition(rootView: View) {

        val titleOffset = resources.getDimension(R.dimen.intro_animation_title_offset)
        val descOffset = resources.getDimension(R.dimen.intro_animation_desc_offset)
        val textDuration = resources.getInteger(R.integer.intro_animation_text_duration).toLong()
        val textStart = resources.getInteger(R.integer.intro_animation_text_start).toLong()
        val blurDuration = resources.getInteger(R.integer.intro_animation_blur_duration).toLong()

        val page1 = pages[0].root

        val titleAnimator = ObjectAnimator.ofPropertyValuesHolder(page1.findViewById<View>(R.id.title),
                PropertyValuesHolder.ofFloat("translationX", titleOffset, 0f),
                PropertyValuesHolder.ofFloat("alpha", 0f, 1f))
        titleAnimator.duration = textDuration
        titleAnimator.startDelay = textStart

        val descAnimator = ObjectAnimator.ofPropertyValuesHolder(page1.findViewById<View>(R.id.desc),
                PropertyValuesHolder.ofFloat("translationX", descOffset, 0f),
                PropertyValuesHolder.ofFloat("alpha", 0f, 1f))
        descAnimator.duration = textDuration
        descAnimator.startDelay = textStart

        val pageIndicatorAnimator = ObjectAnimator.ofPropertyValuesHolder(page1.findViewById<View>(R.id.page_indicator),
                PropertyValuesHolder.ofFloat("translationX", descOffset, 0f),
                PropertyValuesHolder.ofFloat("alpha", 0f, 1f))
        pageIndicatorAnimator.duration = textDuration
        pageIndicatorAnimator.startDelay = textStart

        val blurAnimator = ObjectAnimator.ofFloat(page1.findViewById(R.id.background), "blur", 1f, 0f)
        blurAnimator.duration = blurDuration

        val nextAnimator = ObjectAnimator.ofFloat(rootView.findViewById(R.id.next), "alpha", 0f, 1f)
        nextAnimator.interpolator = AccelerateInterpolator(3f)
        nextAnimator.duration = textDuration
        nextAnimator.startDelay = textStart

        val animator = AnimatorSet()
        animator.playTogether(blurAnimator, titleAnimator, descAnimator, nextAnimator)
        animator.start()
    }

    /**
     * Skips the splash transition animations and shows the views on
     * value proposition page 1.
     */
    private fun bypassSplashTransition(rootView: View) {
        val page1 = pages[0].root

        page1.findViewById<View>(R.id.background)?.alpha = 1f
        page1.findViewById<View>(R.id.title)?.alpha = 1f
        page1.findViewById<View>(R.id.desc)?.alpha = 1f
        rootView.findViewById<View>(R.id.next)?.alpha = 1f

        val background = page1.findViewById<BlurImage>(R.id.background);
        if (background is BlurImage) {
            background.blur = 0f
        }
    }

    /**
     * Sets the ViewModel for the fragment.
     *
     * @param fragmentArguments The fragment arguments.
     */
    override fun inject(fragmentArguments: Bundle?) {
        viewModel = IntroViewModel(dependencyProvider!!)
    }

    /**
     * Pager adapter for displaying the value proposition pages.
     */
    private inner class IntroAdapter : PagerAdapter() {

        /**
         * Create the page for the given position.  The adapter is responsible
         * for adding the view to the container given here, although it only
         * must ensure this is done by the time it returns from
         * [.finishUpdate].
         *
         * @param container The containing View in which the page will be shown.
         * @param position The page position to be instantiated.
         * @return Returns an Object representing the new page.  This does not
         * need to be a View, but can be some other container of the page.
         */
        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val view = pages[position].root
            container.addView(view)
            return view
        }

        /**
         * Remove a page for the given position.  The adapter is responsible
         * for removing the view from its container, although it only must ensure
         * this is done by the time it returns from [.finishUpdate].
         *
         * @param container The containing View from which the page will be removed.
         * @param position The page position to be removed.
         * @param view The same object that was returned by
         * [.instantiateItem].
         */
        override fun destroyItem(container: ViewGroup, position: Int, view: Any) {
            container.removeView(view as View)
        }

        /**
         * Determines whether a page View is associated with a specific key object
         * as returned by [.instantiateItem]. This method is
         * required for a PagerAdapter to function properly.
         *
         * @param view Page View to check for association with `object`
         * @param obj Object to check for association with `view`
         * @return true if `view` is associated with the key object `object`
         */
        override fun isViewFromObject(view: View, obj: Any): Boolean = (view == obj)

        /**
         * Return the number of views available.
         */
        override fun getCount(): Int = pages.size

    }
}