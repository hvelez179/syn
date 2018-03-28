//
// EncryptedConsentDataMapper.kt
// app
//
// Copyright © 2017 Teva. All rights reserved.
//

package com.teva.respiratoryapp.models.dataquery.encrypted

import com.teva.cloud.dataentities.ConsentData
import com.teva.common.utilities.toInstant
import com.teva.common.utilities.toLocalDate
import com.teva.respiratoryapp.models.dataquery.generic.DataMapper
import com.teva.respiratoryapp.services.data.encrypteddata.entities.ConsentDataEncrypted
import org.threeten.bp.Instant


/**
 * This is the mapper class for mapping between consent data entities and consent data models and vice-versa
 */
class EncryptedConsentDataMapper
    : DataMapper<ConsentData, ConsentDataEncrypted> {
    override fun toManagedEntity(source: ConsentData, destination: ConsentDataEncrypted) {
        destination.hasConsented =  if(source.hasConsented) 1 else 0
        destination.status = source.status
        destination.termsAndConditions = source.termsAndConditions
        destination.privacyNotice = source.privacyNotice
        destination.consentStartDate = source.consentStartDate?.toInstant()
        destination.consentEndDate = source.consentEndDate?.toInstant()
        destination.addressCountry = source.addressCountry
        destination.patientDOB = source.patientDOB?.toInstant()

        destination.created = source.created ?: Instant.now()
    }

    override fun toModelObject(source: ConsentDataEncrypted, destination: ConsentData) {
        destination.hasConsented = source.hasConsented == 1
        destination.status = source.status
        destination.termsAndConditions = source.termsAndConditions
        destination.privacyNotice = source.privacyNotice

        source.consentStartDate?.let { startDate ->
            destination.consentStartDate = startDate.toLocalDate()
        }

        source.consentEndDate?.let { endDate ->
            destination.consentEndDate = endDate.toLocalDate()
        }

        destination.addressCountry = source.addressCountry
        destination.created = source.created

        source.patientDOB?.let { dob ->
            destination.patientDOB = dob.toLocalDate()
        }
    }

    override fun preMap(toModel: Boolean) {
    }

    override fun postMap() {
    }
}