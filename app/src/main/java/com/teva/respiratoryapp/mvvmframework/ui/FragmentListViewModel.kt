//
// FragmentListViewModel.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.mvvmframework.ui

import com.teva.respiratoryapp.mvvmframework.utils.ObservableSet
import com.teva.utilities.services.DependencyProvider

/**
 * Base viewmodel class for list fragments.
 *
 * @param dependencyProvider The dependency injection mechanism
 * @param <Item>          The type of the item.
 */
abstract class FragmentListViewModel<Item>(dependencyProvider: DependencyProvider)
    : FragmentViewModel(dependencyProvider) {

    var listChangedListener: ListChangedListener? = null

    /**
     * Gets the list of items.
     */
    abstract val items: List<Item>

    /**
     * Gets the set holding the selected items.
     */
    open val selectedItemSet: ObservableSet<Item>?
        get() = null

    /**
     * Gets the selection mode to use for the list.
     */
    open val listSelectionModel: ListSelectionMode
        get() = ListSelectionMode.MANUAL

    /**
     * Called when an item is clicked.
     *
     * @param item The item that was clicked.
     */
    open fun onItemClicked(item: Item) {}

    /**
     * Calls the onListChanged callback to notify the fragment that the list has changed.
     */
    protected fun notifyListChanged() {
        if (listChangedListener != null) {
            listChangedListener!!.onListChanged()
        }
    }

    /**
     * Listener interface used to notify the fragment when the list changes.
     */
    interface ListChangedListener {
        fun onListChanged()
    }
}
