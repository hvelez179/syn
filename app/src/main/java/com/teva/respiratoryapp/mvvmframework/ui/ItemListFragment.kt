//
// ItemListFragment.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.mvvmframework.ui

import android.databinding.ViewDataBinding

/**
 * Base fragment class for fragments that display ItemLists.
 *
 * @param viewLayoutId The layout id for the view.
 * @param itemLayoutId The layout id for the item.
 */
abstract class ItemListFragment<Binding : ViewDataBinding,
        ViewModel : FragmentListViewModel<Item>,
        ItemBinding : ViewDataBinding,
        ItemViewModel : IItemViewModel<Item>,
        Item>(viewLayoutId: Int, protected var itemLayoutId: Int)
    : BaseListFragment<Binding, ViewModel, Item>(viewLayoutId) {

    internal var itemAdapter: ItemListAdapter<ItemBinding, ItemViewModel, Item>? = null

    override fun createAdapter() {
        val selectedItemSet = viewModel!!.selectedItemSet
        val selectionMode = viewModel!!.listSelectionModel
        itemAdapter = object : ItemListAdapter<ItemBinding, ItemViewModel, Item>(itemLayoutId, selectedItemSet, selectionMode) {
            override fun createViewModel(): ItemViewModel {
                return createItemViewModel()
            }
        }

        val items = viewModel!!.items
        itemAdapter?.setItems(items)

        itemAdapter?.setItemClickListener(object : ItemListAdapter.OnItemClickListener<Item> {
            override fun onItemClick(item: Item) {
                viewModel!!.onItemClicked(item)
            }
        })

        recyclerView!!.adapter = itemAdapter
    }

    /**
     * Called by the viewmodel when the list has changed.
     */
    override fun onListChanged() {
        val items = viewModel!!.items
        itemAdapter?.setItems(items)
    }

    /**
     * Creates a ViewModel for the list item.
     * @return a new list item ViewModel.
     */
    abstract fun createItemViewModel(): ItemViewModel
}
