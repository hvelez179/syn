package com.teva.medication.entities

import com.teva.common.entities.TrackedModelObject

import org.threeten.bp.Instant

/**
 * This class provides prescription information.
 *
 * @property dosesPerDay The number of doses in a day for the prescription.
 * @property inhalesPerDose The number of inhales that make up a dose.
 * @property prescriptionDate The time that a prescription was entered into the app.
 * @property medication The medication that the prescription is associated with.
 */
class Prescription(var dosesPerDay: Int = 0,
                   var inhalesPerDose: Int = 0,
                   var prescriptionDate: Instant? = null,
                   var medication: Medication? = null) : TrackedModelObject() {
    companion object {
        val jsonObjectName = "prescription_medication_order"
    }
}