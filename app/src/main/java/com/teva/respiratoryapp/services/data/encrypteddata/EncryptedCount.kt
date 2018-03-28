//
// EncryptedCount.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.services.data.encrypteddata

/**
 * This class supports retrieving record count from the database.
 */

class EncryptedCount : StatementWithPredicate() {

    private var tableName: String? = null

    /**
     * This method is the setter for the database table name.
     *
     * @param tableName - the database table from which record count needs to be fetched.
     */
    fun setTableName(tableName: String) {
        this.tableName = tableName
    }

    /**
     * This method returns the complete query string for selecting the record count.
     *
     * @return -  the parameterized "select count(*)" query string.
     */
    val queryString: String?
        get() {
            if (tableName == null || tableName!!.isEmpty())
                return null

            val sql = StringBuilder("SELECT COUNT(*) FROM ")

            sql.append(tableName)

            if (searchCriteria != null) {
                sql.append(" WHERE ")
                sql.append(whereClauseString)
            }

            return sql.toString()
        }
}
