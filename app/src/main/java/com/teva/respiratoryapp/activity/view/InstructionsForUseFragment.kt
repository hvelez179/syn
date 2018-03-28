//
// InstructionsForUseFragment.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.activity.view

import android.content.Context
import android.os.Bundle
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.teva.common.services.analytics.enumerations.AnalyticsScreen
import com.teva.respiratoryapp.R
import com.teva.respiratoryapp.activity.viewmodel.instructionsforuse.InstructionsForUseViewModel
import com.teva.respiratoryapp.databinding.InstructionsForUseFragmentBinding
import com.teva.respiratoryapp.mvvmframework.ui.BaseFragment
import android.support.v4.view.ViewPager.OnPageChangeListener



/**
 * This class provides the user interface for the "Instructions For Use" screen.
 */
class InstructionsForUseFragment
    : BaseFragment<InstructionsForUseFragmentBinding, InstructionsForUseViewModel>(R.layout.instructions_for_use_fragment) {

    init {
        screen = AnalyticsScreen.InstructionsForUse()
    }

    // the list of layout ids for each of the pages in the IFU screen.
    private val ifuPageLayoutIds = arrayListOf(
            R.layout.instructions_for_use_page1,
            R.layout.instructions_for_use_page2,
            R.layout.instructions_for_use_page3,
            R.layout.instructions_for_use_page4,
            R.layout.instructions_for_use_page5)

    private var viewPager: ViewPager? = null

    /**
     * Sets the ViewModel for the fragment.
     *
     * @param fragmentArguments The fragment arguments.
     */
    override fun inject(fragmentArguments: Bundle?) {
        viewModel = InstructionsForUseViewModel(dependencyProvider!!)
    }


    /**
     * Implemented by derived fragments to configure the properties of the fragment.
     */
    override fun configureFragment() {
        super.configureFragment()
        toolbarTitle = localizationService!!.getString(R.string.menuInstructionsForUseTitle_text)
    }

    /**
     * This method sets up the pager for scrolling between the report pages.
     *
     * @param view               - the view being created.
     * @param savedInstanceState - the saved instance state.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewPager = findViewById(R.id.pager) as ViewPager?
        viewPager?.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                viewModel?.currentPage = position
            }
        })
        viewPager!!.adapter = InstructionsForUsePagerAdapter(this.context!!)
    }

    /**
     * This class supports traversing between the user report pages.
     */
    private inner class InstructionsForUsePagerAdapter internal constructor(private val context: Context) : PagerAdapter() {

        /**
         * This method instantiates the page at the specified position.
         */
        override fun instantiateItem(collection: ViewGroup, position: Int): Object {
            val inflater = LayoutInflater.from(context)
            val layoutId = ifuPageLayoutIds[position]

            val view = inflater.inflate(layoutId, collection, false)
            collection.addView(view)
            return view as Object
        }

        /**
         * This method cleans up the page at the specified position.
         */
        override fun destroyItem(collection: ViewGroup, position: Int, view: Any) {
            collection.removeView(view as View)
        }

        /**
         * This method returns the number of pages.
         */
        override fun getCount(): Int {
            return ifuPageLayoutIds.size
        }

        /**
         * This method determines whether a page View is associated with a specific object returned by instantiate item.
         */
        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view === `object`
        }

        /**
         * This method returns the title of a page.
         */
        override fun getPageTitle(position: Int): CharSequence {
            return ""
        }
    }

}
