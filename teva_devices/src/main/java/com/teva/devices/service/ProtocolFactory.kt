//
// ProtocolFactory.kt
// teva_devices
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.devices.service

/**
 * Factory object that creates an appropriate Protocol for a specific ConnectionInfo object.
 */
internal interface ProtocolFactory {

    /**
     * Creates a dictionary of Protocols needed for the ConnectionInfos in the specified list.
     */
    fun createProtocols(connectionInfoList: List<ConnectionInfo>): Map<ProtocolType, Protocol>

    /**
     * Creates a Protocol to be used for the specified ProtocolType.
     *
     * @param protocolType The requested protocol type.
     * @return A new protocol object of the requested type.
     */
    fun createProtocol(protocolType: ProtocolType): Protocol
}
