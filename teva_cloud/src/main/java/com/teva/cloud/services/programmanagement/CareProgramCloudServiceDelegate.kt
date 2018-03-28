//
// CareProgramCloudServiceDelegate.kt
// teva_cloud
//
// Copyright © 2017 Teva. All rights reserved.
//

package com.teva.cloud.services.programmanagement

import com.teva.cloud.dataentities.ProgramData
import com.teva.cloud.models.programmanagement.InvitationDetails


/**
 * This interface provides the CareProgramCloudService consumer to be called back
 * on completion of asynchronous events, including Get Program Details,
 * Accept Invitation, Decline Invitation, Leave Program.
 */
interface CareProgramCloudServiceDelegate {

    /**
     * This method is called upon completion of the asynchronous request for Invitation Details.
     * @param errorCode: This parameter contains an error value indicating whether the request succeeded, or reason for failure.
     * @param invitationDetails: This parameter contains the Invitation Details associated with an invitation code.
     */
    fun getInvitationDetailsCompleted(errorCode: CareProgramErrorCode, errorDetails: List<CareProgramErrorDetail>?, invitationDetails: InvitationDetails)

    /**
     * This method is called upon completion of the asynchronous request for Get User Program App List.
     * @param errorCode: This parameter contains an error value indicating whether the request succeeded, or reason for failure.
     * @param patientAppList: This parameter contains a list of Apps that the patient is using.
     */
    fun getUserProgramListCompleted(errorCode: CareProgramErrorCode, errorDetails: List<CareProgramErrorDetail>?, userProgramAppList: List<ProgramData>)

    /**
     * This method is called upon completion of the asynchronous request to accept an invitation to enroll in a Program.
     * @param errorCode: This parameter contains an error value indicating whether the request succeeded, or reason for failure.
     */
    fun acceptInvitationCompleted(errorCode: CareProgramErrorCode, errorDetails: List<CareProgramErrorDetail>?, invitationDetails: InvitationDetails?)

    /**
     * This method is called upon completion of the asynchronous request to opt out consent for Program.
     * @param errorCode: This parameter contains an error value indicating whether the request succeeded, or reason for failure.
     */
    fun leaveProgramCompleted(errorCode: CareProgramErrorCode, errorDetails: List<CareProgramErrorDetail>?, programId: String)

}