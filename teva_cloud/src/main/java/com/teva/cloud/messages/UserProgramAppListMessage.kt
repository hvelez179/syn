//
// UserProgramAppListMessage.kt
// teva_cloud
//
// Copyright © 2017 Teva. All rights reserved.
//

package com.teva.cloud.messages

import com.teva.cloud.dataentities.ProgramData
import com.teva.cloud.services.programmanagement.CareProgramErrorCode
import com.teva.cloud.services.programmanagement.CareProgramErrorDetail


/**
 */
class UserProgramAppListMessage(var errorCode: CareProgramErrorCode = CareProgramErrorCode.NO_ERROR,
                                var errorDetails: List<CareProgramErrorDetail>? = null,
                                var userProgramAppList: List<ProgramData>? = null)