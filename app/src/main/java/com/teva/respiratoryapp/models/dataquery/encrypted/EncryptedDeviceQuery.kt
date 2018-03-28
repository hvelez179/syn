//
// EncryptedDeviceQuery.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.models.dataquery.encrypted

import com.teva.utilities.services.DependencyProvider
import com.teva.devices.entities.Device
import com.teva.respiratoryapp.models.dataquery.generic.GenericDeviceQuery

/**
 * Encrypted data query implementation for device data.
 */
class EncryptedDeviceQuery(dependencyProvider: DependencyProvider)
    : GenericDeviceQuery(
        dependencyProvider,
        dependencyProvider.resolve<EncryptedDeviceMapper>()) {
    init {
        resetCache()
    }

    override fun createModel(): Device {
        return Device()
    }
}
