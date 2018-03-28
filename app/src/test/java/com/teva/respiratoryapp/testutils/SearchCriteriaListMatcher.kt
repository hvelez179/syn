package com.teva.respiratoryapp.testutils

import com.teva.respiratoryapp.services.data.SearchCriteria

import org.mockito.ArgumentMatcher

fun List<SearchCriteria>.matches(argument: List<SearchCriteria>?): Boolean {
    if (argument == null) {
        return false
    }

    val other = argument

    if (size != other.size) {
        return false
    }

    for (i in indices) {
        val expectedCriteria = get(i)
        if (!expectedCriteria.matches(other[i])) {
            return false
        }
    }

    return true
}

