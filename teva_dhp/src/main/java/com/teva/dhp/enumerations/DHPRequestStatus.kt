//
//  DHPRequestStatus.kt
//  Teva_DHP
//
//  Copyright © 2017 Teva. All rights reserved.
//

package com.teva.dhp.enumerations

/**
 Enumeration representing the parsed response codes from the DHP.
 **/
enum class DHPRequestStatus {

    inProgress,
    success,
    failure,
    unauthorized,
    patientAlreadyRegistered,
    consentAlreadyOptedIn,
    payloadFailedValidation,
    weatherPartiallyProcessed
}
