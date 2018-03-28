//
// DailyReportItemViewModel.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.activity.viewmodel.tracker


import android.databinding.BaseObservable
import android.databinding.Bindable
import com.teva.analysis.extensions.enumerations.InhalationEffort
import com.teva.analysis.extensions.inhalationEffort
import com.teva.utilities.services.DependencyProvider
import com.teva.common.utilities.LocalizationService
import com.teva.devices.entities.Device
import com.teva.devices.entities.InhaleEvent
import com.teva.devices.enumerations.InhaleStatus
import com.teva.respiratoryapp.R
import com.teva.respiratoryapp.common.DateTimeLocalization
import org.threeten.bp.LocalTime
import java.util.*

/**
 * Viewmodel class for Daily Report items.
 */
class DailyReportItemViewModel(
        dependencyProvider: DependencyProvider,
        event: InhaleEvent,
        device: Device) : BaseObservable() {

    private var dependencyProvider: DependencyProvider? = dependencyProvider

    /**
     * Gets the time of the item
     */
    var time: LocalTime? = null
        private set
    /**
     * Gets the inhalation effort for the Daily Report item.
     */
    @get:Bindable
    var inhalationEffort: InhalationEffort? = null

    /**
     * Gets the status text.
     */
    var status: String? = null

    /**
     * Gets the inhaler nickname string for the Daily Report item.
     */
    @get:Bindable
    var inhalerName: String? = null
    /**
     * Gets the serial number for the Daily Report item.
     */
    @get:Bindable
    var serialNumber: String? = null

    /**
     * A value indicating whether the item is an event item or a User Feeling item.
     */
    @get:Bindable
    var isEvent: Boolean = false
        private set

    /**
     * The short time string for the Daily Report item.
     */
    val formattedTime: String
        @Bindable
        get() = dependencyProvider!!.resolve<DateTimeLocalization>().toShortTime(time!!)

    init {
        isEvent = true
        time = event.localEventTime.toLocalTime()
        inhalationEffort = event.inhalationEffort
        status = createStatusText(dependencyProvider, inhalationEffort, event)
        inhalerName = device.nickname
        serialNumber = device.serialNumber
    }

    /**
     * Creates the status text for the event.
     *
     * @param dependencyProvider The dependency injection mechanism
     * @param effort             The inhalation effort of the event.
     * @param event              The inhale event .
     * @return A localized string representing the status of the inhale event.
     */
    private fun createStatusText(dependencyProvider: DependencyProvider,
                                 effort: InhalationEffort?,
                                 event: InhaleEvent): String {
        val stringId: Int
        val stringReplacements = HashMap<String, Any>()

        val localizationService = dependencyProvider.resolve<LocalizationService>()

        when (effort) {
            InhalationEffort.GOOD_INHALATION, InhalationEffort.LOW_INHALATION -> stringId = R.string.trackerDoseStatusOk_text

            InhalationEffort.NO_INHALATION -> stringId = R.string.trackerDoseStatusNoInhalation_text

            InhalationEffort.EXHALATION -> stringId = R.string.trackerDoseStatusExhalation_text

            InhalationEffort.ERROR -> stringId = R.string.trackerDoseStatusVentBlocked_text
            InhalationEffort.SYSTEM_ERROR -> {
                stringReplacements.put("ErrorCodes", getSystemErrorCodes(event))
                stringId = R.string.trackerDoseStatusError_text
            }
            else -> stringId = 0
        }

        return localizationService.getString(stringId, stringReplacements)
    }

    /**
     * This method returns the system error codes from an inhale event in a string.
     *
     * @param event - the inhale event with a system error.
     * @return - system error codes from the inhale event.
     */
    private fun getSystemErrorCodes(event: InhaleEvent): String {
        val separator = dependencyProvider!!.resolve<LocalizationService>().getString(R.string.commaSpace_text)
        val issues = event.issues
        val systemErrorCodes = InhaleStatus.getSystemErrorCodes(issues)
        Collections.sort(systemErrorCodes)
        val errorCodes = StringBuilder()
        for (code in systemErrorCodes) {
            if (errorCodes.isNotEmpty()) {
                errorCodes.append(separator)
            }
            errorCodes.append(Integer.toString(code.code))
        }
        return errorCodes.toString()
    }

}
