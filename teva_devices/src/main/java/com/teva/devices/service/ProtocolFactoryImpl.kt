///
// ProtocolFactoryImpl.kt
// teva_devices
//
// Copyright © 2017 Teva. All rights reserved
///

package com.teva.devices.service

import com.teva.utilities.services.DependencyProvider
import java.util.*

/**
 * Factory object that creates an appropriate Protocol for a specific ConnectionInfo object.
 */
internal class ProtocolFactoryImpl(private val dependencyProvider: DependencyProvider): ProtocolFactory {

    /**
     * Creates a dictionary of Protocols needed for the ConnectionInfos in the specified list.
     */
    override fun createProtocols(connectionInfoList: List<ConnectionInfo>): Map<ProtocolType, Protocol> {
        val protocolTypes = HashSet<ProtocolType>()
        val map = HashMap<ProtocolType, Protocol>()

        for (connectionInfo in connectionInfoList) {
            val protocolType = connectionInfo.protocolType
            if (!protocolTypes.contains(protocolType)) {
                protocolTypes.add(protocolType)
                map.put(protocolType, createProtocol(protocolType))
            }
        }

        return map
    }

    /**
     * Creates a Protocol to be used for the specified ProtocolType.
     *
     * @param protocolType The requested protocol type.
     * @return A new protocol object of the requested type.
     */
    override fun createProtocol(protocolType: ProtocolType): Protocol {
        var protocol: Protocol? = null

        when (protocolType) {
            ProtocolType.Inhaler -> protocol = InhalerProtocol(dependencyProvider)
        }

        return protocol
    }
}
