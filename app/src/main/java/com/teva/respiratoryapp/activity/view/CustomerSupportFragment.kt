/*
 *
 *  CustomerSupportFragment.kt
 *  app
 *
 *  Copyright © 2018 Teva. All rights reserved.
 *
 */

package com.teva.respiratoryapp.activity.view

import android.os.Bundle
import android.view.View
import com.teva.respiratoryapp.R
import com.teva.respiratoryapp.activity.viewmodel.dashboard.CustomerSupportViewModel
import com.teva.respiratoryapp.databinding.CustomerSupportFragmentBinding
import com.teva.respiratoryapp.mvvmframework.ui.BaseFragment
import kotlinx.android.synthetic.main.login_fragment.*

/**
 * This class represents the customer support screen.
 */
class CustomerSupportFragment : BaseFragment<CustomerSupportFragmentBinding, CustomerSupportViewModel>(R.layout.customer_support_fragment) {

    //Todo - update with the actual customer support url
    private val customerSupportUrl: String = "http://www.myproair.com/respiclick/"

    /**
     * Sets the ViewModel for the fragment.
     *
     * @param fragmentArguments The fragment arguments.
     */
    override fun inject(fragmentArguments: Bundle?) {
        viewModel = CustomerSupportViewModel(dependencyProvider!!)
    }

    /**
     * Called by the Fragment class after the view has been created.
     *
     * @param view The new view for the fragment.
     * @param savedInstanceState The saved instance state of the fragment.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel!!.webView = webView
        webView.loadUrl(customerSupportUrl)
    }

    /**
     * This method is called by the base fragment for initializing the toolbar.
     * Do nothing here as we have a customized toolbar.
     */
    override fun initToolbar(rootView: View) {
    }


}