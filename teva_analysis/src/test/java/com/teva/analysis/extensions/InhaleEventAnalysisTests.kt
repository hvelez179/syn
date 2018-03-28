//
// InhaleEventAnalysisTests.kt
// teva_analysis
//
// Copyright (c) 2017 Teva. All rights reserved.
//

package com.teva.analysis.extensions

import com.teva.analysis.extensions.enumerations.InhalationEffort
import com.teva.devices.entities.InhaleEvent

import org.junit.Test

import org.junit.Assert.assertEquals

/**
 * This class defines unit tests for the InhaleEventAnalysis class.
 */

class InhaleEventAnalysisTests {
    @Test
    fun testInhaleEventAnalysisReturnsCorrectInhalationEffortForValidInhaleEvents() {
        val inhaleEvent = InhaleEvent()
        inhaleEvent.inhalePeak = 150
        inhaleEvent.isValidInhale = true
        var effort = inhaleEvent.inhalationEffort
        assertEquals(InhalationEffort.NO_INHALATION, effort)

        val inhaleEvent1 = InhaleEvent()
        inhaleEvent1.inhalePeak = 299
        inhaleEvent1.isValidInhale = true
        effort = inhaleEvent1.inhalationEffort
        assertEquals(InhalationEffort.NO_INHALATION, effort)

        val inhaleEvent2 = InhaleEvent()
        inhaleEvent2.inhalePeak = 300
        inhaleEvent2.isValidInhale = true
        effort = inhaleEvent2.inhalationEffort
        assertEquals(InhalationEffort.LOW_INHALATION, effort)

        val inhaleEvent2a = InhaleEvent()
        inhaleEvent2a.inhalePeak = 400
        inhaleEvent2a.isValidInhale = true
        effort = inhaleEvent2a.inhalationEffort
        assertEquals(InhalationEffort.LOW_INHALATION, effort)

        val inhaleEvent3 = InhaleEvent()
        inhaleEvent3.inhalePeak = 450
        inhaleEvent3.isValidInhale = true
        effort = inhaleEvent3.inhalationEffort
        assertEquals(InhalationEffort.LOW_INHALATION, effort)

        val inhaleEvent4 = InhaleEvent()
        inhaleEvent4.inhalePeak = 451
        inhaleEvent4.isValidInhale = true
        effort = inhaleEvent4.inhalationEffort
        assertEquals(InhalationEffort.GOOD_INHALATION, effort)

        val inhaleEvent5 = InhaleEvent()
        inhaleEvent5.inhalePeak = 2000
        inhaleEvent5.isValidInhale = true
        effort = inhaleEvent5.inhalationEffort
        assertEquals(InhalationEffort.GOOD_INHALATION, effort)

        val inhaleEvent6 = InhaleEvent()
        inhaleEvent6.inhalePeak = 2001
        inhaleEvent6.isValidInhale = true
        effort = inhaleEvent6.inhalationEffort
        assertEquals(InhalationEffort.ERROR, effort)

        val inhaleEvent7 = InhaleEvent()
        inhaleEvent7.inhalePeak = 4000
        inhaleEvent7.isValidInhale = true
        effort = inhaleEvent7.inhalationEffort
        assertEquals(InhalationEffort.ERROR, effort)
    }

    @Test
    fun testInhaleEventAnalysisReturnsCorrectInhalationEffortForInvalidInhaleEvents() {
        val inhaleEvent = InhaleEvent()
        inhaleEvent.isValidInhale = false
        inhaleEvent.status = 0
        var effort = inhaleEvent.inhalationEffort
        assertEquals(InhalationEffort.NO_INHALATION, effort)

        val inhaleEvent1 = InhaleEvent()
        inhaleEvent1.isValidInhale = false
        inhaleEvent1.status = 8
        effort = inhaleEvent1.inhalationEffort
        assertEquals(InhalationEffort.EXHALATION, effort)

        val inhaleEvent2 = InhaleEvent()
        inhaleEvent2.isValidInhale = false
        inhaleEvent2.status = 4
        effort = inhaleEvent2.inhalationEffort
        assertEquals(InhalationEffort.NO_INHALATION, effort)

        val inhaleEvent3 = InhaleEvent()
        inhaleEvent3.isValidInhale = false
        inhaleEvent3.status = 2
        effort = inhaleEvent3.inhalationEffort
        assertEquals(InhalationEffort.SYSTEM_ERROR, effort)

        val inhaleEvent4 = InhaleEvent()
        inhaleEvent4.isValidInhale = false
        inhaleEvent4.status = 16
        effort = inhaleEvent4.inhalationEffort
        assertEquals(InhalationEffort.SYSTEM_ERROR, effort)

        val inhaleEvent5 = InhaleEvent()
        inhaleEvent5.isValidInhale = false
        inhaleEvent5.status = 64
        effort = inhaleEvent5.inhalationEffort
        assertEquals(InhalationEffort.SYSTEM_ERROR, effort)
    }
}
