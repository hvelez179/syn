//
// ConsentDataQuery.kt
// teva_cloud
//
// Copyright © 2017 Teva. All rights reserved.
//
package com.teva.cloud.dataquery

import com.teva.cloud.dataentities.ConsentData
import com.teva.common.dataquery.DataQueryForTrackedModels

/**
 * Classes that implement this interface support persistence of consent data.
 */
interface ConsentDataQuery: DataQueryForTrackedModels<ConsentData> {
    /**
     * Returns consent record.
     */
    fun getConsentData(): ConsentData?
}