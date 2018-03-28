//
// RemindProgramConsentMessage.kt
// teva_cloud
//
// Copyright © 2017 Teva. All rights reserved.
//
package com.teva.cloud.messages

import com.teva.cloud.dataentities.ProgramData


/**
 * This message is raised when the DHP request, getUserProgramAppList, completes.
 */
class RemindProgramConsentMessage (var programData: ProgramData? = null, var consentedApps: List<String>? = listOf())