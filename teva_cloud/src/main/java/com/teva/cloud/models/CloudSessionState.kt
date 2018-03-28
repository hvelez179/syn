//
// CloudSessionState.kt
// teva_cloud
//
// Copyright © 2017 Teva. All rights reserved.
//

package com.teva.cloud.models

import android.content.SharedPreferences
import com.teva.cloud.dataentities.ConsentData
import com.teva.cloud.dataquery.ConsentDataQuery
import com.teva.cloud.models.userprofile.UserProfileManager
import com.teva.common.services.TimeService
import com.teva.utilities.services.DependencyProvider
import com.teva.dhp.DataEntities.DHPDataTypes.DHPCodes
//import com.teva.dhp.DataEntities.DHPDataTypes.DHPCodes
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import java.util.*


/**
 * This class maintains the state of the cloud session.
 */
class CloudSessionState {

    companion object {
        var shared = CloudSessionState()
    }

    /**
     * This property is used to get the current time for the app.
     */
    private var timeService = DependencyProvider.default.resolve<TimeService>()

    internal var isClinical = false

    internal var appName: String = if(isClinical) CloudConstants.clinicalAppName else CloudConstants.commercialAppName

    internal var appVersionNumber: String = if(isClinical) CloudConstants.clinicalAppVersionNumber else CloudConstants.commercialAppVersionNumber

    private var userProfileManager: UserProfileManager = DependencyProvider.default.resolve<UserProfileManager>()

    /**
     * This property is used to change dates in the format expected by the DHP.
     */
    internal val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss").withZone(ZoneId.of("GMT"))

    /**
     * This property stores a unique GUID to identify the installation of a Teva app on an iOS device.
     * It is used as an alternative to UIDevice.current.identifierForVendor?.uuidString so that test
     * users will not experience side effects when using both the RespiratoryApp and the ClinicalApp on the same device.
     */
    internal lateinit var mobileUUID: String

    /**
     * This property stores or retrieves the last successful sync date from UserDefaults.
     */
    internal var lastSuccessfulSyncDate: Instant?
        get() {
            val sharedPreferences = DependencyProvider.default.resolve<SharedPreferences>()
            val syncDateEpochSecond = sharedPreferences.getLong(CloudConstants.lastSuccessfulSyncDateKey, -1)

            return if(syncDateEpochSecond == -1L) null else Instant.ofEpochSecond(syncDateEpochSecond)
        }

        set(newValue) {
            val sharedPreferences = DependencyProvider.default.resolve<SharedPreferences>()
            val editor = sharedPreferences.edit()
            editor.putLong(CloudConstants.lastSuccessfulSyncDateKey, newValue?.epochSecond ?: -1)
            editor.apply()
        }


    /**
     * This property stores or retrieves the last failed sync date from UserDefaults.
     */
    internal var lastFailedSyncDate: Instant?
        get() {
            val sharedPreferences = DependencyProvider.default.resolve<SharedPreferences>()
            val syncDateEpochSecond = sharedPreferences.getLong(CloudConstants.lastFailedSyncDateKey, -1)

            return if(syncDateEpochSecond == -1L) null else Instant.ofEpochSecond(syncDateEpochSecond)
        }

        set(newValue) {
            val sharedPreferences = DependencyProvider.default.resolve<SharedPreferences>()
            val editor = sharedPreferences.edit()
            editor.putLong(CloudConstants.lastFailedSyncDateKey, newValue?.epochSecond ?: -1)
            editor.apply()
        }



    internal var studyHashKey: String = ""

    /**
     * Account holder's first name in Identity hub. This gets set after every login to ID hub
     */
    var idHubFirstName: String = ""

    /**
     * Account holder's last name in Identity hub. This gets set after every login to ID hub
     */
    var idHubLastName: String = ""

    /**
     * This method gets the ConsentData from the database.
     */
    var consentData: ConsentData? = null
            get() = DependencyProvider.default.resolve<ConsentDataQuery>().getConsentData() ?: ConsentData()


    /**
     * This property retrieves the active profile's external entity ID.
     * For the main account holder this will be the federation ID (FED-xxxxxxxxxx).
     * For a dependent patient this will be the DHP profile ID, as a GUID.
     */
    internal var activeProfileID: String = ""
        get() = DependencyProvider.default.resolve<UserProfileManager>().getActive()?.profileId ?: ""


    /**
     * This property returns the invoking role for DHP APIs.
     * If the account owner is the active profile, they execute requests as a patient.
     * If a dependent is the active profile, the guardian executes request on their behalf.
     */
    internal var activeInvokingRole: DHPCodes.Role = if(DependencyProvider.default.resolve<UserProfileManager>().getActive()?.isAccountOwner != false)  DHPCodes.Role.patient else DHPCodes.Role.guardian


    /**
     * The most recent server time of the DHP.
     * Only the CloudManagerImpl should be updating this value.
     */
    internal var serverTime: Instant? = null

    fun getMobileUUID(): String {
        val sharedPreferences = DependencyProvider.default.resolve<SharedPreferences>()
        var uuid = sharedPreferences.getString(CloudConstants.tevaAppInstallationUUIDKey, "")

        if (uuid.isNullOrEmpty()) {
            uuid = UUID.randomUUID().toString()
            val editor = sharedPreferences.edit()
            editor.putString(CloudConstants.tevaAppInstallationUUIDKey, uuid)
            editor.apply()
        }
        return uuid
    }

    /**
     * Offset between app time and server time, in seconds.
     * Only the CloudManagerImpl should be updating this value.
     */
    internal var serverTimeOffset: Int? = null

    init {
        dateFormatter.withZone(ZoneId.of("GMT"))
        mobileUUID = getMobileUUID()
    }
}