//
// EncryptedUserProfileDataMapper.kt
// app
//
// Copyright © 2017 Teva. All rights reserved.
//

package com.teva.respiratoryapp.models.dataquery.encrypted

import com.teva.cloud.dataentities.UserProfile
import com.teva.common.utilities.toInstant
import com.teva.common.utilities.toLocalDate
import com.teva.respiratoryapp.models.dataquery.generic.DataMapper
import com.teva.respiratoryapp.services.data.encrypteddata.entities.UserProfileDataEncrypted
import org.threeten.bp.Instant


/**
 * This is the mapper class for mapping between user profile data entities and user profile data models and vice-versa
 */
class EncryptedUserProfileDataMapper : DataMapper<UserProfile, UserProfileDataEncrypted> {
    override fun toManagedEntity(source: UserProfile, destination: UserProfileDataEncrypted) {
        destination.firstName = source.firstName
        destination.lastName = source.lastName
        destination.profileId = source.profileId
        destination.isAccountOwner = if(source.isAccountOwner == true) 1 else 0
        destination.dateOfBirth = source.dateOfBirth?.toInstant()
        destination.isActive = if(source.isActive == true) 1 else 0
        destination.isEmancipated = if (source.isEmancipated == true) 1 else 0

        destination.created = source.created ?: Instant.now()

        destination.hasChanged = if(source.hasChanged) 1 else 0
        destination.changedTime = source.changeTime
        destination.serverTimeOffset = source.serverTimeOffset
    }

    override fun toModelObject(source: UserProfileDataEncrypted, destination: UserProfile) {
        destination.firstName = source.firstName
        destination.lastName = source.lastName
        destination.profileId = source.profileId
        destination.isAccountOwner = source.isAccountOwner == 1
        destination.isActive = source.isActive == 1

        destination.dateOfBirth = source.dateOfBirth?.toLocalDate()

        destination.isEmancipated = source.isEmancipated == 1

        destination.created = source.created

        destination.hasChanged = source.hasChanged == 1
        destination.changeTime = source.changedTime
        destination.serverTimeOffset = source.serverTimeOffset
    }

    override fun preMap(toModel: Boolean) {
    }

    override fun postMap() {
    }
}