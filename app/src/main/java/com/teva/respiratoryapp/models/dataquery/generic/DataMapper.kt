//
// DataMapper.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.models.dataquery.generic

/**
 * The protocol for mapping managed objects and application data model.
 */
interface DataMapper<T, MO> {
    /**
     * Maps a data model object ot a managed entity.
     * @param source The model object.
     * *
     * @param destination The managed entity.
     */
    fun toManagedEntity(source: T, destination: MO)

    /**
     * Maps a managed entity to a data model object.
     * @param source The managed entity.
     * *
     * @param destination The model object.
     */
    fun toModelObject(source: MO, destination: T)

    /**
     * Called before mapping objects to allow the mapper to optimize by initializing common data.
     */
    fun preMap(toModel: Boolean)

    /**
     * Called after mapping objects to allow the mapper to release common data.
     */
    fun postMap()
}
