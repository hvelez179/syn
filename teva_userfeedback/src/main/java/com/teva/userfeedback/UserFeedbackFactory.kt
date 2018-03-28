//
// UserfeedbackFactory.kt
// teva_userfeedback
//
// Copyright (c) 2017 Teva. All rights reserved.
//

package com.teva.userfeedback

import com.teva.utilities.services.DependencyProvider
import com.teva.userfeedback.model.DailyAssessmentReminderManager
import com.teva.userfeedback.model.UserFeelingManager
import com.teva.userfeedback.model.DSAManagerImpl

/**
 * This class provides access to the UserFeedback interfaces.
 */
object UserFeedbackFactory {

    private val dsaManager: DSAManagerImpl by lazy { DSAManagerImpl(DependencyProvider.default) }

    /**
     * Returns an implementation of the Daily Assessment Manager.
     */
    val dailyAssessmentManager: UserFeelingManager = dsaManager

    /**
     * Returns an implementation of the Daily Assessment Reminder Manager.
     */
    val dailyAssessmentReminderManager: DailyAssessmentReminderManager = dsaManager

}
