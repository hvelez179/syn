//
// UserAccountDataEncrypted.kt
// app
//
// Copyright © 2017 Teva. All rights reserved.
//

package com.teva.respiratoryapp.services.data.encrypteddata.entities

import com.teva.respiratoryapp.services.data.encrypteddata.EncryptedEntity
import org.threeten.bp.Instant


/**
 * Represents user account data stored in the database.
 */
class UserAccountEncrypted : EncryptedEntity() {
    
    // Properties
     var studyHashKey: String?
        get() = getNullableStringProperty("studyHashKey")
        set(newValue) {
            schemaMap.put("studyHashKey", newValue)
        }

     var pseudoName: String?
        get() = getNullableStringProperty("pseudoName")
        set(newValue) {
            schemaMap.put("pseudoName",  newValue)
        }
    

     var federationId: String?
        get() = getNullableStringProperty("federationId")
        set(newValue) {
            schemaMap.put("federationId", newValue)
        }

     var username: String? 
        get() = getNullableStringProperty("username")
        set(newValue) {
            schemaMap.put("username", newValue)
        }
    
     var identityHubIdToken: String?
        get() = getNullableStringProperty("identityHubIdToken")
        set(newValue) {
            schemaMap.put("identityHubIdToken", newValue)
        }
    
     var identityHubAccessToken: String?
        get() = getNullableStringProperty("identityHubAccessToken")
        set(newValue) {
            schemaMap.put("identityHubAccessToken", newValue)
        }
    
     var identityHubRefreshToken: String?
        get() = getNullableStringProperty("identityHubRefreshToken")
        set(newValue) {
            schemaMap.put("identityHubRefreshToken", newValue)
        }
    
     var identityHubProfileUrl: String?
        get() = getNullableStringProperty("identityHubProfileUrl")
        set(newValue) {
            schemaMap.put("identityHubProfileUrl", newValue)
        }
    
     var DHPAccessToken: String?
        get() = getNullableStringProperty("DHPAccessToken")
        set(newValue) {
            schemaMap.put("DHPAccessToken", newValue)
        }
    
     var DHPRefreshToken: String?
        get() = getNullableStringProperty("DHPRefreshToken")
        set(newValue) {
            schemaMap.put("DHPRefreshToken", newValue)
        }
    
     var lastInhalerSyncTime: Instant? 
        get() = getInstantProperty("lastInhalerSyncTime")
        set(newValue) = setInstantProperty("lastInhalerSyncTime", newValue)
    
     var lastNonInhalerSyncTime: Instant? 
        get() = getInstantProperty("lastNonInhalerSyncTime")
        set(newValue) = setInstantProperty("lastNonInhalerSyncTime", newValue)
}