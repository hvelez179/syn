//
// QueryInfo.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.services.data

import com.teva.respiratoryapp.services.data.encrypteddata.SortParameter

/**
 * This class is a description of how the query is to select the objects from the data store.
 *
 * @property searchCriteria The condition for object selection in the query. If this is not specified, all objects may be returned.
 * @property count The number of items to return
 * @param sortParameter The columns to sort by.
 */
class QueryInfo(val searchCriteria: SearchCriteria? = null,
                var count: Int? = null,
                vararg sortParameter: SortParameter) {

    constructor(searchCriteria: SearchCriteria?, vararg sortParameter: SortParameter)
            : this(searchCriteria, null, *sortParameter)

    /**
     * The columns to sort by.
     */
    val sortParameter: Array<out SortParameter> = sortParameter
}
