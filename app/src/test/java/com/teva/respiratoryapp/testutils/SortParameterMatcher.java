package com.teva.respiratoryapp.testutils;

import com.teva.respiratoryapp.services.data.encrypteddata.SortParameter;

import org.mockito.ArgumentMatcher;

/**
 * Mockito ArgumentMatcher that compares SortParameter objects.
 */
public class SortParameterMatcher implements ArgumentMatcher<SortParameter> {

    private SortParameter expectedSortParameter;

    public SortParameterMatcher(SortParameter expectedSortParameter) {
        this.expectedSortParameter = expectedSortParameter;
    }
    @Override
    public boolean matches(SortParameter otherSortParameter) {
        if(otherSortParameter == null) {
            return false;
        }

        return ( expectedSortParameter.getPropertyName().equals(otherSortParameter.getPropertyName()) &&
                expectedSortParameter.isAscending() == otherSortParameter.isAscending());
    }
}
