//
// GenericInhaleEventQuery.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.models.dataquery.generic

import com.teva.utilities.services.DependencyProvider
import com.teva.devices.dataquery.InhaleEventDataQuery
import com.teva.devices.entities.Device
import com.teva.devices.entities.InhaleEvent
import com.teva.respiratoryapp.services.data.QueryInfo
import com.teva.respiratoryapp.services.data.SearchCriteria
import com.teva.respiratoryapp.services.data.encrypteddata.SortParameter
import com.teva.respiratoryapp.services.data.encrypteddata.entities.DeviceDataEncrypted
import com.teva.respiratoryapp.services.data.encrypteddata.entities.InhaleEventDataEncrypted
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate

/**
 * The generic implementation of the inhale event data query.
 */
abstract class GenericInhaleEventQuery(
        dependencyProvider: DependencyProvider,
        mapper: DataMapper<InhaleEvent, InhaleEventDataEncrypted>)
    : GenericQueryBaseForTrackedModels<InhaleEvent, InhaleEventDataEncrypted>(
        dependencyProvider, InhaleEventDataEncrypted::class.java, mapper),
        InhaleEventDataQuery {

    override fun uniqueSearchCriteria(model: InhaleEvent): SearchCriteria {
        return SearchCriteria("device = %@ AND eventUID = %@", model.deviceSerialNumber, model.eventUID)
    }

    override fun resetCache() {}

    /**
     * Returns the inhale event with the given event ID and Device.
     * - Parameters:
     * - eventUID: Identifier for the inhale event to get.
     * - device: Device for the inhale event to get.
     * - Returns: Returns the InhaleEvent object if found, otherwise null.
     */
    override operator fun get(eventUID: Int, device: Device): InhaleEvent? {

        val devices = dataService.fetchRequest(DeviceDataEncrypted::class.java, QueryInfo(SearchCriteria("serialNumber = %@", device.serialNumber)))

        if (devices.isEmpty()) {
            return null
        }

        val deviceKey = devices.first().primaryKeyId

        val inhaleEvents = readBasedOnQuery(QueryInfo(SearchCriteria("device = %@ AND eventUID = %@", deviceKey, eventUID)))

        if (inhaleEvents.isEmpty()) {
            return null
        }

        return inhaleEvents[0]
    }

    /**
     * Returns all the inhale events within the given start and end dates. The start and end dates are time zone independent.
     * - Parameters:
     * - startDate: Normalized start date of range to get inhale events.
     * - endDate: Normalized end date of range to get inhale events.
     * - Returns: Returns a list of InhaleEvent objects in the date range passed in.
     */
    override fun get(startDate: LocalDate, endDate: LocalDate): List<InhaleEvent> {
        return readBasedOnQuery(QueryInfo(SearchCriteria("date >= %@ AND date <= %@", startDate, endDate)))
    }

    override fun getCount(serialNumber: String) : Int {
        val deviceData: List<DeviceDataEncrypted> = dataService.fetchRequest( DeviceDataEncrypted::class.java, QueryInfo(SearchCriteria("serialNumber = %@", serialNumber)))
        val key = deviceData.first().primaryKeyId
        return getCount(SearchCriteria("device = %@", key))
    }

    /**
     * Returns the number of inhale events recorded from the date range passed in.
     * - Parameters:
     * - startDate: Normalized start date of range to get inhale events.
     * - endDate: Normalized end date of range to get inhale events.
     * - Returns: Returns the number of inhale events recorded from the startDate through the endDate.
     */
    override fun getCount(startDate: LocalDate, endDate: LocalDate): Int {
        return getCount(SearchCriteria("date >= %@ AND date <= %@", startDate, endDate))
    }

    /**
     * Returns a list of the last (time-ordered) number of inhale events.  The list is returned in reverse chronological order (i.e., last inhalation is first item in list).
     * - Parameters:
     * - numberOfEvents: Maximum number of inhale events to get.
     * - excludingStatuses: Array of statuses to ignore
     * - Returns: Returns up to numberOfEvents InhaleEvents, in reverse chronological order.
     */
    override fun getLast(numberOfEvents: Int, excludingStatuses: IntArray?): List<InhaleEvent>{
        var searchCriteria: SearchCriteria? = null
        if (excludingStatuses!= null && excludingStatuses.isNotEmpty()) {
            var status: Int = 0
            excludingStatuses.forEach({ status = status or it })
            searchCriteria = SearchCriteria("(status & ${status}) = 0")
        }
        val query = QueryInfo(searchCriteria, SortParameter("date", false), SortParameter("timezoneOffset", false))
        query.count = numberOfEvents
        return readBasedOnQuery(query)
    }

    /**
     * Returns a list of inhale events for a given Device.
     * - Parameters:
     * - device: Device for the inhale events to get.
     * - Returns: Returns a list of inhale events for a given Device.
     */
    override fun get(device: Device): List<InhaleEvent> {
        val devices = dataService.fetchRequest(DeviceDataEncrypted::class.java, QueryInfo(SearchCriteria("serialNumber = %@", device.serialNumber)))

        if (devices.isEmpty()) {
            return ArrayList()
        }

        val deviceKey = devices[0].primaryKeyId

        return readBasedOnQuery(QueryInfo(SearchCriteria("device = %@", deviceKey)))
    }

    /**
     * Checks if there are events for the given device.
     * - Parameters:
     * - device: Device to check for event data.
     * - Returns: Returns true if there are events for the device, otherwise false.
     */
    override fun hasData(device: Device): Boolean {
        val devices = dataService.fetchRequest(DeviceDataEncrypted::class.java, QueryInfo(SearchCriteria("serialNumber = %@", device.serialNumber)))

        if (devices.isEmpty()) {
            return false
        }

        val deviceKey = devices.first().primaryKeyId

        return getCount(SearchCriteria("device = %@", deviceKey)) > 0
    }

    /**
     * Returns the earliest inhale event date, or null if there are no inhale events.
     * - Returns: Returns the date of the first recorded Inhale Event, if it exists, otherwise null.
     */
    override fun getEarliestInhaleEventDate(): Instant? {
        val query = QueryInfo(null, SortParameter("date", true), SortParameter("timezoneOffset", true))
        query.count = 1
        val inhaleEvents = readBasedOnQuery(query)

        if (inhaleEvents.isEmpty()) {
            return null
        }

        return inhaleEvents[0].eventTime
    }
}
