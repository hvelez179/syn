//
// ItemListAdapter.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.mvvmframework.ui

import android.databinding.DataBindingUtil
import android.databinding.ObservableList
import android.databinding.ViewDataBinding
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.teva.respiratoryapp.mvvmframework.utils.ObservableListChangedCallback
import com.teva.respiratoryapp.mvvmframework.utils.ObservableSet
import com.teva.respiratoryapp.BR
import com.teva.utilities.utilities.Logger
import com.teva.utilities.utilities.Logger.Level.*

import java.util.ArrayList

/**
 * RecyclerView adapter class for ItemLists
 */
abstract class ItemListAdapter<Binding : ViewDataBinding, out ViewModel : IItemViewModel<Item>, Item>(
        private val itemLayoutId: Int,
        private val selectedItems: ObservableSet<Item>? = null,
        private val listSelectionMode: ListSelectionMode = ListSelectionMode.MANUAL)
    : RecyclerView.Adapter<ItemListAdapter<Binding, ViewModel, Item>.ItemViewHolder>() {

    private var itemClickListener: OnItemClickListener<Item>? = null
    private var items: List<Item>? = null
    private val selectionChangedCallback = object : ObservableSet.OnSetChangedCallback<ObservableSet<Item>, Item>() {
        override fun onSetChanged(sender: ObservableSet<Item>, key: Item?) {
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

    private val listChangedCallback = object : ObservableListChangedCallback<Item>() {
        override fun onAnyChange(sender: ObservableList<Item>) {
            super.onAnyChange(sender)

            notifyDataSetChanged()
        }
    }

    init {
        selectedItems?.addOnSetChangedCallback(selectionChangedCallback)
    }

    /**
     * Sets the listener called when an item is clicked.
     */
    fun setItemClickListener(listener: OnItemClickListener<Item>?) {
        itemClickListener = listener
    }

    /**
     * Creates a new ViewHolder
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        logger.log(VERBOSE, "onCreateViewHolder: " + viewType)

        val inflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate<Binding>(inflater, itemLayoutId, parent, false)

        return ItemViewHolder(binding)
    }

    /**
     * Binds a ViewHolder to a list item.
     */
    override fun onBindViewHolder(holder: ItemListAdapter<Binding, ViewModel, Item>.ItemViewHolder, position: Int) {
        logger.log(VERBOSE, "onBindViewHolder: " + position)

        holder.setItem(items!![position])
        val item = items!![position]

        if (selectedItems != null) {
            holder.setActivated(selectedItems.contains(item))
        }
    }

    /**
     * Gets the number of items in the adapter.
     */
    override fun getItemCount(): Int {
        logger.log(VERBOSE, "getItemCount: " + if (items != null) items!!.size else 0)
        return if (items != null) items!!.size else 0
    }

    /**
     * Sets the list of items rendered by the adapter.
     */
    fun setItems(items: List<Item>) {
        if (this.items != null && this.items is ObservableList<*>) {
            (this.items as ObservableList<Item>).removeOnListChangedCallback(listChangedCallback)
        }

        this.items = items

        if (this.items != null && this.items is ObservableList<*>) {
            (this.items as ObservableList<Item>).addOnListChangedCallback(listChangedCallback)
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
     * Click event handler for the item Views.
     */
    private fun handleClick(position: Int): Boolean {
        var isSelected = false
        val item = items!![position]
        if (listSelectionMode != ListSelectionMode.MANUAL) {
            isSelected = selectedItems!!.contains(item)

            when (listSelectionMode) {
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
     * Overridden by derived classes to create an instance of the ViewModel.
     */
    protected abstract fun createViewModel(): ViewModel

    /**
     * Class that holds the View elements associated with a list item.
     */
    inner class ItemViewHolder(private val binding: Binding) : RecyclerView.ViewHolder(binding.root), View.OnClickListener {
        private val viewModel: ViewModel = createViewModel()

        init {

            binding.setVariable(BR.viewmodel, viewModel)

            if (itemClickListener != null) {
                itemView.setOnClickListener(this)
            }
        }

        /**
         * Sets the list item
         */
        fun setItem(item: Item) {
            viewModel.setItem(item)
            binding.executePendingBindings()
        }

        /**
         * Sets a value indicating whether the item should be displayed as selected.
         */
        fun setActivated(activated: Boolean) {
            itemView.isActivated = activated
        }

        /**
         * Click event handler for the item's view.
         */
        override fun onClick(v: View) {
            val position = adapterPosition
            handleClick(position)
        }
    }

    /**
     * Item click listener interface for the adapter.
     */
    interface OnItemClickListener<in Item> {
        fun onItemClick(item: Item)
    }

    companion object {

        private val logger = Logger("ItemListAdapter")

        // The max number of items that will be individually notified due a
        // clear selection call.  Individually notifying allows the UI to
        // animate the change.
        private val MAX_INVIDUAL_CLEAR_SELECTION_NOTIFY = 10
    }
}
