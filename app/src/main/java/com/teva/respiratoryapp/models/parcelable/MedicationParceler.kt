//
// MedicationParceler.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.models.parcelable

import android.os.Parcel
import android.os.Parcelable

import com.teva.medication.entities.Medication
import com.teva.medication.entities.Prescription
import com.teva.medication.enumerations.MedicationClassification

import java.util.ArrayList

/**
 * This class wraps a Device object and allows it to be added to a Bundle as a Parcelable object.
 */
class MedicationParceler : Parcelable {

    /**
     * Gets the Medication held by this parceler.
     */
    var medication: Medication? = null
        private set

    /**
     * Constructor that initializes the parceler with a Medication.

     * @param medication The Medication to wrap by this parceler.
     */
    constructor(medication: Medication?) {
        this.medication = medication
    }

    /**
     * Constructor used when reading from a parcel.

     * @param in The parcel to read the object's data from.
     */
    private constructor(parcel: Parcel) {
        // check "Object Present Flag" to know whether the object should be
        // deserialized or left as null.
        if (parcel.readInt() == TRUE) {
            val medication = Medication()
            medication.drugUID = parcel.readString()
            medication.brandName = parcel.readString()
            medication.genericName = parcel.readString()

            val ordinalValue = parcel.readValue(MedicationParceler::class.java.classLoader) as Int
            medication.medicationClassification = MedicationClassification.fromOrdinal(ordinalValue)

            medication.overdoseInhalationCount = parcel.readInt()
            medication.minimumDoseInterval = parcel.readInt()
            medication.minimumScheduleInterval = parcel.readInt()
            medication.initialDoseCount = parcel.readInt()
            medication.numberOfMonthsBeforeExpiration = parcel.readInt()
            //medication.lowDosePercentage = parcel.readInt()

            val parcelerList = ArrayList<PrescriptionParceler>()
            parcel.readList(parcelerList, MedicationParceler::class.java.classLoader)

            val prescriptions = ArrayList<Prescription>()
            for (parceler in parcelerList) {
                prescriptions.add(parceler.prescription!!)
            }

            medication.prescriptions = prescriptions

            val currentPrescriptionParceler = parcel.readParcelable<PrescriptionParceler>(PrescriptionParceler::class.java.classLoader)
            medication.currentPrescription = currentPrescriptionParceler.prescription

            this.medication = medication
        }
    }

    /**
     * Flatten this object in to a Parcel.

     * @param dest  The Parcel in which the object should be written.
     * *
     * @param flags Additional flags about how the object should be written.
     * *              May be 0 or [.PARCELABLE_WRITE_RETURN_VALUE].
     */
    override fun writeToParcel(dest: Parcel, flags: Int) {
        if (medication == null) {
            // Write a "false" to indicate the object is null
            dest.writeInt(FALSE)
        } else {
            // Write a "true" to indicate the object is present.
            dest.writeInt(TRUE)

            dest.writeString(medication!!.drugUID)
            dest.writeString(medication!!.brandName)
            dest.writeString(medication!!.genericName)

            val medicationClass = medication!!.medicationClassification
            dest.writeValue(medicationClass.ordinal)

            dest.writeInt(medication!!.overdoseInhalationCount)
            dest.writeInt(medication!!.minimumDoseInterval)
            dest.writeInt(medication!!.minimumScheduleInterval)
            dest.writeInt(medication!!.initialDoseCount)
            dest.writeInt(medication!!.numberOfMonthsBeforeExpiration)
            //dest.writeInt(medication!!.lowDosePercentage)

            val prescriptions = medication!!.prescriptions
            val parcelerList = ArrayList<PrescriptionParceler>()
            if (prescriptions != null) {
                for (prescription in prescriptions) {
                    parcelerList.add(PrescriptionParceler(prescription))
                }
            }

            dest.writeList(parcelerList)
            dest.writeParcelable(PrescriptionParceler(medication!!.currentPrescription!!), 0)
        }
    }

    /**
     * Describe the kinds of special objects contained in this Parcelable
     * instance's marshaled representation. For example, if the object will
     * include a file descriptor in the output of [.writeToParcel],
     * the return value of this method must include the
     * [.CONTENTS_FILE_DESCRIPTOR] bit.

     * @return a bitmask indicating the set of special object types marshaled
     * * by this Parcelable object instance.
     * *
     * @see .CONTENTS_FILE_DESCRIPTOR
     */
    override fun describeContents(): Int {
        return 0
    }

    companion object {

        private val TRUE = 1
        private val FALSE = 0

        /**
         * This class is part of the Parcelable pattern and is used by external code
         * to deserialize the object from a Parcel or Bundle.
         */
        val CREATOR: Parcelable.Creator<MedicationParceler> = object : Parcelable.Creator<MedicationParceler> {
            override fun createFromParcel(`in`: Parcel): MedicationParceler {
                return MedicationParceler(`in`)
            }

            override fun newArray(size: Int): Array<MedicationParceler?> {
                return arrayOfNulls(size)
            }
        }
    }
}
