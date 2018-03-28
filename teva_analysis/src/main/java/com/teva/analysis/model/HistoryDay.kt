//
// HistoryDay.kt
// teva_analysis
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.analysis.model

import com.teva.analysis.entities.HistoryDose
import com.teva.analysis.enumerations.RelieverUsage
import com.teva.analysis.extensions.enumerations.InhalationEffort
import com.teva.analysis.extensions.inhalationEffort
import com.teva.medication.entities.Prescription
import com.teva.userfeedback.entities.DailyUserFeeling
import org.threeten.bp.LocalDate
import java.util.*

/**
 * This class represents the application information corresponding to a single day.
 *
 * @property day The date to which the history belongs.
 * @property dailyUserFeeling DailyUserFeeling information for the day.
 * @property pif Peak Inspiratory Flow for the day.
 * @property relieverDoses Reliever doses for the day.
 * @property invalidDoses Invalid doses for the day.
 * @property systemErrorDoses Doses that encountered system error
 * @property prescriptions Prescriptions for the day.
 * @property connectedInhalerCount Number of inhalers connected on the day.
 */
class HistoryDay(var day: LocalDate,
                 var dailyUserFeeling: DailyUserFeeling? = null,
                 var pif: Int? = null,
                 var relieverDoses: MutableList<HistoryDose> = ArrayList(),
                 val invalidDoses: MutableList<HistoryDose> = ArrayList(),
                 val systemErrorDoses: MutableList<HistoryDose> = ArrayList(),
                 var prescriptions: MutableList<Prescription> = ArrayList(),
                 var connectedInhalerCount: Int = 0)
    : Comparable<HistoryDay> {

    /**
     * This is the overridden method of the Comparable interface which supports sorting of
     * the HistoryDay objects by day.
     *
     * @param other - the other HistoryDay object to be compared to.
     * @return 1 if the current object day is higher than the other object day else -1.
     */
    override fun compareTo(other: HistoryDay): Int {
        return if (this.day.isAfter(other.day)) 1 else -1
    }

    /**
     * Gets the reliever usage for the day.
     *
     * @return Returns RelieverUsage level.
     */
    val relieverUsage: RelieverUsage
        get() {
            val relieverEventCount = relieverDoses.sumBy { it.events.size }

            val invalidEventCount = invalidDoses
                    .flatMap { it.events }
                    .filter { it.inhalationEffort !== InhalationEffort.SYSTEM_ERROR }
                    .count()

            return RelieverUsage.fromCount(relieverEventCount + invalidEventCount)
        }
}
