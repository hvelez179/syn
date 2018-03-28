//
// SortParameter.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.services.data.encrypteddata

/**
 * This class contains sorting information for queries.
 *
 * @property propertyName The column to sort on
 * @property isAscending The sort order
 */
class SortParameter(var propertyName: String?,
                    var isAscending: Boolean)
