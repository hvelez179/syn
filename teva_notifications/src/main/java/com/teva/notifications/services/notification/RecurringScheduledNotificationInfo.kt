///
// RecurringScheduledNotificationInfo.kt
// teva_notifications
//
// Copyright © 2017 Teva. All rights reserved
///

package com.teva.notifications.services.notification

import android.os.Parcel
import com.teva.common.utilities.createParcel

import com.teva.notifications.enumerations.RepeatType

import org.threeten.bp.Instant

/**
 * This class adds recurring notification functionality to scheduled notifications.
 */
class RecurringScheduledNotificationInfo : ScheduledNotificationInfo {
    /**
     * The repeat type of notification.
     */
    val repeatType: RepeatType

    /**
     * Constructor
     *
     * @param categoryId              The category id of the notification.
     * @param notificationData        The data map of the notification.
     * @param fireDateApplicationTime The fire date of the notification in Application time.
     * @param repeatType              The repeat type of the notification.
     */
    constructor(categoryId: String,
                notificationData: Map<String, Any>,
                fireDateApplicationTime: Instant,
                repeatType: RepeatType) : super(categoryId, notificationData, fireDateApplicationTime) {

        this.repeatType = repeatType
    }

    /**
     * Constructor for creating a RecurringScheduledNotification object from a Parcel
     *
     * @param inParcel The parcel containing the object data.
     */
    constructor(inParcel: Parcel) : super(inParcel) {
        this.repeatType = RepeatType.fromOrdinal(inParcel.readInt())
    }

    /**
     * Writes the object's data to a parcel.
     *
     * @param dest  The parcel to write the data into.
     * @param flags Flags controlling the operation
     */
    override fun writeToParcel(dest: Parcel, flags: Int) {
        super.writeToParcel(dest, flags)
        dest.writeInt(repeatType.ordinal)
    }

    companion object {

        /**
         * Creator instance used by the Parcel mechanism to decode or encode parcels.
         */
        @JvmField @Suppress("unused")
        val CREATOR = createParcel { RecurringScheduledNotificationInfo(it) }
    }
}
