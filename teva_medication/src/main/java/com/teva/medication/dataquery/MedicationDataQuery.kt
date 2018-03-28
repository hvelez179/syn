package com.teva.medication.dataquery

import android.support.annotation.WorkerThread

import com.teva.common.dataquery.DataQueryForTrackedModels
import com.teva.medication.entities.Medication
import com.teva.medication.entities.Prescription
import com.teva.medication.enumerations.MedicationClassification

import org.threeten.bp.Instant

/**
 * Classes conforming to this protocol allow access to the medication data.
 */
@WorkerThread
interface MedicationDataQuery : DataQueryForTrackedModels<Medication> {
    /**
     * Gets the medication that matches the drug ID.
     *
     * @param drugUID the drug UID of the Medication being searched for.
     * @return The Medication with matching drug UID.
     */
    operator fun get(drugUID: String): Medication?

    /**
     * Gets the list of medications for the given medication class.
     *
     * @param MedicationClassification The medication class; .Reliever, .Controller, .DualUse
     * @return All the Medications with a matching medication class.
     */
    operator fun get(medicationClassification: MedicationClassification): List<Medication>

    /**
     * Gets the first medication for the given medication class.
     *
     * @param MedicationClassification the medication class; .Reliever, .Controller, .DualUse
     * @return The first medication that matches the MedicationClassification.
     */
    fun getFirst(medicationClassification: MedicationClassification): Medication?

    /**
     * Gets the earliest prescription date.
     *
     * @return The date of the oldest prescription.
     */
    val earliestPrescriptionDate: Instant?

    /**
     * Checks if any Medication exists with the given medication class.
     *
     * @param MedicationClassification the medication class; .Reliever, .Controller, .DualUse
     * @return True if any medication matches the criteria, false otherwise.
     */
    fun hasData(medicationClassification: MedicationClassification): Boolean

    /**
     * Sets the given prescription as the current prescription of the medication it belongs to .
     */
    fun update(currentPrescription: Prescription)
}
