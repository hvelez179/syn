//
// CareProgramManager.kt
// teva_cloud
//
// Copyright © 2017 Teva. All rights reserved.
//

package com.teva.cloud.models.programmanagement

import com.teva.cloud.dataentities.ProgramData
import com.teva.cloud.dataentities.UserProfile

//import com.teva.cloud.services.programmanagement.UserProgramAppListItem

interface CareProgramManager {

    // Properties

    /**
     * This property indicates whether the user can manage Programs.
     */
    var canManagePrograms: Boolean

    /**
     * Returns the name of the running App.
     */
    var currentAppName: String

    /**
     * Returns the version of the running App.
     */
    var currentAppVersionNumber: String

    /**
     * This property indicates whether User Program list has been retrieved, and handled, if needed.
     * This is used to determine whether the user needs to consent to continue using the App with
     * an already enrolled Program.
     */
    var hasManagedInitialUserProgramList: Boolean

    /**
     * This property indicates whether the User Program list needs to be refreshed.
     * Should occur on startup and when a profile is updated.
     */
    var shouldRefreshUserProgramList: Boolean

    // Methods
    /**
     * This method starts to get a list of Apps that the user consented to share data with each Program in which the user is enrolled.
     * If any programs were only joined from another app, program manager facilitests a flow to provide consent.
     */
    fun getAndManageInitialUserProgramListAsync()

    /**
     * This method gets a list of programs for the current active user profile and updates locally.
     */
    fun refreshUserProgramListAsync()

    /**
     * This method starts to get Care Program detail from the DHPManager.
     * Upon completion, the corresponding CareProgramCloudServiceDelegate method is called.
     *
     * @param invitationCode: This parameter is the invitation code used to enroll in a Program.
     */
    fun getInvitationDetailsAsync(invitationCode: String)


    /**
     * This method starts to accept the invitation, from the DHPManager, for the Profile passed in.
     * Upon completion, the corresponding CareProgramCloudServiceDelegate method is called.
     *
     * @param invitationDetails: This parameter is the invitation details used to enroll in a Program.
     */
    fun acceptInvitationAsync(invitationDetails: InvitationDetails)

    /**
     * This method returns a list of enrolled Care Programs for the user Profile passed in.
     *
     * @param userProfile: This parameter is the Profile of the user to get Care Programs.
     */
    fun getCarePrograms(userProfile: UserProfile): List<ProgramData>

    /**
     * This method starts to leave the Care Program from the DHPManager, for the Program ID and Profile ID passed in.
     * Upon completion, the corresponding CareProgramCloudServiceDelegate method is called.
     *
     * @param programId: This parameter is identifier of the program to leave.
     */
    fun leaveCareProgramAsync(programId: String)

    /**
     * This method updates the list of programs for a profile in the database.
     * @param programs: the programs to update
     * @param userProfile: the profile to update programs for
     */
    fun update(programs: List<ProgramData>, userProfile: UserProfile)

    /**
     * Called by consumer to indicate that the user has confirmed to continue to share data with the Program passed in.
     * @param programId: Id of the Program to consent to.
     */
    fun continueConsent(programId: String)

}