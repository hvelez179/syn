///
// ScheduledNotificationInfo.kt
// teva_notifications
//
// Copyright © 2017 Teva. All rights reserved
///

package com.teva.notifications.services.notification

import android.os.Parcel
import com.teva.common.utilities.createParcel

import org.threeten.bp.Instant

/**
 * This class implements the Reminder protocol.  It extends it by adding fireDate.
 */
open class ScheduledNotificationInfo : NotificationInfo {
    /**
     * The notification fire date in application time.  It is converted to Android time before
     * scheduling with Android.
     */
    var fireDateApplicationTime: Instant

    /**
     * Constructor
     *
     * @param categoryId              The category id of the notification.
     * @param notificationData        The data map of the notification.
     * @param fireDateApplicationTime The fire date of the notification in Application time.
     */
    constructor(categoryId: String,
                notificationData: Map<String, Any>,
                fireDateApplicationTime: Instant) : super(categoryId, notificationData) {

        this.fireDateApplicationTime = fireDateApplicationTime
    }

    /**
     * Constructor for creating a ScheduledNotification object from a Parcel
     *
     * @param inParcel The parcel containing the object data.
     */
    constructor(inParcel: Parcel) : super(inParcel) {
        fireDateApplicationTime = Instant.ofEpochMilli(inParcel.readLong())
    }

    /**
     * Writes the object's data to a parcel.
     *
     * @param dest  The parcel to write the data into.
     * @param flags Flags controlling the operation
     */
    override fun writeToParcel(dest: Parcel, flags: Int) {
        super.writeToParcel(dest, flags)
        dest.writeLong(fireDateApplicationTime.toEpochMilli())
    }

    companion object {

        /**
         * Creator instance used by the Parcel mechanism to decode or encode parcels.
         */
        @JvmField @Suppress("unused")
        val CREATOR = createParcel { ScheduledNotificationInfo(it) }
    }
}
