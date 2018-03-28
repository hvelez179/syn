//
// GenericDHPRequest.kt
// teva_dhp
//
// Copyright © 2017 Teva. All rights reserved.
//
package com.teva.dhp.DataEntities.DHPDataTypes

import com.teva.dhp.DataEntities.DHPDataTypes.ResponseBodies.DHPResponseBody
import com.teva.dhp.models.DHPAPIs
import kotlin.reflect.KClass

class GenericDHPRequest<T:DHPResponseBody> {
    var uri: DHPAPIs.DHPAPI
    var payload: DHPDataType
    var callback: (Boolean, String, String?) -> Unit

    var prependObjectName = false

//    inline fun <reified T: DHPResponseBody> getResponseClass() = T::class

    constructor(endpoint: DHPAPIs.DHPAPI, payload: DHPDataType, clas: KClass<T>?, callback: ((GenericDHPResponse<T>) -> Unit)) {
        this.uri = endpoint
        this.payload = payload
        this.callback = { success, message, json  ->
            callback(GenericDHPResponse(success, message, DHPTypes.getResponseBody(json, clas)))
        }
    }

}

class GenericDHPRequestWithOldCallback {
    var uri: DHPAPIs.DHPAPI
    var payload: DHPDataType
    var callback: ((Boolean, String, Any?) -> Unit)
    var prependObjectName = false

    constructor(endpoint: DHPAPIs.DHPAPI, payload: DHPDataType, callback: ((Boolean, String, Any?) -> Unit)) {
        this.uri = endpoint
        this.payload = payload
        this.callback = callback
    }
}