//
// DataQuery.kt
// teva_common
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.common.dataquery

import android.support.annotation.WorkerThread

/**
 * If an object conforms to this interface, it can provide access to the data store.
 * @param <TModel> The type of the model objects accessed via this interface.
 */
@WorkerThread
interface DataQuery<TModel> {

    /**
     * Fetches the first item in the data store.
     */
    fun getFirst(): TModel

    /**
     * Fetches all the items in the data store.
     */
    fun getAll(): List<TModel>

    /**
     * Inserts this object to the data store.
     *
     * @param model The model to insert.
     */
    fun insert(model: TModel)

    /**
     * Inserts an array of objects in the data store.
     *
     * @param modelObjects The list of model objects to insert.
     */
    fun insert(modelList: List<TModel>)

    /**
     * Updates the item in the data store that matches the given object.
     *
     * @param model The model to update.
     */
    fun update(model: TModel)

    /**
     * Updates the items in the data store tha tmatches the given array of objects.
     *
     * @param modelObjects The list of model objects to update.
     */
    fun update(modelList: List<TModel>)

    /**
     * Inserts the given object in the data store.  If it already exists, update instead.
     *
     * @param model The model to insert or update.
     */
    fun insertOrUpdate(model: TModel)

    /**
     * Deletes the object from the data store.
     *
     * @param model The model to delete.
     */
    fun delete(model: TModel)

    /**
     * Checks if there are data in the data store.
     */
    fun hasData(): Boolean

    /**
     * Checks if this data is already in the data store
     *
     * @param model The model to check.
     */
    fun has(model: TModel): Boolean
}
