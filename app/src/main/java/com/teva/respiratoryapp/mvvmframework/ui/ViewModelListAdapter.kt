//
// ViewModelListAdapter.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.mvvmframework.ui

import android.databinding.BaseObservable
import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.view.LayoutInflater
import android.view.ViewGroup
import com.teva.utilities.utilities.Logger.Level.VERBOSE
import com.teva.respiratoryapp.mvvmframework.utils.ObservableSet

/**
 * RecyclerView list adapters that will create View objects for a list of ViewModels
 * and will create the bindings between the Views and ViewModels. Supports a single
 * view layout for item.
 *
 * @param itemLayoutId  The layout id for the Views.
 * @param selectedItems   A set used to hold the selected items.
 * @param selectionMode The selection mode for the list adapter.
 */
class ViewModelListAdapter<Binding : ViewDataBinding, ViewModel : BaseObservable>(
        private val itemLayoutId: Int,
        private val selectedItems: ObservableSet<ViewModel>? = null,
        private val selectionMode: ListSelectionMode = ListSelectionMode.MANUAL)
    : BaseViewModelListAdapter<ViewModel>(selectedItems, selectionMode) {

    /**
     * Called when RecyclerView needs a new ViewHolder of the given type to represent
     * an item.
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     * *                 an adapter position.
     * @param viewType The view type of the new View.
     * @return A new ViewHolder that holds a View of the given view type.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        BaseViewModelListAdapter.logger.log(VERBOSE, "onCreateViewHolder: " + viewType)

        val inflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate<Binding>(inflater, itemLayoutId, parent, false)

        return ItemViewHolder(binding.root, binding)
    }
}
