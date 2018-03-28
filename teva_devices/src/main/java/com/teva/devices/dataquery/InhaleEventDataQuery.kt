//
// InhaleEventDataQuery.kt
// teva_devices
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.devices.dataquery

import android.support.annotation.WorkerThread

import com.teva.common.dataquery.DataQueryForTrackedModels
import com.teva.devices.entities.Device
import com.teva.devices.entities.InhaleEvent

import org.threeten.bp.Instant
import org.threeten.bp.LocalDate

@WorkerThread
interface InhaleEventDataQuery : DataQueryForTrackedModels<InhaleEvent> {

    /**
     * Returns the inhale event with the given event ID and Device.
     * - Parameters:
     * - eventUID: Identifier for the inhale event to get.
     * - device: Device for the inhale event to get.
     * - Returns: Returns the InhaleEvent object if found, otherwise null.
     */
    fun get(eventUID: Int, device: Device): InhaleEvent?

    /**
     * Returns all the inhale events within the given start and end dates. The start and end dates are time zone independent.
     * - Parameters:
     * - startDate: Normalized start date of range to get inhale events.
     * - endDate: Normalized end date of range to get inhale events.
     * - Returns: Returns a list of InhaleEvent objects in the date range passed in.
     */
    fun get(startDate: LocalDate, endDate: LocalDate): List<InhaleEvent>

    /**
     * Returns the number of inhale events recorded for the inhaler with the given serial number.
     * @param serialNumber: The serial number of the device where the inhale event originated from
     * @return: The number of inhale events
     */
    fun getCount(serialNumber: String): Int

    /**
     * Returns the number of inhale events recorded from the date range passed in.
     * - Parameters:
     * - startDate: Normalized start date of range to get inhale events.
     * - endDate: Normalized end date of range to get inhale events.
     * - Returns: Returns the number of inhale events recorded from the startDate through the endDate.
     */
    fun getCount(startDate: LocalDate, endDate: LocalDate): Int

    /**
     * Returns a list of the last (time-ordered) number of inhale events.  The list is returned in reverse chronological order (i.e., last inhalation is first item in list).
     * - Parameters:
     * - numberOfEvents: Maximum number of inhale events to get.
     * - Returns: Returns up to numberOfEvents InhaleEvents, in reverse chronological order.
     */
    fun getLast(numberOfEvents: Int, excludingStatuses: IntArray? = null): List<InhaleEvent>

    /**
     * Returns a list of inhale events for a given Device.
     * - Parameters:
     * - device: Device for the inhale events to get.
     * - Returns: Returns a list of inhale events for a given Device.
     */
    fun get(device: Device): List<InhaleEvent>

    /**
     * Checks if there are events for the given device.
     * - Parameters:
     * - device: Device to check for event data.
     * - Returns: Returns true if there are events for the device, otherwise false.
     */
    fun hasData(device: Device): Boolean

    /**
     * Returns the earliest inhale event date, or null if there are no inhale events.
     * - Returns: Returns the date of the first recorded Inhale Event, if it exists, otherwise null.
     */
    fun getEarliestInhaleEventDate(): Instant?
}
