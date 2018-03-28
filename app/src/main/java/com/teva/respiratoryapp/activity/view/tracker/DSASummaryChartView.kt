//
// DSASummaryChartView.kt
// app
//
// Copyright (c) 2017 Teva. All rights reserved.
//

package com.teva.respiratoryapp.activity.view.tracker

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import com.teva.utilities.services.DependencyProvider
import com.teva.respiratoryapp.R
import com.teva.respiratoryapp.common.DateTimeLocalization
import com.teva.respiratoryapp.mvvmframework.utils.applyAppearance
import com.teva.userfeedback.entities.DailyUserFeeling
import com.teva.userfeedback.enumerations.UserFeeling
import org.threeten.bp.LocalDate

/**
 * This view represents the graph displayed in the DSA summary report.
 */
class DSASummaryChartView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    private val dateTimeLocalization = DependencyProvider.default.resolve<DateTimeLocalization>()

    var currentDate: LocalDate? = null

    private var dayWiseDSA: Map<LocalDate, DailyUserFeeling?>? = null

    private val goodDsaColor: Int
    private val poorDsaColor: Int
    private val badDsaColor: Int
    private val dotRadius: Float

    private val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
    private val linePaint = Paint()
    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val graphBounds = RectF()

    private var printable: Boolean = false

    private val redSmiley: Drawable
    private val  yellowSmiley: Drawable
    private val greenSmiley: Drawable

    private val yLabelPadding: Float
    private val xLabelPadding: Float
    private val yLabelMargin: Float
    private val xLabelMargin: Float

    private var columnSpacing = 0f
    private var rowSpacing = 0f

    private var smileyWidth: Int
    private var smileyHeight: Int

    private val goodSmileyBounds = Rect()
    private val poorSmileyBounds = Rect()
    private val badSmileyBounds = Rect()

    init {

        var labelAppearanceId = 0

        val ta = context.obtainStyledAttributes(attrs, R.styleable.SummaryGraphView, 0, 0)
        try {
            val isPrintable = ta.getBoolean(R.styleable.SummaryGraphView_printable, false)

            printable = isPrintable

            linePaint.color = ta.getColor(R.styleable.SummaryGraphView_lineColor, 0)
            linePaint.strokeWidth = if (printable) {
                resources.getDimension(R.dimen.daily_summary_printable_graph_line_stroke_width)
            } else {
                resources.getDimension(R.dimen.daily_summary_graph_line_stroke_width)
            }

            labelAppearanceId = ta.getResourceId(R.styleable.SummaryGraphView_labelTextAppearance, 0)

            dotRadius = ta.getDimension(R.styleable.SummaryGraphView_dotDiameter, 1f) / 2f
            goodDsaColor = ta.getColor(R.styleable.SummaryGraphView_goodDsaColor, 0)
            poorDsaColor = ta.getColor(R.styleable.SummaryGraphView_poorDsaColor, 0)
            badDsaColor = ta.getColor(R.styleable.SummaryGraphView_badDsaColor, 0)

            yLabelPadding = ta.getDimension(R.styleable.SummaryGraphView_yLabelPadding, 0f)
            xLabelPadding = ta.getDimension(R.styleable.SummaryGraphView_xLabelPadding, 0f)
            xLabelMargin = ta.getDimension(R.styleable.SummaryGraphView_xLabelMargin, 0f)
            yLabelMargin = ta.getDimension(R.styleable.SummaryGraphView_yLabelMargin, 0f)

            if (printable) {
                smileyHeight = resources.getDimensionPixelSize(R.dimen.dsa_summary_printable_graph_smiley_height)
                smileyWidth = resources.getDimensionPixelSize(R.dimen.dsa_summary_printable_graph_smiley_width)
            } else {
                smileyHeight = resources.getDimensionPixelSize(R.dimen.dsa_summary_graph_smiley_height)
                smileyWidth = resources.getDimensionPixelSize(R.dimen.dsa_summary_graph_smiley_width)
            }
        } finally {
            ta.recycle()
        }

        textPaint.applyAppearance(context, labelAppearanceId)
        dotPaint.style = Paint.Style.FILL

        redSmiley = resources.getDrawable(R.drawable.ic_report_smiley_bad, null)
        yellowSmiley = resources.getDrawable(R.drawable.ic_report_smiley_poor, null)
        greenSmiley = resources.getDrawable(R.drawable.ic_report_smiley_good, null)
    }

    /**
     * This method is the setter for the day wise DSA to be plotted
     * @param dayWiseDSA - the day wise DSA
     */
    fun setDayWiseDSA(dayWiseDSA: Map<LocalDate, DailyUserFeeling?>) {
        this.dayWiseDSA = dayWiseDSA
        invalidate()
    }

    protected fun calculateGraphDimensions() {
        graphBounds.left = yLabelPadding
        graphBounds.right = measuredWidth.toFloat()
        graphBounds.top = 0f
        graphBounds.bottom = measuredHeight - xLabelPadding

        columnSpacing = graphBounds.width() / COLUMNS
        rowSpacing = graphBounds.height() / ROWS

        val smileyX = (graphBounds.left - (yLabelMargin + smileyWidth)).toInt()
        val smileyYOffset = ((rowSpacing - smileyHeight) / 2).toInt()
        val goodSmileyY = smileyYOffset
        val poorSmileyY = (rowSpacing + smileyYOffset).toInt()
        val badSmileyY = (2*rowSpacing + smileyYOffset).toInt()

        goodSmileyBounds.set(smileyX, goodSmileyY, smileyX + smileyWidth, goodSmileyY + smileyHeight)
        greenSmiley.bounds = goodSmileyBounds

        poorSmileyBounds.set(smileyX, poorSmileyY, smileyX + smileyWidth, poorSmileyY + smileyHeight)
        yellowSmiley.bounds = poorSmileyBounds

        badSmileyBounds.set(smileyX, badSmileyY, smileyX + smileyWidth, badSmileyY + smileyHeight)
        redSmiley.bounds = badSmileyBounds
    }

    /**
     * This method plots the dsa values.
     * @param canvas - the canvas on which the dsa values are plotted.
     */
    public override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (dayWiseDSA == null || currentDate == null) {
            return
        }

        calculateGraphDimensions()

        // Draw grid
        for(tick in 0..ROWS) {
            val y = tick * rowSpacing
            canvas.drawLine(graphBounds.left, y, graphBounds.right, y, linePaint)
        }

        for(tick in 0..COLUMNS) {
            val x = graphBounds.left + tick * columnSpacing
            canvas.drawLine(x, graphBounds.top, x, graphBounds.bottom, linePaint)
        }

        redSmiley.draw(canvas)
        yellowSmiley.draw(canvas)
        greenSmiley.draw(canvas)

        val tickHeight = if (printable) {
            resources.getDimension(R.dimen.dsa_summary_printable_graph_tick_height)
        } else {
            resources.getDimension(R.dimen.dsa_summary_graph_tick_height)
        }

        val y1 = graphBounds.bottom
        val y2 = y1 + tickHeight

        for(tick in FIRST_TICK_COLUMN..COLUMNS step TICK_STEP) {
            val x = graphBounds.right - (tick * columnSpacing + columnSpacing/2)
            canvas.drawLine(x, y1, x, y2, linePaint)

            val label = dateTimeLocalization.toShortMonthDay(currentDate!!.minusDays(tick.toLong()))
            canvas.drawText(label, x - textPaint.measureText(label)/2, y2 - textPaint.fontMetrics.ascent, textPaint)
        }

        for(column in 0 until COLUMNS) {
            val date = currentDate!!.minusDays(column.toLong())
             dayWiseDSA!![date]?.userFeeling?.let {userFeeling ->
                 val row = when (userFeeling) {
                     UserFeeling.BAD -> 2
                     UserFeeling.POOR -> 1
                     else -> 0
                 }

                 val x = graphBounds.right - (column * columnSpacing + columnSpacing/2)
                 val y = graphBounds.top + row * rowSpacing + rowSpacing/2

                 dotPaint.color = when(userFeeling) {
                     UserFeeling.BAD -> badDsaColor
                     UserFeeling.POOR -> poorDsaColor
                     else -> goodDsaColor
                 }

                 canvas.drawOval(x - dotRadius, y - dotRadius, x + dotRadius, y + dotRadius, dotPaint)
             }
        }
    }

    companion object {
        private val ROWS = 3
        private val FIRST_TICK_COLUMN = 3
        private val TICK_STEP = 7
        private val COLUMNS = 30
    }
}