package com.teva.environment.entities

import com.teva.environment.enumerations.PollenLevel
import com.teva.environment.enumerations.PollenLevel.*

import org.threeten.bp.Instant

/**
 * This class provides pollen information from the pollen provider.
 *
 * @property providerName The provider's name.
 * @property treePollenLevel The tree pollen level.
 * @property grassPollenLevel The grass pollen level.
 * @property weedPollenLevel The weed pollen level.
 * @property expirationDate The date/time that this data is declared as stale.
 *                          If not available, its value is null.
 * @property isValid Indicates whether all of the property values are valid.
 */

class PollenInfo(
        var providerName: String,
        var treePollenLevel: PollenLevel = NO_DATA,
        var grassPollenLevel: PollenLevel = NO_DATA,
        var weedPollenLevel: PollenLevel = NO_DATA,
        var expirationDate: Instant? = null,
        var isValid: Boolean = false) {

    /**
     * Secondary constructor required for Java unit tests
     */
    constructor(providerName: String) : this(providerName, NO_DATA, NO_DATA, NO_DATA, null, false)
}
