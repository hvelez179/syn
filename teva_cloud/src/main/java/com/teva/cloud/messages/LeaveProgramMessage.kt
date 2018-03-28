//
// LeaveProgramMessage.kt
// teva_cloud
//
// Copyright © 2017 Teva. All rights reserved.
//

package com.teva.cloud.messages

import com.teva.cloud.services.programmanagement.CareProgramErrorCode
import com.teva.cloud.services.programmanagement.CareProgramErrorDetail


/**
 * This message is sent due to a request to leave a program.
 * It contains an error code indicating success or reason for failure.
 */
class LeaveProgramMessage (var errorCode: CareProgramErrorCode = CareProgramErrorCode.NO_ERROR,
                           var errorDetails: List<CareProgramErrorDetail>? = null,
                           var programId: String? = null)