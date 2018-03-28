//
// GenericPrescriptionQuery.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.models.dataquery.generic

import android.support.annotation.MainThread
import com.teva.utilities.services.DependencyProvider
import com.teva.medication.dataquery.PrescriptionDataQuery
import com.teva.medication.entities.Prescription
import com.teva.respiratoryapp.services.data.QueryInfo
import com.teva.respiratoryapp.services.data.SearchCriteria
import com.teva.respiratoryapp.services.data.encrypteddata.SortParameter
import com.teva.respiratoryapp.services.data.encrypteddata.entities.PrescriptionDataEncrypted
import org.threeten.bp.Instant

/**
 * An instance of this class allows access to the prescription data in core data.
 */
abstract class GenericPrescriptionQuery(
        dependencyProvider: DependencyProvider,
        mapper: DataMapper<Prescription, PrescriptionDataEncrypted>)
    : GenericQueryBaseForTrackedModels<Prescription, PrescriptionDataEncrypted>(
        dependencyProvider, PrescriptionDataEncrypted::class.java, mapper),
        PrescriptionDataQuery {

    private val prescriptionCache: ModelCache<Prescription> = ModelCache()

    /**
     * Returns the unique search criteria to use for fetching the corresponding managed object of the given data model object.
     * - Parameters:
     * - object: The Prescription model object.
     * - Returns: The unique search criteria for the Prescription.
     */
    override fun uniqueSearchCriteria(model: Prescription): SearchCriteria {
        return SearchCriteria("medication.drugUID = %@ AND prescriptionDate = %@", model.medication!!.drugUID, model.prescriptionDate)
    }

    /**
     * Returns the earliest prescription date.
     * - Returns: The earliest prescription date.
     */
    override fun getEarliestPrescriptionDate(): Instant? {

        val sorting = SortParameter("prescriptionDate", true)
        val query = QueryInfo(null, sorting)


        val results = dataService.fetchRequest(PrescriptionDataEncrypted::class.java, query)

        if (results.isEmpty()) {
            return null
        }

        return results[0].prescriptionDate
    }

    /**
     * Fetches all the items in the data store.
     */
    override fun getAll(): List<Prescription> {
        return prescriptionCache.cache ?: super.getAll()
    }

    @MainThread
    override fun resetCache() {
        prescriptionCache.cache = super.getAll()
    }
}
