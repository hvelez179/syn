//
// EncryptedProgramDataMapper.kt
// app
//
// Copyright © 2017 Teva. All rights reserved.
//

package com.teva.respiratoryapp.models.dataquery.encrypted

import com.teva.cloud.dataentities.ProgramData
import com.teva.respiratoryapp.models.dataquery.generic.DataMapper
import com.teva.respiratoryapp.services.data.encrypteddata.entities.ProgramDataEncrypted


/**
 * This is the mapper class for mapping between program data entities and program data models and vice-versa.
 */
class EncryptedProgramDataMapper : DataMapper<ProgramData, ProgramDataEncrypted> {
    override fun toManagedEntity(source: ProgramData, destination: ProgramDataEncrypted) {
        destination.programName = source.programName
        destination.programId = source.programId
        destination.profileId = source.profileId
    }

    override fun toModelObject(source: ProgramDataEncrypted, destination: ProgramData) {
        destination.programName = source.programName
        destination.programId = source.programId
        destination.profileId = source.profileId
    }

    override fun preMap(toModel: Boolean) {
    }

    override fun postMap() {
    }
}