//
// AboutAppViewModel.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.activity.viewmodel.dashboard

import com.teva.utilities.services.DependencyProvider
import com.teva.respiratoryapp.BuildConfig
import com.teva.respiratoryapp.R
import com.teva.respiratoryapp.activity.viewmodel.SupportEvents
import com.teva.respiratoryapp.common.DateTimeLocalization
import com.teva.respiratoryapp.mvvmframework.ui.FragmentViewModel

import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter

/**
 * This class is the view model for the "About the app" screen.
 *
 * @param dependencyProvider The dependency injection mechanism.
 */
class AboutAppViewModel(dependencyProvider: DependencyProvider) : FragmentViewModel(dependencyProvider) {

    /**
     * The application version.
     */
    var version: String? = null
        private set

    /**
     * The release date
     */
    var customerSupportText: String? = null
        private set

    /**
     * Events produced by the viewmodel to request actions by the activity.
     */
    interface Events {
        fun onInstructionsForUseClick()
        fun onTermsOfUseClick()
        fun onPrivacyNoticeClick()
        fun onLicensesClick()
        fun onWebsiteClick()
    }

    /**
     * This method called by the BaseFragment when the fragment's onStart() lifecycle method is called.
     */
    override fun onStart() {
        super.onStart()
        val buildReleaseDate = LocalDate.parse(BuildConfig.RELEASE_DATE, DateTimeFormatter.ofPattern("M/d/yyyy"))
        val dateTimeLocalization = dependencyProvider.resolve<DateTimeLocalization>()
        version = getString(R.string.appVersion_text) + " " + BuildConfig.VERSION_NAME + "\n" +
                getString(R.string.releaseDate_text) + " " + dateTimeLocalization.toShortMonthDayYear(buildReleaseDate) + "\n" +
                getString(R.string.copyright_text)
        customerSupportText = getString(R.string.menuServiceCenterDescription_text)
    }

    /**
     * This method is called when the instructions for use link is clicked.
     */
    fun onClickInstructionsForUse() {
        dependencyProvider.resolve<AboutAppViewModel.Events>().onInstructionsForUseClick()
    }

    /**
     * This method is called when the customer support link is clicked.
     * (per bug task SYN-434, onClick should not go to ContactCustomerSupport it should to to ContactSupport...)
     */
//    fun onContactCustomerSupportClick() {
//        dependencyProvider.resolve<SupportEvents>().onSupport()
//    }

    /**
     * This method is called when the customer support link is clicked.
     */

    fun onContactSupportClicked() {
        dependencyProvider.resolve<ContactSupportViewModel.Events>().onContactSupport()
    }

    /**
     * This method is called when the terms of use link is clicked.
     */
    fun onTermsOfUseClick() {
        dependencyProvider.resolve<AboutAppViewModel.Events>().onTermsOfUseClick()
    }

    /**
     * This method is called when the privacy notice link is clicked.
     */
    fun onPrivacyNoticeClick() {
        dependencyProvider.resolve<AboutAppViewModel.Events>().onPrivacyNoticeClick()
    }

    /**
     * This method is called when the Licenses link is clicked.
     */
    fun onLicensesClick() {
        dependencyProvider.resolve<AboutAppViewModel.Events>().onLicensesClick()
    }

    /**
     * This method is called when the ProAirDigihaler.com link is clicked.
     */
    fun onWebsiteClick() {
        dependencyProvider.resolve<AboutAppViewModel.Events>().onWebsiteClick()
    }
}
