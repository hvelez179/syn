package com.teva.respiratoryapp.activity.viewmodel.tracker


import android.databinding.Bindable
import android.support.annotation.WorkerThread

import com.teva.analysis.extensions.enumerations.InhalationEffort
import com.teva.analysis.extensions.inhalationEffort
import com.teva.analysis.model.AnalyzedDataProvider
import com.teva.analysis.model.HistoryDay
import com.teva.cloud.dataentities.UserProfile
import com.teva.cloud.models.userprofile.UserProfileManager
import com.teva.respiratoryapp.common.DateTimeLocalization
import com.teva.respiratoryapp.mvvmframework.ui.FragmentViewModel
import com.teva.common.services.TimeService
import com.teva.common.utilities.DataTask
import com.teva.utilities.services.DependencyProvider
import com.teva.common.utilities.LocalizationService
import com.teva.devices.entities.InhaleEvent
import com.teva.respiratoryapp.R
import com.teva.userfeedback.dataquery.DailyUserFeelingDataQuery
import com.teva.userfeedback.entities.DailyUserFeeling

import org.threeten.bp.LocalDate
import java.util.HashMap

/**
 * This class is the viewmodel for the user reports.
 */
class ReportViewModel(dependencyProvider: DependencyProvider) : FragmentViewModel(dependencyProvider) {

    /**
     * This class stores the start and end days for each of the weeks
     * in the weekly inhalation summary.
     */
    class WeekBounds(val startDay:LocalDate, val endDay: LocalDate)

    /**
     * Data class to hold week summary items
     */
    class WeekSummary(val bounds: WeekBounds,
                      val events: HashMap<InhalationEffort, Int>)

    private var dailySummaryStartDay: LocalDate? = null
    private var weeklySummaryStartDay: LocalDate? = null

    var dailySummaryDateRange: String? = null
        private set
    var currentDisplayDate: String? = null
        private set
    var currentDisplayTime: String? = null
        private set
    var currentDate: LocalDate? = null
        private set
    var printableReportTitle: String? = null
        private set
    var printableDateAndTime: String? = null
        private set
    private val dayWiseInhaleEvents = HashMap<LocalDate, HashMap<InhalationEffort, Int>>()


    private val weekWiseInhaleEvents = HashMap<Int, WeekSummary>()

    var weeklySummaryDateRange: String? = null
        private set


    var dayWiseDSA: Map<LocalDate, DailyUserFeeling?> = HashMap()
        private set

    /**
     * The name of the user
     */
    @get:Bindable
    var name: String = ""

    /**
     * This function returns a map of inhalation count for each inhalation effort type
     * for each day in the daily inhalation summary report
     *
     * @return - Returns a map containing inhalation event count for each inhalation type
     * for each day
     */
    fun getDayWiseInhaleEvents(): HashMap<LocalDate, HashMap<InhalationEffort, Int>> {
        return dayWiseInhaleEvents
    }

    /**
     * This function returns a map of inhalation count for each inhalation effort type
     * for each week in the weekly inhalation summary report
     *
     * @return - Returns a map containing inhalation event count for each inhalation type
     * for each week
     */
    fun getWeekWiseInhaleEvents(): HashMap<Int, WeekSummary> {
        return weekWiseInhaleEvents
    }

    /**
     * Function called by the BaseFragment when the fragment's onStart() lifecycle method is called.
     */
    override fun onStart() {
        initializeDateRanges()
        loadDataAsynchronously()
    }

    /**
     * This function initializes the date range for the reports and updates
     * the date labels for display
     */
    private fun initializeDateRanges() {
        val dateTimeLocalizationService = dependencyProvider.resolve(DateTimeLocalization::class.java)
        val timeService = dependencyProvider.resolve(TimeService::class.java)

        val today = timeService.today()
        currentDate = today
        val endDate = dateTimeLocalizationService.toShortMonthDayYear(today)

        val dailySummaryBeginDate = today.minusDays((DAILY_SUMMARY_NUMBER_OF_DAYS - 1).toLong())
        dailySummaryStartDay = dailySummaryBeginDate
        val dailySummaryStartDate = dateTimeLocalizationService.toShortMonthDayYear(dailySummaryBeginDate)
        dailySummaryDateRange = dailySummaryStartDate + " - " + endDate

        val weeklySummaryBeginDate = today.minusDays((WEEKLY_SUMMARY_NUMBER_OF_DAYS - 1).toLong())
        weeklySummaryStartDay = weeklySummaryBeginDate
        val weeklySummaryStartDate = dateTimeLocalizationService.toShortMonthDayYear(weeklySummaryBeginDate)
        weeklySummaryDateRange = weeklySummaryStartDate + " - " + endDate

        currentDisplayDate = endDate
        currentDisplayTime = dateTimeLocalizationService.toShortTime(timeService.localTime())
        printableReportTitle = dependencyProvider.resolve(LocalizationService::class.java).getString(R.string.user_report_printable_title_text)
        printableDateAndTime = endDate + "\n" + dateTimeLocalizationService.toShortTime(timeService.localTime())
    }

    /**
     * This function asynchronously loads the data needed for the user reports.
     */
    private fun loadDataAsynchronously() {
        DataTask<Unit, ReportData>("ReportViewModel_loadData")
                .inBackground {
                    return@inBackground loadData()
                }
                .onResult {
                    result ->
                    result?.let {
                        processInhaleEvents(result.historyDays)
                        dayWiseDSA = result.dsaData

                        val formatter: DateTimeLocalization = dependencyProvider.resolve()

                        name = result.profile?.let { profile ->
                            val dob = result.profile.dateOfBirth?.let { formatter.toNumericMonthDayYear(it)} ?: ""

                            val arguments = mapOf(
                                    "firstName" to (result.profile.firstName ?: ""),
                                    "lastName" to (result.profile.lastName ?: ""),
                                    "dob" to dob)

                             getString(R.string.user_report_name_dob, arguments)
                        } ?: ""

                        notifyChange()
                    }
                }
                .execute()
    }

    /**
     * This function sets up the collection for filling the weekly inhalation summary data.
     */
    private fun initializeWeekWiseInhaleEvents() {
        currentDate?.let { summaryEndDate ->
            for(week in 0 until WEEKLY_SUMMARY_NUMBER_OF_WEEKS) {
                val bounds = WeekBounds(summaryEndDate.minusDays(((week + 1) * DAYS_PER_WEEK - 1).toLong()),
                        summaryEndDate.minusDays((week * DAYS_PER_WEEK).toLong()))
                val summary = WeekSummary(bounds, HashMap<InhalationEffort, Int>())
                weekWiseInhaleEvents.put(week, summary)
            }
        }
    }

    /**
     * This function returns the week in the weekly inhalation summary report to which the
     * given date belongs.
     *
     * @param day - the day for which the week number is to be determined
     * @return - the number of the weeks from the current week to which the day belongs
     * @throws - exception if the day is not within 12 weeks from the current day
     */
    private fun findWeekForHistoryDay(day: LocalDate) : Int {
        for(week in 0..WEEKLY_SUMMARY_NUMBER_OF_WEEKS - 1) {
            val startDay = weekWiseInhaleEvents[week]?.bounds?.startDay
            val endDay = weekWiseInhaleEvents[week]?.bounds?.endDay
            if(day.isEqual(startDay) || day.isEqual(endDay) || (day.isBefore(endDay) && day.isAfter(startDay))) {
                return week
            }
        }
        throw Exception("Invalid Day")
    }

    /**
     * This function updates the inhalation count for each inhalation effort type for a single day.
     *
     * @param inhaleEvents - the inhale events list to be processed for updating the daily inhale event information
     */
    private fun updateInhaleEventCountForDay( inhaleEvents: List<InhaleEvent>, day: LocalDate) {
        for(inhaleEvent: InhaleEvent in inhaleEvents) {
            val inhalationEffort: InhalationEffort = inhaleEvent.inhalationEffort

            var eventCount = dayWiseInhaleEvents[day]?.get(inhalationEffort) ?: 0
            eventCount++

            dayWiseInhaleEvents[day]?.put(inhalationEffort, eventCount)
        }
    }

    /**
     * This function updates the inhalation count for each inhalation effort type for a week from the day.
     *
     * @param inhaleEvents - the inhale events list to be processed for updating the weekly inhale event information.
     */
    private fun updateInhaleEventCountForWeek( inhaleEvents: List<InhaleEvent>, day: LocalDate) {
        val week = findWeekForHistoryDay(day)

        for(inhaleEvent: InhaleEvent in inhaleEvents) {
            val inhalationEffort: InhalationEffort = inhaleEvent.inhalationEffort

            var eventCount = weekWiseInhaleEvents[week]?.events?.get(inhalationEffort) ?: 0
            eventCount++

            weekWiseInhaleEvents[week]?.events?.put(inhalationEffort, eventCount)
        }
    }

    /**
     * This function processes the inhale events for each history day and updates
     * the daily and weekly inhalation count.
     *
     * @param historyDays - the list of history days containing inhalation event information.
     */
    private fun processInhaleEvents(historyDays: List<HistoryDay>) {
        dayWiseInhaleEvents.clear()
        weekWiseInhaleEvents.clear()
        initializeWeekWiseInhaleEvents()

        for (historyDay in historyDays) {

            var processForDailySummary = false

            if(!historyDay.day.isBefore(dailySummaryStartDay)) {
                processForDailySummary = true
                dayWiseInhaleEvents.put(historyDay.day, HashMap<InhalationEffort, Int>())
            }

            for (dose in historyDay.relieverDoses) {
                updateInhaleEventCountForWeek(dose.events, historyDay.day)

                if(processForDailySummary) {
                    updateInhaleEventCountForDay(dose.events, historyDay.day)
                }
            }

            for (dose in historyDay.invalidDoses) {
                updateInhaleEventCountForWeek(dose.events, historyDay.day)

                if(processForDailySummary) {
                    updateInhaleEventCountForDay(dose.events, historyDay.day)
                }
            }
        }
    }

    /**
     * This function loads the data required for each of the three user reports.
     *
     * @return - a pair containing the history days and dsa information.
     */
    @WorkerThread
    private fun loadData(): ReportData? {

        return currentDate?.let {today ->
            val dailySummaryStartDate = dailySummaryStartDay ?: today.minusDays((DAILY_SUMMARY_NUMBER_OF_DAYS - 1).toLong())
            val weeklySummaryStartDate = weeklySummaryStartDay ?: today.minusDays((WEEKLY_SUMMARY_NUMBER_OF_DAYS - 1).toLong())

            val historyDays = dependencyProvider.resolve(AnalyzedDataProvider::class.java)
                    .getHistory(weeklySummaryStartDate, today)

            val dsaData = dependencyProvider.resolve(DailyUserFeelingDataQuery::class.java)
                    .get(dailySummaryStartDate, today)

            val profile = dependencyProvider.resolve<UserProfileManager>().getActive()

            ReportData(historyDays, dsaData, profile)
        } ?: null
    }

    private data class ReportData(
            val historyDays: List<HistoryDay>,
            val dsaData: Map<LocalDate, DailyUserFeeling?>,
            val profile: UserProfile?)

    companion object {
        /**
         * Daily inhalation summary constants
         */
        private val DAILY_SUMMARY_NUMBER_OF_DAYS = 30

        /**
         * Weekly inhalation summary constants
         */
        private val WEEKLY_SUMMARY_NUMBER_OF_WEEKS = 12
        private val DAYS_PER_WEEK = 7
        private val WEEKLY_SUMMARY_NUMBER_OF_DAYS = DAYS_PER_WEEK * WEEKLY_SUMMARY_NUMBER_OF_WEEKS
    }
}
