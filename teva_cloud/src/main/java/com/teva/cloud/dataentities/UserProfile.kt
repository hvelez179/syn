//
// UserProfile.kt
// teva_cloud
//
// Copyright © 2017 Teva. All rights reserved.
//

package com.teva.cloud.dataentities

import com.teva.cloud.extensions.*
import com.teva.cloud.models.CloudSessionState
import com.teva.common.entities.TrackedModelObject
import com.teva.dhp.DataEntities.DHPDataTypes.DHPCodes
import com.teva.dhp.DataEntities.DHPDataTypes.FHIRObjects.DHPProfileInfo
import com.teva.dhp.models.DHPSession
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import java.io.Serializable


/**
 * This class represents user profile information.
 */
class UserProfile(var profileId: String? = null, 
                  var firstName: String? = null, 
                  var lastName: String? = null,
                  var isAccountOwner: Boolean? = null,
                  var isActive: Boolean? = null,
                  var isEmancipated: Boolean? = null,
                  var dateOfBirth: LocalDate? = null,
                  var created: Instant? = null) : TrackedModelObject(), Serializable {

    internal fun toDHPType(role: DHPCodes.Role? = null, messageId: String = "", forUpload: Boolean = false): DHPProfileInfo {

        val profileInfo = DHPProfileInfo()

        profileInfo.role = role ?: DHPCodes.Role.patient
        profileInfo.username = DHPSession.shared.username
        profileInfo.emailID = DHPSession.shared.identityHubProfile?.get("email") as? String ?: "Unknown"
        profileInfo.firstName = firstName
        profileInfo.lastName = lastName
        profileInfo.dateofBirth = CloudSessionState.shared.dateFormatter.format(dateOfBirth?.atStartOfDay())
        profileInfo.gender = "Unknown"
        profileInfo.addressZip = "Unknown"
        profileInfo.addressState = "Unknown"
        profileInfo.addressCountry = "Unknown"

        // If creating a new profile, this will be nil, excluded from payload.
        profileInfo.externalEntityID = profileId

        // Account owner must be an adult; dependent must not be an adult.
        profileInfo.isAdult = (isAccountOwner ?: false).toString()

        if (role == DHPCodes.Role.guardian) {
            profileInfo.relationshipStatus = "active"
        }

        if (forUpload) {
            profileInfo.objectName = profileInfo.dhpObjectName
            profileInfo.serverTimeOffset = serverTimeOffset?.toServerTimeOffsetString()
        }

        profileInfo.addCommonAttributes(messageId)

        return profileInfo
    }


    
    companion object {
        val jsonObjectName = "user_profile_info"

        internal fun fromDHPType(obj: DHPProfileInfo): UserProfile? {

            val profile = UserProfile()

            profile.profileId = obj.externalEntityID
            profile.firstName = obj.firstName
            profile.lastName = obj.lastName
            val dob = localDateFromGMTString(obj.dateofBirth.fromStringOrUnknown())
            profile.dateOfBirth = dob
            profile.isAccountOwner = obj.isAdult?.toStringOrUnknown() == "true"
            profile.isActive = true
            profile.created = instantFromGMTString(obj.sourceTime_GMT.fromStringOrUnknown())

            return profile
        }
    }
}