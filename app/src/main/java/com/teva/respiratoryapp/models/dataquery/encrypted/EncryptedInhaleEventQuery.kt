//
// EncryptedInhaleEventQuery.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.models.dataquery.encrypted


import com.teva.utilities.services.DependencyProvider
import com.teva.devices.entities.InhaleEvent
import com.teva.respiratoryapp.models.dataquery.generic.GenericInhaleEventQuery
import com.teva.respiratoryapp.services.data.QueryInfo
import com.teva.respiratoryapp.services.data.SearchCriteria
import com.teva.respiratoryapp.services.data.encrypteddata.entities.DeviceDataEncrypted

/**
 * The encrypted data query implementation for inhale events.
 */
class EncryptedInhaleEventQuery(dependencyProvider: DependencyProvider)
    : GenericInhaleEventQuery(
        dependencyProvider,
        dependencyProvider.resolve<EncryptedInhaleEventMapper>()) {

    override fun createModel(): InhaleEvent {
        return InhaleEvent()
    }

    /**
     * Returns the unique search criteria to use for fetching the corresponding managed object of the given data model object.
     */
    override fun uniqueSearchCriteria(model: InhaleEvent): SearchCriteria {
        val results: List<DeviceDataEncrypted> = dataService.fetchRequest(DeviceDataEncrypted::class.java, QueryInfo(SearchCriteria( "serialNumber = %@", model.deviceSerialNumber)))
        val deviceID = results.first().primaryKeyId

        return SearchCriteria("device = %@ AND eventUID = %@", deviceID, model.eventUID)
    }
}
