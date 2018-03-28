//
// DHPGetRelatedProfilesResponseBody.kt
// teva_dhp
//
// Copyright © 2017 Teva. All rights reserved.
//
package com.teva.dhp.DataEntities.DHPDataTypes.ResponseBodies

class DHPGetRelatedProfilesResponseBody(json: String) : DHPResponseBody(json) {
    data class ProfileInfo(var firstName: String, var lastName: String, var dob: String, var profileId: String)
    var profiles: List<ProfileInfo> = listOf<ProfileInfo>()
    private var firstName = ""
    private var lastName = ""
    private var dateOfBirth = ""
    private var profileId = ""

    private data class DHPPersonReturnObject(
        var resourceType: String?,
        var id: String?,
        var name: List<DHPPersonResourceNameField>?,
        var identifier: List<DHPPersonResourceIdentifierField>?,
        var birthDate: String?,
        var gender: String?
    ): DHPReturnObject {
        override fun isValidObject() : Boolean {
            return true
        }
    }
    private data class DHPPersonResourceNameField (
        var use: String?,
        var text: String?,
        var family: List<String>?,
        var given: List<String>?
    )

    private data class DHPPersonResourceIdentifierField (
        var use: String?,
        var system: String?,
        var value: String?
    )

    init {
        val objects = decodeReturnObjects<DHPPersonReturnObject>(Array<DHPPersonReturnObject>::class.java)
        for (person in objects) {
            val nameKey = person.name
            val identifierKey = person.identifier
            if (nameKey == null || identifierKey == null) {
                continue
            }
            val nameObject = nameKey.filter({ ((it.family != null && it.given != null) && (it.family!!.count() == it.given!!.count())) }).first()

            val lastName = nameObject.family?.first()
            val firstName = nameObject.given?.first()
            val dateOfBirth = person.birthDate
            if (lastName != null && firstName != null && dateOfBirth != null) {
                this.lastName = lastName
                this.firstName = firstName
                this.dateOfBirth = dateOfBirth
            }

            val identifierObject = identifierKey.filter({ it.system != null && it.system!!.contains("externalEntityID") }).first()

            val value = identifierObject.value
            if (value != null) {
                this.profileId = value
            }

            profiles += ProfileInfo(this.firstName, this.lastName, this.dateOfBirth, profileId)
        }
    }
}