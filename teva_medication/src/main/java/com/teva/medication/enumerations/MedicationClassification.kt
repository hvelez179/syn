package com.teva.medication.enumerations

import java.util.*

/**
 * Enum listing the different classes of medication.
 * Supports conversion from ordinal values to enum values
 */
enum class MedicationClassification(val value: Int) {
    CONTROLLER(1 shl 0),
    RELIEVER (1 shl 1),
    DUAL_USE (CONTROLLER.value and RELIEVER.value);

    val isController: Boolean
        get() = contains(CONTROLLER)

    val isReliever: Boolean
        get() = contains(RELIEVER)

    fun contains(classification: MedicationClassification): Boolean {
        if ((this.value and classification.value) == classification.value) {
            return true
        }
        return false
    }

    companion object {

        fun fromOrdinal(rawValue: Int): MedicationClassification {
            val matchedClassification = MedicationClassification.values().firstOrNull{rawValue == it.value}
            if(matchedClassification != null) {
                return matchedClassification
            }
            throw IndexOutOfBoundsException("Invalid medication classification")

        }

        fun fromValue(isReliever: Boolean, isController: Boolean): MedicationClassification {
            var value = 0

            if(isReliever) {
                value = value or RELIEVER.value
            }

            if(isController) {
                value = value or CONTROLLER.value
            }

            return fromOrdinal(value)
        }
    }
}
