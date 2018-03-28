package com.teva.respiratoryapp.testutils

import com.teva.respiratoryapp.services.data.QueryInfo
import com.teva.respiratoryapp.services.data.encrypteddata.SortParameter

import org.mockito.ArgumentMatcher

fun QueryInfo.matches(otherQueryInfo: QueryInfo?): Boolean {
    if (otherQueryInfo == null) {
        return false
    }

    if (searchCriteria != null) {
        if (!searchCriteria!!.matches(otherQueryInfo.searchCriteria)) {
            return false
        }
    }

    val expectedSortParameters = sortParameter
    val otherSortParameters = otherQueryInfo.sortParameter

    if (expectedSortParameters.size != otherSortParameters.size) {
        return false
    }

    for (i in expectedSortParameters.indices) {
        val sortParameterMatcher = SortParameterMatcher(expectedSortParameters[i])
        if (!sortParameterMatcher.matches(otherSortParameters[i])) {
            return false
        }
    }

    return true
}

