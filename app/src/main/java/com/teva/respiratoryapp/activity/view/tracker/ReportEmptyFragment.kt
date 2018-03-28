package com.teva.respiratoryapp.activity.view.tracker

import android.os.Bundle
import com.teva.respiratoryapp.R
import com.teva.respiratoryapp.activity.viewmodel.tracker.ReportEmptyViewModel
import com.teva.respiratoryapp.databinding.ReportEmptyFragmentBinding
import com.teva.respiratoryapp.mvvmframework.ui.BaseFragment

/**
 * View class for the screen displayed when a user report is requested by no data exists.
 */
class ReportEmptyFragment
    : BaseFragment<ReportEmptyFragmentBinding, ReportEmptyViewModel>(R.layout.report_empty_fragment) {

    override fun configureFragment() {
        super.configureFragment()

        menuId = R.menu.user_report_empty_menu
        toolbarTitle = getString(R.string.userReportNoDataTitle_text)
    }

    /**
     * Sets the ViewModel for the fragment.
     *
     * @param fragmentArguments The fragment arguments.
     */
    override fun inject(fragmentArguments: Bundle?) {
        viewModel = ReportEmptyViewModel(dependencyProvider!!)
    }
}