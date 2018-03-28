//
// UserProfileStatusCode.kt
// teva_cloud
//
// Copyright © 2017 Teva. All rights reserved.
//

package com.teva.cloud.enumerations

/**
 * This enum contains error codes associated with User Profile Management.
 * DID_SETUP_ACTIVE_PROFILE: Indicates that the async call to setup a profile succeeded.
 * ERROR_DURING_SETUP_ACTIVE_PROFILE: Indicates that an error occurred during the async call to setup a profile..
 * DID_GET_ALL_PROFILES: Indicates that the async call to get all profiles succeeded.
 * ERROR_DURING_GET_ALL_PROFILES: Indicates that an error occurred during the async call to get all profiles.
 */
enum class UserProfileStatusCode {
    DID_SETUP_ACTIVE_PROFILE,
    ERROR_DURING_SETUP_ACTIVE_PROFILE,
    DID_GET_ALL_PROFILES,
    ERROR_DURING_GET_ALL_PROFILES,
}