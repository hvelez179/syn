//
// RelieverUsage.kt
// teva_analysis
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.analysis.enumerations

/**
 * The characterization for reliever use.
 * Normal: usage is normal (use count is on or below the threshold)
 * High: usage is high (use count is higher than the threshold).
 */

enum class RelieverUsage {
    NORMAL,
    HIGH;

    companion object {

        private val NORMAL_THRESHOLD = 12
        fun fromCount(count: Int): RelieverUsage {
            if (count > NORMAL_THRESHOLD) {
                return RelieverUsage.HIGH
            } else {
                return RelieverUsage.NORMAL
            }
        }
    }
}
