//
// GenericQueryBaseForTrackedModels.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.models.dataquery.generic

import com.teva.common.entities.TrackedModelObject
import com.teva.utilities.services.DependencyProvider
import com.teva.respiratoryapp.services.data.EntityProtocol
import com.teva.respiratoryapp.services.data.QueryInfo
import com.teva.respiratoryapp.services.data.SearchCriteria

/**
 * The base class for data query on tracked model objects.
 */
abstract class GenericQueryBaseForTrackedModels<T : TrackedModelObject, MO : EntityProtocol>(
        dependencyProvider: DependencyProvider,
        entityClass: Class<MO>,
        mapper: DataMapper<T, MO>)
    : GenericQueryBase<T, MO>(dependencyProvider, entityClass, mapper) {

    /**
     * Gets all the objects marked as changed.
     */
    fun getAllChanged(): List<T> {
            val searchCriteria = SearchCriteria("hasChanged = %@", true)
            val query = QueryInfo(searchCriteria)

            return readBasedOnQuery(query)
        }

    /**
     * Gets all the objects that have not a server time offset applied.
     */
    fun getAllRequiringServerTimeOffset(): List<T> {
        val query = QueryInfo(SearchCriteria("(CASE WHEN serverTimeOffset IS NULL THEN 1 ELSE 0) = %@", true))
        return readBasedOnQuery(query)
    }

    /**
     * Inserts this object in the data store and mark as changed if specified.
     */
    fun insert(model: T, changed: Boolean) {
        model.markAsChanged(changed)
        insert(model)
    }

    /**
     * Inserts the objects in the data store and mark as changed if specified.
     */
    fun insert(modelObjects: List<T>, changed: Boolean) {
        if (changed) {
            for (model in modelObjects) {
                model.markAsChanged(true)
            }
        }

        insert(modelObjects)
    }

    /**
     * Updates the item in the data store that matches the given object and mark as changed if specified.
     */
    fun update(model: T, changed: Boolean) {
        model.markAsChanged(changed)
        update(model)
    }

    /**
     * Updates the objects in the data store that matches the given object and mark as changed if specified.
     */
    fun update(modelObjects: List<T>, changed: Boolean) {
        if (changed) {
            for (model in modelObjects) {
                model.markAsChanged(true)
            }
        }

        update(modelObjects)
    }

    /**
     * Inserts the given object in the data store. If it already exists, update instead. Mark the object as changed if specified.
     */
    fun insertOrUpdate(model: T, changed: Boolean) {
        model.markAsChanged(changed)
        insertOrUpdate(model)
    }

    /**
     * Reset the change flag without raising a model updated.
     */
    fun resetChangedFlag(model: T, changed: Boolean) {
        model.hasChanged = changed
        insertOrUpdateModel(model, false)
    }
}
