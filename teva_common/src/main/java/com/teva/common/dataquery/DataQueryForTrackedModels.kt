//
// DataQueryForTrackedModels.kt
// teva_common
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.common.dataquery

import android.support.annotation.WorkerThread

/**
 * If an object conforms to this protocol, it can provide access to the data store
 * and changes to the data will be tracked.
 */
@WorkerThread
interface DataQueryForTrackedModels<TModel> {

    /**
     * Fetches all the items in the data store.
     */
    fun getAll(): List<TModel>

    /**
     * Gets all the data that are flagged as changed.
     */
    fun getAllChanged(): List<TModel>

    /**
     * Inserts this object in the data store and mark as changed if specified.
     *
     * @param model The model to insert.
     * @param changed A value indicating whether the record should be marked as changed.
     */
    fun insert(model: TModel, changed: Boolean)

    /**
     * Inserts the objects in the data store and mark as changed if specified.
     *
     * @param modelObjects The list of model objects to insert.
     * @param changed A value indicating whether the record should be marked as changed.
     */
    fun insert(modelObjects: List<TModel>, changed: Boolean)

    /**
     * Updates the item in the data store that matches the given object and
     * mark as changed if specified.
     *
     * @param model The model to update.
     * @param changed A value indicating whether the record should be marked as changed.
     */
    fun update(model: TModel, changed: Boolean)

    /**
     * Updates the objects in the data store that matches the given object
     * and mark as changed if specified.
     *
     * @param modelObjects The list of model objects to update.
     * @param changed A value indicating whether the record should be marked as changed.
     */
    fun update(modelObjects: List<TModel>, changed: Boolean)

    /**
     * Inserts the given object in the data store. If it already exists, update instead.
     * Mark the object as changed if specified.
     *
     * @param model The model to insert or update.
     * @param changed A value indicating whether the record should be marked as changed.
     */
    fun insertOrUpdate(model: TModel, changed: Boolean)

    /**
     * Deletes the object from the data store.
     *
     * @param model The model to delete.
     */
    fun delete(model: TModel)

    /**
     * Resets the changed flag and do no pubilsh a model changed message.
     * @param model The model whose changed flag will be reset.
     */
    fun resetChangedFlag(model: TModel, changed: Boolean)
}
