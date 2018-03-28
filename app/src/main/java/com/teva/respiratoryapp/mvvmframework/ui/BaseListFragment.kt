//
// BaseListFragment.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.mvvmframework.ui

import android.databinding.ViewDataBinding
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.teva.respiratoryapp.R

/**
 * Base class for fragments that display lists.
 *
 * @param viewLayoutId The layout id for the view.
 * @param <Binding> The data binding type for the fragment.
 * @param <ViewModel> The viewmodel type for the fragment.
 * @param <Item> The type of the item.
 */
abstract class BaseListFragment<Binding : ViewDataBinding,
        ViewModel : FragmentListViewModel<Item>,
        Item>(viewLayoutId: Int)
    : BaseFragment<Binding, ViewModel>(viewLayoutId),
        FragmentListViewModel.ListChangedListener {

    protected var recyclerView: RecyclerView? = null

    /**
     * Android lifecycle method called to create the fragment's view
     *
     * @param inflater           The view inflater for the fragment.
     * @param container          The container that the view will be added to.
     * @param savedInstanceState The saved state of the fragment.
     * @return The view for the fragment.
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        recyclerView = view.findViewById(R.id.item_list)
        createAdapter()

        recyclerView?.let {
            setLayoutManager(it)
            setListDecorations(it)
        }

        return view
    }

    /**
     * Creates the list adapter.
     */
    protected abstract fun createAdapter()

    /**
     * Called by the viewmodel when the list has changed.
     */
    abstract override fun onListChanged()

    /**
     * Initializes the item decorations for the RecyclerView.
     * @param recyclerView The RecyclerView to initialize.
     */
    protected open fun setListDecorations(recyclerView: RecyclerView) {}

    /**
     * Initializes the layout manager of the RecyclerView.
     * @param recyclerView The RecyclerView to initialize.
     */
    @Suppress("UNUSED_PARAMETER")
    protected fun setLayoutManager(recyclerView: RecyclerView) {}

    override var viewModel: ViewModel?
        get() = super.viewModel
        set(viewModel) {
            val oldViewModel = viewModel
            oldViewModel?.listChangedListener = null

            super.viewModel = viewModel

            viewModel?.listChangedListener = this
        }
}
