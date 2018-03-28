//
// EncryptedMedicationMapper.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.models.dataquery.encrypted

import com.teva.respiratoryapp.models.dataquery.generic.DataMapper
import com.teva.respiratoryapp.services.data.DataService
import com.teva.respiratoryapp.services.data.QueryInfo
import com.teva.respiratoryapp.services.data.SearchCriteria
import com.teva.respiratoryapp.services.data.encrypteddata.entities.MedicationDataEncrypted
import com.teva.respiratoryapp.services.data.encrypteddata.entities.PrescriptionDataEncrypted
import com.teva.utilities.services.DependencyProvider
import com.teva.medication.entities.Medication
import com.teva.medication.entities.Prescription
import com.teva.medication.enumerations.MedicationClassification

import java.util.ArrayList

/**
 * This is the mapper class for mapping between medication entities and medication models and vice-versa
 */
class EncryptedMedicationMapper(private val dependencyProvider: DependencyProvider)
    : DataMapper<Medication, MedicationDataEncrypted> {

    /**
     * Maps a data model object ot a managed entity.
     *
     * @param source      The model object.
     * @param destination The managed entity.
     */
    override fun toManagedEntity(source: Medication, destination: MedicationDataEncrypted) {
        destination.drugUID = source.drugUID
        destination.brandName = source.brandName
        destination.genericName = source.genericName
        destination.medicationClassification = source.medicationClassification.value
        destination.overdoseInhalationCount = source.overdoseInhalationCount
        destination.minimumDoseInterval = source.minimumDoseInterval
        destination.minimumScheduleInterval = source.minimumScheduleInterval
        destination.drugUID = source.drugUID
        destination.genericName = source.genericName
        destination.brandName = source.brandName
        destination.numberOfMonthsBeforeExpiration = source.numberOfMonthsBeforeExpiration

        destination.hasChanged = if (source.hasChanged) 1 else 0
        destination.changedTime = source.changeTime
    }

    /**
     * Maps a managed entity to a data model object.
     *
     * @param source      The managed entity.
     * @param destination The model object.
     */
    override fun toModelObject(source: MedicationDataEncrypted, destination: Medication) {
        destination.drugUID = source.drugUID
        destination.brandName = source.brandName
        destination.genericName = source.genericName
        destination.medicationClassification = MedicationClassification.fromOrdinal(source.medicationClassification)
        destination.overdoseInhalationCount = source.overdoseInhalationCount
        destination.minimumDoseInterval = source.minimumDoseInterval
        destination.minimumScheduleInterval = source.minimumScheduleInterval
        destination.numberOfMonthsBeforeExpiration = source.numberOfMonthsBeforeExpiration

        destination.hasChanged = source.hasChanged != 0
        destination.changeTime = source.changedTime

        //destination.initialDoseCount = source.initialDoseCount
        destination.numberOfMonthsBeforeExpiration = source.numberOfMonthsBeforeExpiration

        val prescriptionMapper = dependencyProvider.resolve<EncryptedPrescriptionMapper>()
        val dataService = dependencyProvider.resolve<DataService>()

        // Get all matching prescriptions.
        val searchCriteria = SearchCriteria("medication = %@", source.primaryKeyId)
        val prescriptionResults = dataService.fetchRequest(PrescriptionDataEncrypted::class.java, QueryInfo(searchCriteria))
        val prescriptions = ArrayList<Prescription>()

        for (entity in prescriptionResults) {
            if (entity.medication!!.primaryKeyId == source.primaryKeyId) {
                val prescription = Prescription()

                prescriptionMapper.toModelObject(entity, prescription)
                prescriptions.add(prescription)
            }
        }

        destination.prescriptions = prescriptions
    }

    /**
     * Called before mapping objects to allow the mapper to optimize by initializing common data.
     */
    override fun preMap(toModel: Boolean) {
        dependencyProvider.resolve<EncryptedPrescriptionMapper>().preMap(true)
    }

    /**
     * Called after mapping objects to allow the mapper to release common data.
     */
    override fun postMap() {
        dependencyProvider.resolve<EncryptedPrescriptionMapper>().postMap()
    }
}
