//
// EncryptedDailyAirQualityQuery.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.models.dataquery.encrypted

import com.teva.utilities.services.DependencyProvider
import com.teva.environment.entities.DailyAirQuality
import com.teva.respiratoryapp.models.dataquery.generic.GenericDailyAirQualityQuery

/**
 * Encrypted data query implementation for daily air quality.
 */
class EncryptedDailyAirQualityQuery(dependencyProvider: DependencyProvider)
    : GenericDailyAirQualityQuery(
        dependencyProvider,
        dependencyProvider.resolve<EncryptedDailyAirQualityMapper>()) {

    /**
     * This method creates a model object for daily air quality.
     * @return
     */
    override fun createModel(): DailyAirQuality {
        return DailyAirQuality()
    }
}
