//
// EncryptedUserAccountQuery.kt
// app
//
// Copyright © 2017 Teva. All rights reserved.
//

package com.teva.respiratoryapp.models.dataquery.encrypted

import com.teva.cloud.dataentities.UserAccount
import com.teva.cloud.dataquery.UserAccountQuery
import com.teva.utilities.services.DependencyProvider
import com.teva.respiratoryapp.models.dataquery.generic.GenericQueryBaseForTrackedModels
import com.teva.respiratoryapp.services.data.SearchCriteria
import com.teva.respiratoryapp.services.data.encrypteddata.entities.UserAccountEncrypted


/**
 * This class defines the data query implementation for user account.
 */
class EncryptedUserAccountQuery (dependencyProvider: DependencyProvider) : GenericQueryBaseForTrackedModels<UserAccount, UserAccountEncrypted>(dependencyProvider, UserAccountEncrypted::class.java, dependencyProvider.resolve<EncryptedUserAccountMapper>()), UserAccountQuery {
    override fun getUserAccount(): UserAccount? {
        return first
    }

    override fun uniqueSearchCriteria(model: UserAccount): SearchCriteria {
        return SearchCriteria("created = %@", model.created)
    }

    override fun resetCache() {
    }

    override fun createModel(): UserAccount {
        return UserAccount()
    }
}