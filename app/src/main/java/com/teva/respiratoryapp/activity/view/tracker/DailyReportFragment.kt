package com.teva.respiratoryapp.activity.view.tracker


import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.view.View
import com.teva.analysis.extensions.enumerations.InhalationEffort
import com.teva.common.services.analytics.enumerations.AnalyticsScreen
import com.teva.respiratoryapp.R
import com.teva.respiratoryapp.activity.controls.DailyReportDecoration
import com.teva.respiratoryapp.activity.viewmodel.tracker.DailyReportItemViewModel
import com.teva.respiratoryapp.activity.viewmodel.tracker.DailyReportViewModel
import com.teva.respiratoryapp.databinding.DailyReportEventBinding
import com.teva.respiratoryapp.databinding.DailyReportFragmentBinding
import com.teva.respiratoryapp.mvvmframework.ui.ViewModelListFragment
import org.threeten.bp.LocalDate

/**
 * The view fragment class for the Daily Report popup.
 */
class DailyReportFragment
    : ViewModelListFragment<DailyReportFragmentBinding, DailyReportViewModel, DailyReportEventBinding, DailyReportItemViewModel>(R.layout.daily_report_fragment, R.layout.daily_report_event) {

    init {
        screen = AnalyticsScreen.DailyReport()
    }
    /**
     * Sets the ViewModel for the fragment.
     *
     * @param fragmentArguments The fragment arguments.
     */
    override fun inject(fragmentArguments: Bundle?) {
        // get the date of this report from the fragment arguments.
        val localDate = LocalDate.ofEpochDay(fragmentArguments!!.getLong(EPOCH_DAY_BUNDLE_KEY))

        // create a viewmodel for the view.
        viewModel = DailyReportViewModel(dependencyProvider!!, localDate)
    }

    /**
     * Called by the base class after the view is created.
     *
     * @param view The view that was created
     * @param savedInstanceState The saved instance state of the fragment.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        attachScrollBehavior(view.findViewById<View>(R.id.item_list))
    }

    /**
     * Initializes the item decorations for the RecyclerView.
     * @param recyclerView The RecyclerView to initialize.
     */
    override fun setListDecorations(recyclerView: RecyclerView) {
        super.setListDecorations(recyclerView)

        val decoration = DailyReportDecoration(
                getColor(R.color.blue30),
                getDimension(R.dimen.daily_report_stroke_width),
                getDimension(R.dimen.daily_report_stroke_xoffset),
                getDimension(R.dimen.daily_report_stroke_yoffset))
        recyclerView.addItemDecoration(decoration)
    }

    companion object {
        private const val EPOCH_DAY_BUNDLE_KEY = "DailyReportDate"

        /**
         * This method creates an arguments Bundle for the fragment.
         *
         * @param localDate The date argument for the fragment.
         * @return A Bundle containing the fragment arguments.
         */
        fun createArguments(localDate: LocalDate): Bundle {
            val bundle = Bundle()

            val epochDay = localDate.toEpochDay()
            bundle.putLong(EPOCH_DAY_BUNDLE_KEY, epochDay)

            return bundle
        }

        /**
         * InhalationEffort to image id onversion method used by the data bindings in the view layout.
         *
         * @param inhalationEffort The inhalation effort value to convert
         * @return The image id to represet the inhalation effort.
         */
        @JvmStatic
        fun inhalationEffortToImageId(inhalationEffort: InhalationEffort): Int {
            return when (inhalationEffort) {
                InhalationEffort.GOOD_INHALATION, InhalationEffort.LOW_INHALATION -> R.drawable.ic_inhalation_small_green

                InhalationEffort.NO_INHALATION,
                InhalationEffort.EXHALATION,
                InhalationEffort.ERROR,
                InhalationEffort.SYSTEM_ERROR -> R.drawable.ic_inhalation_small_red

                else -> android.R.color.transparent
            }
        }
    }

}
