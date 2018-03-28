/*
 *
 *  EnvironmentMessageFragment.kt
 *  app
 *
 *  Copyright © 2018 Teva. All rights reserved.
 *
 */

package com.teva.respiratoryapp.activity.view

import android.os.Bundle
import com.teva.respiratoryapp.R
import com.teva.respiratoryapp.activity.viewmodel.EmancipatedMessageViewModel
import com.teva.respiratoryapp.databinding.EmancipatedMessageFragmentBinding
import com.teva.respiratoryapp.mvvmframework.ui.BaseFragment

/**
 * This class represents the emancipated message fragment.
 */
class EmancipatedMessageFragment : BaseFragment<EmancipatedMessageFragmentBinding, EmancipatedMessageViewModel>(R.layout.emancipated_message_fragment) {
    override fun inject(fragmentArguments: Bundle?) {
        viewModel = EmancipatedMessageViewModel(dependencyProvider!!)
    }
}