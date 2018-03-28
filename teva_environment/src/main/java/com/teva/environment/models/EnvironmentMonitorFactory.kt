///
// EnvironmentMonitorFactory.kt
// teva_environment
//
// Copyright © 2017 Teva. All rights reserved
///

package com.teva.environment.models

import com.teva.utilities.services.DependencyProvider
import com.teva.environment.services.EnvironmentServiceImpl
import com.teva.environment.services.providers.EnvironmentProviderFactoryImpl

/**
 * This class provides the concrete implementation of the EnvironmentMonitor.
 */
object EnvironmentMonitorFactory {

    private val environmentMonitorImpl: EnvironmentMonitorImpl
            by lazy { createEnvironmentMonitorImpl() }

    val environmentMonitor: EnvironmentMonitor = environmentMonitorImpl

    val environmentalReminderManager: DailyEnvironmentalReminderManager = environmentMonitorImpl

    /**
     * This method creates an instance of ths environment monitor.
     *
     * @return - the environment monitor.
     */
    private fun createEnvironmentMonitorImpl(): EnvironmentMonitorImpl {
        val dependencyProvider = DependencyProvider.default
        val environmentProviderFactory = EnvironmentProviderFactoryImpl(dependencyProvider)
        val environmentService = EnvironmentServiceImpl(dependencyProvider, environmentProviderFactory)
        return EnvironmentMonitorImpl(dependencyProvider, environmentService)
    }

}
