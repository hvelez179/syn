//
// ConnectionMetaDataQuery.kt
// teva_devices
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.devices.dataquery

import com.teva.common.dataquery.DataQueryForTrackedModels
import com.teva.devices.entities.ConnectionMeta
import com.teva.devices.entities.Device
import org.threeten.bp.LocalDate

/**
 * Classes conforming to this interface allow access to the device connection metadata.
 */

interface ConnectionMetaDataQuery : DataQueryForTrackedModels<ConnectionMeta> {

    /**
     * Returns the number of connections that were made on a given day.

     * @param date - the date for which the number of connections needs to be returned.
     * *
     * @return - the number of connections made on the specified date.
     */
    fun get(date: LocalDate): Int

    /**
     * Returns a map containing number of connections made on each day in a date range.

     * @param startDate - the start date of the date range.
     * *
     * @param endDate   - the end date of the date range.
     * *
     * @return - a map containing the number of connections for each day
     * * in the specified date range.
     */
    fun get(startDate: LocalDate, endDate: LocalDate): Map<LocalDate, Int>

    /**
     * Returns the ConnectionMeta for a given device on a given day, if one exists.

     * @param device -  the associated device.
     * *
     * @param date   - the date for the connection.
     */
    fun get(device: Device, date: LocalDate): ConnectionMeta?
}
