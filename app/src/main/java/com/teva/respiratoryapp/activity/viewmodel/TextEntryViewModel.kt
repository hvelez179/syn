//
// TextEntryViewModel.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.activity.viewmodel

import com.teva.utilities.services.DependencyProvider
import com.teva.respiratoryapp.mvvmframework.ui.FragmentViewModel

/**
 * Base class for the view models of text entry screens.
 *
 * @param dependencyProvider The dependency injection mechanism.
 */
abstract class TextEntryViewModel(dependencyProvider: DependencyProvider)
    : FragmentViewModel(dependencyProvider) {

    /**
     * Called by the fragment when the keyboard's action button is pressed.
     */
    open fun onEditorActionButton() {}
}
