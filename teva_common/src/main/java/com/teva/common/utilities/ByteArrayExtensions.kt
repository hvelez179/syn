package com.teva.common.utilities

import android.os.Parcel
import android.os.Parcelable


/**
 * This method reads a Parcelable from a byte array.
 *
 * @return a byte array containing the parcelable data.
 */
fun ByteArray.unmarshall(classLoader: ClassLoader) : Parcelable {
    val parcel = Parcel.obtain()
    parcel.unmarshall(this, 0, this.size)
    parcel.setDataPosition(0)
    val parcelable = parcel.readParcelable<Parcelable>(classLoader)
    parcel.recycle()
    return parcelable
}
