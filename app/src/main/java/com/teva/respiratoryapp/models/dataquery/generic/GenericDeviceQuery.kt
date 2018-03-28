//
// GenericDeviceQuery.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.models.dataquery.generic

import android.support.annotation.MainThread
import com.teva.utilities.services.DependencyProvider
import com.teva.devices.dataquery.DeviceDataQuery
import com.teva.devices.entities.Device
import com.teva.respiratoryapp.services.data.QueryInfo
import com.teva.respiratoryapp.services.data.SearchCriteria
import com.teva.respiratoryapp.services.data.encrypteddata.SortParameter
import com.teva.respiratoryapp.services.data.encrypteddata.entities.DeviceDataEncrypted

/**
 * An instance of this class provides access to device data.
 */
abstract class GenericDeviceQuery(
        dependencyProvider: DependencyProvider,
        mapper: DataMapper<Device, DeviceDataEncrypted>)
    : GenericQueryBaseForTrackedModels<Device, DeviceDataEncrypted>(
        dependencyProvider, DeviceDataEncrypted::class.java, mapper),
        DeviceDataQuery {

    /**
     * Cached device objects used to avoid doing unneccesary queries.
     */
    private val deviceCache: ModelCache<Device> = ModelCache()

    /**
     * Returns the search criteria for returning a unique device data managed object.
     *
     * - Parameters:
     * - object: the Device model object.
     * - Returns:
     * - The search criteria.
     */
    override fun uniqueSearchCriteria(model: Device): SearchCriteria {
        return SearchCriteria("serialNumber = %@", model.serialNumber)
    }

    /**
     * Resets the device cache
     */
    @MainThread
    override fun resetCache() {
        deviceCache.cache = super.getAll()
    }

    /**
     * Gets the device with the given serial number.
     *
     * - Parameters:
     * - serialNumber: the device serial number.
     * - Returns:
     * - the device with the given serial number.
     */
    override fun get(serialNumber: String): Device? {
        val device = deviceCache.first { obj -> obj.serialNumber == serialNumber }

        if (device != null) {
            return device
        }

        val devices = readBasedOnQuery(QueryInfo(SearchCriteria("serialNumber = %@", serialNumber)))
        if (devices.isNotEmpty()) {
            return devices[0]
        }

        return null
    }

    /**
     * Fetches all the items in the data store.
     */
    override fun getAll(): List<Device> {
        return deviceCache.cache ?: super.getAll()
    }

    /**
     * Gets all active devices.
     *
     * - Returns:
     * - All active devices.
     */
    override fun getAllActive(): List<Device> {
        var activeDevices = deviceCache.any { obj -> obj.isActive }

        if (activeDevices.isEmpty()) {
            activeDevices = readBasedOnQuery(
                    QueryInfo(SearchCriteria("isActive = %@", true),
                            SortParameter("lastConnection", false)))
        }

        return activeDevices
    }

    /**
     * Gets the last connected active controller.

     * - Returns:
     * - the active controller device that connected most recently.
     */
    override fun lastConnectedActiveController(): Device? {
        val devices = readBasedOnQuery(QueryInfo(SearchCriteria("isActive = %@", true), SortParameter("lastConnection", false)))
        return devices.firstOrNull {it.medication?.isController == true}
    }

    /**
     * Gets the last connected active reliever.

     * - Returns:
     * - the active reliever device that connected most recently.
     */
    override fun lastConnectedActiveReliever(): Device? {
        val devices = readBasedOnQuery(QueryInfo(SearchCriteria("isActive = %@", true), SortParameter("lastConnection", false)))
        return devices.firstOrNull {it.medication?.isReliever == true}
    }

    /**
     * Marks the device as deleted. This will de-activate and disconnect with the device.

     * - Parameters:
     * - device: the device to be marked as in-active.
     */
    override fun markAsDeleted(device: Device) {
        device.isActive = false
        device.lastConnection = null
        update(device, true)
    }

    /**
     * This method effectively 'un-deletes' a previously deleted device by marking isActive=true

     * - Parameters:
     * - device: the device to be marked as in-active.
     */
    override fun undoMarkAsDeleted(device: Device) {
        device.isActive = true
        update(device, true)
    }

    /**
     * Get a device by nickname.

     * - Parameters:
     * - nickname: the device nickname.
     * - Returns:
     * - true if a device with given nickname exists.
     */
    override fun has(nickname: String): Boolean {
        val foundInCache = deviceCache.contains { obj -> obj.nickname == nickname }

        return foundInCache || hasData(SearchCriteria("nickname = %@", nickname))
    }

    /**
     * Checks if there are active devices.

     * - Returns:
     * - true if there are any active devices.
     */
    override fun hasActiveDevices(): Boolean {
        val foundInCache = deviceCache.contains { obj -> obj.isActive }

        return foundInCache || hasData(SearchCriteria("isActive = %@", true))
    }
}
