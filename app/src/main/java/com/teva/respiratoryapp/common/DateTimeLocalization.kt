//
// DateTimeLocalization.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.common

import com.teva.common.services.TimeService
import com.teva.respiratoryapp.R
import com.teva.utilities.services.DependencyProvider
import com.teva.common.utilities.LocalizationService

import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle
import java.util.*

/**
 * This class provides specialized date and time formatting used by the viewmodels.
 *
 * @param dependencyProvider The dependency injection mechanism.
 */
class DateTimeLocalization(private val dependencyProvider: DependencyProvider,
                           private val supportsUSLocaleOnly: Boolean) {
    private val shortDayOfWeek: MutableMap<DayOfWeek, String>
    private val today: String
    private val shortMonthDayFormatter: DateTimeFormatter
    private val shortMonthDayYearFormatter: DateTimeFormatter
    private val monthDayNumericFormatter: DateTimeFormatter
    private val fullWeekDayFormatter: DateTimeFormatter
    private val shortMonthFormatter: DateTimeFormatter
    private val monthDayYearNumericFormatter: DateTimeFormatter

    init {
        val localizationService = dependencyProvider.resolve<LocalizationService>()

        shortDayOfWeek = HashMap<DayOfWeek, String>()
        shortDayOfWeek.put(DayOfWeek.SUNDAY, localizationService.getString(R.string.tracker_Day_Sunday_text))
        shortDayOfWeek.put(DayOfWeek.MONDAY, localizationService.getString(R.string.tracker_Day_Monday_text))
        shortDayOfWeek.put(DayOfWeek.TUESDAY, localizationService.getString(R.string.tracker_Day_Tuesday_text))
        shortDayOfWeek.put(DayOfWeek.WEDNESDAY, localizationService.getString(R.string.tracker_Day_Wednesday_text))
        shortDayOfWeek.put(DayOfWeek.THURSDAY, localizationService.getString(R.string.tracker_Day_Thursday_text))
        shortDayOfWeek.put(DayOfWeek.FRIDAY, localizationService.getString(R.string.tracker_Day_Friday_text))
        shortDayOfWeek.put(DayOfWeek.SATURDAY, localizationService.getString(R.string.tracker_Day_Saturday_text))

        today = localizationService.getString(R.string.today)

        val shortMonthDayFormat = localizationService.getString(R.string.short_month_day_format_text)
        shortMonthDayFormatter = if(supportsUSLocaleOnly) DateTimeFormatter.ofPattern(shortMonthDayFormat, Locale.US) else DateTimeFormatter.ofPattern(shortMonthDayFormat)

        val shortMonthDayYearFormat = localizationService.getString(R.string.short_month_day_year_format_text)
        shortMonthDayYearFormatter = if(supportsUSLocaleOnly) DateTimeFormatter.ofPattern(shortMonthDayYearFormat, Locale.US) else DateTimeFormatter.ofPattern(shortMonthDayYearFormat)

        val shortMonthFormat = localizationService.getString(R.string.short_month_format_text)
        shortMonthFormatter = if(supportsUSLocaleOnly) DateTimeFormatter.ofPattern(shortMonthFormat, Locale.US) else DateTimeFormatter.ofPattern(shortMonthFormat)


        //Todo - this format needs to vary based on locale
        val monthDayNumericFormat = localizationService.getString(R.string.month_day_numeric_format_text)
        monthDayNumericFormatter = if(supportsUSLocaleOnly) DateTimeFormatter.ofPattern(monthDayNumericFormat, Locale.US) else DateTimeFormatter.ofPattern(monthDayNumericFormat)

        //Todo - this format needs to vary based on locale
        val monthDayYearNumericFormat = localizationService.getString(R.string.month_day_year_numeric_format_text)
        monthDayYearNumericFormatter = if(supportsUSLocaleOnly) DateTimeFormatter.ofPattern(monthDayYearNumericFormat, Locale.US) else DateTimeFormatter.ofPattern(monthDayYearNumericFormat)

        val fullWeekDayFormat = localizationService.getString(R.string.full_week_day_format_text)
        fullWeekDayFormatter = if(supportsUSLocaleOnly) DateTimeFormatter.ofPattern(fullWeekDayFormat, Locale.US) else DateTimeFormatter.ofPattern(fullWeekDayFormat)
    }

    /**
     * This method returns a short day of week abbreviation (MON, TUE, etc) for a LocalDate.
     *
     * @param date The localDate to format
     * *
     * @return A day of week abbreviation.
     */
    fun toShortDayOfWeek(date: LocalDate, useToday: Boolean = false): String {
        if (useToday && dependencyProvider.resolve<TimeService>().today() == date) {
            return today
        }

        return shortDayOfWeek[date.dayOfWeek]!!
    }

    /**
     * This method formats a local date into a short month and day string.  Ex: "Feb 2"

     * @param date The localDate to format.
     * *
     * @return A short month and day string for the LocalDate.
     */
    fun toShortMonthDay(date: LocalDate): String {
        return shortMonthDayFormatter.format(date)
    }

    /**
     * This method formats a local date into a short month string.  Ex: "Feb"

     * @param date The localDate to format.
     * *
     * @return A short month string for the LocalDate.
     */
    fun toShortMonth(date: LocalDate): String {
        return shortMonthFormatter.format(date)
    }

    /**
     * This method formats a local date into a numeric month and day string.  Ex: "02/02"

     * @param date The localDate to format.
     * *
     * @return A numeric month and day string for the LocalDate.
     */
    fun toNumericMonthDay(date: LocalDate): String {
        return monthDayNumericFormatter.format(date)
    }

    /**
     * This method formats a local date into a numeric month, day, year string.  Ex: "02/05/2018"
     *
     * @param date The localDate to format.
     *
     * @return A numeric month and day string for the LocalDate.
     */
    fun toNumericMonthDayYear(date: LocalDate): String {
        return monthDayYearNumericFormatter.format(date)
    }

    /**
     * This method formats a local date into a complete day of the week string.  Ex: "Tuesday"

     * @param date The localDate to format.
     * *
     * @return A full day of the week string for the LocalDate.
     */
    fun toFullWeekDay(date: LocalDate): String {
        return fullWeekDayFormatter.format(date)
    }

    /**
     * This method formats a local date into a short month, day, and year string. Ex: "Feb 2, 2017"

     * @param date The localDate to format.
     * *
     * @return A short month, day, and year string for the LocalDate.
     */
    fun toShortMonthDayYear(date: LocalDate): String {
        return shortMonthDayYearFormatter.format(date)
    }

    /**
     * This method formats a local time into a short time string. Ex: "12:56 PM"

     * @param localTime The local time to format.
     * *
     * @return A short time string.
     */
    fun toShortTime(localTime: LocalTime): String {
        return if(supportsUSLocaleOnly) DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(Locale.US).format(localTime) else DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).format(localTime)
    }

}
