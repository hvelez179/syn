package com.teva.medication.entities

import com.teva.common.entities.TrackedModelObject
import com.teva.medication.enumerations.MedicationClassification

/**
 * This class provides medication information.
 *
 * @property drugUID The drug id for the medication.
 * @property brandName The brand name for the medication
 * @property genericName The generic name for the medication
 * @property medicationClassification An enumerated value indicating whether the drug is a controller, reliever, or dual-use
 * @property overdoseInhalationCount The number of doses in one day that is considered an overdose
 * @property minimumDoseInterval The minimum interval in minutes recommended between doses of the medication
 * @property minimumScheduleInterval The minimum interval in minutes recommended between two scheduled doses.
 * @property initialDoseCount The initial count of doses for an inhaler with this medication.
 * @property numberOfMonthsBeforeExpiration The expiration duration in months.
 * @property lowDosePercentage Percentage of doses remaining at which the medication is considered 'Near Empty'
 * @property prescriptions The list of prescriptions recorded for this medication
 * @property currentPrescription The most recent prescription for this medication.
 */
class Medication(var drugUID: String = "",
                 var brandName: String = "",
                 var genericName: String = "",
                 var medicationClassification: MedicationClassification = MedicationClassification.DUAL_USE,
                 var overdoseInhalationCount: Int = 0,
                 var minimumDoseInterval: Int = 0,
                 var minimumScheduleInterval: Int = 0,
                 var initialDoseCount: Int = 0,
                 var numberOfMonthsBeforeExpiration: Int = 0,
                 var prescriptions: List<Prescription>? = null,
                 var currentPrescription: Prescription? = null) : TrackedModelObject() {

    var products: List<Product> = ArrayList()

    val nearEmptyDoseCount = 20

    val emptyDoseCountThreshold = 4

    /**
     * A value indicating whether this medication can be used as a controller.
     */
    val isController: Boolean
        get() = medicationClassification.isController

    /**
     * A value indicating whether this medication can be used as a reliever.
     */
    val isReliever: Boolean
        get() = medicationClassification.isReliever
}
