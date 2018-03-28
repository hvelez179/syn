//
// DashboardPopupFragment.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.activity.view.popup


import android.databinding.BindingAdapter
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout

import com.teva.respiratoryapp.R
import com.teva.respiratoryapp.activity.viewmodel.dashboard.DashboardStateViewModel
import com.teva.respiratoryapp.activity.viewmodel.popup.DashboardPopupViewModel
import com.teva.respiratoryapp.activity.viewmodel.popup.PopupColor
import com.teva.respiratoryapp.activity.viewmodel.popup.PopupDashboardButton
import com.teva.respiratoryapp.activity.viewmodel.popup.PopupDashboardButton.*
import com.teva.respiratoryapp.databinding.DashboardPopupFragmentBinding
import com.teva.respiratoryapp.mvvmframework.ui.BaseFragment

/**
 * A base class for dashboard popup fragments
 */
abstract class DashboardPopupFragment : BaseFragment<DashboardPopupFragmentBinding, DashboardPopupViewModel>(R.layout.dashboard_popup_fragment) {

    /**
     * Initializes the binding with the viewmodel. Can be overloaded by derived fragment classes
     * to perform additional initialization of the binding.
     *
     * @param binding The main fragment binding.
     */
    override fun initBinding(binding: DashboardPopupFragmentBinding) {
        super.initBinding(binding)

        binding.popupBackground?.let {
            it.viewmodel = viewModel
            it.dashboardState = dashboardStateViewModel
        }
    }

    /**
     * This method returns the DashboardStateViewModel to be used for displaying the
     * background of the popup. This base implementation returns the actual
     * DashboardStateViewModel from the dependency provider. Derived classes such as
     * walkthroughs can override this method to provide their own data values
     * to be displayed in the background.
     *
     * @return - the DashboardStateViewModel to use for populating the popup background.
     */
    protected open val dashboardStateViewModel: DashboardStateViewModel
        get() = dependencyProvider!!.resolve<DashboardStateViewModel>()

    /**
     * Creates the content view for this popup.
     *
     * @param inflater The LayoutInflator for the fragment.
     * @return A view to be added as the content of the popup.
     */
    protected abstract fun onCreateContentView(inflater: LayoutInflater,
                                               container: ViewGroup): View

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

        val contentContainer = view.findViewById<FrameLayout>(R.id.content_container)
        contentContainer.addView(onCreateContentView(inflater!!, container!!))

        return view
    }

    companion object {

        /**
         * Converts popup colors to the popup header drawable resource id.
         *
         * @param popupColor The popup color
         * @return A drawable id for the popup background
         */
        @JvmStatic
        fun PopupColorToPopupHeaderDrawableId(popupColor: PopupColor): Int {
            val resourceId = when (popupColor) {
                PopupColor.GREEN -> R.drawable.popup_header_green
                PopupColor.RED -> R.drawable.popup_header_red
                else -> 0
            }

            return resourceId
        }

        /**
         * Converts popup colors to the button background drawable resource id.
         *
         * @param popupColor The popup color
         * @return A drawable id for the button background
         */
        @JvmStatic
        fun PopupColorToButtonBackgroundDrawableId(popupColor: PopupColor): Int {
            val resourceId = when (popupColor) {
                PopupColor.GREEN -> R.drawable.cta_button_green
                PopupColor.RED -> R.drawable.cta_button_red
                else -> R.drawable.cta_button_blue
            }

            return resourceId
        }

        /**
         * Converts popup colors to the button text drawable resource id.
         *
         * @param popupColor The popup color
         * @return A color id for the popup color.
         */
        @JvmStatic
        fun PopupColorToButtonTextColorId(popupColor: PopupColor): Int {
            val resourceId =  when (popupColor) {
                PopupColor.GREEN, PopupColor.RED -> R.color.popup_button_light_text
                else -> R.color.popup_button_dark_text
            }

            return resourceId
        }

        /**
         * Conversion function used during binding to convert a popup arrow display state into
         * a gravity value for the popup card.
         * @param state The arrow display state.
         */
        @JvmStatic
        fun arrowStateToGravity(state: PopupDashboardButton): Int {
            return when(state) {

                MENU, REPORT, DSA, SUPPORT -> Gravity.TOP
                else -> Gravity.BOTTOM
            }
        }

        /**
         * Binding adapter that adjusts a popup card's top and bottom margins based on the
         * button display state.
         * @param view The view to be modified.
         * @param state the popup button display state.
         */
        @BindingAdapter("cardMargin")
        @JvmStatic
        fun setMargin(view: View, state: PopupDashboardButton?) {
            val layoutParams = view.layoutParams as ViewGroup.MarginLayoutParams

            state?.let {
                layoutParams.topMargin = when(it) {
                    MENU, REPORT, DSA, SUPPORT -> view.resources.getDimensionPixelOffset(R.dimen.popup_card_arrow_top_margin)
                    else -> view.resources.getDimensionPixelOffset(R.dimen.popup_card_no_arrow_margin)
                }

                layoutParams.bottomMargin = when(it) {
                    EVENTS, ENVIRONMENT, DEVICES, ALL, NONE -> view.resources.getDimensionPixelOffset(R.dimen.popup_card_arrow_bottom_margin)
                    else -> view.resources.getDimensionPixelOffset(R.dimen.popup_card_no_arrow_margin)
                }
            }

            view.layoutParams = layoutParams
        }
    }
}
