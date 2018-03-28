//
// Alert.java
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.activity.view


import com.teva.respiratoryapp.R
import com.teva.respiratoryapp.activity.viewmodel.AlertViewModel
import com.teva.respiratoryapp.databinding.AlertFragmentBinding
import com.teva.respiratoryapp.mvvmframework.ui.BaseActivity
import com.teva.respiratoryapp.mvvmframework.ui.BaseDialog


/**
 * A full screen alert dialog
 */
class Alert(context: BaseActivity, layoutId: Int = R.layout.alert_fragment)
    : BaseDialog<AlertFragmentBinding, AlertViewModel>(context, layoutId) {

    init {
        setLightStatusBar(true)
        hasBlurredBackground = true
    }
}
