//
// TextEntryFragment.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.activity.view

import android.content.Context
import android.databinding.OnRebindCallback
import android.databinding.ViewDataBinding
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import com.teva.respiratoryapp.R
import com.teva.respiratoryapp.activity.viewmodel.TextEntryViewModel
import com.teva.respiratoryapp.mvvmframework.ui.BaseFragment

/**
 * Base Fragment class for the screens with a text entry field.
 */
abstract class TextEntryFragment<Binding : ViewDataBinding, ViewModel : TextEntryViewModel>(layoutId: Int)
    : BaseFragment<Binding, ViewModel>(layoutId) {

    private var editText: EditText? = null
    private var selectAll: Boolean = false
    private var haveNotPaused: Boolean = true

    /**
     * Android lifecycle method called to create the fragment's view
     *
     * @param inflater           The view inflater for the fragment.
     * @param container          The container that the view will be added to.
     * @param savedInstanceState The saved state of the fragment.
     * @return The view for the fragment.
     */
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        editText = view.findViewById(R.id.editText)
        editText?.setOnEditorActionListener(TextView.OnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                viewModel!!.onEditorActionButton()
                return@OnEditorActionListener true
            }
            false
        })

        selectAll = savedInstanceState == null

        return view
    }

    /**
     * Implemented by derived fragments to configure the properties of the fragment.
     */
    override fun configureFragment() {
        super.configureFragment()

        menuId = R.menu.next_menu
    }

    /**
     * Initializes the binding with the viewmodel. Can be overloaded by derived fragment classes
     * to perform additional initialization of the binding.
     *
     * @param binding The main fragment binding.
     */
    override fun initBinding(binding: Binding) {
        super.initBinding(binding)

        binding.addOnRebindCallback(object : OnRebindCallback<ViewDataBinding>() {
            override fun onBound(binding: ViewDataBinding?) {
                if (haveNotPaused && editText?.hasFocus() != true)
                {
                    focusEditText()
                }
            }
        })
    }

    /**
     * Sets the focus in the custom nickname field and selects all text if the view was just loaded.
     */
    private fun focusEditText() {
        editText?.requestFocus()

        if (selectAll) {
            editText?.selectAll()
            selectAll = false
        }

        editText?.context?.let { context ->
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    /**
     * Set the haveNotPaused flag to block focusing on text
     */
    override fun onPause() {
        super.onPause()
        haveNotPaused = false
    }
}
