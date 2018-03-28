//
// InhaleEventAnalysis.kt
// teva_analysis
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.analysis.extensions

import com.teva.analysis.extensions.enumerations.InhalationEffort
import com.teva.devices.entities.InhaleEvent
import com.teva.devices.enumerations.InhaleStatus
import java.util.*

private val HIGH_INHALATION_THRESHOLD = 2000
private val LOW_INHALATION_THRESHOLD = 450
private val NO_INHALATION_THRESHOLD = 300

/**
 * This method determines whether the peak inspiratory flow (PIF) is high.
 *
 * @return - Returns true if peak inspiratory flow is above the high threshold.
 */
fun InhaleEvent.isHighFlow() = peakInspiratoryFlow > HIGH_INHALATION_THRESHOLD

fun EnumSet<InhaleStatus.InhaleStatusFlag>.hasSystemErrors(): Boolean {
    return contains(InhaleStatus.InhaleStatusFlag.INHALE_STATUS_TIMESTAMP_ERROR) ||
            contains(InhaleStatus.InhaleStatusFlag.INHALE_STATUS_INHALE_PARAMETER_ERROR) ||
            contains(InhaleStatus.InhaleStatusFlag.INHALE_STATUS_BAD_DATA)
}

/**
 * Calculates the inhalation effort based on the peak inspiratory flow values and the inhale event states.
 *
 * @return - Returns the inhalation effort.
 */
val InhaleEvent.inhalationEffort: InhalationEffort
    get() {

    val inhalationEffort: InhalationEffort

    if (!isValidInhale) {
        if (issues.contains(InhaleStatus.InhaleStatusFlag.INHALE_STATUS_UNEXPECTED_EXHALATION)) {
            inhalationEffort = InhalationEffort.EXHALATION
        } else if (issues.hasSystemErrors()) {
            inhalationEffort = InhalationEffort.SYSTEM_ERROR
        } else {
            inhalationEffort = InhalationEffort.NO_INHALATION
        }
    } else {
        if (peakInspiratoryFlow < NO_INHALATION_THRESHOLD) {
            inhalationEffort = InhalationEffort.NO_INHALATION
        } else if (peakInspiratoryFlow <= LOW_INHALATION_THRESHOLD) {
            inhalationEffort = InhalationEffort.LOW_INHALATION
        } else if (peakInspiratoryFlow <= HIGH_INHALATION_THRESHOLD) {
            inhalationEffort = InhalationEffort.GOOD_INHALATION
        } else {
            inhalationEffort = InhalationEffort.ERROR
        }
    }

    return inhalationEffort
}
