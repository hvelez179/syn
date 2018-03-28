//
// InvitationDetailsMessage.kt.kt
// teva_cloud
//
// Copyright © 2017 Teva. All rights reserved.
//

package com.teva.cloud.messages

import com.teva.cloud.models.programmanagement.InvitationDetails
import com.teva.cloud.services.programmanagement.CareProgramErrorCode
import com.teva.cloud.services.programmanagement.CareProgramErrorDetail


/**
 * The InvitationDetails message.
 */
class InvitationDetailsMessage(var invitationDetails: InvitationDetails,
                               var errorCode: CareProgramErrorCode = CareProgramErrorCode.NO_ERROR,
                               var errorDetails: List<CareProgramErrorDetail>? = null)