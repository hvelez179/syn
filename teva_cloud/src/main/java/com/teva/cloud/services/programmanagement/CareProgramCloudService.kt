//
// CareProgramCloudService.kt
// teva_cloud
//
// Copyright © 2017 Teva. All rights reserved.
//

package com.teva.cloud.services.programmanagement

import com.teva.cloud.dataentities.ProgramData
import com.teva.cloud.models.programmanagement.InvitationDetails


/**
 * This interface provides the ability to manage Programs in the Cloud service layer.
 */
interface CareProgramCloudService {

    /**
     * This method type is called upon completion of the asynchronous request for Get Patient App List.
     */
    var didGetUserProgramList: ((errorCode: CareProgramErrorCode, errorDetails: List<CareProgramErrorDetail>?, programs: List<ProgramData>) -> Unit)?

    /**
     * This method type is called upon completion of the asynchronous request for Invitation Details.
     */
    var didGetInvitationDetails: ((errorCode: CareProgramErrorCode, errorDetails: List<CareProgramErrorDetail>?, invitationDetails: InvitationDetails) -> Unit)?

    /**
     * This method type is called upon completion of the asynchronous request to accept an invitation to enroll in a Program.
     */
    var didAcceptInvitation: ((errorCode: CareProgramErrorCode, errorDetails: List<CareProgramErrorDetail>?, invitationDetails: InvitationDetails?) -> Unit)?

    /**
     * This method type is called upon completion of the asynchronous request to opt out consent for Program.
     */
    var didLeaveProgram: ((errorCode: CareProgramErrorCode, errorDetails: List<CareProgramErrorDetail>?, programId: String) -> Unit)?

    /**
     * This method creates a Get User Program App List API request, and executes or queues the request.
     * This API is used to get a list of Apps that the user consented to share data with each Program in which the user is enrolled.
     */
    fun getUserProgramListAsync()

    /**
     * This method starts to get Care Program detail from the DHPManager.
     * Upon completion, the corresponding CareProgramCloudServiceDelegate method is called.
     * @param invitationCode: This parameter is the invitation code used to enroll in a Program.
     */
    fun getInvitationDetailsAsync(invitationCode: String)

    /**
     * This method starts to accept the invitation, from the DHPManager, for the Profile passed in.
     * Upon completion, the corresponding CareProgramCloudServiceDelegate method is called.
     * @param invitationCode: This parameter is the invitation code used to enroll in a Program.
     */
    fun acceptInvitationAsync(invitationDetails: InvitationDetails)

    /**
     * This method starts to Consent Opt Out from the DHPManager, for the Program ID and Profile ID passed in.
     * Upon completion, the corresponding CareProgramCloudServiceDelegate method is called.
     * @param programId: This parameter is identifier of the program to opt out.
     */
    fun leaveProgramAsync(programId: String)

}