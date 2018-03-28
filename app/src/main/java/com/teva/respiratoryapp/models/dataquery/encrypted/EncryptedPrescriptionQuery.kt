//
// EncryptedPrescriptionQuery.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.models.dataquery.encrypted

import com.teva.utilities.services.DependencyProvider
import com.teva.medication.entities.Prescription
import com.teva.respiratoryapp.models.dataquery.generic.GenericPrescriptionQuery
import com.teva.respiratoryapp.services.data.QueryInfo
import com.teva.respiratoryapp.services.data.SearchCriteria
import com.teva.respiratoryapp.services.data.encrypteddata.entities.MedicationDataEncrypted

/**
 * The encrypted data query implementation for prescriptions.
 */
class EncryptedPrescriptionQuery(dependencyProvider: DependencyProvider)
    : GenericPrescriptionQuery(
        dependencyProvider,
        dependencyProvider.resolve<EncryptedPrescriptionMapper>()) {

    init {
        resetCache()
    }

    override fun createModel(): Prescription {
        return Prescription()
    }

    /**
     * Returns the unique search criteria to use for fetching the corresponding managed object of the given data model object.
     */
    override fun uniqueSearchCriteria(model: Prescription): SearchCriteria {
        val results: List<MedicationDataEncrypted> = dataService.fetchRequest(MedicationDataEncrypted::class.java, QueryInfo(SearchCriteria( "drugUID = %@", model.medication!!.drugUID)))
        val key = results.first().primaryKeyId
        return SearchCriteria("medication = %@ AND prescriptionDate = %@", key, model.prescriptionDate?.epochSecond)
    }
}
