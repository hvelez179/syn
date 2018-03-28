//
// TrackerItemViewModel.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.activity.viewmodel.tracker

import android.databinding.BaseObservable
import android.databinding.Bindable
import com.teva.analysis.model.HistoryDay
import com.teva.utilities.services.DependencyProvider
import com.teva.respiratoryapp.BR
import com.teva.respiratoryapp.common.DateTimeLocalization
import com.teva.respiratoryapp.mvvmframework.ui.IItemViewModel
import com.teva.userfeedback.enumerations.UserFeeling
import org.threeten.bp.LocalDate

/**
 * This class is the viewmodel for items on the Tracker screen.
 *
 * @param dependencyProvider The dependency injection mechanism
 */
class TrackerItemViewModel(dependencyProvider: DependencyProvider)
    : BaseObservable(), IItemViewModel<HistoryDay> {

    /**
     * The date of the TrackerItem
     */
    var date: LocalDate? = null
        set(date) {
            field = date
            historyDay = null

        }
    private var historyDay: HistoryDay? = null
    private val dateTimeLocalization: DateTimeLocalization = dependencyProvider.resolve<DateTimeLocalization>()

    /**
     * A value indicating whether the number of inhalations for the history day
     * exceeds the critical threshold.
     */
    @get:Bindable
    @get:JvmName("getInhalationCountCritical")
    var isInhalationCountCritical: Boolean = false
        private set(value) {
            field = value
            notifyPropertyChanged(BR.inhalationCountCritical)
        }

    /**
     * A value indicating whether the viewmodel has been loaded with the history day.
     */
    val isLoaded: Boolean
        @Bindable
        get() = historyDay != null

    /**
     * The formatted date string for the History Day.
     */
    val formattedDate: String?
        @Bindable
        get() {
            if (this.date != null) {
                return dateTimeLocalization.toShortMonthDay(this.date!!)
            }

            return null
        }

    /**
     * The formatted date string for the History Day.
     */
    val dayName: String?
        @Bindable
        get() {
            if (this.date != null) {
                return dateTimeLocalization.toShortDayOfWeek(this.date!!, true)
            }

            return null
        }

    /**
     * The number of inhalers that connected during the history day.
     */
    val connectedInhalers: Int
        @Bindable
        get() {
            if (historyDay != null) {
                return historyDay!!.connectedInhalerCount
            }

            return 0
        }

    /**
     * The number of inhalations that occurred during the history day.
     */
    val inhalationCount: Int
        @Bindable
        get() {
            if (historyDay == null) {
                return 0
            }

            return historyDay!!.relieverDoses.size + historyDay!!.invalidDoses.size
        }

    /**
     * A value indicating whether there are connected inhalers for the history day.
     */
    val isInhalationCountValid: Boolean
        @Bindable
        get() = historyDay != null && connectedInhalers > 0

    /**
     * The Daily Self Assessment value for the history day
     */
    val dailySelfAssessment: UserFeeling
        @Bindable
        get() {
            if (historyDay != null && historyDay!!.dailyUserFeeling != null) {
                return historyDay!!.dailyUserFeeling!!.userFeeling
            }

            return UserFeeling.UNKNOWN
        }

    /**
     * Sets the model item into the viewmodel.
     *
     * @param item The model item.
     */
    override fun setItem(item: HistoryDay) {
        this.historyDay = item

        // check if the inhale events exceeds the overdose count.
        isInhalationCountCritical = false
        val prescriptionList = item.prescriptions
        if (prescriptionList.size > 0) {
            val overdoseCount = prescriptionList[0].medication!!.overdoseInhalationCount
            isInhalationCountCritical = inhalationCount >= overdoseCount
        }

        notifyChange()
    }
}
