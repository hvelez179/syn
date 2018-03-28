//
// DHPReturnObject.kt
// teva_dhp
//
// Copyright © 2017 Teva. All rights reserved.
//
package com.teva.dhp.DataEntities.DHPDataTypes.ResponseBodies

/**
Types that conform to this protocol represent objects returned within the returnObjects JSON array of a DHP response body.
 */
interface DHPReturnObject{

    /**
    This function checks whether the JSON object is a valid object of the implementing class's type.
    - Returns: true if the object is of the expected type
     */
    fun isValidObject(): Boolean
}