//
// EnvironmentDataMonitor.kt
// teva_analysis
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.analysis.model.datamonitors

import com.teva.analysis.entities.SummaryInfo
import com.teva.analysis.enumerations.SummaryTextId
import com.teva.utilities.services.DependencyProvider
import com.teva.common.utilities.Messenger
import com.teva.environment.messages.EnvironmentUpdatedMessage
import com.teva.environment.models.EnvironmentMonitor
import org.greenrobot.eventbus.Subscribe

/**
 * This class monitors availability of Environment information.
 */

class EnvironmentDataMonitor(private val dependencyProvider: DependencyProvider) {

    init {
        dependencyProvider.resolve<Messenger>().subscribe(this)
    }

    /**
     * Message handler for the Environment Updated message
     */
    @Suppress("UNUSED_PARAMETER")
    @Subscribe
    fun onEnvironmentUpdated(environmentUpdatedMessage: EnvironmentUpdatedMessage) {
        val environmentInfo = dependencyProvider.resolve<EnvironmentMonitor>().currentEnvironmentInfo

        val summaryMessageQueue = dependencyProvider.resolve<SummaryMessageQueue>()

        if (environmentInfo == null || (environmentInfo.airQualityInfo == null && environmentInfo.pollenInfo == null && environmentInfo.weatherInfo == null)) {
            summaryMessageQueue.removeMessage(SummaryInfo(SummaryTextId.ENVIRONMENT_MESSAGE, null))
        } else {
            summaryMessageQueue.addMessage(SummaryInfo(SummaryTextId.ENVIRONMENT_MESSAGE, null))
        }
    }
}
