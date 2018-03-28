//
// ProgramData.kt
// teva_cloud
//
// Copyright © 2017 Teva. All rights reserved.
//

package com.teva.cloud.dataentities

import com.teva.common.entities.TrackedModelObject

import java.io.Serializable

/**
 * This class holds data related to a program.
 */
class ProgramData (var programName: String? = null,
                   var programId: String? = null,
                   var profileId: String? = null,
                   var consentedApps: MutableList<CloudAppData>? = null,
                   var invitationCode: String? = null,
                   var active: Boolean = true) : Serializable, TrackedModelObject()