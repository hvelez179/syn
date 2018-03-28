//
// EncryptedMedicationQuery.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.models.dataquery.encrypted

import com.teva.utilities.services.DependencyProvider
import com.teva.medication.entities.Medication
import com.teva.respiratoryapp.models.dataquery.generic.GenericMedicationQuery

/**
 * The encrypted data query implementation for medications.
 */
class EncryptedMedicationQuery(dependencyProvider: DependencyProvider)
    : GenericMedicationQuery(
        dependencyProvider,
        dependencyProvider.resolve<EncryptedMedicationMapper>()) {

    init {
        resetCache()
    }

    override fun createModel(): Medication {
        return Medication()
    }
}
