///
// NotificationInfo.kt
// teva_notifications
//
// Copyright © 2017 Teva. All rights reserved
///

package com.teva.notifications.services.notification

import android.os.Parcel
import android.os.Parcelable
import com.teva.common.utilities.createAndReadMap
import com.teva.common.utilities.createParcel
import java.util.*

/**
 * This class describes a notification.
 */
open class NotificationInfo : Parcelable {
    /**
     * The category id of the notification.
     */
    var categoryId: String

    /**
     * The data map of the notification.
     */
    var notificationData: MutableMap<String, Any>

    /**
     * The category of the notification.
     */
    var notificationCategory: String? = null

    /**
     * Constructor
     *
     * @param categoryId       The category id of the notification.
     * @param notificationData The data map of the notification.
     */
    constructor(categoryId: String, notificationData: Map<String, Any>) {
        this.categoryId = categoryId

        // copy the notification data.
        this.notificationData = HashMap(notificationData)
    }

    /**
     * Constructor for creating a NotificationData object from a Parcel
     *
     * @param inParcel The parcel containing the object data.
     */
    protected constructor(inParcel: Parcel) {
        categoryId = inParcel.readString()
        notificationCategory = inParcel.readString()
        notificationData = inParcel.createAndReadMap()
    }

    /**
     * Writes the object's data to a parcel.
     *
     * @param dest  The parcel to write the data into.
     * @param flags Flags controlling the operation
     */
    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(categoryId)
        dest.writeString(notificationCategory)
        dest.writeMap(notificationData)
    }

    /**
     * Describe the kinds of special objects contained in this Parcelable
     * instance's marshaled representation. For example, if the object will
     * include a file descriptor in the output of [.writeToParcel],
     * the return value of this method must include the
     * [.CONTENTS_FILE_DESCRIPTOR] bit.
     *
     * @return a bitmask indicating the set of special object types marshaled
     * by this Parcelable object instance.
     * @see .CONTENTS_FILE_DESCRIPTOR
     */
    override fun describeContents(): Int {
        return 0
    }

    /**
     * Returns a string representation of the object.
     * Intended to be used in debug logs
     */
    override fun toString(): String {
        val stringBuilder = StringBuilder("NotificationData:")
        stringBuilder.append("\n  Category Id = ")
        stringBuilder.append(categoryId)
        stringBuilder.append("\n  NotificationData Data = { ")
        for ((key, value) in notificationData) {
            stringBuilder.append("{\"")
            stringBuilder.append(key)
            stringBuilder.append("\" : \"")
            stringBuilder.append(value)
            stringBuilder.append("\"} ")
        }
        stringBuilder.append("\n  NotificationData Category = ")
        stringBuilder.append(notificationCategory)
        return stringBuilder.toString()
    }

    companion object {

        /**
         * Creator instance used by the Parcel mechanism to decode or encode parcels.
         */
        @JvmField @Suppress("unused")
        val CREATOR = createParcel { NotificationInfo(it) }
    }
}
