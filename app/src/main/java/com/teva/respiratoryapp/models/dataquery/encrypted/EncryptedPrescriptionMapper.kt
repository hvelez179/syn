//
// EncryptedPrescriptionMapper.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.models.dataquery.encrypted

import com.teva.utilities.services.DependencyProvider
import com.teva.medication.entities.Medication
import com.teva.medication.entities.Prescription
import com.teva.respiratoryapp.models.dataquery.generic.DataMapper
import com.teva.respiratoryapp.services.data.DataService
import com.teva.respiratoryapp.services.data.QueryInfo
import com.teva.respiratoryapp.services.data.SearchCriteria
import com.teva.respiratoryapp.services.data.encrypteddata.EncryptedEntity
import com.teva.respiratoryapp.services.data.encrypteddata.entities.MedicationDataEncrypted
import com.teva.respiratoryapp.services.data.encrypteddata.entities.PrescriptionDataEncrypted

/**
 * This is the mapper class for mapping between prescription entities and prescription models and vice-versa
 */
class EncryptedPrescriptionMapper(private val dependencyProvider: DependencyProvider) : DataMapper<Prescription, PrescriptionDataEncrypted> {

    private var medicationMap: Map<Int, MedicationDataEncrypted>? = null

    /**
     * Maps a data model object ot a managed entity.
     *
     * @param source      The model object.
     * @param destination The managed entity.
     */
    override fun toManagedEntity(source: Prescription, destination: PrescriptionDataEncrypted) {
        destination.dosesPerDay = source.dosesPerDay
        destination.inhalesPerDose = source.inhalesPerDose
        destination.prescriptionDate = source.prescriptionDate

        destination.hasChanged = if (source.hasChanged) 1 else 0
        destination.changedTime = source.changeTime
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
    override fun toModelObject(source: PrescriptionDataEncrypted, destination: Prescription) {
        destination.dosesPerDay = source.dosesPerDay
        destination.inhalesPerDose = source.inhalesPerDose
        destination.prescriptionDate = source.prescriptionDate

        destination.hasChanged = source.hasChanged != 0
        destination.changeTime = source.changedTime
        destination.serverTimeOffset = source.serverTimeOffset

        val medicationEntity = source.medication
        if (medicationEntity != null) {
            val fullMedicationEntity = medicationMap!![medicationEntity.primaryKeyId]
            if (fullMedicationEntity != null) {
                val medication = Medication()
                medication.drugUID = fullMedicationEntity.drugUID
                destination.medication = medication
            }
        }
    }


    /**
     * Called before mapping objects to allow the mapper to optimize by initializing common data.
     */
    override fun preMap(toModel: Boolean) {
        val dataService = dependencyProvider.resolve<DataService>()
        medicationMap = EncryptedEntity.toMap(dataService.fetchRequest(MedicationDataEncrypted::class.java, null))
    }

    /**
     * Called after mapping objects to allow the mapper to release common data.
     */
    override fun postMap() {
        medicationMap = null
    }
}
