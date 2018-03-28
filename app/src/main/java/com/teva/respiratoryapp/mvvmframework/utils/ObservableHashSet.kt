//
// ObservableHashSet.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.mvvmframework.utils

import android.databinding.CallbackRegistry
import java.util.HashSet

/**
 * An ObservableSet implamentation for HashSets.
 * @param <K> The type of the set items.
 */
class ObservableHashSet<K> : HashSet<K>(), ObservableSet<K> {
    private val callbackRegistry: CallbackRegistry<ObservableSet.OnSetChangedCallback<ObservableSet<K>, K>, ObservableSet<K>, Any>

    /**
     * CallbackRegistry callback object that perform the callbacks
     */
    private val NOTIFIER_CALLBACK = object : CallbackRegistry.NotifierCallback<ObservableSet.OnSetChangedCallback<ObservableSet<K>, K>, ObservableSet<K>, Any>() {
        override fun onNotifyCallback(callback: ObservableSet.OnSetChangedCallback<ObservableSet<K>, K>?, sender: ObservableSet<K>?, arg: Int, arg2: Any?) {
            if (sender != null) {
                callback?.onSetChanged(sender, arg2 as K?)
            }
        }
    }

    /**
     * Constructor
     */
    init {
        callbackRegistry = CallbackRegistry<ObservableSet.OnSetChangedCallback<ObservableSet<K>, K>, ObservableSet<K>, Any>(NOTIFIER_CALLBACK)
    }

    /**
     * Adds a SetChangedCallback object.
     * @param callback The callback to start listening for events.
     */
    override fun addOnSetChangedCallback(callback: ObservableSet.OnSetChangedCallback<ObservableSet<K>, K>) {
        callbackRegistry.add(callback)
    }

    /**
     * Removes a SetChangedCallback object.
     * @param callback The callback that no longer needs to be notified of map changes.
     */
    override fun removeOnSetChangedCallback(callback: ObservableSet.OnSetChangedCallback<ObservableSet<K>, K>) {
        callbackRegistry.remove(callback)
    }

    /**
     * Notifies the registered callbacks that a change occurred in the set.
     * @param key
     */
    private fun notifyChange(key: K?) {
        callbackRegistry.notifyCallbacks(this, 0, key)
    }

    /**
     * Clears the set.
     */
    override fun clear() {
        if (isNotEmpty()) {
            val keys = this.toList()
            super.clear()

            if (keys.size > MAX_INVIDUAL_CLEAR_ITEM_NOTIFY) {
                notifyChange(null)
            } else {
                for (key in keys) {
                    notifyChange(key)
                }
            }
        }
    }

    /**
     * Adds an element to the set.
     * @param element The element to add.
     * *
     * @return True if the set did not already contain the element.
     */
    override fun add(element: K): Boolean {
        val result = super.add(element)
        notifyChange(element)

        return result
    }

    /**
     * Adds all of the elements in a collection to the set.
     * @param elements The collection of elements.
     * *
     * @return True if the set did not already contain the elements.
     */
    override fun addAll(elements: Collection<K>): Boolean {
        val result = super.addAll(elements)
        notifyChange(null)

        return result
    }


    /**
     * Removes an element from the set.
     * @param element The element to remove.
     * *
     * @return True if the set contained the element.
     */
    override fun remove(element: K): Boolean {
        val changed = super.remove(element)
        if (changed) {
            notifyChange(element)
        }

        return changed
    }

    /**
     * Removes all of the elements in a collection.
     * @param elements The elements to remove.
     * *
     * @return True if the set contained the elements.
     */
    override fun removeAll(elements: Collection<K>): Boolean {
        val changed = super.removeAll(elements)
        if (changed) {
            notifyChange(null)
        }

        return changed
    }

    /**
     * Remove all of the elements except the ones contained in the collection.
     * @param elements The elements to retain.
     * *
     * @return True if the set contained the elements.
     */
    override fun retainAll(elements: Collection<K>): Boolean {
        val changed = super.retainAll(elements)
        if (changed) {
            notifyChange(null)
        }

        return changed
    }

    companion object {
        val MAX_INVIDUAL_CLEAR_ITEM_NOTIFY = 10
    }
}
