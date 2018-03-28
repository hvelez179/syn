//
// DailyReportViewModel.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.activity.viewmodel.tracker


import android.databinding.Bindable
import android.support.annotation.WorkerThread
import com.teva.analysis.entities.HistoryDose
import com.teva.analysis.messages.HistoryUpdatedMessage
import com.teva.analysis.model.AnalyzedDataProvider
import com.teva.analysis.model.HistoryDay
import com.teva.cloud.dataentities.UserProfile
import com.teva.cloud.models.userprofile.UserProfileManager
import com.teva.common.utilities.DataTask
import com.teva.utilities.services.DependencyProvider
import com.teva.common.utilities.Messenger
import com.teva.devices.dataquery.DeviceDataQuery
import com.teva.respiratoryapp.BR
import com.teva.respiratoryapp.R
import com.teva.respiratoryapp.common.DateTimeLocalization
import com.teva.respiratoryapp.mvvmframework.ui.FragmentListViewModel
import com.teva.respiratoryapp.mvvmframework.ui.FragmentViewModel
import org.greenrobot.eventbus.Subscribe
import org.threeten.bp.LocalDate
import java.util.*

/**
 * Viewmodel for the Daily Report Popup.
 * The viewmodel is provided the localDate of the day to be displayed and asynchronously
 * loads the daily report data once the view is displayed. The viewmodel subscribes to the
 * HistoryUpdatedMessage and asynchronously reloads the daily report data when that occurs.
 *
 * @param dependencyProvider The dependency injection mechanism.
 * @param date The local date of the report.
 */
class DailyReportViewModel(dependencyProvider: DependencyProvider,
                           private val date: LocalDate) : FragmentListViewModel<DailyReportItemViewModel>(dependencyProvider) {

    private var eventItems = ArrayList<DailyReportItemViewModel>()

    /**
     * Gets the list of items.
     */
    override val items: List<DailyReportItemViewModel>
        get() = eventItems

    /**
     * A string indicating the number of inhalation events occurred during the day of the report.
     */
    @get:Bindable
    var eventsToday: String? = null
        private set(value) {
            field = value
            notifyPropertyChanged(BR.eventsToday)
        }

    /**
     * A string indicating the number of inhalers that were connected during the day of the report.
     */
    @get:Bindable
    var connectedInhalers: String? = null
        private set(value) {
            field = value
            notifyPropertyChanged(BR.connectedInhalers)
        }

    private var updateTask: UpdateTask? = null
    private var updatePending: Boolean = false

    init {
        updateFooter()
    }

    /**
     * Handles clicks on the popup close button.
     */
    fun onClose() {
        dependencyProvider.resolve<FragmentViewModel.NavigationEvents>().onBackPressed()
    }

    /**
     * Gets a formatted date for the daily report.
     */
    val formattedDate: String
        @Bindable
        get() = dependencyProvider.resolve<DateTimeLocalization>().toShortMonthDayYear(date)

    /**
     * The full text of the footer.
     */
    @get:Bindable
    var footerText: String = getString(R.string.tracker_footer_no_patient)
        set(value) {
            if (field != value) {
                field = value
                notifyPropertyChanged(BR.footerText)
            }
        }

    /**
     * Method called by the BaseFragment when the fragment's onStart() lifecycle method is called.
     */
    override fun onStart() {
        super.onStart()

        dependencyProvider.resolve<Messenger>().subscribe(this)
        updateList()
    }

    /**
     * Method called by the BaseFragment when the fragment's onStop() lifecycle method is called.
     */
    override fun onStop() {
        super.onStop()

        dependencyProvider.resolve<Messenger>().unsubscribeToAll(this)
    }

    /**
     * Message handler for the HistoryUpdatedMessage which is broadcast when the history
     * has been updated.
     *
     * @param message The message received.
     */
    @Subscribe
    fun onHistoryUpdated(message: HistoryUpdatedMessage) {
        updateList()
    }

    /**
     * Starts a new update task to asynchronously update the daily report data.
     */
    private fun updateList() {
        if (updateTask == null) {
            updatePending = false
            val task = UpdateTask(date)
            task.execute()
        } else {
            // an update is already in progress, so set a flag indicating that a change
            // occurred that might require the data to be updated again.
            updatePending = true
        }
    }

    /**
     * Updates the patient name footer asynchronously.
     */
    private fun updateFooter() {
        DataTask<Unit, UserProfile>("TrackerViewModel_updateFooter")
                .inBackground {
                    dependencyProvider.resolve<UserProfileManager>().getActive()
                }
                .onResult { userProfile ->
                    val formatter: DateTimeLocalization = dependencyProvider.resolve()

                    if (userProfile != null) {
                        val dob = userProfile.dateOfBirth?.let { formatter.toNumericMonthDayYear(it)} ?: ""

                        val arguments = mapOf(
                                "firstName" to (userProfile.firstName ?: ""),
                                "lastName" to (userProfile.lastName ?: ""),
                                "dob" to dob)

                        footerText = getString(R.string.tracker_footer_format, arguments)
                    }
                }
                .execute()
    }

    /**
     * A Background task that updates the daily report data in a worker thread.
     */
    private inner class UpdateTask(private val date: LocalDate)
        : DataTask<Unit, Unit>("DailyReportViewModel_UpdateTask") {
        private var historyDay: HistoryDay? = null
        private val newItems: ArrayList<DailyReportItemViewModel> = ArrayList()

        /**
         * Called to perform the task on a worker thread.

         * @param params The task parameters
         * *
         * @return The result of the task.
         */
        override fun doInBackground(vararg params: Unit): Unit? {
            val analyzedDataProvider = dependencyProvider.resolve<AnalyzedDataProvider>()
            historyDay = analyzedDataProvider.getHistory(date, date)[0]

            addDoses(newItems, historyDay!!.invalidDoses)
            addDoses(newItems, historyDay!!.relieverDoses)
            addDoses(newItems, historyDay!!.systemErrorDoses)

            // sort by time
            newItems.sortWith(Comparator { item1, item2 -> item2.time!!.compareTo(item1.time!!) })

            return null
        }

        /**
         * Creates DailyReportItemViewModels from a list of HistoryDose objects and adds the
         * viewmodels to a list.

         * @param newItems The list to add new viewmodels to.
         * *
         * @param doses    The list of HistoryDose objects.
         */
        @WorkerThread
        private fun addDoses(newItems: MutableList<DailyReportItemViewModel>, doses: List<HistoryDose>) {
            val deviceQuery = dependencyProvider.resolve<DeviceDataQuery>()

            // create a map of the devices by serial number.
            val deviceMap = deviceQuery.getAll().associateBy { it.serialNumber }

            for (dose in doses) {
                for (event in dose.events) {
                    val device = deviceMap[event.deviceSerialNumber]

                    newItems.add(DailyReportItemViewModel(dependencyProvider, event, device!!))
                }
            }
        }

        /**
         * Called on the main thread after the task is executed.

         * @param result Unused return result.
         */
        override fun onPostExecute(result: Unit?) {
            // update the DailyReport items
            this@DailyReportViewModel.eventItems = newItems

            // update the event count text
            var count = historyDay!!.relieverDoses.size + historyDay!!.invalidDoses.size
            var text: String
            if (count == 0) {
                text = getString(
                        R.string.trackerInhaleEventsToday_zero_text)
            } else if (count == 1) {
                text = getString(R.string.trackerInhaleEventsToday_one_text)
            } else {
                val map = HashMap<String, Any>()
                map.put("embeddedValue", count)
                text = getString(R.string.trackerInhaleEventsToday_text, map)
            }

            eventsToday = text

            // update the inhalers connected text
            count = historyDay!!.connectedInhalerCount
            if (count == 0) {
                text = getString(R.string.trackerConnectedInhalersToday_zero_text)
            } else if (count == 1) {
                text = getString(R.string.trackerConnectedInhalersToday_one_text)
            } else {
                val map = HashMap<String, Any>()
                map.put("embeddedValue", count)
                text = getString(R.string.trackerConnectedInhalersToday_text, map)
            }

            connectedInhalers = text

            notifyListChanged()

            updateTask = null
            if (updatePending) {
                updateList()
            }
        }
    }
}
