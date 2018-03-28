package com.teva.respiratoryapp.mvvmframework.ui

import android.databinding.ListChangeRegistry
import android.databinding.ObservableList
import android.os.AsyncTask
import android.support.annotation.MainThread
import android.support.annotation.WorkerThread

import com.teva.utilities.utilities.Logger
import com.teva.common.utilities.Stopwatch

import java.util.ArrayList

/**
 * A observable list class that detects changes to the list by merging in items from an update list.

 * @param <TModel>  The type of the viewmodel.
 * *
 * @param <TIdType> The type of the item ids.
</TIdType></TModel> */
@MainThread
abstract class ItemList<TModel, out TIdType> : AbstractMutableList<TModel>(), ObservableList<TModel> {
    private var mListeners: ListChangeRegistry? = ListChangeRegistry()
    internal var itemList: MutableList<TModel>
    private var mergeTask: MergeTask? = null
    private var pendingMergeList: List<TModel>? = null

    private var suppressNotifications: Boolean = false

    /**
     * Constructor
     */
    init {
        itemList = ArrayList<TModel>()
    }

    /**
     * Adds a list changed callback
     */
    override fun addOnListChangedCallback(listener: ObservableList.OnListChangedCallback<out ObservableList<TModel>>?) {
        if (mListeners == null) {
            mListeners = ListChangeRegistry()
        }
        mListeners?.add(listener)
    }


    /**
     * Removes a list changed callback.
     */
    override fun removeOnListChangedCallback(listener: ObservableList.OnListChangedCallback<out ObservableList<TModel>>?) {
        if (mListeners != null) {
            mListeners?.remove(listener)
        }
    }

    /**
     * Completely replaces the list with a new list instead of performing a merge.

     * @param newModels - The list of new items used to replace the existing items.
     */
    fun replace(newModels: List<TModel>) {
        suppressNotifications = true
        try {
            clear()
            itemList.addAll(newModels)
        } finally {
            suppressNotifications = false
        }

        mListeners?.notifyChanged(this)
    }

    /**
     * Merges a list of Model objects into the list of ViewModels that wrap them.
     */
    fun merge(newModels: List<TModel>) {

        pendingMergeList = newModels
        if (mergeTask == null) {
            doMerge()
        }
    }

    /**
     * Executes a pending merge using an AsyncTask.
     */
    private fun doMerge() {
        val task = MergeTask(pendingMergeList!!, itemList)
        pendingMergeList = null
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    /**
     * Gets a ViewModel at the specified location.
     */
    override fun get(index: Int): TModel {
        return itemList[index]
    }

    /**
     * The number of items in the ItemList.
     */
    override val size: Int
        get() = itemList.size

    /**
     * Adds a ViewModel at the specified location.
     */
    override fun add(index: Int, element: TModel) {
        itemList.add(index, element)

        if (!suppressNotifications) {
            mListeners?.notifyInserted(this, index, 1)
        }
    }

    /**
     * Removes the ViewModel from the specified location.
     */
    override fun removeAt(index: Int): TModel {
        val viewModel = itemList.removeAt(index)
        if (!suppressNotifications) {
            mListeners?.notifyRemoved(this, index, 1)
        }

        return viewModel
    }


    /**
     * Moves a ViewModel from one location to another in the ViewModel list.
     */
    fun move(from: Int, to: Int) {
        val item = itemList.removeAt(from)
        itemList.add(to, item)
        if (!suppressNotifications) {
            mListeners?.notifyMoved(this, from, to, 1)
        }
    }

    /**
     * Sets the value at the specified location with a new object.

     * @param index The location to replace.
     * *
     * @param element   The new object.
     * *
     * @return The object previously at the location.
     */
    override fun set(index: Int, element: TModel): TModel {
        val result = itemList.set(index, element)

        if (!suppressNotifications) {
            mListeners?.notifyChanged(this, index, 1)
        }
        return result
    }

    /**
     * Returns the id of the specified object.
     */
    protected abstract fun getItemId(item: TModel): TIdType

    /**
     * Compares two Model items for equality.
     */
    @WorkerThread
    abstract fun compareItems(item1: TModel, item2: TModel): Boolean

    /**
     * An AsyncTask class to perform the list merging.
     *
     * @param newModels      The list of models to merge into the existing list.
     * @param existingModels The existing list of models.
     */
    private inner class MergeTask(newModels: List<TModel>, existingModels: List<TModel>)
        : AsyncTask<Void, Void, Void>() {
        internal var newModels: List<TModel> = ArrayList(newModels)
        internal var existingModels: MutableList<TModel> = ArrayList(existingModels)
        internal var notifications: MutableList<Notification> = ArrayList()

        /**
         * Background method of AsyncTask().  Peforms the list merging.
         */
        override fun doInBackground(vararg unused: Void): Void? {
            val stopwatch = Stopwatch.Start(logger)
            // deletes first

            var indexExisting = 0
            while (indexExisting < existingModels.size) {
                if (indexInModelList(newModels, existingModels[indexExisting]) == -1) {
                    remove(indexExisting)
                } else {
                    indexExisting++
                }
            }

            // reorder the items based on their positions in new model list
            var updated: Boolean
            do {
                updated = false

                for (indexNew in newModels.indices) {
                    val changedItem = newModels[indexNew]
                    val indexOfChangedItemInOldList = indexOfModel(changedItem)

                    if (indexOfChangedItemInOldList == indexNew) {
                        // matched
                        continue
                    }

                    updated = true

                    if (indexOfChangedItemInOldList == -1 || indexNew > existingModels.size) {
                        // This is a new item
                        addModel(indexNew, changedItem)
                    } else {
                        val oldItem = existingModels[indexNew]

                        val indexOfOldItemInNewList = indexInModelList(newModels, oldItem)
                        // Item moved from somewhere else
                        val deltaOld = indexNew - indexOfChangedItemInOldList
                        val deltaChanged = indexOfOldItemInNewList - indexNew

                        val moveForward = Math.abs(deltaChanged) > Math.abs(deltaOld)
                        val from = if (moveForward) indexNew else indexOfChangedItemInOldList
                        val to = if (moveForward) if (indexOfOldItemInNewList < existingModels.size) indexOfOldItemInNewList else existingModels.size - 1 else indexNew
                        moveModel(from, to)
                    }
                    break
                }
            } while (updated)

            // copy the new model items into the viewmodels and report changes.
            for (i in newModels.indices) {
                val model = newModels[i]
                val changed = compareItems(existingModels[i], model)
                existingModels[i] = model
                if (changed) {
                    val notification = Notification()
                    notification.type = NotificationType.CHANGED
                    notification.to = i

                    notifications.add(notification)
                }
            }

            stopwatch.mark(Logger.Level.VERBOSE, "Merge Time")
            return null
        }

        /**
         * Method called on the Main Thread when the background task is completed.
         */
        override fun onPostExecute(unused: Void?) {
            itemList = existingModels
            mergeTask = null

            // send out the notifications
            if (!suppressNotifications) {
                if (notifications.size > MAX_MERGE_NOTIFICATIONS) {
                    mListeners?.notifyChanged(this@ItemList)
                } else {
                    for (notification in notifications) {
                        when (notification.type) {
                            ItemList.NotificationType.CHANGED -> mListeners?.notifyChanged(this@ItemList, notification.to, 1)

                            ItemList.NotificationType.ADD -> mListeners?.notifyInserted(this@ItemList, notification.to, 1)

                            ItemList.NotificationType.REMOVE -> mListeners?.notifyRemoved(this@ItemList, notification.to, 1)

                            ItemList.NotificationType.MOVE -> mListeners?.notifyMoved(this@ItemList, notification.from, notification.to, 1)
                        }
                    }
                }
            }

            // check to see if there is a pending merge and start it if there is.
            if (pendingMergeList != null) {
                doMerge()
            }
        }

        /**
         * Adds a ViewModel at the specified location.
         */
        private fun addModel(location: Int, `object`: TModel) {
            existingModels.add(location, `object`)

            val notification = Notification()
            notification.type = NotificationType.ADD
            notification.to = location

            notifications.add(notification)
        }

        /**
         * Removes the ViewModel from the specified location.
         */
        private fun remove(location: Int): TModel {
            val viewModel = existingModels.removeAt(location)

            val notification = Notification()
            notification.type = NotificationType.REMOVE
            notification.to = location

            notifications.add(notification)

            return viewModel
        }

        /**
         * Moves a ViewModel from one location to another in the ViewModel list.
         */
        private fun moveModel(from: Int, to: Int) {
            val item = existingModels.removeAt(from)
            existingModels.add(to, item)

            val notification = Notification()
            notification.type = NotificationType.MOVE
            notification.from = from
            notification.to = to

            notifications.add(notification)
        }

        /**
         * Finds the index of a model in a list of models using the IItem.getId()
         * for comparison.
         */
        private fun indexInModelList(list: List<TModel>, item: TModel): Int {
            for (i in list.indices) {
                val id1 = getItemId(item)
                val id2 = getItemId(list[i])
                if (id1 == id2) {
                    return i
                }
            }

            return -1
        }

        /**
         * Finds the index of a Model item in the ViewModel list by using IItem.getId()
         * for the comparison.
         */
        private fun indexOfModel(item: TModel): Int {
            return existingModels.indexOfFirst { getItemId(item) == getItemId(it) }
        }
    }

    /**
     * Enumeration describing the possible notification types that occur during a list merge.
     */
    private enum class NotificationType {
        CHANGED,
        ADD,
        REMOVE,
        MOVE
    }

    /**
     * Class representing a notification for a change that occurred during the list merge.
     */
    private inner class Notification {
        internal var type: NotificationType? = null
        internal var from: Int = 0
        internal var to: Int = 0
    }

    companion object {
        private val MAX_MERGE_NOTIFICATIONS = 5

        private val logger = Logger("ItemList")
    }
}

