//
// PrescriptionParceler.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.models.parcelable

import android.os.Parcel
import android.os.Parcelable
import com.teva.common.utilities.readInstant
import com.teva.common.utilities.writeInstant

import com.teva.medication.entities.Medication
import com.teva.medication.entities.Prescription

/**
 * This class wraps a Prescription object and allows it to be added
 * to a Bundle as a Parcelable object.
 */

class PrescriptionParceler : Parcelable {

    /**
     * Gets the Prescription held by this parceler.
     */
    var prescription: Prescription? = null
        private set

    /**
     * Constructor that initializes the parceler with a Prescription.

     * @param prescription The Prescription to wrap by this parceler.
     */
    constructor(prescription: Prescription) {
        this.prescription = prescription
    }


    /**
     * Constructor used when reading from a parcel.

     * @param parcel The parcel to read the object's data from.
     */
    protected constructor(parcel: Parcel) {
        // check "Object Present Flag" to know whether the object should be
        // deserialized or left as null.
        if (parcel.readInt() == TRUE) {
            prescription = Prescription()
            prescription!!.dosesPerDay = parcel.readInt()
            prescription!!.inhalesPerDose = parcel.readInt()
            prescription!!.prescriptionDate = parcel.readInstant()

            val drugUID = parcel.readString()
            if (drugUID != null) {
                val medication = Medication()
                medication.drugUID = drugUID
                prescription!!.medication = medication
            }
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
        if (prescription == null) {
            // Write a "false" to indicate the object is null
            dest.writeInt(FALSE)
        } else {
            // Write a "true" to indicate the object is present.
            dest.writeInt(TRUE)

            dest.writeInt(prescription!!.dosesPerDay)
            dest.writeInt(prescription!!.inhalesPerDose)
            dest.writeInstant(prescription!!.prescriptionDate)

            val medication = prescription!!.medication
            val drugUID = medication?.drugUID

            dest.writeString(drugUID)
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
        val CREATOR: Parcelable.Creator<PrescriptionParceler> = object : Parcelable.Creator<PrescriptionParceler> {
            override fun createFromParcel(`in`: Parcel): PrescriptionParceler {
                return PrescriptionParceler(`in`)
            }

            override fun newArray(size: Int): Array<PrescriptionParceler?> {
                return arrayOfNulls(size)
            }
        }
    }
}
