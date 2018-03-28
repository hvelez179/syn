//
// IItemViewModel.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.mvvmframework.ui

/**
 * Interface implemented by list item viewmodels to provide a method to set the model object
 * that the viewmodel is representing.
 *
 * @param <Item>
 */
interface IItemViewModel<Item> {
    /**
     * Sets a model object as the item for this viewmodel.
     *
     * @param item The model object to use as the item.
     */
    fun setItem(item: Item)
}
