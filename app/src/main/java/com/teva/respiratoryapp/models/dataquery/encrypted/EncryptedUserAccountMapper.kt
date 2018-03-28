//
// EncryptedUserAccountMapper.kt
// app
//
// Copyright © 2017 Teva. All rights reserved.
//

package com.teva.respiratoryapp.models.dataquery.encrypted

import com.teva.cloud.dataentities.UserAccount
import com.teva.respiratoryapp.models.dataquery.generic.DataMapper
import com.teva.respiratoryapp.services.data.encrypteddata.entities.UserAccountEncrypted


/**
 */
class EncryptedUserAccountMapper : DataMapper<UserAccount, UserAccountEncrypted> {
    override fun toManagedEntity(source: UserAccount, destination: UserAccountEncrypted) {
        destination.studyHashKey = source.studyHashKey
        destination.pseudoName = source.pseudoName
        destination.federationId = source.federationId
        destination.username = source.username

        destination.identityHubIdToken = source.identityHubIdToken
        destination.identityHubAccessToken = source.identityHubAccessToken
        destination.identityHubRefreshToken = source.identityHubRefreshToken
        destination.identityHubProfileUrl = source.identityHubProfileUrl

        destination.DHPAccessToken = source.DHPAccessToken
        destination.DHPRefreshToken = source.DHPRefreshToken
        destination.lastInhalerSyncTime = source.lastInhalerSyncTime
        destination.lastNonInhalerSyncTime = source.lastNonInhalerSyncTime

        destination.created = source.created
    }

    override fun toModelObject(source: UserAccountEncrypted, destination: UserAccount) {
        destination.studyHashKey = source.studyHashKey
        destination.pseudoName = source.pseudoName
        destination.federationId = source.federationId
        destination.username = source.username

        destination.identityHubIdToken = source.identityHubIdToken
        destination.identityHubAccessToken = source.identityHubAccessToken
        destination.identityHubRefreshToken = source.identityHubRefreshToken
        destination.identityHubProfileUrl = source.identityHubProfileUrl

        destination.DHPAccessToken = source.DHPAccessToken
        destination.DHPRefreshToken = source.DHPRefreshToken
        destination.lastInhalerSyncTime = source.lastInhalerSyncTime
        destination.lastNonInhalerSyncTime = source.lastNonInhalerSyncTime

        destination.created = source.created
    }

    override fun preMap(toModel: Boolean) {
    }

    override fun postMap() {
    }

}