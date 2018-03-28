//
// EncryptedConnectionMetaMapper.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.models.dataquery.encrypted

import com.teva.utilities.services.DependencyProvider
import com.teva.devices.entities.ConnectionMeta
import com.teva.respiratoryapp.models.dataquery.generic.DataMapper
import com.teva.respiratoryapp.services.data.DataService
import com.teva.respiratoryapp.services.data.QueryInfo
import com.teva.respiratoryapp.services.data.SearchCriteria
import com.teva.respiratoryapp.services.data.encrypteddata.EncryptedEntity
import com.teva.respiratoryapp.services.data.encrypteddata.entities.ConnectionMetaDataEncrypted
import com.teva.respiratoryapp.services.data.encrypteddata.entities.DeviceDataEncrypted

/**
 * This is the mapper class for mapping between connection meta entities and connection meta models and vice-versa
 *
 * @param dependencyProvider - the dependency provider.
 */

class EncryptedConnectionMetaMapper(private val dependencyProvider: DependencyProvider)
    : DataMapper<ConnectionMeta, ConnectionMetaDataEncrypted> {

    private var deviceMap: Map<Int, DeviceDataEncrypted>? = null

    /**
     * Maps a data model object to a managed entity.
     *
     * @param source      The model object.
     * @param destination The managed entity.
     */
    override fun toManagedEntity(source: ConnectionMeta, destination: ConnectionMetaDataEncrypted) {
        destination.connectionDate = source.connectionDate
        val dataService = dependencyProvider.resolve<DataService>()
        val queryInfo = QueryInfo(SearchCriteria("serialNumber = %@", source.serialNumber!!))
        val devices = dataService.fetchRequest(DeviceDataEncrypted::class.java, queryInfo)

        destination.device = devices.firstOrNull()
    }

    /**
     * Maps a managed entity to a data model object.
     *
     * @param source      The managed entity.
     * @param destination The model object.
     */
    override fun toModelObject(source: ConnectionMetaDataEncrypted, destination: ConnectionMeta) {
        destination.connectionDate = source.connectionDate
        destination.serialNumber = deviceMap!![source.device!!.primaryKeyId]!!.serialNumber
    }

    /**
     * Called before mapping objects to allow the mapper to optimize by initializing common data.
     */
    override fun preMap(toModel: Boolean) {
        val dataService = dependencyProvider.resolve<DataService>()
        deviceMap = EncryptedEntity.toMap(dataService.fetchRequest(DeviceDataEncrypted::class.java, null))
    }

    /**
     * Called after mapping objects to allow the mapper to release common data.
     */
    override fun postMap() {
        deviceMap = null
    }
}
