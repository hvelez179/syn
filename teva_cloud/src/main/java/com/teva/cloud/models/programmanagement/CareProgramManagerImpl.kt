//
// CareProgramManagerImpl.kt
// teva_cloud
//
// Copyright © 2017 Teva. All rights reserved.
//

package com.teva.cloud.models.programmanagement

import com.teva.cloud.dataentities.CloudAppData
import com.teva.cloud.dataentities.ProgramData
import com.teva.cloud.dataentities.UserProfile
import com.teva.cloud.dataquery.ProgramDataQuery
import com.teva.cloud.messages.*
import com.teva.cloud.models.CloudManager
import com.teva.cloud.models.CloudSessionState
import com.teva.cloud.models.userprofile.UserProfileManager
import com.teva.cloud.services.programmanagement.*
import com.teva.utilities.services.DependencyProvider
import com.teva.utilities.utilities.Logger
import com.teva.utilities.utilities.Logger.Level.ERROR
import com.teva.utilities.utilities.Logger.Level.INFO
import com.teva.common.utilities.Messenger


/**
 */
class CareProgramManagerImpl() : CareProgramManager {

    /**
     * This property is the Logger used to log to the console.
     */
    internal val logger = Logger("CareProgramManagerImpl")

    private var messenger = DependencyProvider.default.resolve<Messenger>()

    // Properties

    /**
     * This property stores the ProgramDataQuery dependency.
     */
    internal var programDataQuery: ProgramDataQuery = DependencyProvider.default.resolve()

    internal var careProgramService: CareProgramCloudService = DHPCareProgramCloudService()

    var userProfileManager: UserProfileManager = DependencyProvider.default.resolve()

    override var canManagePrograms: Boolean = false
        get(){
            return DependencyProvider.default.resolve<CloudManager>().isLoggedIn && hasManagedInitialUserProgramList && userProfileManager.activeUserAppList != null
        }

    /**
     * Flag inidicating whether this manager has retrieved the program list from the Cloud. If so, do not overwrite locally.
     */
    var hasRetrievedInitialUserProgramList = false

    /**
     * This property indicates whether the User Program list needs to be refreshed.
     * Should occur on startup and when a profile is updated.
     */
    override var shouldRefreshUserProgramList = true

    /**
     * This property indicates whether an API call to get the user program list is in progress.
     */
    var isGettingUserProgramList = false

    /**
    This property indicates whether User Program list has been retrieved, and handled, if needed.
    This is used to determine whether the user needs to consent to continue using the App with
    an already enrolled Program.
     */
    override var hasManagedInitialUserProgramList: Boolean = false

    /**
     * Accesses the CloudSessionState to return the name of the running App.
     */
    override var currentAppName: String = ""
        get(){
        return CloudSessionState.shared.appName
    }

    /**
     * Accesses the CloudSessionState to return the version of the running App.
     */
    override var currentAppVersionNumber: String = ""
        get() {
        return CloudSessionState.shared.appVersionNumber
    }

    /**
     * Initialize by setting delegate
     */
    init {
        careProgramService.didGetUserProgramList = this::getUserProgramListCompleted
        careProgramService.didGetInvitationDetails = this::getInvitationDetailsCompleted
        careProgramService.didAcceptInvitation = this::acceptInvitationCompleted
        careProgramService.didLeaveProgram = this::leaveProgramCompleted
    }

    /**
     * This method sets the ProgramDataQuery dependency. It is set directly rather than registered with the DependencyProvider to prevent access by other objects.
     * @param query: This parameter is the DataQuery object to use to access Program information.
     */
    fun setQuery(query: ProgramDataQuery) {
        programDataQuery = query
    }

    /**
     * Saves the Program, passed in, to the database.
     * @param programId: ID of the Program to save.
     * @param programName: Display name of the Program to save.
     */
    fun saveProgram(programId: String, programName: String, profileId: String, consentedApps: MutableList<CloudAppData>, invitationCode: String) {
        val programData = ProgramData(programName, programId, profileId, consentedApps, invitationCode)
        programDataQuery.insertOrUpdate(programData, true)
    }

    /**
     * Deletes the Program, passed in, from the database.
     * @param programId: ID of the Program to save.
     */
    fun deleteProgram(programId: String) {
        val profileId = CloudSessionState.shared.activeProfileID

        val programData = ProgramData(programId = programId, profileId = profileId)
        programDataQuery.delete(programData)
    }

    // CareProgramManager implementation

    /**
     * This method starts to get a list of Apps that the user consented to share data with each Program in which the user is enrolled.
     * If any programs were only joined from another app, program manager facilitests a flow to provide consent.
     */
    override fun getAndManageInitialUserProgramListAsync() {
        if (hasRetrievedInitialUserProgramList){
            checkUserConsentedToAllProgramsWithThisAppName()
            return
        }

        refreshUserProgramListAsync()
    }

    override fun refreshUserProgramListAsync() {
        if (isGettingUserProgramList){
            return
        }
        isGettingUserProgramList = true
        careProgramService.getUserProgramListAsync()
    }

    /**
     * This method starts to get Care Program detail from the DHPManager.
     * Upon completion, the corresponding CareProgramCloudServiceDelegate method is called.
     * @param invitationCode: This parameter is the invitation code used to enroll in a Program.
     */
    override fun getInvitationDetailsAsync(invitationCode: String) {
        careProgramService.getInvitationDetailsAsync(invitationCode)
    }

    /**
     * This method requests to join a program given an invitation code, and a User Profile.
     * Upon completion, the corresponding delegate method is called indicating success or failure.
     * @param invitationCode: This parameter is the invitation code used to enroll in a Program.
     */
    override fun acceptInvitationAsync(invitationDetails: InvitationDetails) {
        careProgramService.acceptInvitationAsync(invitationDetails)
    }

    /**
     * This method retrieves a list of enrolled Care Programs corresponding to the Profile identifier passed in.
     * This method returns immediately with the result.
     * @param userProfile: This parameter is the Profile of the user to get Care Programs.
     * @return: Returns a list of enrolled Programs.
     */
    override fun getCarePrograms(userProfile: UserProfile) : List<ProgramData> {

        val profileId = userProfile.profileId

        // Query database for enrolled Care Programs for passed-in user profile.
        if(profileId == null) {
            logger.log(ERROR, "getCarePrograms(): profileId = null.")
            return ArrayList()
        }

        return programDataQuery.getCarePrograms(profileId)
    }

    /**
     * This method updates the list of programs for a profile in the database.
     * @param programs: the programs to update
     * @param userProfile: the profile to update programs for
     */
    override fun update(programs: List<ProgramData>, userProfile: UserProfile) {
        val profileId = userProfile.profileId
        if (profileId == null) {
            logger.log(ERROR, "Valid profile was not provided.")
            return
        }
        val carePrograms = getCarePrograms(userProfile = userProfile)
        for (program in programs) {
            val existingProgram = carePrograms.firstOrNull{ it.programId == program.programId }
            if (existingProgram != null) {
                program.programName = existingProgram.programName
            }
            saveProgram(program.programId!!, program.programName!!, profileId, program.consentedApps!!, program.invitationCode!!)
        }

        // Remove any Programs from the database that are no longer active.
        for (careProgram in carePrograms) {
            if (programs.firstOrNull{ it.programId == careProgram.programId } == null) {
                deleteProgram(careProgram.programId!!)
            }
        }
        messenger.post(ProgramListUpdatedMessage())

    }

    /**
     * This method requests to remove a profile from a Care Program. Upon completion, the corresponding
     * delegate method is called indicating success or failure.
     * @param programId: This parameter is identifier of the program to leave.
     */
    override fun leaveCareProgramAsync(programId: String) {
        careProgramService.leaveProgramAsync(programId)
    }

    /**
     * Used to determine if there is a program in the list which requires consent. If not, marks the process as done.
     */
    fun checkUserConsentedToAllProgramsWithThisAppName() {
        var programRequiresConsent = false

        val activeUserProfile = userProfileManager.getActive()

        if (activeUserProfile != null) {

            val programs = getCarePrograms(activeUserProfile)

            for (program in programs) {
                val consentedAppNames = program.consentedApps?.map({ it.appName })

                if (consentedAppNames != null) {
                    if (!consentedAppNames.contains(CloudSessionState.shared.appName)) {

                        messenger.post(RemindProgramConsentMessage(program, consentedAppNames))
                        programRequiresConsent = true
                        break
                    }
                }
            }
        }
        if (!programRequiresConsent) {
            hasManagedInitialUserProgramList = true
            messenger.post(SyncCloudMessage())
        }

    }

    /**
     * Called by consumer to indicate that the user has confirmed to continue to share data with the Program passed in.
     * @param programId: Id of the Program to consent to.
     */
    override fun continueConsent(programId: String) {
        val program = programDataQuery.getCarePrograms(CloudSessionState.shared.activeProfileID).firstOrNull{ it.programId == programId }
        if (program != null) {
            program.consentedApps?.add(CloudAppData(CloudSessionState.shared.appName, CloudSessionState.shared.appVersionNumber))
            programDataQuery.update(program, true)

            val activeProfile = userProfileManager.getActive()
            if (activeProfile != null) {
                userProfileManager.update(activeProfile, true)
            }
        }

    }

    // CareProgramCloudServiceDelegate implementation

    /**
     * This method is called upon completion of the asynchronous request for Get User Program App List.
     * This method sends UserProgramAppListMessage with a list of active Programs and their corresponding consented Apps.
     * @param success: Indicates whether the REST request was successful.
     * @param errorCode: This parameter contains an error value indicating whether the request succeeded, or reason for failure.
     * @param userProgramAppList: This parameter contains a list of Programs in which the user is enrolled, and the Program's associated Apps that the user consented to share data.
     */
    fun getUserProgramListCompleted(errorCode: CareProgramErrorCode, errorDetails: List<CareProgramErrorDetail>?, userProgramAppList: List<ProgramData>) {
        val logString = "getUserProgramListCompleted: UserProgramAppList: ${userProgramAppList})"
        logger.log(INFO, logString)

        isGettingUserProgramList = false
        shouldRefreshUserProgramList = errorCode != CareProgramErrorCode.NO_ERROR

        if (errorCode == CareProgramErrorCode.NO_ERROR) {
            val activeUserProgramAppList = userProgramAppList.filter { it.active }
            val activeUserProfile = userProfileManager.getActive()
            if (activeUserProfile != null) {
                update(activeUserProgramAppList, activeUserProfile)
            }
            messenger.post(UserProgramAppListMessage(errorCode, errorDetails, activeUserProgramAppList))

            if (!hasManagedInitialUserProgramList) {
                hasRetrievedInitialUserProgramList = true
                checkUserConsentedToAllProgramsWithThisAppName()
            }
        }

    }

    /**
     * This method is called upon completion of the asynchronous request for Invitation Details.
     * @param errorCode: This parameter contains an error value indicating whether the request succeeded, or reason for failure.
     * @param invitationProgramInfo: This parameter contains the Invitation Details associated with an invitation code.
     */
    fun getInvitationDetailsCompleted(errorCode: CareProgramErrorCode, errorDetails: List<CareProgramErrorDetail>?, invitationDetails: InvitationDetails) {

        var logString = "getInvitationDetailsCompleted: Raising InvitationDetailsMessage errorCode: $errorCode"
        logString += ", programId: ${invitationDetails.programId}"
        logString += ", programName: ${invitationDetails.programName}"
        logString += ", programConsentText: ${invitationDetails.programConsentText}"

        logger.log(INFO, logString)

        var errorCodeToReturn = errorCode

        // Save results in CloudManagerImpl state.
        if (errorCode == CareProgramErrorCode.NO_ERROR) {
            val alreadyEnrolledInProgram = programDataQuery.getCarePrograms(CloudSessionState.shared.activeProfileID).find{ it.programId == invitationDetails.programId }

            if (alreadyEnrolledInProgram != null) {
                errorCodeToReturn = CareProgramErrorCode.ALREADY_ENROLLED_IN_PROGRAM
            } else {
                val userAppNames = userProfileManager.activeUserAppList?.map { it.appName }
                if (userAppNames != null) {
                    invitationDetails.programSupportedUserApps = invitationDetails.programSupportedApps.filter { userAppNames.contains(it.appName) }

                    if(invitationDetails.programSupportedUserApps.isEmpty()) {
                        errorCodeToReturn = CareProgramErrorCode.APP_NOT_SUPPORTED_BY_PROGRAM
                    }
                }
            }
        }

        messenger.post(InvitationDetailsMessage(invitationDetails, errorCodeToReturn, errorDetails))
    }

    /**
     * This method is called upon completion of the asynchronous request to accept an invitation to enroll in a Program.
     * @param errorCode: This parameter contains an error value indicating whether the request succeeded, or reason for failure.
     * @param errorDetails: Details about the error.
     * @param invitationDetails: the invitation that was attempted to be accepted
     */
    fun acceptInvitationCompleted(errorCode: CareProgramErrorCode, errorDetails: List<CareProgramErrorDetail>?, invitationDetails: InvitationDetails?) {

        logger.log(INFO, "acceptInvitationCompleted: Raising InvitationAcceptedMessage(errorCode: $errorCode)")

        messenger.post(InvitationAcceptedMessage(errorCode, errorDetails, invitationDetails))

        if (errorCode == CareProgramErrorCode.NO_ERROR && invitationDetails != null) {
            saveProgram(invitationDetails.programId,
                    invitationDetails.programName,
                    CloudSessionState.shared.activeProfileID,
                    invitationDetails.programSupportedApps.toMutableList(),
                    invitationDetails.invitationCode)
        }

        // Sync the active profile
        if (userProfileManager.getActive() != null){
            userProfileManager.update(userProfileManager.getActive()!!, true)
            messenger.post(SyncCloudMessage())
        }
    }

    /**
     * This method is called upon completion of the asynchronous request to opt out consent for Program.
     * @param errorCode: This parameter contains an error value indicating whether the request succeeded, or reason for failure.
     */
    fun leaveProgramCompleted(errorCode: CareProgramErrorCode, errorDetails: List<CareProgramErrorDetail>?, programId: String) {

        logger.log(INFO, "consentOptOutCompleted: Raising LeaveProgramMessage(errorCode: $errorCode, programId: $programId)")

        messenger.post(LeaveProgramMessage(errorCode, errorDetails))

        // If no error, find and delete the program corresponding to the removed programId
        // from the list of programs of the current profile.
        if(errorCode != CareProgramErrorCode.NO_ERROR)  {
            return
        }

        deleteProgram(programId)

        // Sync the active profile
        if (userProfileManager.getActive() != null){
            userProfileManager.update(userProfileManager.getActive()!!, true)
            messenger.post(SyncCloudMessage())
        }
    }

}