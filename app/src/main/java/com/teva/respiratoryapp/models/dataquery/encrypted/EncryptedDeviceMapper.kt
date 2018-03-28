//
// EncryptedDeviceMapper.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.models.dataquery.encrypted

import com.teva.utilities.services.DependencyProvider
import com.teva.devices.entities.Device
import com.teva.devices.enumerations.InhalerNameType
import com.teva.medication.entities.Medication
import com.teva.respiratoryapp.models.dataquery.generic.DataMapper
import com.teva.respiratoryapp.services.data.DataService
import com.teva.respiratoryapp.services.data.QueryInfo
import com.teva.respiratoryapp.services.data.SearchCriteria
import com.teva.respiratoryapp.services.data.encrypteddata.entities.DeviceDataEncrypted
import com.teva.respiratoryapp.services.data.encrypteddata.entities.MedicationDataEncrypted

/**
 * This is the mapper class for mapping between device entities and device models and vice-versa
 */
class EncryptedDeviceMapper(private val dependencyProvider: DependencyProvider) : DataMapper<Device, DeviceDataEncrypted> {

    /**
     * Maps a data model object to a managed entity.
     *
     * @param source      The model object.
     * @param destination The managed entity.
     */
    override fun toManagedEntity(source: Device, destination: DeviceDataEncrypted) {
        destination.isActive = if (source.isActive) 1 else 0
        destination.dateCode = source.dateCode
        destination.doseCount = source.doseCount
        destination.expirationDate = source.expirationDate
        destination.hardwareRevision = source.hardwareRevision
        destination.softwareRevision = source.softwareRevision
        destination.inhalerNameType = source.inhalerNameType.ordinal
        destination.lastConnection = source.lastConnection
        destination.lotCode = source.lotCode
        destination.lastRecordId = source.lastRecordId
        destination.manufacturerName = source.manufacturerName
        destination.nickname = source.nickname
        destination.remainingDoseCount = source.remainingDoseCount
        destination.serialNumber = source.serialNumber
        destination.sharedKey = source.authenticationKey
        destination.changedTime = source.changeTime
        destination.hasChanged = if (source.hasChanged) 1 else 0
        destination.serverTimeOffset = source.serverTimeOffset

        val medication = source.medication
        if (medication != null) {
            val drugUID = medication.drugUID
            val dataService = dependencyProvider.resolve<DataService>()
            val searchCriteria = SearchCriteria("drugUID = %@", drugUID)
            val results = dataService.fetchRequest(MedicationDataEncrypted::class.java, QueryInfo(searchCriteria))
            if (results.isNotEmpty()) {
                destination.medication = results[0]
            }
        }
    }

    /**
     * Maps a managed entity to a data model object.
     *
     * @param source      The managed entity.
     * @param destination The model object.
     */
    override fun toModelObject(source: DeviceDataEncrypted, destination: Device) {
        destination.isActive = source.isActive != 0
        destination.dateCode = source.dateCode
        destination.serialNumber = source.serialNumber
        destination.manufacturerName = source.manufacturerName
        destination.nickname = source.nickname
        destination.authenticationKey = source.sharedKey
        destination.doseCount = source.doseCount
        destination.expirationDate = source.expirationDate
        destination.hardwareRevision = source.hardwareRevision
        destination.inhalerNameType = InhalerNameType.fromOrdinal(source.inhalerNameType)!!
        destination.lastConnection = source.lastConnection
        destination.lastRecordId = source.lastRecordId
        destination.lotCode = source.lotCode
        destination.remainingDoseCount = source.remainingDoseCount
        destination.softwareRevision = source.softwareRevision

        destination.hasChanged = source.hasChanged != 0
        destination.changeTime = source.changedTime
        destination.serverTimeOffset = source.serverTimeOffset

        val medicationEntity = source.medication
        if (medicationEntity != null) {

            val dataService = dependencyProvider.resolve<DataService>()
            val medicationMapper = dependencyProvider.resolve<EncryptedMedicationMapper>()
            val searchCriteria = SearchCriteria("Z_PK = %@", medicationEntity.primaryKeyId)
            val results = dataService.fetchRequest(MedicationDataEncrypted::class.java, QueryInfo(searchCriteria))

            if (results.isNotEmpty()) {
                val medication = Medication()

                medicationMapper.toModelObject(results[0], medication)

                destination.medication = medication
            }
        }
    }

    /**
     * Called before mapping objects to allow the mapper to optimize by initializing common data.
     */
    override fun preMap(toModel: Boolean) {
        dependencyProvider.resolve<EncryptedMedicationMapper>().preMap(toModel)
    }

    /**
     * Called after mapping objects to allow the mapper to release common data.
     */
    override fun postMap() {
        dependencyProvider.resolve<EncryptedMedicationMapper>().postMap()
    }
}
