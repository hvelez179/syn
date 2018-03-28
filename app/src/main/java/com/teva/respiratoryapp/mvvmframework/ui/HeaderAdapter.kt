//
// HeaderAdapter.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.mvvmframework.ui

/**
 * This interface is used by the HeaderDecorator to retrieve header information from RecyclerView adapters.
 */
interface HeaderAdapter<HeaderViewModel> {
    /**
     * Gets the id of the header at the specified adapter position.
     */
    fun getHeaderId(position: Int): Int

    /**
     * Gets the header viewmodel at the specified adapter position.
     */
    fun getHeaderViewModel(position: Int): HeaderViewModel
}
