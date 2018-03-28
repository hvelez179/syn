//
// GenericDHPResponse.kt
// teva_dhp
//
// Copyright © 2017 Teva. All rights reserved.
//
package com.teva.dhp.DataEntities.DHPDataTypes

import com.teva.dhp.DataEntities.DHPDataTypes.ResponseBodies.DHPResponseBody

class GenericDHPResponse<T> where T: DHPResponseBody {
    var success: Boolean
    var message: String
    var body: T?

    constructor(success: Boolean, message: String, body: T?) {
        this.success = success
        this.message = message
        this.body = body
    }
}