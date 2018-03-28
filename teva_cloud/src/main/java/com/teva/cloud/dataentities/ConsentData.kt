//
// ConsentData.kt
// teva_cloud
//
// Copyright © 2017 Teva. All rights reserved.
//

package com.teva.cloud.dataentities

import com.teva.common.entities.TrackedModelObject
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate

/**
 * This class represents consent data.
 */
class ConsentData(var hasConsented: Boolean = false,
                  var status: String? = null,
                  var termsAndConditions: String? = null,
                  var privacyNotice: String? = null,
                  var consentStartDate: LocalDate? = null,
                  var consentEndDate: LocalDate? = null,
                  var addressCountry: String? = null,
                  var patientDOB: LocalDate? = null,
                  var created: Instant? = null) : TrackedModelObject()