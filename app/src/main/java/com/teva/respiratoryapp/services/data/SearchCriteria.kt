//
// SearchCriteria.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.services.data

/**
 * This class describes the condition for selecting objects from the data store.
 *
 * @property criteria Specifies the selection condition in string format.
 * @param values The values of the properties in the criteria. The order should follow how they appear in the criteria property.
 */
class SearchCriteria(val criteria: String,
                     vararg values: Any?) {

    /**
     * The values of the properties in the criteria. The order should follow how they appear in the criteria property.
     */
    val values: Array<out Any?> = values
}
