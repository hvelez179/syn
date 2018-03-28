//
// SQLGenerationTests.java
// app
//
// Copyright (c) 2017 Teva. All rights reserved.
//

package com.teva.respiratoryapp.services.data

import com.teva.respiratoryapp.services.data.encrypteddata.EncryptedFetch
import com.teva.respiratoryapp.services.data.encrypteddata.SortParameter
import com.teva.respiratoryapp.testutils.BaseTest

import org.junit.Test

import org.junit.Assert.*

class SQLGenerationTests : BaseTest() {
    @Test
    @Throws(Exception::class)
    fun fetch_no_arguments() {
        val expectedSql = "SELECT * FROM DeviceData"

        val fetch = EncryptedFetch()
        fetch.setTableName("DeviceData")

        val sql = fetch.queryString

        assertEquals(sql, expectedSql)
    }

    @Test
    @Throws(Exception::class)
    fun fetch_with_search_criteria() {
        val expectedSql = "SELECT * FROM DeviceData WHERE serialNumber like ? AND doseCount < ?"
        val expectedWhereClauseArguments = arrayOf("'%1234%'", "5")

        val fetch = EncryptedFetch()
        fetch.setTableName("DeviceData")

        val criteria = SearchCriteria("serialNumber like %@ AND doseCount < %@", "1234", 5)
        fetch.setQueryInfo(QueryInfo(criteria))

        val sql = fetch.queryString
        val whereClauseArguments = fetch.whereClauseArguments

        assertEquals(expectedSql, sql)
        assertArrayEquals(expectedWhereClauseArguments, whereClauseArguments)
    }

    @Test
    @Throws(Exception::class)
    fun fetch_with_one_sort() {
        val expectedSql = "SELECT * FROM DeviceData ORDER BY lastConnection DESC"

        val fetch = EncryptedFetch()
        fetch.setTableName("DeviceData")
        fetch.setQueryInfo(QueryInfo(null, SortParameter("lastConnection", false)))

        val sql = fetch.queryString

        assertEquals(expectedSql, sql)
    }

    @Test
    @Throws(Exception::class)
    fun fetch_with_multiple_sort() {
        val expectedSql = "SELECT * FROM DeviceData ORDER BY doseCount ASC, lastConnection DESC"

        val fetch = EncryptedFetch()
        fetch.setTableName("DeviceData")
        fetch.setQueryInfo(QueryInfo(null,
                SortParameter("doseCount", true),
                SortParameter("lastConnection", false)))

        val sql = fetch.queryString

        assertEquals(expectedSql, sql)
    }

    @Test
    @Throws(Exception::class)
    fun fetch_with_search_criteria_and_sort() {
        val expectedSql = "SELECT * FROM DeviceData WHERE serialNumber like ? AND doseCount < ? ORDER BY doseCount ASC, lastConnection DESC"
        val expectedWhereClauseArguments = arrayOf("'%1234%'", "5")

        val fetch = EncryptedFetch()
        fetch.setTableName("DeviceData")
        val criteria = SearchCriteria("serialNumber like %@ AND doseCount < %@", "1234", 5)
        fetch.setQueryInfo(QueryInfo(criteria,
                SortParameter("doseCount", true),
                SortParameter("lastConnection", false)))

        val sql = fetch.queryString
        val whereClauseArguments = fetch.whereClauseArguments

        assertEquals(expectedSql, sql)
        assertArrayEquals(expectedWhereClauseArguments, whereClauseArguments)
    }

    @Test
    @Throws(Exception::class)
    fun fetch_with_limit() {
        val expectedSql = "SELECT * FROM DeviceData LIMIT 5"

        val fetch = EncryptedFetch()
        fetch.setTableName("DeviceData")
        val queryInfo = QueryInfo(null)
        queryInfo.count = 5
        fetch.setQueryInfo(queryInfo)

        val sql = fetch.queryString

        assertEquals(sql, expectedSql)
    }

    @Test
    @Throws(Exception::class)
    fun fetch_with_search_criteria_and_sort_and_limit() {
        val expectedSql = "SELECT * FROM DeviceData WHERE serialNumber like ? AND doseCount < ? ORDER BY doseCount ASC, lastConnection DESC LIMIT 5"
        val expectedWhereClauseArguments = arrayOf("'%1234%'", "5")

        val fetch = EncryptedFetch()
        fetch.setTableName("DeviceData")
        val criteria = SearchCriteria("serialNumber like %@ AND doseCount < %@", "1234", 5)
        val queryInfo = QueryInfo(criteria,
                SortParameter("doseCount", true),
                SortParameter("lastConnection", false))
        queryInfo.count = 5
        fetch.setQueryInfo(queryInfo)

        val sql = fetch.queryString
        val whereClauseArguments = fetch.whereClauseArguments

        assertEquals(expectedSql, sql)
        assertArrayEquals(expectedWhereClauseArguments, whereClauseArguments)
    }
    /*
    @Test
    public void insert_one_column() throws Exception {
        final String expectedSql = "INSERT INTO tableName (One) VALUES (5);";

        EncryptedInsert insert = new EncryptedInsert();
        insert.setTableName("tableName");

        Map<String, Object> columns = new HashMap<>();
        columns.put("One", 5);

        insert.setParameters(columns);

        String sql = insert.toString();

        assertEquals(expectedSql, sql);
    }

    @Test
    public void insert_multiple_column() throws Exception {
        final String expectedSql = "INSERT INTO tableName (One, Two, Three) VALUES (5, 'Hello There', 3.0);";

        EncryptedInsert insert = new EncryptedInsert();
        insert.setTableName("tableName");

        Map<String, Object> columns = new HashMap<>();
        columns.put("One", 5);
        columns.put("Two", "Hello There");
        columns.put("Three", 3.0);

        insert.setParameters(columns);

        String sql = insert.toString();

        assertEquals(expectedSql, sql);
    }

    @Test
    public void batch_insert() throws Exception {
        final String expectedSql = "INSERT INTO tableName (One, Two, Three) VALUES (5, 'Five', 5.0), (6, 'Six', 6.0), (7, 'Seven', 7.0);";

        BatchEncryptedInsert batchInsert = new BatchEncryptedInsert();

        EncryptedInsert insert = new EncryptedInsert();
        insert.setTableName("tableName");

        Map<String, Object> columns = new HashMap<>();
        columns.put("One", 5);
        columns.put("Two", "Five");
        columns.put("Three", 5.0);

        insert.setParameters(columns);
        batchInsert.add(insert);

        insert = new EncryptedInsert();
        insert.setTableName("tableName");

        columns = new HashMap<>();
        columns.put("One", 6);
        columns.put("Two", "Six");
        columns.put("Three", 6.0);

        insert.setParameters(columns);
        batchInsert.add(insert);

        insert = new EncryptedInsert();
        insert.setTableName("tableName");

        columns = new HashMap<>();
        columns.put("One", 7);
        columns.put("Two", "Seven");
        columns.put("Three", 7.0);

        insert.setParameters(columns);
        batchInsert.add(insert);

        String sql = batchInsert.toString();

        assertEquals(expectedSql, sql);
    } */
}
