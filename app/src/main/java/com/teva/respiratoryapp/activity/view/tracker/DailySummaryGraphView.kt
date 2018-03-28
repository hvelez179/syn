//
// DailySummaryGraphView.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.activity.view.tracker

import android.content.Context
import android.util.AttributeSet
import com.teva.analysis.extensions.enumerations.InhalationEffort
import org.threeten.bp.LocalDate

/**
 * This view represents the graph displayed in the daily summary report.
 */

class DailySummaryGraphView(context: Context, attrs: AttributeSet? = null) : SummaryGraphView(context, attrs) {

    private var currentDate: LocalDate? = null

    private var dayWiseInhaleEvents: HashMap<LocalDate, HashMap<InhalationEffort, Int>>? = null

    init {
        verticalTicks = 15
        boldTickStep = 5
        verticalTickStep = 1
        columns = 30
        firstHorizontalTickColumn = 4
        horizontalTickStep = 7
        redLineTick = 12

        maxNumberOfEvents = 15
    }

    /**
     * This method is the setter for the day wise inhalation events to be plotted
     * @param dayWiseInhaleEvents - the day wise inhalation events
     */
    fun setDayWiseInhaleEvents(dayWiseInhaleEvents: HashMap<LocalDate, HashMap<InhalationEffort, Int>>) {
        this.dayWiseInhaleEvents = dayWiseInhaleEvents
        invalidate()
    }

    /**
     * This method is the setter for the current date
     * @param currentDate - the current date
     */
    fun setCurrentDate(currentDate: LocalDate) {
        this.currentDate = currentDate
        invalidate()
    }

    override fun getHorizontalTickLabel(tick: Int): String? {
        return currentDate?.let {
            var labelDate = it.minusDays(tick.toLong())
            dateTimeLocalization.toShortMonthDay(it.minusDays(tick.toLong()))
        }
    }

    override fun getVerticalTickLabel(tick: Int): String? {
        return if (tick % boldTickStep == 0) tick.toString() else null
    }

    override fun getColumnEvents(column: Int): HashMap<InhalationEffort, Int>? {
        val date = currentDate!!.minusDays(column.toLong())
        return dayWiseInhaleEvents?.get(date)
    }
}
