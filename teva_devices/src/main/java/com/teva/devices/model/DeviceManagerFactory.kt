//
// DeviceManagerFactory.kt
// teva_devices
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.devices.model

import com.teva.utilities.services.DependencyProvider
import com.teva.devices.dataquery.DeviceDataQuery
import com.teva.devices.service.*

/**
 * Factory object for creating concrete DeviceManger objects
 *
* @param deviceDataQuery The query object used by the DeviceManagerImpl to fulfill database requests.
 */
class DeviceManagerFactory(dependencyProvider: DependencyProvider,
                           deviceDataQuery: DeviceDataQuery) {

    private val dependencyProvider: DependencyProvider = DependencyProvider(dependencyProvider)
    private val instance by lazy { DeviceManagerImpl(dependencyProvider, deviceDataQuery) }

    init {
        dependencyProvider.register(DeviceService::class) { DeviceServiceImpl(this.dependencyProvider) }
        dependencyProvider.register(Scanner::class) { BLEScanner(this.dependencyProvider) }
        dependencyProvider.register(ProtocolFactory::class) { ProtocolFactoryImpl(this.dependencyProvider) }
    }

    val deviceManager: DeviceManager
        get() = instance

    val deviceQuery: DeviceQuery
        get() = instance.deviceQuery
}
