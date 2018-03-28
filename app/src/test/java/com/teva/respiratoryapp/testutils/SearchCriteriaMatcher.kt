package com.teva.respiratoryapp.testutils

import com.teva.respiratoryapp.services.data.SearchCriteria

import org.mockito.ArgumentMatcher

fun SearchCriteria.matches(otherCriteria: SearchCriteria?): Boolean {
    if (otherCriteria == null) {
        return false
    }

    if (criteria != otherCriteria.criteria) {
        return false
    }

    val expectedValues = values
    val otherValues = otherCriteria.values

    if (expectedValues.size != otherValues.size) {
        return false
    }

    for (i in expectedValues.indices) {
        if (expectedValues[i] != otherValues[i]) {
            return false
        }
    }

    return true
}

/**
 * Mockito ArgumentMatcher that compares two SearchCriteria objects.
 */
class SearchCriteriaMatcher(private val expectedCriteria: SearchCriteria) : ArgumentMatcher<SearchCriteria> {

    override fun matches(otherCriteria: SearchCriteria?): Boolean {
        if (otherCriteria == null) {
            return false
        }

        if (expectedCriteria.criteria != otherCriteria.criteria) {
            return false
        }

        val expectedValues = expectedCriteria.values
        val otherValues = otherCriteria.values

        if (expectedValues.size != otherValues.size) {
            return false
        }

        for (i in expectedValues.indices) {
            if (expectedValues[i] != otherValues[i]) {
                return false
            }
        }

        return true
    }
}
