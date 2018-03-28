//
// ReportFragment.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.activity.view.tracker


import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.teva.common.services.analytics.enumerations.AnalyticsScreen
import com.teva.respiratoryapp.BR
import com.teva.respiratoryapp.R
import com.teva.respiratoryapp.UserReportContentProvider
import com.teva.respiratoryapp.activity.viewmodel.tracker.ReportViewModel
import com.teva.respiratoryapp.databinding.ReportFragmentBinding
import com.teva.respiratoryapp.databinding.UserReportPrintableBinding
import com.teva.respiratoryapp.mvvmframework.ui.BaseFragment
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

/**
 * Default Constructor
 */
class ReportFragment : BaseFragment<ReportFragmentBinding, ReportViewModel>(R.layout.report_fragment) {

    private var viewPager: ViewPager? = null
    private val reportPageLayoutIds = ArrayList(
            Arrays.asList(
                    R.layout.user_report_description_fragment,
                    R.layout.user_report_daily_summary_fragment,
                    R.layout.user_report_weekly_summary_fragment,
                    R.layout.user_report_dsa_fragment
            ))
    private var printableView: View? = null

    init {
        screen = AnalyticsScreen.UserReport()
    }

    /**
     * Implemented by derived fragments to configure the properties of the fragment.
     */
    override fun configureFragment() {
        super.configureFragment()
        menuId = R.menu.user_report_menu
        toolbarTitle = localizationService!!.getString(R.string.hcpReportTitle_text)
    }

    /**
     * Initializes the toolbar properties.
     */
    override fun initToolbar(rootView: View) {
        super.initToolbar(rootView)
        toolbar!!.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.menu_item_share) {
                createPdfForPrintableView()

                val shareIntent = Intent()
                shareIntent.action = Intent.ACTION_SEND
                shareIntent.type = UserReportContentProvider.USER_REPORT_MIME_TYPE
                shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(UserReportContentProvider.USER_REPORT_URI))

                startActivity(Intent.createChooser(shareIntent, resources.getString(R.string.user_report_share_dialog_title_text)))
            }
            true
        }
    }

    /**
     * This method converts the readable user report into a pdf.
     */
    private fun createPdfForPrintableView() {

        val pdfWidth = resources.getDimension(R.dimen.user_report_printable_document_width)
        val pdfHeight = resources.getDimension(R.dimen.user_report_printable_document_height)
        val userReportPrintableBinding = DataBindingUtil.inflate<UserReportPrintableBinding>(LayoutInflater.from(context), R.layout.user_report_printable, null, false)

        if (userReportPrintableBinding != null) {
            userReportPrintableBinding.setVariable(BR.viewmodel, viewModel)
            userReportPrintableBinding.executePendingBindings()
            printableView = userReportPrintableBinding.root
            printableView!!.measure((pdfWidth ).toInt(), (pdfHeight ).toInt())
            printableView!!.layout(0, 0, (pdfWidth ).toInt(), (pdfHeight ).toInt())
        }

        // create a new document
        val document = PdfDocument()

        // create a page description
        val pageInfo = PdfDocument.PageInfo.Builder(printableView!!.width, printableView!!.height, 1).create()

        // start a page
        val page = document.startPage(pageInfo)

        // draw the printable view the page
        printableView!!.draw(page.canvas)

        // finish the page
        document.finishPage(page)

        try {
            val file = File(context?.filesDir?.absolutePath, UserReportContentProvider.USER_REPORT_FILE_NAME)
            val output = FileOutputStream(file)
            document.writeTo(output)
            document.close()
            output.close()
        } catch (exception: IOException) {
            Log.e(TAG, "createPdfForPrintableView: ", exception)
        }

    }

    /**
     * This method sets up the pager for scrolling between the report pages.

     * @param view               - the view being created.
     * *
     * @param savedInstanceState - the saved instance state.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewPager = findViewById(R.id.pager) as ViewPager?
        viewPager!!.adapter = ReportPagerAdapter(this.context!!)
    }

    /**
     * Gets the screen orientation for the fragment.
     */
    override val orientation: Int
        get() = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE

    /**
     * Gets a value indicating whether the status bar and navigation bar should be hidden.
     */
    override val isImmersive: Boolean
        get() = true

    /**
     * Sets the ViewModel for the fragment.

     * @param fragmentArguments The fragment arguments.
     */
    override fun inject(fragmentArguments: Bundle?) {
        viewModel = ReportViewModel(dependencyProvider!!)
    }

    /**
     * This class supports traversing between the user report pages.
     */
    private inner class ReportPagerAdapter(private val context: Context) : PagerAdapter() {

        override fun instantiateItem(collection: ViewGroup, position: Int): Object {
            var view: View? = null

            if (position < reportPageLayoutIds.size) {
                val inflater = LayoutInflater.from(context)
                val layoutId = reportPageLayoutIds[position]

                //if the view does not use data binding, inflate it normally.
                val viewDataBinding = DataBindingUtil.inflate<ViewDataBinding>(inflater, layoutId, collection, false)

                if (viewDataBinding != null) {
                    viewDataBinding.setVariable(BR.viewmodel, viewModel)
                    view = viewDataBinding.root
                } else {
                    view = inflater.inflate(layoutId, collection, false)
                }
                collection.addView(view)
            }
            return view as Object
        }

        override fun destroyItem(collection: ViewGroup, position: Int, view: Any) {
            collection.removeView(view as View)
        }

        override fun getCount(): Int {
            return reportPageLayoutIds.size
        }

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view === `object`
        }

        override fun getPageTitle(position: Int): CharSequence {
            return ""
        }
    }

    companion object {
        private val TAG = "ReportFragment"
    }
}
