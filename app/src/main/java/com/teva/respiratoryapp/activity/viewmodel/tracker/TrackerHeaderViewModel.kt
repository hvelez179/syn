//
// TrackerHeaderViewModel.java
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.activity.viewmodel.tracker

import android.databinding.BaseObservable
import android.databinding.Bindable

import org.threeten.bp.LocalDate

/**
 * This class is the ViewModel for the Tracker list headers.
 *
 * @param date The date to be displayed by the header.
 */
class TrackerHeaderViewModel(date: LocalDate) : BaseObservable() {

    /**
     * Gets the year of the header date.
     */
    @get:Bindable
    var year: Int = 0
        internal set

    init {
        year = date.year
    }
}
