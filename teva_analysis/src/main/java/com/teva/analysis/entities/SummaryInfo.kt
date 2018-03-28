//
// SummaryInfo.kt
// teva_analysis
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.analysis.entities

import com.teva.analysis.enumerations.SummaryTextId

/**
 * This class represents the summary message displayed on the dashboard.
 *
 * @property id      - id indicating the message type.
 * @property message - message specific data.
 */

class SummaryInfo(val id: SummaryTextId, val message: Map<String, Any>?) : Comparable<SummaryInfo> {

    /**
     * Compares the SummaryInfo object to another SummaryInfo object.
     *
     * @property other The other object to compare to the current object.
     * @return 1 if the current object is greater than the other object day else -1.
     */
    override fun compareTo(other: SummaryInfo): Int {
        return this.id.ordinal - other.id.ordinal
    }
}
