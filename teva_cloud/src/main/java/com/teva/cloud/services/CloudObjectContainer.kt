//
// CloudObjectContainer.kt
// teva_cloud
//
// Copyright © 2017 Teva. All rights reserved.
//

package com.teva.cloud.services

import com.teva.cloud.dataentities.UserProfile
import com.teva.devices.entities.Device
import com.teva.devices.entities.InhaleEvent
import com.teva.medication.entities.Prescription
import com.teva.notifications.entities.ReminderSetting
import com.teva.userfeedback.entities.DailyUserFeeling


/**
 * This class is a container to pass data to upload & download between the CloudManager and CloudService.
 */
class CloudObjectContainer {
    internal var prescriptions = ArrayList<Prescription>()

    internal var devices = ArrayList<Device>()

    internal var inhaleEvents = ArrayList<InhaleEvent>()

    internal var dsas =  ArrayList<DailyUserFeeling>()

    internal var settings =  ArrayList<ReminderSetting>()

    internal var profiles = ArrayList<UserProfile>()

    /**
     * This function determines whether any of the arrays in the container have data.
     *
     * @return - true if there is any data in the container
     */
    internal fun hasData(): Boolean {

        return !(
                prescriptions.isEmpty() &&
                        devices.isEmpty() &&
                        inhaleEvents.isEmpty() &&
                        dsas.isEmpty() &&
                        settings.isEmpty() &&
                        profiles.isEmpty())
    }

    /**
     * This function removes all data from the container.
     */
    internal fun removeAllData() {
        prescriptions.clear()
        devices.clear()
        inhaleEvents.clear()
        dsas.clear()
        settings.clear()
        profiles.clear()
    }

    /**
     * This function returns the count of all objects in the container.
     * @return: an integer of the total objects
     */
    internal fun objectCount(): Int {

        return (prescriptions.size +
                devices.size +
                inhaleEvents.size +
                dsas.size +
                settings.size +
                profiles.size)
    }

    /**
     * This function returns the count of all objects in the container.
     * @return: an integer of the total objects
     */
    internal fun objectCountString(): String {
        val countString = StringBuilder()
        countString.append(when {prescriptions.size > 0 -> "${prescriptions.size} prescription(s); " else -> ""} )
                .append(when {devices.size > 0 -> "${devices.size} device(s); " else -> ""} )
                .append(when {inhaleEvents.size > 0 -> "${inhaleEvents.size} inhale event(s); " else -> ""} )
                .append(when {dsas.size > 0 -> "${dsas.size} dsa(s); " else -> ""} )
                .append(when {settings.size > 0 -> "${prescriptions.size} setting(s); " else -> ""} )
                .append(when {profiles.size > 0 -> "${prescriptions.size} profile(s); " else -> ""})
        return countString.toString()
    }
}