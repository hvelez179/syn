package com.teva.respiratoryapp.activity.view.programs

import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import com.teva.common.services.analytics.enumerations.AnalyticsScreen
import com.teva.respiratoryapp.R
import com.teva.respiratoryapp.activity.viewmodel.programs.AddCareProgramViewModel
import com.teva.respiratoryapp.activity.viewmodel.programs.AddCareProgramViewModel.Companion.ConsentDialogId
import com.teva.respiratoryapp.databinding.AddCareProgramFragmentBinding
import com.teva.respiratoryapp.mvvmframework.ui.BaseFragment
import com.teva.respiratoryapp.services.alert.AlertConfiguration
import com.teva.respiratoryapp.services.alert.AlertConfigurationProvider
import com.teva.respiratoryapp.services.alert.AlertType
import kotlinx.android.synthetic.main.add_care_program_fragment.*

/**
 * Fragment class for the Add Care Program screen.
 */
class AddCareProgramFragment
    : BaseFragment<AddCareProgramFragmentBinding,
        AddCareProgramViewModel>(R.layout.add_care_program_fragment),
      AlertConfigurationProvider{

    init {
        screen = AnalyticsScreen.AddCareProgram()
    }

    /**
     * Sets the ViewModel for the fragment.
     *
     * @param fragmentArguments The fragment arguments.
     */
    override fun inject(fragmentArguments: Bundle?) {
        viewModel = AddCareProgramViewModel(dependencyProvider!!)
    }

    /**
     * Implemented by derived fragments to configure the properties of the fragment.
     */
    override fun configureFragment() {
        super.configureFragment()

        setSaveViewModelState(true)
        toolbarTitle = getString(R.string.add_care_program)
    }

    /**
     * Gets the layout id for an alert.
     *
     * @param id The id of the dialog
     * @return The layout id to use or null to use the default layout.
     */
    override fun getAlertConfiguration(id: String?): AlertConfiguration? {
        return if (id == ConsentDialogId) consentDialogConfiguration else null
    }

    /**
     * Lifetime method called after the fragment's view has been created.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        invitationCode.setOnEditorActionListener(TextView.OnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                viewModel?.onEditorActionButton()
                return@OnEditorActionListener true
            }
            false
        })
    }

    /**
     * Android lifecycle method called when the fragment is becomes the focused fragment.
     */
    override fun onResume() {
        super.onResume()

        val clipboardManager = context!!.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = clipboardManager.primaryClip
        if (clipData != null) {
            viewModel?.clipboardText = clipData.getItemAt(0).text.toString()
        }
    }

    companion object {
        val consentDialogConfiguration = AlertConfiguration(AlertType.CUSTOM_DIALOG,
                dialogClass = CareProgramConsentFragment::class)
    }
}