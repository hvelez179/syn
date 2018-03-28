//
// DeviceListFragment.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.activity.view.device


import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.view.View
import com.teva.common.services.analytics.enumerations.AnalyticsScreen

import com.teva.respiratoryapp.R
import com.teva.respiratoryapp.activity.viewmodel.MessageShadeViewModel
import com.teva.respiratoryapp.activity.viewmodel.device.DeviceItemState
import com.teva.respiratoryapp.activity.viewmodel.device.DeviceItemViewModel
import com.teva.respiratoryapp.activity.viewmodel.device.DeviceListViewModel
import com.teva.respiratoryapp.databinding.DeviceItemBinding
import com.teva.respiratoryapp.databinding.DeviceListFragmentBinding
import com.teva.respiratoryapp.mvvmframework.controls.DividerDecoration
import com.teva.respiratoryapp.mvvmframework.ui.ItemListFragment
import com.teva.devices.entities.Device

/**
 * Fragment class for the Device List screen.
 */
class DeviceListFragment
    : ItemListFragment<DeviceListFragmentBinding, DeviceListViewModel, DeviceItemBinding, DeviceItemViewModel, Device>(
        R.layout.device_list_fragment, R.layout.device_item) {

    private var messageShadeViewModel: MessageShadeViewModel? = null

    init {
        screen = AnalyticsScreen.MyInhalers()
    }

    /**
     * Implemented by derived fragments to configure the properties of the fragment.
     */
    override fun configureFragment() {
        super.configureFragment()

        toolbarTitle = localizationService!!.getString(R.string.inhaler_text)
    }

    /**
     * Sets the ViewModel for the fragment.
     */
    override fun inject(fragmentArguments: Bundle?) {
        messageShadeViewModel = MessageShadeViewModel(dependencyProvider!!)
        viewModel = DeviceListViewModel(dependencyProvider!!)
    }

    /**
     * Initializes the item decorations for the RecyclerView.
     *
     * @param recyclerView The RecyclerView to initialize.
     */
    override fun setListDecorations(recyclerView: RecyclerView) {
        recyclerView.addItemDecoration(
                DividerDecoration(getDrawable(R.drawable.list_divider), R.dimen.devicelist_item_divider_margin))
    }

    /**
     * Initializes the binding with the viewmodel. Can be overloaded by derived fragment classes
     * to perform additional initialization of the binding.
     *
     * @param binding The main fragment binding.
     */
    override fun initBinding(binding: DeviceListFragmentBinding) {
        super.initBinding(binding)

        binding.messageShade?.let { it.viewmodel = messageShadeViewModel }
    }

    /**
     * Called by the base class after the view is created.
     *
     * @param view The view that was created
     * @param savedInstanceState The saved instance state of the fragment.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        attachScrollBehavior(view.findViewById<View>(R.id.item_list))
    }

    /**
     * Android lifecycle method called when the fragment is displayed on the screen.
     */
    override fun onStart() {
        super.onStart()

        messageShadeViewModel!!.onStart()
    }

    /**
     * Android lifecycle method called when the fragment is removed from the screen.
     */
    override fun onStop() {
        super.onStop()

        messageShadeViewModel!!.onStop()
    }

    /**
     * Creates a ViewModel for the list item.
     *
     * @return a new list item ViewModel.
     */
    override fun createItemViewModel(): DeviceItemViewModel {
        return DeviceItemViewModel(dependencyProvider!!)
    }

    companion object {

        /**
         * Binding converter method to convert a DeviceItemState to a drawable.
         */
        @JvmStatic
        fun DeviceItemStateToDrawableId(state: DeviceItemState): Int {
            when (state) {
                DeviceItemState.NEAR_EMPTY -> return R.drawable.ic_wid_my_inhalers_red

                else -> return R.drawable.ic_wid_my_inhalers
            }
        }
    }
}
