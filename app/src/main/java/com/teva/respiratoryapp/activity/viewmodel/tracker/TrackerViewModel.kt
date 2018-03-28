//
// TrackerViewModel.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.activity.viewmodel.tracker

import android.annotation.SuppressLint
import android.databinding.Bindable
import android.view.MenuItem
import com.teva.analysis.messages.HistoryUpdatedMessage
import com.teva.analysis.model.AnalyzedDataProvider
import com.teva.analysis.model.HistoryDay
import com.teva.cloud.dataentities.UserProfile
import com.teva.cloud.models.userprofile.UserProfileManager
import com.teva.common.services.TimeService
import com.teva.common.utilities.DataTask
import com.teva.utilities.services.DependencyProvider
import com.teva.utilities.utilities.Logger.Level.VERBOSE
import com.teva.common.utilities.Messenger
import com.teva.respiratoryapp.BR
import com.teva.respiratoryapp.R
import com.teva.respiratoryapp.common.DateTimeLocalization
import com.teva.respiratoryapp.mvvmframework.ui.FragmentViewModel
import org.greenrobot.eventbus.Subscribe
import org.threeten.bp.LocalDate
import org.threeten.bp.temporal.ChronoUnit
import java.lang.ref.WeakReference
import java.util.*

/**
 * This class is the viewmodel for the Tracker screen
 *
 * @param dependencyProvider The dependency injection mechanism.
 */
class TrackerViewModel(dependencyProvider: DependencyProvider) : FragmentViewModel(dependencyProvider) {

    private var trackerStartDate: LocalDate? = null
    private var today: LocalDate? = null

    private val recycleQueue = ArrayDeque<TrackerItemViewModel>()
    private val trackerItems = ArrayList<WeakReference<TrackerItemViewModel>>()

    @SuppressLint("UseSparseArrays") // SparseArrays don't work in unit tests
    private val historyCache = HashMap<Int, CacheItem>()

    private val dataProvider: AnalyzedDataProvider = dependencyProvider.resolve()
    private var listener: TrackerListener? = null

    private var cacheTask: CacheTask? = null

    /**
     * A value indicating whether the tracker has been started.
     */
    @get:Bindable
    var trackerStarted: Boolean = false
        set(value) {
            if (this.trackerStarted != value) {
                field = value
                notifyPropertyChanged(BR.trackerStarted)
            }
        }

    init {

        updateTracker()
        updateFooter()
    }

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
    }

    /**
     * Method called by the BaseFragment when the fragment's onStop() lifecycle method is called.
     */
    override fun onStop() {
        super.onStop()
        dependencyProvider.resolve<Messenger>().unsubscribeToAll(this)
    }

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
     * Updates the tracker data.
     */
    private fun updateTracker() {
        DataTask<Unit, LocalDate>("TrackerViewModel_updateTracker")
                .inBackground {
                    dataProvider.trackingStartDate
                }
                .onResult { result ->
                    val newToday = dependencyProvider.resolve<TimeService>().today()

                    // first check if tracking has been started yet.
                    if (result == null) {
                        // tracking not started yet, don't do anything
                        return@onResult
                    }

                    // Update the tracker started flag.
                    trackerStarted = true

                    // check for a change in the start and end dates of the tracker data.
                    if (today != newToday || trackerStartDate != result) {
                        today = newToday
                        trackerStartDate = result

                        // notify the fragment about the change in the tracker data.
                        if (listener != null) {
                            listener!!.onUpdated()
                        }
                    }
                }
                .execute()
    }

    /**
     * Sets the listener for changes to the tracker data.
     */
    fun setListener(trackerListener: TrackerListener?) {
        listener = trackerListener
    }

    /**
     * This method gets the number of items in the Tracker list.
     */
    val itemCount: Int
        get() {
            if (trackerStartDate != null) {
                val days = trackerStartDate!!.until(today!!, ChronoUnit.DAYS).toInt()
                return days + 1
            }

            return 0
        }

    /**
     * This method gets the date of a position in the Tracker list.

     * @param position The position in the list
     * *
     * @return The date for the position
     */
    fun getLocalDateForPosition(position: Int): LocalDate {
        return today!!.minusDays(position.toLong())
    }

    /**
     * This method gets an item viewmodel for the position in the Tracker list.

     * @param position The item position
     * *
     * @return A viewmodel for the item position
     */
    fun getTrackerItem(position: Int): TrackerItemViewModel {
        var trackerItem: TrackerItemViewModel? = recycleQueue.poll()
        if (trackerItem == null) {
            trackerItem = TrackerItemViewModel(dependencyProvider)
        }

        val date = getLocalDateForPosition(position)
        trackerItem.date = date

        addTrackerItem(trackerItem)

        val historyDay = getHistoryDayFromCache(date)
        if (historyDay != null) {
            trackerItem.setItem(historyDay)
        }

        updateCache()

        return trackerItem
    }

    /**
     * This method retrieves a HistoryDay from the cache.
     *
     * @param date The date of the HistoryDay to retrieve.
     * @return A HistoryDay object for the date or null if one is not cached.
     */
    private fun getHistoryDayFromCache(date: LocalDate): HistoryDay? {
        var historyDay: HistoryDay? = null

        val pageId = getPageId(date)
        val cacheItem = historyCache[pageId]

        if (cacheItem != null) {
            historyDay = cacheItem.getHistoryDay(date)
        }

        return historyDay
    }

    /**
     * This method recycles a tracker item.

     * @param trackerItem The tracker item to recycle.
     */
    fun recycleTrackerItem(trackerItem: TrackerItemViewModel) {
        removeTrackerItem(trackerItem)

        recycleQueue.add(trackerItem)
    }

    /**
     * Gets the id of the cache pageId for the specified date.
     */
    private fun getPageId(date: LocalDate): Int {
        return (date.toEpochDay() / PAGE_SIZE).toInt()
    }

    private fun getDateForPageId(pageId: Int): LocalDate {
        return LocalDate.ofEpochDay((pageId * PAGE_SIZE).toLong())
    }

    /**
     * This method cleans and updates the collection pages in the cache and
     * then starts a task to update the pageId with the most pending tracker items.
     */
    private fun updateCache() {
        logger.log(VERBOSE, "updateCache()")

        var minPageId = Integer.MAX_VALUE
        var maxPageId = Integer.MIN_VALUE

        // check if cache update is already in progress
        if (cacheTask == null) {
            cleanTrackerItemList()

            // scan pending tracker items for the pages referenced and track the
            // number of pending tracker items for each pageId.
            @Suppress("UseSparseArrays") // SparseArray does not work in unit tests.
            val pagesReferenced = HashMap<Int, Int>()
            for (index in trackerItems.indices) {
                val currentItem = trackerItems[index].get()
                if (currentItem != null) {
                    val pageId = getPageId(currentItem.date!!)

                    minPageId = Math.min(minPageId, pageId)
                    maxPageId = Math.max(maxPageId, pageId)

                    // get the current pending item count for the cache pageId
                    val value = pagesReferenced[pageId]
                    var count = value ?: 0

                    // increment the page item count
                    count++

                    // store the page item count
                    // (will create the reference if it didn't already exist in the SparseArray)
                    pagesReferenced.put(pageId, count)
                }
            }

            val timeService = dependencyProvider.resolve<TimeService>()
            val todayPageId = getPageId(timeService.today())

            // cache 2 pages before and 2 pages after the referenced pages.
            minPageId = Math.max(0, minPageId - OFFSCREEN_PAGE_COUNT)
            maxPageId = Math.min(todayPageId, maxPageId + OFFSCREEN_PAGE_COUNT)

            // remove pages not in the current range
            val keys = ArrayList(historyCache.keys)
            for (key in keys) {
                if (key < minPageId || key > maxPageId) {
                    historyCache.remove(key)
                }
            }

            // add cache pages for current range
            for (index in minPageId..maxPageId) {
                if (historyCache[index] == null) {
                    val pageDate = getDateForPageId(index)
                    historyCache.put(index, CacheItem(index, pageDate))
                }
            }

            // find the pageId with the most pending tracker items
            var maxPendingCount = 0
            var pageToLoad: CacheItem? = null
            for (cacheItem in historyCache.values) {
                if (cacheItem.updatePending) {
                    // save the first unloaded page if we don't have one yet.
                    if (pageToLoad == null) {
                        pageToLoad = cacheItem
                    }

                    val pendingCount = pagesReferenced[cacheItem.pageId]
                    if (pendingCount != null && pendingCount > maxPendingCount) {
                        maxPendingCount = pendingCount
                        pageToLoad = cacheItem
                    }
                }
            }

            // Start an async task to update the pageId with the most pending tracker items.
            if (pageToLoad != null) {
                cacheTask = CacheTask(pageToLoad)
                cacheTask?.execute()
            }
        }
    }

    /**
     * This method updates the TrackerItemViewModels with data from a newly laoded CacheItem.

     * @param cacheItem The CacheItem to update the TrackerItemViewModels with.
     */
    private fun fillTrackerItems(cacheItem: CacheItem) {
        logger.log(VERBOSE, "fillTrackerItems: " + cacheItem.startDate)

        val cachePageId = cacheItem.pageId

        cleanTrackerItemList()

        // search for pending items for this cache pageId and also prune
        for (index in trackerItems.indices) {
            val currentItem = trackerItems[index].get()
            if (currentItem != null) {
                val date = currentItem.date!!
                val pageId = getPageId(date)
                if (pageId == cachePageId) {
                    logger.log(VERBOSE, "setting item " + date)
                    currentItem.setItem(cacheItem.getHistoryDay(date)!!)
                }
            }
        }

        cacheItem.updatePending = false
    }

    /**
     * This method cleans the garbage collected items from the trackerItems list.
     */
    private fun cleanTrackerItemList() {
        for (index in trackerItems.indices.reversed()) {
            val currentItem = trackerItems[index].get()
            if (currentItem == null) {
                // Item pointed to by weak reference has been garbage collected
                trackerItems.removeAt(index)
            }
        }
    }

    /**
     * This method adds a tracker item to the item list

     * @param item The item to add.
     */
    private fun addTrackerItem(item: TrackerItemViewModel) {
        cleanTrackerItemList()

        // first check if the item is already in the list.
        val found = trackerItems.any {
            weakReference ->
            weakReference.get() === item
        }

        if (!found) {
            // item not found in list, so add it.
            trackerItems.add(WeakReference(item))
        }
    }

    /**
     * This method removes a tracker item from the item list

     * @param item The item to remove
     */
    private fun removeTrackerItem(item: TrackerItemViewModel) {
        cleanTrackerItemList()

        val index = trackerItems.indexOfFirst {
            weakReference ->
            weakReference.get() === item
        }

        if (index != -1) {
            trackerItems.removeAt(index)
        }
    }

    /**
     * Method called by the BaseFragment when a toolbar menu item is clicked.

     * @param item The menu item that was clicked.
     */
    override fun onMenuItem(item: MenuItem): Boolean {
        if (item.itemId == R.id.show_report) {
            DataTask<Unit, Boolean>("DashboardViewModel_UserReportDataCheck")
                    .inBackground {
                        dependencyProvider.resolve<AnalyzedDataProvider>().trackingStartDate != null
                    }
                    .onResult { dataExists ->
                        if (dataExists!!) {
                            dependencyProvider.resolve<TrackerEvents>().onReport()
                        } else {
                            dependencyProvider.resolve<TrackerEvents>().onReportEmpty()
                        }
                    }
                    .execute()

            return true
        }

        return false
    }

    /**
     * Invalides the cache items that are in the cache so they will be reloaded.
     */
    private fun invalidateCache() {
        logger.log(VERBOSE, "invalidateCache()")

        for (cacheItem in historyCache.values) {
            cacheItem.updatePending = true
        }
    }

    /**
     * Method called when a tracker item at [position] is clicked.
     */
    fun onTrackerItemClicked(position: Int) {
        val date = getLocalDateForPosition(position)
        dependencyProvider.resolve<TrackerEvents>().onDailyReport(date)
    }

    /**
     * Click handler for the Add Inhaler button.
     */
    fun onAddInhaler() {
        dependencyProvider.resolve<TrackerEvents>().onAddInhaler()
    }

    /**
     * Message handler for the HistoryUpdatedMessage.
     * @param message The message received.
     */
    @Subscribe
    fun onHistoryUpdated(message: HistoryUpdatedMessage) {
        logger.log(VERBOSE, "onHistoryUpdated")
        invalidateCache()
        updateTracker()
        updateCache()
    }

    /**
     * This interface is implemented by the TrackerFragment to receive change notifications.
     */
    interface TrackerListener {
        /**
         * This method is called when something in the tracker changes.
         */
        fun onUpdated()
    }

    /**
     * This class is used to cache history results.
     *
     * @property pageId    The pageId index of the cache item.
     * @property startDate The start date of the history contained by this cache.
     */
    private class CacheItem(val pageId: Int,
                            val startDate: LocalDate) {

        var updatePending: Boolean = true

        var history: List<HistoryDay>? = null

        /**
         * This method checks to see if the CacheItem contains a history day for
         * the specified [date].
         */
        fun contains(date: LocalDate): Boolean {
            if (history == null) {
                return false
            }

            // history is sorted newest to oldest
            val index = date.until(history!![0].day, ChronoUnit.DAYS).toInt()
            return index >= 0 && index < history!!.size
        }

        /**
         * Gets a HistoryDay from the cached data for the specified [date].
         */
        fun getHistoryDay(date: LocalDate): HistoryDay? {
            var historyDay: HistoryDay? = null

            if (contains(date)) {
                // history is sorted newest to oldest
                val index = date.until(history!![0].day, ChronoUnit.DAYS).toInt()
                historyDay = history?.get(index)
            }

            return historyDay
        }
    }

    /**
     * This class updates the history cache in a worker thread.
     */
    private inner class CacheTask(private val cacheItem: CacheItem)
        : DataTask<Unit, List<HistoryDay>>("TrackerViewModel_CacheTask") {

        internal var startDate: LocalDate
        internal var endDate: LocalDate

        init {

            logger.log(VERBOSE, "CacheTask() " + cacheItem.startDate)
            startDate = cacheItem.startDate
            endDate = startDate.plusDays((PAGE_SIZE - 1).toLong())
        }

        /**
         * This method runs on a worker thread and retrieves a cache page from the analysis package.
         */
        override fun doInBackground(vararg params: Unit): List<HistoryDay>? {
            return dataProvider.getHistory(startDate, endDate)
        }

        /**
         * This method runs on the main thread after the worker thread completes.
         * It updates the cache with the data from analysis package.
         */
        override fun onPostExecute(result: List<HistoryDay>?) {
            cacheItem.history = result
            fillTrackerItems(cacheItem)
        }

        /**
         * This method is called after the task completes and onPostExecute() or onCanceled()
         * has been called.
         */
        override fun onComplete() {
            cacheTask = null
            updateCache()
        }
    }

    /**
     * Events produced by the ViewModel to request actions by the Activity.
     */
    interface TrackerEvents {
        /**
         * Requests to show the report.
         */
        fun onReport()

        /**
         * Requests that the screen indicating that the report is empty is displayed.
         */
        fun onReportEmpty()

        /**
         * Requests to display the daily report
         */
        fun onDailyReport(date: LocalDate)

        /**
         * Request to add an inhaler.
         */
        fun onAddInhaler()
    }

    companion object {
        private val PAGE_SIZE = 7
        val OFFSCREEN_PAGE_COUNT = 2
    }
}
