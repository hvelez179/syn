//
// InvitationAcceptedMessage.kt
// teva_cloud
//
// Copyright © 2017 Teva. All rights reserved.
//

package com.teva.cloud.messages

import com.teva.cloud.models.programmanagement.InvitationDetails
import com.teva.cloud.services.programmanagement.CareProgramErrorCode
import com.teva.cloud.services.programmanagement.CareProgramErrorDetail


/**
 * This message is sent due to a request to accept an invitation.
 * It contains an error code indicating success or reason for failure.
 */
class InvitationAcceptedMessage(var errorCode: CareProgramErrorCode = CareProgramErrorCode.NO_ERROR,
                                var errorDetails: List<CareProgramErrorDetail>? = null,
                                var invitationDetails: InvitationDetails? = null)