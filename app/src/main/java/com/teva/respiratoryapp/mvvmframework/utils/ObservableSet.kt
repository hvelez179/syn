//
// ObservableSet.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.mvvmframework.utils

/**
 * An ObservableSet implementation.
 */
interface ObservableSet<K> : MutableSet<K> {
    /**
     * Adds a callback to listen for changes to the ObservableMap.

     * @param callback The callback to start listening for events.
     */
    fun addOnSetChangedCallback(
            callback: OnSetChangedCallback<ObservableSet<K>, K>)

    /**
     * Removes a previously added callback.

     * @param callback The callback that no longer needs to be notified of map changes.
     */
    fun removeOnSetChangedCallback(
            callback: OnSetChangedCallback<ObservableSet<K>, K>)

    /**
     * A callback receiving notifications when an ObservableMap changes.
     */
    abstract class OnSetChangedCallback<T : ObservableSet<K>, K> {

        /**
         * Called whenever an ObservableMap changes, including values inserted, deleted,
         * and changed.

         * @param sender The changing map.
         * *
         * @param key    The key of the value inserted, removed, or changed.
         */
        abstract fun onSetChanged(sender: T, key: K?)
    }
}
