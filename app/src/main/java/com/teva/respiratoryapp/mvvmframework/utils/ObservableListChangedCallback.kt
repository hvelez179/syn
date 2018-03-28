//
// ObservableListChangedCallback.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.mvvmframework.utils

import android.databinding.ObservableList

/**
 * A callback class implementing the ObservableList callback interface and allowing derived classes
 * to override only the methods required.
 *
 * @param <Item> The type of the item.
 */
open class ObservableListChangedCallback<Item> : ObservableList.OnListChangedCallback<ObservableList<Item>>() {

    /**
     * Called when any change occurs.
     *
     * @param sender The changing list.
     */
    open fun onAnyChange(sender: ObservableList<Item>) {}

    /**
     * Called when a change of unknown type occurs.
     *
     * @param sender The changing list.
     */
    override fun onChanged(sender: ObservableList<Item>) {
        onAnyChange(sender)
    }

    /**
     * Called when one or more items in the list have changed.
     *
     * @param sender        The changing list.
     * @param positionStart The starting index that has changed.
     * @param itemCount     The number of items that have changed.
     */
    override fun onItemRangeChanged(sender: ObservableList<Item>, positionStart: Int, itemCount: Int) {
        onAnyChange(sender)
    }

    /**
     * Called whenever items have been inserted into the list.
     *
     * @param sender        The changing list
     * @param positionStart The insertion index
     * @param itemCount     The number of items that have been inserted
     */
    override fun onItemRangeInserted(sender: ObservableList<Item>, positionStart: Int, itemCount: Int) {
        onAnyChange(sender)
    }

    /**
     * Called whenever items in the list have been moved.
     *
     * @param sender       The changing list
     * @param fromPosition The position from which the items were moved
     * @param toPosition   The destination position of the items
     * @param itemCount    The number of items moved
     */
    override fun onItemRangeMoved(sender: ObservableList<Item>, fromPosition: Int, toPosition: Int, itemCount: Int) {
        onAnyChange(sender)
    }

    /**
     * Called whenever items in the list have been deleted.
     *
     * @param sender        The changing list
     * @param positionStart The starting index of the deleted items
     * @param itemCount     The number of items removed
     */
    override fun onItemRangeRemoved(sender: ObservableList<Item>, positionStart: Int, itemCount: Int) {
        onAnyChange(sender)
    }
}
