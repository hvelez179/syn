//
// StatementWithPredicate.java
// app
//
// Copyright (c) 2017 Teva. All rights reserved.
//

package com.teva.respiratoryapp.services.data.encrypteddata

import com.teva.respiratoryapp.services.data.SearchCriteria

import org.threeten.bp.LocalDate

/**
 * This class supports database operations with search criteria.
 * This class is used for database update, delete and select queries.
 */
open class StatementWithPredicate {

    /**
     * The getter for the SearchCriteria.
     */
    var searchCriteria: SearchCriteria? = null

    /**
     * The parameterized where clause string.
     */
    val whereClauseString: String?
        get() {

            if (searchCriteria == null) {
                return null
            }

            if (!searchCriteria!!.criteria.contains("%@")){
                return searchCriteria!!.criteria
            }

            val parts = searchCriteria!!.criteria.split("%@".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

            val searchString = StringBuilder()

            for (part in parts) {
                searchString.append(part)
                searchString.append("?")
            }

            return searchString.toString()
        }

    /**
     * The array or arguments to be substituted in the parameterized where clause string.
     */
    val whereClauseArguments: Array<String>?
        get() {

            if (searchCriteria == null) {
                return null
            }

            if (!searchCriteria!!.criteria.contains("%@")){
                return null
            }

            val searchParameters = searchCriteria!!.criteria.split("%@".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val args = searchCriteria!!.values

            for (i in 0 until minOf(searchParameters.size, args.size)) {
                if (searchParameters[i].contains("like")) {
                    searchParameters[i] = "'%" + args[i].toString() + "%'"
                } else {
                    if (args[i] is Boolean) {
                        searchParameters[i] = if (args[i] as Boolean) "1" else "0"
                    } else if (args[i] is LocalDate) {
                        searchParameters[i] = java.lang.Long.toString((args[i] as LocalDate).toEpochDay())
                    } else {
                        searchParameters[i] = args[i].toString()
                    }
                }
            }

            return searchParameters
        }
}
