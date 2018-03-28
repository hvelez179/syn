//
// EncryptedFetch.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.services.data.encrypteddata

import com.teva.respiratoryapp.services.data.QueryInfo

/**
 * This class supports the database fetch operation.
 */
class EncryptedFetch : StatementWithPredicate() {
    private var sortParameters: Array<out SortParameter>? = null
    private var count: Int? = null
    private var tableName: String? = null

    /**
     * This method is the setter for the QueryInfo.
     * @param queryInfo - the QueryInfo to be set.
     */
    fun setQueryInfo(queryInfo: QueryInfo?) {

        if (queryInfo == null) {
            return
        }
        searchCriteria = queryInfo.searchCriteria
        sortParameters = queryInfo.sortParameter
        count = queryInfo.count
    }

    /**
     * This method is the setter for the database table name.
     * @param tableName - the database table from which data needs to be fetched.
     */
    fun setTableName(tableName: String) {
        this.tableName = tableName
    }

    /**
     * This method creates the "Order By" string based on the QueryInfo.
     * @return - the "Order By" string specifying the sort order.
     */
    private val sortOrderString: String
        get() {
            val orderBySql = StringBuilder()

            if (sortParameters?.isNotEmpty() ?: false) {
                orderBySql.append(" ORDER BY ")

                var isFirst = true
                for (sortParameter in sortParameters!!) {
                    if (!isFirst) {
                        orderBySql.append(", ")
                    }

                    isFirst = false
                    orderBySql.append(sortParameter.propertyName)
                    orderBySql.append(if (sortParameter.isAscending) " ASC" else " DESC")
                }
            }

            return orderBySql.toString()
        }

    /**
     * This method creates the "limit" string based on the record count specified in the QueryInfo.
     * @return -  the "limit" string specifying the number of records to be returned.
     */
    private val limitString: String?
        get() {
            var limitString: String? = null

            if (count != null) {
                limitString = " LIMIT " + count!!.toString()
            }

            return limitString
        }

    /**
     * This method returns the complete query string including the parameterized where clause,
     * the sort order and the record limit.
     * @return -  the parameterized "select" query string.
     */
    val queryString: String?
        get() {
            if (tableName == null || tableName!!.isEmpty())
                return null

            val sql = StringBuilder("SELECT * FROM ")

            sql.append(tableName)

            if (searchCriteria != null) {
                sql.append(" WHERE ")
                sql.append(whereClauseString)
            }

            if (sortParameters != null) {
                sql.append(sortOrderString)
            }

            if (count != null) {
                sql.append(limitString)
            }

            return sql.toString()
        }
}
