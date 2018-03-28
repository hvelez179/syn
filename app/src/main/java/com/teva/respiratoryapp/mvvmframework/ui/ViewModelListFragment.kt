//
// ViewModelListFragment.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.mvvmframework.ui

import android.databinding.BaseObservable
import android.databinding.ViewDataBinding

/**
 * Base fragment class for fragments that display ItemLists.
 *
 * @param viewLayoutId The layout id for the view.
 * @param itemLayoutId The layout id for the item.
 */
abstract class ViewModelListFragment<Binding : ViewDataBinding,
        ViewModel : FragmentListViewModel<ItemViewModel>,
        ItemBinding : ViewDataBinding, ItemViewModel : BaseObservable>(
        viewLayoutId: Int,
        protected var itemLayoutId: Int)
    : BaseListFragment<Binding, ViewModel, ItemViewModel>(viewLayoutId) {

    internal var itemAdapter: ViewModelListAdapter<ItemBinding, ItemViewModel>? = null

    /**
     * Creates a list adapter object that maps the item list of the ViewModel to
     * UI elements in the RecyclerView.
     */
    override fun createAdapter() {
        val selectedItemSet = viewModel?.selectedItemSet
        val selectionMode = viewModel?.listSelectionModel ?: ListSelectionMode.MANUAL
        itemAdapter = ViewModelListAdapter<ItemBinding, ItemViewModel>(itemLayoutId, selectedItemSet, selectionMode)

        val items = viewModel!!.items
        itemAdapter?.setItems(items)

        itemAdapter?.setItemClickListener(object : BaseViewModelListAdapter.OnItemClickListener<ItemViewModel> {
            override fun onItemClick(item: ItemViewModel) {
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
}
