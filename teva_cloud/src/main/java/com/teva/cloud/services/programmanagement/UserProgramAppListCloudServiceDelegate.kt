//
// UserProgramAppListCloudServiceDelegate.kt
// teva_cloud
//
// Copyright © 2017 Teva. All rights reserved.
//

package com.teva.cloud.services.programmanagement


/**
 * This interface provides the CareProgramCloudService consumer to be called back
 * on completion of Get User Program App List.
 */

interface UserProgramAppListCloudServiceDelegate {
    var userProgramAppList: List<UserProgramAppListItem>?
        get
        set

    /**
     * This method is called upon completion of the asynchronous request for Get User Program App List.
     *
     * @param success: Indicates whether the REST request was successful.
     * @param errorCode: This parameter contains an error value indicating whether the request succeeded, or reason for failure.
     * @param userProgramAppList: This parameter contains a list of Programs in which the user is enrolled, and the Program's associated Apps that the user consented to share data.
     */
    fun getUserProgramAppListCompleted(success: Boolean, errorCode: CareProgramErrorCode, errorDetails: List<CareProgramErrorDetail>?, userProgramAppList: List<UserProgramAppListItem>)
}