//
// WeeklyInhalationSummaryGraphView.kt
// app
//
// Copyright (c) 2017 Teva. All rights reserved.
//

package com.teva.respiratoryapp.activity.view.tracker

import android.content.Context
import android.graphics.*
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import com.teva.analysis.extensions.enumerations.InhalationEffort
import com.teva.analysis.extensions.inhalationEffort
import com.teva.devices.entities.InhaleEvent
import com.teva.respiratoryapp.R
import com.teva.respiratoryapp.activity.viewmodel.tracker.ReportViewModel
import org.threeten.bp.LocalDate

/**
 * This view represents the graph displayed in the weekly inhalation summary report.
 */

class WeeklyInhalationSummaryGraphView(context: Context, attrs: AttributeSet? = null) : SummaryGraphView(context, attrs) {

    private var weekWiseInhaleEvents: HashMap<Int, ReportViewModel.WeekSummary>? = null

    init {
        verticalTicks = 30
        boldTickStep = 10
        verticalTickStep = 5
        columns = 12
        firstHorizontalTickColumn = 0
        horizontalTickStep = 1
        redLineTick = -1

        maxNumberOfEvents = 30

    }

    /**
     * This method is the setter for the week wise inhalation events to be plotted
     * @param weekWiseInhaleEvents - the week wise inhalation events
     */
    fun setWeekWiseInhaleEvents(weekWiseInhaleEvents: HashMap<Int, ReportViewModel.WeekSummary>) {
        this.weekWiseInhaleEvents = weekWiseInhaleEvents
        invalidate()
    }

    /**
     * This method plots the inhale events.
     * @param canvas - the canvas on which the events are plotted.
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        weekWiseInhaleEvents?.let { weeks ->
            linePaint.color = tickLineColor
            linePaint.strokeWidth = if (printable) {
                resources.getDimension(R.dimen.weekly_summary_printable_graph_month_tick_stroke_width)
            } else {
                resources.getDimension(R.dimen.weekly_summary_graph_month_tick_stroke_width)
            }
            val fontMetrics = textPaint.fontMetrics
            val textHeight = - fontMetrics.ascent + fontMetrics.descent
            val y1 = graphBounds.bottom + xLabelMargin + textHeight
            val y2 = y1 + textHeight + 2 * fontMetrics.leading
            val yLabel = y1 + fontMetrics.leading - textPaint.fontMetrics.ascent
            val xLabelOffset = if (printable) {
                resources.getDimension(R.dimen.weekly_summary_printable_graph_month_margin)
            } else {
                resources.getDimension(R.dimen.weekly_summary_graph_month_margin)
            }

            for (tick in 0 until columns) {
                weeks[tick]?.let { week->
                    if (week.bounds.startDay.month != week.bounds.endDay.month) {
                        val x = graphBounds.right - ((tick * columnSpacing) + (columnSpacing / 2))
                        canvas.drawLine(x, y1, x, y2, linePaint)

                        val labelText = dateTimeLocalization.toShortMonth(week.bounds.endDay)
                        canvas.drawText(labelText, x + xLabelOffset, yLabel, textPaint)
                    }
                }
            }
        }
    }

    override fun getHorizontalTickLabel(tick: Int): String? {
        return weekWiseInhaleEvents?.get(tick)?.let {
            "${it.bounds.startDay.dayOfMonth}-${it.bounds.endDay.dayOfMonth}"
        }
    }

    override fun getVerticalTickLabel(tick: Int): String? {
        return if (tick % boldTickStep == 0) tick.toString() else null
    }

    override fun getColumnEvents(column: Int): HashMap<InhalationEffort, Int>? {
        return weekWiseInhaleEvents?.get(column)?.events
    }

}