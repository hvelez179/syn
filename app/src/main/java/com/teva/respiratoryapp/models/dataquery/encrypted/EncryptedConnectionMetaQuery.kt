//
// EncryptedConnectionMetaQuery.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.models.dataquery.encrypted

import com.teva.utilities.services.DependencyProvider
import com.teva.devices.entities.ConnectionMeta
import com.teva.respiratoryapp.models.dataquery.generic.GenericConnectionMetaQuery
import com.teva.respiratoryapp.services.data.QueryInfo
import com.teva.respiratoryapp.services.data.SearchCriteria
import com.teva.respiratoryapp.services.data.encrypteddata.entities.DeviceDataEncrypted

/**
 * Encrypted data query implementation for device connection meta data.
 * @param dependencyProvider - the dependency provider.
 */

class EncryptedConnectionMetaQuery(dependencyProvider: DependencyProvider)
    : GenericConnectionMetaQuery(
        dependencyProvider,
        dependencyProvider.resolve<EncryptedConnectionMetaMapper>()) {

    /**
     * This method returns unique search criteria to be used
     * while searching for connection meta data.
     *
     * @param model -  the connection meta object.
     * @return - the search criteria to be used for searching.
     */
    override fun uniqueSearchCriteria(model: ConnectionMeta): SearchCriteria {
        val devices = dataService.fetchRequest(DeviceDataEncrypted::class.java,
                QueryInfo(SearchCriteria("serialNumber = %@", model.serialNumber!!)))

        return SearchCriteria("connectionDate = %@ AND device = %@", model.connectionDate,
                devices.first().primaryKeyId)
    }


    /**
     * This method creates a ConnectionMeta model object.
     *
     * @return - a ConnectionMeta object.
     */
    override fun createModel(): ConnectionMeta {
        return ConnectionMeta()
    }
}
