//
// EncryptedVASQuery.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.models.dataquery.encrypted

import android.support.annotation.MainThread
import com.teva.utilities.services.DependencyProvider
import com.teva.respiratoryapp.models.dataquery.generic.GenericVASQuery
import com.teva.userfeedback.entities.DailyUserFeeling

/**
 * The encrypted data query implementation for VAS data.
 */
class EncryptedVASQuery @MainThread
constructor(dependencyProvider: DependencyProvider)
    : GenericVASQuery(
        dependencyProvider,
        dependencyProvider.resolve<EncryptedVASMapper>()) {

    /**
     * This method creates a new DailyUserFeeling model object.

     * @return - a new DailyUserFeeling object.
     */
    override fun createModel(): DailyUserFeeling {
        return DailyUserFeeling()
    }
}
