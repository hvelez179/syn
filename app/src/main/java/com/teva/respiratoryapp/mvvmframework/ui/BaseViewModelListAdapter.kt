package com.teva.respiratoryapp.mvvmframework.ui

import android.databinding.BaseObservable
import android.databinding.ObservableList
import android.databinding.ViewDataBinding
import android.support.v7.widget.RecyclerView
import android.view.View
import com.teva.utilities.utilities.Logger
import com.teva.respiratoryapp.BR
import com.teva.respiratoryapp.mvvmframework.utils.ObservableListChangedCallback
import com.teva.respiratoryapp.mvvmframework.utils.ObservableSet
import java.util.*

/**
 * Base class for RecyclerView list adapters that will create View objects for a list of ViewModels
 * and will create the bindings between the Views and ViewModels.
 *
 * @param selectedItems   A set used to hold the selected items.
 * @param selectionMode The selection mode for the list adapter.
 */
abstract class BaseViewModelListAdapter<ViewModel : BaseObservable>(
        private val selectedItems: ObservableSet<ViewModel>? = null,
        private val selectionMode: ListSelectionMode = ListSelectionMode.MANUAL)
    : RecyclerView.Adapter<BaseViewModelListAdapter<ViewModel>.ItemViewHolder>() {

    private var itemClickListener: OnItemClickListener<ViewModel>? = null
    private var items: List<ViewModel>? = null

    private val selectionChangedCallback = object : ObservableSet.OnSetChangedCallback<ObservableSet<ViewModel>, ViewModel>() {
        override fun onSetChanged(sender: ObservableSet<ViewModel>, key: ViewModel?) {
            if (key != null) {
                val index = items!!.indexOf(key)
                if (index != -1) {
                    notifyItemChanged(index)
                }
            } else {
                notifyDataSetChanged()
            }
        }
    }

    private val listChangedCallback = object : ObservableListChangedCallback<ViewModel>() {
        override fun onAnyChange(sender: ObservableList<ViewModel>) {
            super.onAnyChange(sender)

            notifyDataSetChanged()
        }
    }

    init {

        selectedItems?.addOnSetChangedCallback(selectionChangedCallback)
    }

    /**
     * Sets a listener that receives item click events.
     */
    fun setItemClickListener(listener: OnItemClickListener<ViewModel>?) {
        itemClickListener = listener
    }

    /**
     * Called by RecyclerView to display the data at the specified position. This method
     * should update the contents of the ViewHolder to reflect the item at
     * the given position.
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     * *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    override fun onBindViewHolder(holder: BaseViewModelListAdapter<ViewModel>.ItemViewHolder, position: Int) {
        logger.log(Logger.Level.VERBOSE, "onBindViewHolder: " + position)

        holder.setItem(items!![position])
        val item = items!![position]

        if (selectedItems != null) {
            holder.setActivated(selectedItems.contains(item))
        }
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    override fun getItemCount(): Int {
        logger.log(Logger.Level.VERBOSE, "getItemCount: " + if (items != null) items!!.size else 0)
        return if (items != null) items!!.size else 0
    }

    /**
     * Sets the list of ViewModel items.
     */
    fun setItems(items: List<ViewModel>) {
        if (this.items != null && this.items is ObservableList<*>) {
            (this.items as ObservableList<ViewModel>).removeOnListChangedCallback(listChangedCallback)
        }

        this.items = items

        if (this.items != null && this.items is ObservableList<*>) {
            (this.items as ObservableList<ViewModel>).addOnListChangedCallback(listChangedCallback)
        }

        if (selectedItems != null) {
            // remove selected items that no longer exist in the selectedItems set
            val itemsToRemove = selectedItems.filterNot { items.contains(it) }

            selectedItems.removeAll(itemsToRemove)
        }

        notifyDataSetChanged()
    }

    /**
     * Clears the set of selected items.
     */
    fun clearSelection() {
        if (selectedItems != null) {
            if (selectedItems.size > MAX_INVIDUAL_CLEAR_SELECTION_NOTIFY) {
                selectedItems.clear()
            } else {
                val removeList = ArrayList(selectedItems)
                for (item in removeList) {
                    selectedItems.remove(item)
                }
            }
        }
    }

    /**
     * Handler for item click events that selects or unselects items based on the selection mode.
     *
     * @param position The list position of the item that was clicked.
     * @return True if the clicked item is selected, false if it is unselected.
     */
    private fun handleClick(position: Int): Boolean {
        var isSelected = false
        val item = items!![position]
        if (selectionMode !== ListSelectionMode.MANUAL) {
            isSelected = selectedItems!!.contains(item)

            when (selectionMode) {
                ListSelectionMode.MANUAL -> {
                }

                ListSelectionMode.SINGLE -> {
                    if (!isSelected) {
                        clearSelection()
                        selectedItems.add(item)
                    }
                    isSelected = true
                }

                ListSelectionMode.MULTIPLE -> {
                    isSelected = !isSelected
                    if (isSelected) {
                        selectedItems.add(item)
                    } else {
                        selectedItems.remove(item)
                    }
                }
            }
        }

        if (itemClickListener != null) {
            itemClickListener!!.onItemClick(item)
        }

        return isSelected
    }

    /**
     * RecyclerView ViewHolder class that holds a reference to the View and bindings for the ViewModel.
     *
     * @param binding The View data binding object.
     */
    inner class ItemViewHolder(root: View, private val binding: ViewDataBinding?)
        : RecyclerView.ViewHolder(root), View.OnClickListener {

        init {

            if (itemClickListener != null) {
                itemView.setOnClickListener(this)
            }
        }

        /**
         * Sets the ViewModel for the ViewHolder.
         *
         * @param item The ViewModel
         */
        fun setItem(item: ViewModel) {
            binding?.setVariable(BR.viewmodel, item)
            binding?.executePendingBindings()
        }

        /**
         * Sets the selection state of the item's View.
         */
        fun setActivated(activated: Boolean) {
            itemView.isActivated = activated
        }

        /**
         * Click event handler for the view.
         *
         * @param view The view that was clicked.
         */
        override fun onClick(view: View) {
            val position = adapterPosition
            handleClick(position)
        }

    }

    /**
     * Item Click listener interface to receive click events for the ViewModel.
     *
     * @param <ViewModel>
     */
    interface OnItemClickListener<in ViewModel> {
        fun onItemClick(item: ViewModel)
    }

    companion object {

        val logger = Logger("ItemListAdapter")

        // The max number of items that will be individually notified due a
        // clear selection call.  Individually notifying allows the UI to
        // animate the change.
        private val MAX_INVIDUAL_CLEAR_SELECTION_NOTIFY = 10
    }
}
