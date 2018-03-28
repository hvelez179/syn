//
// EncryptedInhaleEventMapper.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.models.dataquery.encrypted

import android.annotation.SuppressLint
import com.teva.utilities.services.DependencyProvider
import com.teva.devices.entities.InhaleEvent
import com.teva.respiratoryapp.models.dataquery.generic.DataMapper
import com.teva.respiratoryapp.services.data.DataService
import com.teva.respiratoryapp.services.data.QueryInfo
import com.teva.respiratoryapp.services.data.SearchCriteria
import com.teva.respiratoryapp.services.data.encrypteddata.entities.DeviceDataEncrypted
import com.teva.respiratoryapp.services.data.encrypteddata.entities.InhaleEventDataEncrypted
import com.teva.respiratoryapp.services.data.encrypteddata.entities.MedicationDataEncrypted
import org.threeten.bp.ZoneOffset

/**
 * This is the mapper class for mapping between inhale event entities and inhale event models and vice-versa
 */
class EncryptedInhaleEventMapper(private val dependencyProvider: DependencyProvider) : DataMapper<InhaleEvent, InhaleEventDataEncrypted> {

    private var deviceToDrugId: Map<Int, String>? = null
    private var deviceToSerialNumber: Map<Int, String>? = null

    /**
     * Maps a data model object to a managed entity.
     *
     * @param source      The model object.
     * @param destination The managed entity.
     */
    override fun toManagedEntity(source: InhaleEvent, destination: InhaleEventDataEncrypted) {
        destination.eventUID = source.eventUID

        // Inhale dose event time is stored as normalizedDate, time of day and timezone in the database
        destination.eventTime = source.eventTime
        destination.timezoneOffset = source.timezoneOffsetMinutes

        destination.inhaleEventTime = source.inhaleEventTime
        destination.duration = source.inhaleDuration
        destination.inhalePeak = source.inhalePeak
        destination.inhaleTimeToPeak = source.inhaleTimeToPeak
        destination.status = source.status
        destination.closeTime = source.closeTime
        destination.doseId = source.doseId
        destination.cartridgeUID = source.cartridgeUID
        destination.upperThresholdTime = source.upperThresholdTime
        destination.upperThresholdDuration = source.upperThresholdDuration
        destination.isValidInhale = if (source.isValidInhale) 1 else 0
        destination.serverTimeOffset = source.serverTimeOffset

        // Save eventTime as LocalDate for querying.
        val eventTime = source.eventTime
        val timezoneOffsetMinutes = source.timezoneOffsetMinutes
        val zoneOffset = ZoneOffset.ofHoursMinutes(timezoneOffsetMinutes / MINUTES_PER_HOUR,
                Math.abs(timezoneOffsetMinutes) % MINUTES_PER_HOUR)
        val date = eventTime!!.atOffset(zoneOffset).toLocalDate()
        destination.date = date

        destination.changedTime = source.changeTime
        destination.hasChanged = if (source.hasChanged) 1 else 0

        val deviceSerialNumber = source.deviceSerialNumber
        if (!deviceSerialNumber.isNullOrEmpty()) {
            val dataService = dependencyProvider.resolve<DataService>()
            val devices = dataService.fetchRequest(DeviceDataEncrypted::class.java, QueryInfo(SearchCriteria("serialNumber = %@", source.deviceSerialNumber)))

            if (devices.isNotEmpty()) {
                destination.device = devices[0]
            }
        }
    }

    /**
     * Maps a managed entity to a data model object.
     *
     * @param source      The managed entity.
     * @param destination The model object.
     */
    override fun toModelObject(source: InhaleEventDataEncrypted, destination: InhaleEvent) {
        destination.eventUID = source.eventUID
        destination.eventTime = source.eventTime
        destination.timezoneOffsetMinutes = source.timezoneOffset


        destination.inhaleEventTime = source.inhaleEventTime
        destination.inhaleDuration = source.duration
        destination.inhalePeak = source.inhalePeak
        destination.inhaleTimeToPeak = source.inhaleTimeToPeak
        destination.status = source.status

        destination.closeTime = source.closeTime
        destination.doseId = source.doseId
        destination.cartridgeUID = source.cartridgeUID
        destination.upperThresholdTime = source.upperThresholdTime
        destination.upperThresholdDuration = source.upperThresholdDuration
        destination.isValidInhale = source.isValidInhale != 0

        destination.hasChanged = source.hasChanged != 0
        destination.changeTime = source.changedTime

        destination.drugUID = deviceToDrugId!![source.device!!.primaryKeyId]!!
        destination.deviceSerialNumber = deviceToSerialNumber!![source.device!!.primaryKeyId]!!
        destination.serverTimeOffset = source.serverTimeOffset
    }

    /**
     * Called before mapping objects to allow the mapper to optimize by initializing common data.
     */
    @SuppressLint("UseSparseArrays")
    override fun preMap(toModel: Boolean) {
        if (toModel) {
            val dataService = dependencyProvider.resolve<DataService>()

            val medicationEntities = dataService.fetchRequest(MedicationDataEncrypted::class.java, null)
            val medicationMap = medicationEntities.associateBy { it.primaryKeyId }

            val deviceEntities = dataService.fetchRequest(DeviceDataEncrypted::class.java, null)

            deviceToDrugId = deviceEntities.associate {
                it.primaryKeyId to medicationMap[it.medication!!.primaryKeyId]!!.drugUID
            }

            deviceToSerialNumber = deviceEntities.associate { it.primaryKeyId to it.serialNumber }
        }
    }

    /**
     * Called after mapping objects to allow the mapper to release common data.
     */
    override fun postMap() {
        deviceToDrugId = null
        deviceToSerialNumber = null
    }

    companion object {
        private val MINUTES_PER_HOUR = 60
    }
}
