package com.teva.respiratoryapp.activity.view.programs

import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.teva.cloud.models.programmanagement.InvitationDetails
import com.teva.common.services.analytics.enumerations.AnalyticsScreen
import com.teva.respiratoryapp.R
import com.teva.respiratoryapp.activity.viewmodel.programs.CareProgramConsentViewModel
import com.teva.respiratoryapp.databinding.CareProgramConsentFragmentBinding
import com.teva.respiratoryapp.mvvmframework.ui.BaseFragment
import kotlinx.android.synthetic.main.care_program_consent_fragment.*

/**
 * Custom dialog class for consenting to a care program.
 */
class CareProgramConsentFragment()
    : BaseFragment<CareProgramConsentFragmentBinding,
        CareProgramConsentViewModel>(R.layout.care_program_consent_fragment) {

    init {
        this.viewModel = viewModel

        setLightStatusBar(true)
//        hasBlurredBackground = true
        screen = AnalyticsScreen.ConsentToShareData()
    }

    /**
     * Sets the ViewModel for the fragment.
     *
     * @param fragmentArguments The fragment arguments.
     */
    override fun inject(fragmentArguments: Bundle?) {
        val programDetails = fragmentArguments?.getSerializable(PROGRAM_DETAILS_KEY) as InvitationDetails
        viewModel = CareProgramConsentViewModel(dependencyProvider!!, programDetails)
    }

    /**
     * Called by the base class after the view is created.
     *
     * @param view The view that was created
     * @param savedInstanceState The saved instance state of the fragment.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        attachScrollBehavior(scrollView)

        // setup "App you are using" list
        val apps = viewModel?.apps
        if (apps?.isNotEmpty() == true) {
            appsLabel.visibility = View.VISIBLE
            appsContainer.visibility = View.VISIBLE

            for (name in apps) {
                val itemView = layoutInflater.inflate(
                        R.layout.care_program_consent_app_item, appsContainer, false) as? TextView
                itemView?.text = name
                appsContainer.addView(itemView)
            }
        }

        // setup "Other apps you can use" list
        val otherApps = viewModel?.otherApps
        if (otherApps?.isNotEmpty() == true) {
            otherAppsLabel.visibility = View.VISIBLE
            otherAppsContainer.visibility = View.VISIBLE

            for (name in otherApps) {
                val itemView = layoutInflater.inflate(
                        R.layout.care_program_consent_app_item, otherAppsContainer, false) as? TextView
                itemView?.text = name
                otherAppsContainer.addView(itemView)
            }
        }
    }

    companion object {
        val PROGRAM_DETAILS_KEY = "InvitationDetails"

        /**
         * Static method to create an arguments bundle for the fragment.
         *
         * @param invitationDetails The program details to consent to
         */
        fun createFragmentArguments(invitationDetails: InvitationDetails): Bundle {
            val bundle = Bundle()
            bundle.putSerializable(PROGRAM_DETAILS_KEY, invitationDetails)

            return bundle
        }
    }
}
