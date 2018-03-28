//
// HistoryDose.kt
// teva_analysis
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.analysis.entities

import com.teva.devices.entities.InhaleEvent

/**
 * This class represents the information corresponding to a dose taken by the user.
 *
 * @property drugUID the drug UID.
 * @property events  the inhalation events associated with the dose.
 * @property isController Indicates if the medication is classified as a controller.
 * @property isReliever Indicates if the medication is classified as a reliever.
 * @property index The dose index.
 * @property isTooSoon Indicates if the dose was taken too soon.
 * @property isComplete Indicates if the dose was complete.
 * @property hasIssues Indicates that the dose should be presented to the user as abnormal.
 *                     For instance: unscheduled doses, doses with warnings, incomplete doses,
 *                     cap open/close without inhalation, etc
 */
class HistoryDose(var drugUID: String?,
                  var events: List<InhaleEvent>,
                  var isController: Boolean = false,
                  var isReliever: Boolean = false,
                  var index: Int? = null,
                  var isTooSoon: Boolean = false,
                  var isComplete: Boolean = true,
                  var hasIssues: Boolean = false)
