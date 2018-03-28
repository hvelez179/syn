package com.teva.respiratoryapp.activity.view.programs

import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.view.View
import com.teva.cloud.dataentities.ProgramData
import com.teva.common.services.analytics.enumerations.AnalyticsScreen
import com.teva.respiratoryapp.R
import com.teva.respiratoryapp.activity.viewmodel.programs.CareProgramItemViewModel
import com.teva.respiratoryapp.activity.viewmodel.programs.CareProgramsViewModel
import com.teva.respiratoryapp.databinding.CareProgramItemBinding
import com.teva.respiratoryapp.databinding.CareProgramsFragmentBinding
import com.teva.respiratoryapp.mvvmframework.controls.DividerDecoration
import com.teva.respiratoryapp.mvvmframework.ui.ItemListFragment

class CareProgramsFragment
    : ItemListFragment<CareProgramsFragmentBinding,
        CareProgramsViewModel,
        CareProgramItemBinding,
        CareProgramItemViewModel,
        ProgramData>(
        R.layout.care_programs_fragment, R.layout.care_program_item) {

    init {
        screen = AnalyticsScreen.CareProgram()
    }

    /**
     * Implemented by derived fragments to configure the properties of the fragment.
     */
    override fun configureFragment() {
        super.configureFragment()

        toolbarTitle = getString(R.string.care_program_title)
    }

    /**
     * Creates a ViewModel for the list item.
     *
     * @return a new list item ViewModel.
     */
    override fun createItemViewModel(): CareProgramItemViewModel {
        val item = CareProgramItemViewModel()
        item.leaveProgramCallback = { programData -> onLeaveProgram(programData)}

        return item
    }

    /**
    * Handler for the Leave Program hyperlink on the program items.
    */
    fun onLeaveProgram(programData: ProgramData) {
        viewModel?.onLeaveProgram(programData)
    }

    /**
     * Sets the ViewModel for the fragment.
     *
     * @param fragmentArguments The fragment arguments.
     */
    override fun inject(fragmentArguments: Bundle?) {
        viewModel = CareProgramsViewModel(dependencyProvider!!)
    }

    /**
     * Initializes the toolbar properties.
     */
    override fun initToolbar(rootView: View) {
        super.initToolbar(rootView)

        attachScrollBehavior(rootView.findViewById(R.id.item_list))
    }

    /**
     * Initializes the item decorations for the RecyclerView.
     *
     * @param recyclerView The RecyclerView to initialize.
     */
    override fun setListDecorations(recyclerView: RecyclerView) {
        recyclerView.addItemDecoration(
                DividerDecoration(getDrawable(R.drawable.list_divider),
                        showDividerAtTop = true, showDividerAtBottom = true))
    }


}