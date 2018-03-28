//
// DeviceParceler.java
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.models.parcelable

import android.os.Parcel
import android.os.Parcelable
import com.teva.common.utilities.*

import com.teva.devices.entities.Device
import com.teva.devices.enumerations.InhalerNameType

/**
 * This class wraps a Device object and allows it to be added to a Bundle as a Parcelable object.
 */
class DeviceParceler : Parcelable {

    /**
     * Gets the device held by this parceler.
     */
    var device: Device? = null
        private set

    /**
     * Constructor that initializes the parceler with a device.

     * @param device The device to wrap by this parceler.
     */
    constructor(device: Device) {
        this.device = device
    }

    /**
     * Constructor used when reading from a parcel.

     * @param parcel The parcel to read the object's data from.
     */
    private constructor(parcel: Parcel) {
        // check "Object Present Flag" to know whether the object should be
        // deserialized or left as null.
        if (parcel.readInt() == TRUE) {
            val device = Device()
            device.serialNumber = parcel.readString()
            device.authenticationKey = parcel.readString()
            device.manufacturerName = parcel.readString()
            device.hardwareRevision = parcel.readString()
            device.softwareRevision = parcel.readString()
            device.lotCode = parcel.readString()
            device.dateCode = parcel.readString()
            device.expirationDate = parcel.readLocalDate()
            device.doseCount = parcel.readInt()
            device.remainingDoseCount = parcel.readInt()
            device.lastRecordId = parcel.readInt()
            device.lastConnection = parcel.readInstant()

            val ordinal = parcel.readValue(DeviceParceler::class.java.classLoader) as Int
            device.inhalerNameType = InhalerNameType.fromOrdinal(ordinal)!!

            device.nickname = parcel.readString()
            device.isActive = parcel.readInt() != 0
            device.isConnected = parcel.readInt() != 0
            device.disconnectedTimeSpan = parcel.readDuration()

            val parceler = parcel.readParcelable<MedicationParceler>(DeviceParceler::class.java.classLoader)
            device.medication = parceler.medication

            this.device = device
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
        if (device == null) {
            // Write a "false" to indicate the object is null
            dest.writeInt(FALSE)
        } else {
            // Write a "true" to indicate the object is present.
            dest.writeInt(TRUE)

            dest.writeString(device!!.serialNumber)
            dest.writeString(device!!.authenticationKey)
            dest.writeString(device!!.manufacturerName)
            dest.writeString(device!!.hardwareRevision)
            dest.writeString(device!!.softwareRevision)
            dest.writeString(device!!.lotCode)
            dest.writeString(device!!.dateCode)
            dest.writeLocalDate(device!!.expirationDate)
            dest.writeInt(device!!.doseCount)
            dest.writeInt(device!!.remainingDoseCount)
            dest.writeInt(device!!.lastRecordId)
            dest.writeInstant(device!!.lastConnection)

            val inhalerNameType = device!!.inhalerNameType
            dest.writeValue(inhalerNameType.ordinal)

            dest.writeString(device!!.nickname)
            dest.writeInt(if (device!!.isActive) 1 else 0)
            dest.writeInt(if (device!!.isConnected) 1 else 0)
            dest.writeDuration(device!!.disconnectedTimeSpan)

            dest.writeParcelable(MedicationParceler(device!!.medication), 0)
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
        val CREATOR: Parcelable.Creator<DeviceParceler> = object : Parcelable.Creator<DeviceParceler> {
            override fun createFromParcel(parcel: Parcel): DeviceParceler {
                return DeviceParceler(parcel)
            }

            override fun newArray(size: Int): Array<DeviceParceler?> {
                return arrayOfNulls(size)
            }
        }
    }
}
