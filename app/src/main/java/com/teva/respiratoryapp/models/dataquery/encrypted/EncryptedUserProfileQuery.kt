//
// EncryptedUserProfileQuery.kt
// app
//
// Copyright © 2017 Teva. All rights reserved.
//

package com.teva.respiratoryapp.models.dataquery.encrypted

import com.teva.cloud.dataentities.UserProfile
import com.teva.cloud.dataquery.UserProfileQuery
import com.teva.utilities.services.DependencyProvider
import com.teva.respiratoryapp.models.dataquery.generic.GenericQueryBaseForTrackedModels
import com.teva.respiratoryapp.services.data.QueryInfo
import com.teva.respiratoryapp.services.data.SearchCriteria
import com.teva.respiratoryapp.services.data.encrypteddata.entities.UserProfileDataEncrypted


/**
 * This class defines the data query implementation for user profile.
 */
class EncryptedUserProfileQuery (dependencyProvider: DependencyProvider) : GenericQueryBaseForTrackedModels<UserProfile, UserProfileDataEncrypted>(dependencyProvider, UserProfileDataEncrypted::class.java, dependencyProvider.resolve<EncryptedUserProfileDataMapper>()), UserProfileQuery {
    override fun getUserProfile(profileId: String): UserProfile? {
        val searchCriteria = SearchCriteria("profileId = %@", profileId)
        val query = QueryInfo(searchCriteria)
        val userProfiles: List<UserProfile> = readBasedOnQuery(query)

        return userProfiles.firstOrNull()
    }

    override fun getAccountOwner(): UserProfile? {
        val searchCriteria = SearchCriteria("isAccountOwner = %@", true)
        val query = QueryInfo(searchCriteria)
        val userProfiles = readBasedOnQuery(query)
        return userProfiles.firstOrNull()
    }

    override fun getActive(): UserProfile? {
        val searchCriteria = SearchCriteria("isActive = %@", true)
        val query = QueryInfo(searchCriteria)
        val userProfiles = readBasedOnQuery(query)
        return userProfiles.firstOrNull()
    }

    override fun uniqueSearchCriteria(model: UserProfile): SearchCriteria {
        return SearchCriteria("profileId = %@", model.profileId)
    }

    override fun resetCache() {
    }

    override fun createModel(): UserProfile {
        return UserProfile()
    }
}