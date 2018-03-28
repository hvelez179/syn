//
// ProgramDataQuery.kt
// teva_cloud
//
// Copyright © 2017 Teva. All rights reserved.
//

package com.teva.cloud.dataquery

import com.teva.cloud.dataentities.ProgramData
import com.teva.common.dataquery.DataQueryForTrackedModels


/**
 * Classes that implement this interface support persistence of program data.
 */
interface ProgramDataQuery: DataQueryForTrackedModels<ProgramData> {
    /**
     * Gets all the Care Programs associated with a profile id.
     * @param profileId: the profile id
     * @return: An array of ProgramData
     */
    fun getCarePrograms(profileId: String): List<ProgramData>
}