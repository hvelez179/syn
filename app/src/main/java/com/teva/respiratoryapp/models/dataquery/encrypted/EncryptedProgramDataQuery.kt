//
// EncryptedProgramDataQuery.kt
// app
//
// Copyright © 2017 Teva. All rights reserved.
//

package com.teva.respiratoryapp.models.dataquery.encrypted

import com.teva.cloud.dataentities.ProgramData
import com.teva.cloud.dataquery.ProgramDataQuery
import com.teva.utilities.services.DependencyProvider
import com.teva.respiratoryapp.models.dataquery.generic.GenericQueryBaseForTrackedModels
import com.teva.respiratoryapp.services.data.QueryInfo
import com.teva.respiratoryapp.services.data.SearchCriteria
import com.teva.respiratoryapp.services.data.encrypteddata.entities.ProgramDataEncrypted


/**
 * This class defines the data query implementation for program data.
 */
class EncryptedProgramDataQuery (dependencyProvider: DependencyProvider) : GenericQueryBaseForTrackedModels<ProgramData, ProgramDataEncrypted>(dependencyProvider, ProgramDataEncrypted::class.java, dependencyProvider.resolve<EncryptedProgramDataMapper>()), ProgramDataQuery {
    override fun getCarePrograms(profileId: String): List<ProgramData> {
        val query = QueryInfo(SearchCriteria("profileId = %@", profileId))
        return readBasedOnQuery(query)
    }

    override fun uniqueSearchCriteria(model: ProgramData): SearchCriteria {
        return SearchCriteria("profileId = %@ AND programId = %@", model.profileId, model.programId)
    }

    override fun resetCache() {
    }

    override fun createModel(): ProgramData {
        return ProgramData()
    }
}