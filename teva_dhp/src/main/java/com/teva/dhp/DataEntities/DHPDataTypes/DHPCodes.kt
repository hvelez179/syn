//
// DHPCodes.kt
// teva_dhp
//
// Copyright © 2017 Teva. All rights reserved.
//
package com.teva.dhp.DataEntities.DHPDataTypes

class DHPCodes {

    enum class AnswerValue(val value: String) {
        good("1"),
        average("2"),
        poor("3")
    }

    enum class ApiExecutionMode(val value: String) {

        synchronous("synchronous"),
        asynchronous("asynchronous"),
    }

    enum class Boolean(val value: String) {

        `true`("true"),
        `false`("false")
    }

    enum class ConsentType(val value: String) {

        cloudConsent("cloudConsent"),
        shareDataStatement("shareDataStatement"),
        termsOfUse("termsOfUse"),
        namedConsent("namedConsent"),
        technicalContract("technicalContract")
    }

    enum class DataEntryClassification(val value: String) {

        automated("automated"),
        manual("manual")
    }

    enum class DeviceStatus(val value: String) {

        deleted("0"),
        active("1")
    }

    enum class DrugUID(val value: String) {

        proAir("745750"),
        fp("745752")
    }

    enum class Gender(val value: String) {

        male("Male"),
        female("Female"),
        unknown("")
    }

    enum class InvitationType(val value: String) {

        patientGuardian("PatientGuardian"),
        thirdParty("3rdParty"),
        admin("Admin")
    }

    enum class InvokingRole(val value: String) {

        patient("patient"),
        guardian("guardian"),
        cmSuperAdmin("cm_super_admin"),
        cmAdmin("cm_admin"),
        cmCareManager("cm_care_manager"),
        sysAdmin("sys_admin"),
        thirdParty("3rdParty")
    }

    enum class DeviceClassification(val value: String) {

        smartInhaler("Smart Inhaler"),
        sensor("Sensor")
    }

    enum class DeviceType(val value: String) {
        mdi("mdi")
    }

    enum class DeviceTechnology(val value: String) {
        proAirDigihaler("ProAir Digihaler")
    }

    enum class DeviceName(val value: String) {
        proAirDigihaler("ProAir HFA Digihaler")
    }

    enum class RelationshipStatusForProfileInfo(val value: String) {

        active("active"),
        inactive("inactive")
    }

    enum class Role(val value: String) {

        patient("patient"),
        guardian("guardian"),
        cmSuperAdmin("cm_super_admin"),
        cmAdmin("cm_admin"),
        cmCareManager("cm_care_manager"),
        sysAdmin("sys_admin"),
        thirdParty("3rdParty")
    }

    enum class RoleOfAcceptor(val value: String) {

        patient("patient"),
        thirdParty("3rdParty"),
        guardian("guardian")
    }

    enum class RoleOfDeleter(val value: String) {

        patient("patient"),
        guardian("guardian"),
        cmSuperAdmin("cm_super_admin"),
        cmAdmin("cm_admin"),
        cmCareManager("cm_care_manager"),
        sysAdmin("sys_admin"),
        thirdParty("3rdParty")
    }

    enum class RoleOfSender(val value: String) {

        patient("patient"),
        guardian("guardian"),
        cmSuperAdmin("cm_super_admin"),
        cmAdmin("cm_admin"),
        cmCareManager("cm_care_manager"),
        sysAdmin("sys_admin"),
        thirdParty("3rdParty")
    }

    enum class RoleForProfileInfo(val value: String) {

        patient("patient"),
        guardian("guardian"),
        cmSuperAdmin("cm_super_admin"),
        cmAdmin("cm_admin"),
        cmCareManager("cm_care_manager"),
        sysAdmin("sys_admin"),
        thirdParty("3rdParty"),
        dataManager("data_manager"),
        clinicalInvestigator("clinical_investigator")
    }

    enum class Status(val value: String) {

        notUsed("not_used"),
        used("used"),
        deleted("deleted")
    }

    enum class UnitOfMeasure(val value: String) {

        milliliter("ml"),
        seconds("seconds"),
        milliliterPerMinute("ml/minute"),
        milliseconds("milliseconds"),
        literPerMinute("l/minute"),
        perDay("day"),
        inhalation("puff"),
        asNeeded("asNeeded")
    }

    enum class OSInfo(val value: String) {
        android("Android"),
        iOS("IOS")
    }
}