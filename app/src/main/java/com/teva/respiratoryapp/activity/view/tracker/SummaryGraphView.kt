package com.teva.respiratoryapp.activity.view.tracker

import android.content.Context
import android.graphics.*
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import com.teva.analysis.extensions.enumerations.InhalationEffort
import com.teva.utilities.services.DependencyProvider
import com.teva.respiratoryapp.R
import com.teva.respiratoryapp.common.DateTimeLocalization
import com.teva.respiratoryapp.mvvmframework.utils.applyAppearance
import java.lang.Math.max

/**
 * Base class for summary graphs.
 */
abstract class SummaryGraphView(context: Context,
                       attrs: AttributeSet?) : View(context, attrs) {
    protected val boldLineColor: Int
    protected val lineColor: Int
    protected val redLineColor: Int
    protected val tickLineColor: Int

    protected val drawPaint = Paint()
    protected val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
    protected val overflowTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
    protected val linePaint = Paint()

    protected val columnWidth: Float

    protected val graphBounds = RectF()
    protected var verticalTicks = 0
    protected var verticalTickSpacing = 0f
    protected var boldTickStep = 0
    protected var verticalTickStep = 1
    protected var columns = 0
    protected var columnSpacing = 0f
    protected var firstHorizontalTickColumn = 0
    protected var horizontalTickStep = 0
    protected var redLineTick = -1

    protected var maxNumberOfEvents = 1

    protected val yLabelPadding: Float
    protected val xLabelPadding: Float
    protected val yLabelMargin: Float
    protected val xLabelMargin: Float
    protected val graphRightPadding: Float

    protected var printable: Boolean = false

    protected val dateTimeLocalization = DependencyProvider.default.resolve<DateTimeLocalization>()

    private val goodInhalationBitmap: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_user_report_good1)
    private val goodInhalationBitmapShader: BitmapShader = BitmapShader(goodInhalationBitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
    private val lowInhalationBitmap: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_user_report_good2)
    private val lowInhalationBitmapShader: BitmapShader = BitmapShader(lowInhalationBitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
    private val noInhalationBitmap: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_user_report_no)
    private val noInhalationBitmapShader: BitmapShader = BitmapShader(noInhalationBitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
    private val exhalationBitmap: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_user_report_exhalation)
    private val exhalationBitmapShader: BitmapShader = BitmapShader(exhalationBitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
    private val airVentBlockBitmap: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_user_report_possible)
    private val airVentBlockBitmapShader: BitmapShader = BitmapShader(airVentBlockBitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)

    init {

        var labelAppearanceId = 0
        var overflowAppearanceId = 0

        val ta = context.obtainStyledAttributes(attrs, R.styleable.SummaryGraphView, 0, 0)
        try {
            val isPrintable = ta.getBoolean(R.styleable.SummaryGraphView_printable, false)

            printable = isPrintable

            boldLineColor = ta.getColor(R.styleable.SummaryGraphView_boldLineColor, 0)
            lineColor = ta.getColor(R.styleable.SummaryGraphView_lineColor, 0)
            redLineColor = ta.getColor(R.styleable.SummaryGraphView_redLineColor, 0)
            tickLineColor = ta.getColor(R.styleable.SummaryGraphView_tickLineColor, 0)

            columnWidth = ta.getDimension(R.styleable.SummaryGraphView_columnWidth, 1f)

            labelAppearanceId = ta.getResourceId(R.styleable.SummaryGraphView_labelTextAppearance, 0)
            overflowAppearanceId = ta.getResourceId(R.styleable.SummaryGraphView_overflowTextAppearance, 0)

            yLabelPadding = ta.getDimension(R.styleable.SummaryGraphView_yLabelPadding, 0f)
            xLabelPadding = ta.getDimension(R.styleable.SummaryGraphView_xLabelPadding, 0f)
            xLabelMargin = ta.getDimension(R.styleable.SummaryGraphView_xLabelMargin, 0f)
            yLabelMargin = ta.getDimension(R.styleable.SummaryGraphView_yLabelMargin, 0f)

            graphRightPadding = ta.getDimension(R.styleable.SummaryGraphView_graphRightPadding, 0f)
        } finally {
            ta.recycle()
        }

        textPaint.applyAppearance(context, labelAppearanceId)
        overflowTextPaint.applyAppearance(context, overflowAppearanceId)
    }

    protected fun calculateGraphDimensions() {
        graphBounds.left = yLabelPadding
        graphBounds.right = measuredWidth.toFloat() - graphRightPadding
        graphBounds.top = if(printable) {
            resources.getDimension(R.dimen.daily_summary_printable_graph_top_axis_offset)
        } else {
            resources.getDimension(R.dimen.daily_summary_graph_top_axis_offset)
        }
        graphBounds.bottom = measuredHeight - xLabelPadding

        verticalTickSpacing = graphBounds.height() / verticalTicks
        columnSpacing = graphBounds.width() / columns
    }

    /**
     * This method plots the inhale events.
     * @param canvas - the canvas on which the events are plotted.
     */
    public override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        calculateGraphDimensions()

        val axisOffset = if (printable) {
            resources.getDimension(R.dimen.daily_summary_printable_graph_axis_offset)
        } else {
            resources.getDimension(R.dimen.daily_summary_graph_axis_offset)
        }
        val left = graphBounds.left - axisOffset
        val right = graphBounds.right

        val lineStrokeWidth = if (printable) {
            resources.getDimension(R.dimen.daily_summary_printable_graph_line_stroke_width)
        } else {
            resources.getDimension(R.dimen.daily_summary_graph_line_stroke_width)
        }

        val tickStrokeWidth = if (printable) {
            resources.getDimension(R.dimen.daily_summary_printable_graph_tick_stroke_width)
        } else {
            resources.getDimension(R.dimen.daily_summary_graph_tick_stroke_width)
        }

        linePaint.strokeWidth = lineStrokeWidth

        // draw horizontal tick lines and labels
        val yLabelOffset = (textPaint.fontMetrics.ascent + textPaint.fontMetrics.descent) / 2
        for (tick in 0..verticalTicks step verticalTickStep) {
            val y = graphBounds.bottom - tick * verticalTickSpacing

            if (tick != 0) {
                linePaint.color = when {
                    tick == redLineTick -> redLineColor
                    tick % boldTickStep == 0 -> boldLineColor
                    else -> lineColor
                }

                canvas.drawLine(left, y, right, y, linePaint)
            }


            val labelText = getVerticalTickLabel(tick)
            labelText?.let {
                val yText = y - yLabelOffset
                val xText = graphBounds.left - (yLabelMargin + textPaint.measureText(it))
                canvas.drawText(it, xText, yText, textPaint)
            }
        }

        linePaint.color = tickLineColor
        linePaint.strokeWidth = tickStrokeWidth
        val tickHeight = if (printable) {
            resources.getDimension(R.dimen.daily_summary_printable_graph_tick_height)
        } else {
            resources.getDimension(R.dimen.daily_summary_graph_tick_height)
        }
        val y1 = graphBounds.bottom
        val y2 = y1 + tickHeight
        val yLabel = graphBounds.bottom -
                textPaint.fontMetrics.ascent +
                xLabelMargin

        for (tick in firstHorizontalTickColumn until columns step horizontalTickStep) {
            val x = graphBounds.right - ((tick * columnSpacing) + (columnSpacing / 2))
            canvas.drawLine(x, y1, x, y2, linePaint)

            val labelText = getHorizontalTickLabel(tick)
            labelText?.let {
                canvas.drawText(it, x - textPaint.measureText(it)/2, yLabel, textPaint)
            }
        }

        val additionalHeight = if (printable) {
            resources.getDimension(R.dimen.daily_summary_printable_graph_axis_offset)
        } else {
            resources.getDimension(R.dimen.daily_summary_graph_axis_offset)
        }

        val inhalationEffortsInProcessingOrder = arrayOf(InhalationEffort.ERROR, InhalationEffort.EXHALATION, InhalationEffort.NO_INHALATION, InhalationEffort.LOW_INHALATION, InhalationEffort.GOOD_INHALATION)

        var xCurrent = graphBounds.right - (columnSpacing / 2)
        var halfColumnWidth = columnWidth / 2f

        for (column in 0 until columns) {
            val events = getColumnEvents(column)

            val totalEventCount = events?.values?.sum() ?: 0
            var eventCount = 0
            var yBottom = graphBounds.bottom

            for (currentEffort in 0..inhalationEffortsInProcessingOrder.size - 1) {
                val inhalationEffort = inhalationEffortsInProcessingOrder[currentEffort]
                var eventCountOfCurrentEffortType = events?.get(inhalationEffort) ?: 0

                if (eventCountOfCurrentEffortType == 0) {
                    continue
                }

                val yTop = max(yBottom - eventCountOfCurrentEffortType * verticalTickSpacing,
                        graphBounds.top - additionalHeight)

                eventCount += eventCountOfCurrentEffortType

                drawPaint.shader = when (inhalationEffort) {
                    InhalationEffort.LOW_INHALATION -> lowInhalationBitmapShader
                    InhalationEffort.GOOD_INHALATION -> goodInhalationBitmapShader
                    InhalationEffort.NO_INHALATION -> noInhalationBitmapShader
                    InhalationEffort.EXHALATION -> exhalationBitmapShader
                    InhalationEffort.ERROR -> airVentBlockBitmapShader
                    else -> null
                }


                canvas.drawRect(xCurrent-halfColumnWidth, yTop, xCurrent+halfColumnWidth, yBottom, drawPaint)

                yBottom = yTop

                if (eventCount >= maxNumberOfEvents) {
                    break
                }
            }

            if (totalEventCount > maxNumberOfEvents) {
                val eventCountText = totalEventCount.toString()
                val textWidth = overflowTextPaint.measureText(eventCountText)
                val textX = xCurrent - (textWidth / 2)
                val textY = yBottom - overflowTextPaint.fontMetrics.descent
                canvas.drawText(totalEventCount.toString(), textX, textY, overflowTextPaint)
            }

            xCurrent -= columnSpacing
        }

        // draw axis lines
        linePaint.color = boldLineColor
        linePaint.strokeWidth = lineStrokeWidth
        canvas.drawLine(left, graphBounds.bottom, right, graphBounds.bottom, linePaint)
        canvas.drawLine(graphBounds.left, graphBounds.top - axisOffset, graphBounds.left, graphBounds.bottom, linePaint)
    }

    protected abstract fun getVerticalTickLabel(tick: Int): String?

    protected abstract fun getHorizontalTickLabel(tick: Int): String?

    protected abstract fun getColumnEvents(column: Int): HashMap<InhalationEffort, Int>?

}