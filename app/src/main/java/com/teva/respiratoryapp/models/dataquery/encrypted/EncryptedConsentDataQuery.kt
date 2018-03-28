//
// EncryptedConsentDataQuery.kt
// app
//
// Copyright © 2017 Teva. All rights reserved.
//

package com.teva.respiratoryapp.models.dataquery.encrypted

import com.teva.respiratoryapp.services.data.encrypteddata.entities.ConsentDataEncrypted
import com.teva.cloud.dataentities.*
import com.teva.cloud.dataquery.ConsentDataQuery
import com.teva.utilities.services.DependencyProvider
import com.teva.respiratoryapp.models.dataquery.generic.GenericQueryBaseForTrackedModels
import com.teva.respiratoryapp.services.data.QueryInfo
import com.teva.respiratoryapp.services.data.SearchCriteria
import com.teva.respiratoryapp.services.data.encrypteddata.SortParameter

/**
 * This class defines the data query implementation for consent data.
 */
class EncryptedConsentDataQuery(dependencyProvider: DependencyProvider) : GenericQueryBaseForTrackedModels<ConsentData, ConsentDataEncrypted>(dependencyProvider, ConsentDataEncrypted::class.java, dependencyProvider.resolve<EncryptedConsentDataMapper>()), ConsentDataQuery {
    override fun getConsentData(): ConsentData? {
        val query = QueryInfo(null, SortParameter("created", false))
        return readBasedOnQuery(query).getOrNull(0)
    }

    override fun uniqueSearchCriteria(model: ConsentData): SearchCriteria {
        return SearchCriteria( "created = %@",  model.created)
    }

    override fun resetCache() {
    }

    override fun createModel(): ConsentData {
        return ConsentData()
    }

}