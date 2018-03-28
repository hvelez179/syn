//
// DateValidator.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.activity.controls


import android.databinding.InverseBindingListener
import android.os.Handler
import android.text.Editable
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import com.teva.common.services.TimeService
import com.teva.utilities.services.DependencyProvider
import com.teva.respiratoryapp.R
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import java.util.*

/**
 * Input validator class that validates dates.
 */
class DateValidator : InputValidator() {

    /**
     * The format of the date (mm/dd/yyy or dd/mm/yyy)
     */
    var format = DateFormat.MONTH_DAY_YEAR
        set(value) {
            field = value
            pattern = value.pattern
        }

    /**
     * The minimum valid year
     */
    var minYear = 1900

    /**
     * The maximum valid year
     */
    var maxYear = DependencyProvider.default.resolve<TimeService>().today().year

    /**
     * The hint string resource id for the current format.
     */
    val hint: Int
        get() { return format.hintResource }

    /**
     * The current entered date or null if the date is incomplete or invalid.
     */
    var date: LocalDate? = null
        set(value) {
            if (field != value) {
                field = value
                Handler().post {
                    dateListener?.onChange()
                }
            }
        }

    /**
     * The listener for date changes.
     */
    var dateListener: InverseBindingListener? = null

    /**
     * Verifies if a string is a valid or incomplete day value.
     *
     * @param str The date component string.
     */
    private fun isDayStringValid(str: String?): Boolean {
        val day = str?.toInt()

        return (str?.length == 1 && day in 0..3) ||
                (str?.length == 2 && day in 1..31)
    }

    /**
     * Verifies if a string is a valid or incomplete month value.
     *
     * @param str The date component string.
     */
    private fun isMonthStringValid(str: String?): Boolean {
        val month = str?.toInt()

        return (str?.length == 1 && month in 0..1) ||
                (str?.length == 2 && month in 1..12)
    }

    /**
     * Verifies if a string is a valid or incomplete year value.
     *
     * @param str The date component string.
     */
    private fun isYearStringValid(str: String?): Boolean {
        val year = str?.toInt()

        return (str?.length == 1 && year in (minYear/1000)..(maxYear/1000)) ||
                (str?.length == 2 && year in (minYear/100)..(maxYear/100)) ||
                (str?.length == 3 && year in (minYear/10)..(maxYear/10)) ||
                (str?.length == 4 && year in minYear..maxYear)
    }

    /**
     * Verifies if a month and day combination is valid.
     *
     * @param day The day date component string.
     * @param month The month date component string.
     * @param year The year date component string.
     */
    private fun isMonthAndDayValid(day: String?, month: String?, year: String?): Boolean {

        if (!isMonthStringValid(month) || !isDayStringValid(day)) {
            return false
        }

        // use a leap year if year not provided or invalid
        val evalYear = if (isYearStringValid(year) && year?.length == YEAR_LENGTH) {
            year.toInt()
        } else {
            A_LEAP_YEAR
        }

        val calendar = GregorianCalendar(evalYear, month!!.toInt() - 1, 1)
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        return day!!.toInt() <= daysInMonth
    }

    /**
     * Extracts a date component field from the input string.
     *
     * @param str The input string.
     * @param startPosition The starting index of the date component field.
     * @param count The number of characters in the field.
     */
    private fun getField(str: String, startPosition: Int, count: Int): String? {
        if (str.length <= startPosition) {
            return null
        }

        val endPosition = minOf(str.length, startPosition + count)

        return str.substring(startPosition, endPosition)
    }

    /**
     * Validates the input string and highlights date components that are in error.
     *
     * @param editable The input string.
     */
    override fun validate(editable: Editable) {
        super.validate(editable)

        val str = editable.toString()

        val day = getField(str, format.dayPosition, DAY_LENGTH)
        val month = getField(str, format.monthPosition, MONTH_LENGTH)
        val year = getField(str, format.yearPosition, YEAR_LENGTH)

        val yearValid = isYearStringValid(year)
        val monthAndDayValid = isMonthAndDayValid(day, month, year)

        // remove the current color spans
        val spans = editable.getSpans(0, editable.length, ForegroundColorSpan::class.java)
        for(span in spans) {
            editable.removeSpan(span)
        }

        var newState = ValidationState.INCOMPLETE

        if (str.length == pattern!!.length && yearValid && monthAndDayValid) {
            // everything is valid set the date
            date = LocalDate.of(year!!.toInt(), month!!.toInt(), day!!.toInt())
            newState = ValidationState.VALID
        } else {
            date = null
            val monthValid = isMonthStringValid(month)
            val dayValid = isDayStringValid(day)

            if ((month != null && (!monthValid || (dayValid && !monthAndDayValid))) ||
                    (day != null && (!dayValid || (monthValid && !monthAndDayValid))) ||
                    (year != null && !yearValid)){
                // highlight year
                newState = ValidationState.IN_ERROR
                editable.setSpan(
                        ForegroundColorSpan(highlightColor),
                        0,
                        editable.length,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }

        validationState = newState
    }

    /**
     * Converts a LocalDate into a string that matches the pattern.
     *
     * @param localDate The LocalDate to convert.
     */
    fun textFromDate(localDate: LocalDate?): String? {
        if (localDate == null) {
            return null
        }

        val formatter = DateTimeFormatter.ofPattern(format.formatString)
        return localDate.format(formatter)
    }

    /**
     * An enumeration of supported date formats.
     *
     * @param hintResource The string resource id for the hint.
     * @param dayPosition The index of the day field in the pattern.
     * @param monthPosition The index of the month field in the pattern.
     * @param yearPosition The index of the year field in the pattern.
     * @param pattern The input pattern string.
     * @param formatString The DateFormatter format string for this format.
     */
    enum class DateFormat(
            internal val hintResource: Int,
            internal val dayPosition: Int,
            internal val monthPosition: Int,
            internal val yearPosition: Int,
            internal val pattern: String,
            internal val formatString: String)
    {
        MONTH_DAY_YEAR(R.string.date_month_day_year_hint, 5, 0, 10, "## / ## / ####", "MM / dd / yyyy"),
        DAY_MONTH_YEAR(R.string.date_day_month_year_hint, 0, 5, 10, "## / ## / ####", "dd / MM / yyyy")
    }

    companion object {
        val A_LEAP_YEAR = 2016

        val MONTH_LENGTH = 2
        val DAY_LENGTH = 2
        val YEAR_LENGTH = 4
    }
}
