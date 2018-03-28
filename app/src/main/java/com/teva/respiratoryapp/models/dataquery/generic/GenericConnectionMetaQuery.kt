//
// GenericConnectionMetaQuery.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.models.dataquery.generic

import com.teva.utilities.services.DependencyProvider
import com.teva.devices.dataquery.ConnectionMetaDataQuery
import com.teva.devices.entities.ConnectionMeta
import com.teva.devices.entities.Device
import com.teva.respiratoryapp.services.data.QueryInfo
import com.teva.respiratoryapp.services.data.SearchCriteria
import com.teva.respiratoryapp.services.data.encrypteddata.SortParameter
import com.teva.respiratoryapp.services.data.encrypteddata.entities.ConnectionMetaDataEncrypted
import com.teva.respiratoryapp.services.data.encrypteddata.entities.DeviceDataEncrypted
import org.threeten.bp.LocalDate
import java.util.*

/**
 * Instances of this class provide access to the connection metadata.
 */
abstract class GenericConnectionMetaQuery(
        dependencyProvider: DependencyProvider,
        mapper: DataMapper<ConnectionMeta, ConnectionMetaDataEncrypted>)
    : GenericQueryBaseForTrackedModels<ConnectionMeta, ConnectionMetaDataEncrypted>(
        dependencyProvider,ConnectionMetaDataEncrypted::class.java, mapper),
        ConnectionMetaDataQuery {

    /**
     * This method resets the cache for query classes which use the cache for
     * returning data. Since this class does not use the cache, we do not do anything here.
     */
    override fun resetCache() {}

    /**
     * Returns the number of connections that were made on a given day.
     *
     * @param date - the date for which the number of connections needs to be returned.
     * @return - the number of connections made on the specified date.
     */
    override fun get(date: LocalDate): Int {
        val queryInfo = QueryInfo(SearchCriteria("connectionDate = %@", date))
        val connectionsMade = readBasedOnQuery(queryInfo)
        return connectionsMade.size
    }

    /**
     * Returns a map containing number of connections made on each day in a date range.
     *
     * @param startDate - the start date of the date range.
     * @param endDate   - the end date of the date range.
     * @return - a map containing the number of connections for each day
     * * in the specified date range.
     */
    override fun get(startDate: LocalDate, endDate: LocalDate): Map<LocalDate, Int> {
        val queryInfo = QueryInfo(SearchCriteria("connectionDate >= %@ AND connectionDate <= %@", startDate, endDate), SortParameter("connectionDate", true))
        val connectionsMade = readBasedOnQuery(queryInfo)
        val connectionHistory = HashMap<LocalDate, Int>()
        for ((key) in connectionsMade) {
            var previousCount = 0
            if (connectionHistory.containsKey(key!!)) {
                previousCount = connectionHistory[key]!!
            }
            connectionHistory[key] = previousCount + 1
        }

        return connectionHistory
    }

    /**
     * Returns the ConnectionMeta for a given device on a given day, if one exists.
     *
     * @param device -  the associated device.
     * @param date   - the date for the connection.
     */
    override fun get(device: Device, date: LocalDate): ConnectionMeta? {

        val queryInfo = QueryInfo(SearchCriteria("serialNumber = %@", device.serialNumber))
        val matchingDevices = dataService.fetchRequest(DeviceDataEncrypted::class.java, queryInfo)

        if (matchingDevices.isNotEmpty()) {
            val matchingDevice = matchingDevices[0]
            val queryInfo2 = QueryInfo(SearchCriteria("connectionDate = %@ AND device like %@", date, matchingDevice.primaryKeyId))
            val connections = readBasedOnQuery(queryInfo2)

            if (connections.isNotEmpty()) {
                return connections[0]
            }
        }
        return null
    }
}
