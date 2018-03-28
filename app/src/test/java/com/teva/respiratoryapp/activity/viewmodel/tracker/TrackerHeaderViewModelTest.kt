///
// TrackerHeaderViewModelTest.java
// app
//
// Copyright © 2017 Teva. All rights reserved
///

package com.teva.respiratoryapp.activity.viewmodel.tracker

import com.teva.respiratoryapp.testutils.BaseTest

import org.junit.Test
import org.threeten.bp.LocalDate

import org.junit.Assert.*

class TrackerHeaderViewModelTest : BaseTest() {
    @Test
    fun testThatYearReturnsCorrectValue() {
        val expectedYear = 2017
        val date = LocalDate.of(expectedYear, 2, 18)
        val viewModel = TrackerHeaderViewModel(date)

        assertEquals(expectedYear.toLong(), viewModel.year.toLong())
    }
}