//
// GenericQueryBase.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.models.dataquery.generic

import com.teva.respiratoryapp.common.messages.ModelUpdateType
import com.teva.respiratoryapp.common.messages.ModelUpdatedMessage
import com.teva.respiratoryapp.services.data.DataService
import com.teva.respiratoryapp.services.data.EntityProtocol
import com.teva.respiratoryapp.services.data.QueryInfo
import com.teva.respiratoryapp.services.data.SearchCriteria
import com.teva.utilities.services.DependencyProvider
import com.teva.common.utilities.Messenger

import java.util.ArrayList

/**
 * The base class that provides common data access methods to the encrypted data store using the encrypted data service.
 */
abstract class GenericQueryBase<MO, E : EntityProtocol>(protected var dependencyProvider: DependencyProvider,
                                                        protected var entityClass: Class<E>,
                                                        protected val mapper: DataMapper<MO, E>) {

    protected var dataService: DataService = dependencyProvider.resolve()

    protected abstract fun uniqueSearchCriteria(model: MO): SearchCriteria

    protected abstract fun resetCache()

    protected abstract fun createModel(): MO

    protected fun uniqueQueryInfo(model: MO): QueryInfo {
        val queryInfo = QueryInfo(uniqueSearchCriteria(model))
        queryInfo.count = 1

        return queryInfo
    }

    /**
     * Inserts the given object into the data store.
     */
    fun insert(model: MO) {
        val modelObjects = ArrayList<MO>()
        modelObjects.add(model)

        insert(modelObjects)
    }

    /**
     * Inserts the array of objects into the data store.
     */
    fun insert(modelObjects: List<MO>) {
        val entities = ArrayList<E>()
        mapper.preMap(false)
        for (model in modelObjects) {
            val entity = dataService.create(entityClass)
            mapper.toManagedEntity(model, entity)

            entities.add(entity)
        }
        mapper.postMap()

        save(modelObjects, entities, true)
    }

    fun update(model: MO) {
        val modelObjects = ArrayList<MO>()
        modelObjects.add(model)

        update(modelObjects)
    }

    /**
     * Updates the item in the data store that matches the given object.
     */
    fun update(modelObjects: List<MO>) {
        val entities = ArrayList<E>()
        mapper.preMap(false)
        for (model in modelObjects) {
            val queryInfo = uniqueQueryInfo(model)
            val fetchResult = dataService.fetchRequest(entityClass, queryInfo)

            if (fetchResult.size == 1) {
                val entity = fetchResult[0]
                mapper.toManagedEntity(model, entity)
                entities.add(entity)
            }
        }
        mapper.postMap()

        save(modelObjects, entities, true)
    }

    fun insertOrUpdate(model: MO) {
        insertOrUpdateModel(model, true)
    }

    /**
     * Inserts the given object in the data store. If it already exists, update instead. This will publish a model updated message.
     * - Parameter object: the object to change.
     */
    fun insertOrUpdateModel(model: MO, publishChanges: Boolean) {
        val modelObjects = ArrayList<MO>()
        modelObjects.add(model)

        val entities = ArrayList<E>()

        val queryInfo = uniqueQueryInfo(model)
        val fetchResult = dataService.fetchRequest(entityClass, queryInfo)
        val entity: E

        if (fetchResult.size == 1) {
            entity = fetchResult[0]
        } else {
            entity = dataService.create(entityClass)
        }

        mapper.preMap(false)
        mapper.toManagedEntity(model, entity)
        mapper.postMap()

        entities.add(entity)

        save(modelObjects, entities, publishChanges)
    }

    /**
     * Deletes the object from the data store.
     */
    fun delete(model: MO) {
        val queryInfo = uniqueQueryInfo(model)

        dataService.delete(entityClass, queryInfo)

        resetCache()
    }

    /**
     * Checks if there are data in the data store
     */
    fun hasData(): Boolean {
        return getCount(null) > 0
    }

    /**
     * Checks if there are data in the data store matching the specified search criteria
     */
    fun hasData(searchCriteria: SearchCriteria): Boolean {
        return getCount(searchCriteria) > 0
    }

    /**
     * Checks if there is data in the data store matching this object
     */
    fun has(model: MO): Boolean {
        return getCount(uniqueSearchCriteria(model)) > 0
    }

    val first: MO?
        get() {
            val queryInfo = QueryInfo(null)
            queryInfo.count = 1

            val modelObjects = readAndMap(queryInfo)

            return if (modelObjects.isNotEmpty()) modelObjects[0] else null
        }

    /**
     * Fetches all the items in the data store.
     */
    open fun getAll(): List<MO> {
        return readAndMap(null)
    }

    /**
     * Returns the number of objects that match the given criteria.
     */
    fun getCount(searchCriteria: SearchCriteria?): Int {
        return dataService.getCount(entityClass, searchCriteria)
    }

    /**
     * Returns the an array of objects that matches the query information.
     */
    fun readBasedOnQuery(query: QueryInfo): List<MO> {
        return readAndMap(query)
    }


    /**
     * Saves the changes to the data service and posts a Model Updated Message.
     */
    fun save(modelObjects: List<MO>, managedEntities: List<E>, publishChanges: Boolean) {
        val searchCriteria = modelObjects.map { uniqueSearchCriteria(it) }

        dataService.save(entityClass, managedEntities, searchCriteria)

        resetCache()

        if (publishChanges) {
            val messenger = dependencyProvider.resolve<Messenger>()
            val objectList = ArrayList<Any>(modelObjects)
            messenger.post(ModelUpdatedMessage(ModelUpdateType.ENTITIES, objectList))
        }
    }

    /**
     * Gets the managed object from the database that matches the given query information and maps the managed object to the data model.
     */
    private fun readAndMap(query: QueryInfo?): List<MO> {
        val entities = dataService.fetchRequest(entityClass, query)

        val modelObjects = ArrayList<MO>()

        mapper.preMap(true)

        for (entity in entities) {
            val model = createModel()
            mapper.toModelObject(entity, model)
            modelObjects.add(model)
        }

        mapper.postMap()

        return modelObjects
    }
}
