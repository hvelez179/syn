package com.teva.common.utilities

import android.os.Parcelable
import android.os.Parcel



/**
 * This method writes a Parcelable to a byte array.
 *
 * @return a byte array containing the parcelable data.
 */
fun Parcelable.marshall() : ByteArray {
    val parcel = Parcel.obtain()
    parcel.writeParcelable(this, 0)
    val bytes = parcel.marshall()
    parcel.recycle()
    return bytes
}
