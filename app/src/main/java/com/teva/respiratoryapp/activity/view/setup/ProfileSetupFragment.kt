package com.teva.respiratoryapp.activity.view.setup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.teva.common.services.analytics.enumerations.AnalyticsScreen
import com.teva.utilities.utilities.Logger
import com.teva.respiratoryapp.R
import com.teva.respiratoryapp.activity.viewmodel.setup.ProfileItemViewModel
import com.teva.respiratoryapp.activity.viewmodel.setup.ProfileItemViewModel.ItemType.ADD_ANOTHER_DEPENDENT
import com.teva.respiratoryapp.activity.viewmodel.setup.ProfileSetupViewModel
import com.teva.respiratoryapp.databinding.ProfileItemBinding
import com.teva.respiratoryapp.databinding.ProfileSetupFragmentBinding
import com.teva.respiratoryapp.mvvmframework.ui.*
import com.teva.respiratoryapp.mvvmframework.utils.ObservableSet

/**
 * Fragment class for the Profile Setup screen
 */
class ProfileSetupFragment
    : BaseListFragment<ProfileSetupFragmentBinding,
        ProfileSetupViewModel,
        ProfileItemViewModel>(R.layout.profile_setup_fragment) {

    private var itemAdapter: Adapter? = null

    override var isLogoutable = false

    init {
        screen = AnalyticsScreen.SetupProfile()
    }

    /**
     * Creates the list adapter.
     */
    override fun createAdapter() {
        val selectedItemSet = viewModel?.selectedItemSet
        val selectionMode = viewModel?.listSelectionModel ?: ListSelectionMode.MANUAL
        itemAdapter = Adapter(selectedItemSet, selectionMode)

        itemAdapter?.setItems(viewModel!!.items)

        itemAdapter?.setItemClickListener(
                object : BaseViewModelListAdapter.OnItemClickListener<ProfileItemViewModel> {
                    override fun onItemClick(item: ProfileItemViewModel) {
                        viewModel!!.onItemClicked(item)
                    }
                })

        recyclerView?.adapter = itemAdapter
    }

    /**
     * Called by the viewmodel when the list has changed.
     */
    override fun onListChanged() {
        val items = viewModel!!.items
        itemAdapter?.setItems(items)
    }

    /**
     * Sets the ViewModel for the fragment.
     *
     * @param fragmentArguments The fragment arguments.
     */
    override fun inject(fragmentArguments: Bundle?) {
        viewModel = ProfileSetupViewModel(dependencyProvider!!)
    }

    /**
     * Initializes the binding with the viewmodel. Can be overloaded by derived fragment classes
     * to perform additional initialization of the binding.
     *
     * @param binding The main fragment binding.
     */
    override fun initBinding(binding: ProfileSetupFragmentBinding) {
        super.initBinding(binding)
    }

    /**
     * RecyclerView adapter class for the Profile Setup screen.
     */
    inner class Adapter(selectedItems: ObservableSet<ProfileItemViewModel>?,
                        selectionMode: ListSelectionMode) :
            BaseViewModelListAdapter<ProfileItemViewModel>(selectedItems, selectionMode) {

        /**
         * Return the view type of the item at `position` for the purposes
         * of view recycling.
         *
         * @param position position to query
         * @return integer value identifying the type of the view needed to represent the item at
         * `position`. Type codes need not be contiguous.
         */
        override fun getItemViewType(position: Int): Int {
            return when (viewModel!!.items[position].itemType) {
                ADD_ANOTHER_DEPENDENT -> ADD_DEPENDENT_ITEM_TYPE
                else -> PROFILE_ITEM_TYPE
            }
        }

        /**
         * Called when RecyclerView needs a new [ViewHolder] of the given type to represent
         * an item.
         *
         * @param parent The ViewGroup into which the new View will be added after it is bound to
         *               an adapter position.
         * @param viewType The view type of the new View.
         * @return A new ViewHolder that holds a View of the given view type.
         */
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
            BaseViewModelListAdapter.logger.log(Logger.Level.VERBOSE, "onCreateViewHolder: " + viewType)
            val inflater = LayoutInflater.from(parent.context)

            return when (viewType) {
                ADD_DEPENDENT_ITEM_TYPE ->
                    ItemViewHolder(inflater.inflate(R.layout.profile_item_add_dependent, parent, false), null)

                else -> {
                    val binding = ProfileItemBinding.inflate(inflater, parent, false) //DataBindingUtil.inflate<ProfileItemBinding>(inflater, R.layout.profile_item, parent, false)
                    ItemViewHolder(binding.root, binding)
                }
            }
        }
    }

    companion object {
        val PROFILE_ITEM_TYPE = 0
        val A_DEPENDENT_ITEM_TYPE = 1
        val ADD_DEPENDENT_ITEM_TYPE = 2
    }

}