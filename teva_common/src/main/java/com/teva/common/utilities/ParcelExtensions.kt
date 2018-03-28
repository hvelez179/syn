///
// ParcelExtensions.kt
// teva_common
//
// Copyright © 2017 Teva. All rights reserved
///

package com.teva.common.utilities

import android.os.Parcel
import android.os.Parcelable
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate

/**
 * Empty class used to get the class loader for Parcel extensions
 */
private class ClassLoaderProvider

private val parcelClassLoader = ClassLoaderProvider::class.java.classLoader

/**
 * Helper method for implementing the Creator for a Parcelable.
 */
inline fun <reified T : Parcelable> createParcel(
        crossinline createFromParcel: (Parcel) -> T?): Parcelable.Creator<T> =
        object : Parcelable.Creator<T> {
            override fun createFromParcel(source: Parcel): T? = createFromParcel(source)
            override fun newArray(size: Int): Array<out T?> = arrayOfNulls(size)
        }

/**
 * Writes a LocalDate to a Parcel.
 *
 * @param localDate The LocalDate to write.
 */
fun Parcel.createAndReadMap() : MutableMap<String, Any> {
    val map = HashMap<String, Any>()
    readMap(map, parcelClassLoader)
    return map
}

/**
 * Writes a LocalDate to a Parcel.
 *
 * @param localDate The LocalDate to write.
 */
fun Parcel.writeLocalDate(localDate: LocalDate?) {
    val value = localDate?.toEpochDay()
    writeValue(value)
}

/**
 * Reads a LocalDate from a Parcel.
 *
 * @return The LocalDate read from the Parcel
 */
fun Parcel.readLocalDate(): LocalDate? {
    val value = readValue(parcelClassLoader) as Long?
    return if (value != null) LocalDate.ofEpochDay(value) else null
}

/**
 * Writes an Instant to a Parcel.
 *
 * @param instant The Instant to write.
 */
fun Parcel.writeInstant(instant: Instant?) {
    val value = instant?.toEpochMilli()
    writeValue(value)
}

/**
 * Reads an Instant from a Parcel.
 *
 * @return The Instant read from the Parcel
 */
fun Parcel.readInstant(): Instant? {
    val value = readValue(parcelClassLoader) as Long?
    return if (value != null) Instant.ofEpochMilli(value) else null
}

/**
 * Writes an Duration to a Parcel.
 *
 * @param duration The Duration to write.
 */
fun Parcel.writeDuration(duration: Duration?) {
    val value = duration?.seconds
    writeValue(value)
}

/**
 * Reads an Duration from a Parcel.
 *
 * @return The Instant read from the Parcel
 */
fun Parcel.readDuration(): Duration? {
    val value = readValue(parcelClassLoader) as Long?
    return if (value != null) Duration.ofSeconds(value) else null
}
