//
// GenericMedicationQuery.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.models.dataquery.generic

import android.content.res.AssetManager
import com.google.gson.Gson
import com.teva.utilities.services.DependencyProvider
import com.teva.medication.dataquery.MedicationDataQuery
import com.teva.medication.dataquery.PrescriptionDataQuery
import com.teva.medication.entities.Medication
import com.teva.medication.entities.Prescription
import com.teva.medication.entities.Product
import com.teva.medication.enumerations.MedicationClassification
import com.teva.respiratoryapp.services.data.SearchCriteria
import com.teva.respiratoryapp.services.data.encrypteddata.entities.MedicationDataEncrypted
import org.threeten.bp.Instant
import java.io.IOException
import java.nio.charset.Charset

/**
 * An instance of this class allows access to the medication data in core data.
 */
abstract class GenericMedicationQuery(
        dependencyProvider: DependencyProvider,
        mapper: DataMapper<Medication, MedicationDataEncrypted>)
    : GenericQueryBaseForTrackedModels<Medication, MedicationDataEncrypted>(
        dependencyProvider, MedicationDataEncrypted::class.java, mapper),
        MedicationDataQuery {

    /**
     * Cache of medication objects.
     */
    private val medicationCache: ModelCache<Medication> = ModelCache()

    private val productCache: ModelCache<Product> = ModelCache()

    private var cacheIsBeingReset: Boolean = false

    /**
     * Returns the unique search criteria to use for fetching the corresponding managed object of the given data model object.
     * - Parameters:
     * - object: The Medication model object.
     * - Returns: The unique search criteria for the Medication.
     */
    override fun uniqueSearchCriteria(model: Medication): SearchCriteria {
        return SearchCriteria("drugUID = %@", model.drugUID)
    }

    /**
     * Gets the medication that matches the drug ID.
     * - Parameters:
     * - drugUID: the drug UID of the Medication being searched for.
     * - Returns: The Medication with matching drug UID.
     */
    override fun get(drugUID: String): Medication? {
        return medicationCache.first { obj -> obj.drugUID == drugUID }
    }

    /**
     * Gets all the medications which belong to the specified medication classification.
     * - Parameters:
     * - medicationClassification: The medication classification; .Reliever, .Controller, .DualUse
     * - Returns: All the Medications with a matching medication classification.
     */
    override fun get(medicationClassification: MedicationClassification): List<Medication> {
        return medicationCache.any { obj -> obj.medicationClassification.contains(medicationClassification) }
    }

    /**
     * Gets the first medication for the given medication classification.
     * - Parameters:
     * - medicationclassification: the medication classification; .Reliever, .Controller, .DualUse
     * - Returns: The first medication that matches the medicationclassification.
     */
    override fun getFirst(medicationClassification: MedicationClassification): Medication? {
        return medicationCache.first { obj -> obj.medicationClassification.contains(medicationClassification) }
    }

    /**
     * Sets the given prescription as the current prescription of the medication it belongs to.
     * - Parameters:
     * - currentPrescription: the current prescription belonging to the medication.
     */
    override fun update(currentPrescription: Prescription) {
        val query = dependencyProvider.resolve<PrescriptionDataQuery>()

        query.insert(currentPrescription, true)

        resetCache()
    }

    /**
     * Gets the earliest prescription date.
     * - Returns: The date of the oldest prescription.
     */
    override val earliestPrescriptionDate: Instant?
        get() {
            val query = dependencyProvider.resolve<PrescriptionDataQuery>()

            return query.getEarliestPrescriptionDate()
        }

    /**
     * Checks if any Medication exists with the given medication classification.
     * - Parameters:
     * - medicationClassification: the medication classification; .Reliever, .Controller, .DualUse
     * - Returns: true if any medication matches the criteria, false otherwise.
     */
    override fun hasData(medicationClassification: MedicationClassification): Boolean {
        return medicationCache.contains { obj -> obj.medicationClassification.contains(medicationClassification) }
    }

    /**
     * Fetches all the items in the data store.
     */
    override fun getAll(): List<Medication> {
        return medicationCache.cache ?: super.getAll()
    }

    override fun resetCache() {
        if(cacheIsBeingReset) {
            return
        }

        cacheIsBeingReset = true
        productCache.cache = reload(Array<Product>::class.java, "medicationsku.json").toList()
        if (dataService.shouldReloadInitialData) {
            reloadMedicationData()
        }
        medicationCache.cache = super.getAll()

        for (medication in medicationCache.cache!!) {
            medication.products = productCache.cache!!.filter { it.drugUID == medication.drugUID }
        }

        cacheIsBeingReset = false
    }

    private fun reloadMedicationData() {

        val medicationDataArray: Array<JsonMedicationData> = reload( Array<JsonMedicationData>::class.java, "medication.json")
        medicationDataArray
                .map { it.toMedication() }
                .forEach { insertOrUpdate(it, false) }
    }

    private fun <T> reload(clazz: Class<Array<T>>, resourceName: String): Array<T> {
        val jsonString = loadJSONFromAsset(resourceName)
        val gson = Gson()
        val returnObjectsList: Array<T> = gson.fromJson(jsonString, clazz)
        return returnObjectsList
    }

    private fun loadJSONFromAsset(fileName: String): String? {
        val assetManager = dependencyProvider.resolve<AssetManager>()
        var json: String? = null
        try {
            val inputStream = assetManager.open(fileName)
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            json = String(buffer, Charset.forName("UTF-8"))
        } catch (ex: IOException) {
            ex.printStackTrace()
            return null
        }

        return json
    }

    private class JsonMedicationData {

        var drugUID: String = ""
        var brandName: String = ""
        var genericName: String = ""
        var overdoseInhalationCount: Int = 0
        var minimumDoseInterval: Int = 0 // minutes
        var minimumScheduleInterval: Int = 0
        var isController: Boolean = false
        var isReliever: Boolean = false

        fun toMedication(): Medication {

            val medication = Medication()
            medication.drugUID = drugUID
            medication.brandName = brandName
            medication.genericName = genericName
            medication.overdoseInhalationCount = overdoseInhalationCount
            medication.minimumDoseInterval = minimumDoseInterval
            medication.minimumScheduleInterval = minimumScheduleInterval
            medication.medicationClassification = MedicationClassification.fromValue(isReliever, isController)
            medication.hasChanged = true
            medication.changeTime = Instant.now()

            return medication
        }

    }

}
