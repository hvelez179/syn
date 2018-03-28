//
// CloudManagerImpl.kt
// teva_cloud
//
// Copyright © 2017 Teva. All rights reserved.
//

package com.teva.cloud.models

import com.teva.cloud.enumerations.CloudManagerState
import com.teva.cloud.enumerations.CloudManagerState.*
import com.teva.cloud.messages.CloudLoginStateChangedMessage
import com.teva.cloud.messages.ServerTimeOffsetUpdatedMessage
import com.teva.cloud.messages.SyncCloudMessage
import com.teva.cloud.models.programmanagement.CareProgramManager
import com.teva.cloud.models.sync.SyncManager
import com.teva.cloud.models.userprofile.UserProfileManager
import com.teva.cloud.services.CloudService
import com.teva.cloud.services.CloudServiceDelegate
import com.teva.common.services.ServerTimeService
import com.teva.common.services.TimeService
import com.teva.utilities.services.DependencyProvider
import com.teva.utilities.utilities.Logger
import com.teva.utilities.utilities.Logger.Level.INFO
import com.teva.utilities.utilities.Logger.Level.VERBOSE
import com.teva.common.utilities.Messenger
import org.greenrobot.eventbus.Subscribe
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import org.threeten.bp.ZoneOffset

/**
 * The implementation of the cloud manager interface.
 */
class CloudManagerImpl(val cloudService: CloudService): CloudManager, CloudServiceDelegate, ServerTimeService {
    // Internal properties

    /**
     * The current state of the cloud manager.
     */
    internal var cloudManagerState = CloudManagerState.UNINITIALIZED
        set(newValue) {
            field = newValue
            logger.log(VERBOSE, "cloudManagerState: $cloudManagerState")
        }

    private var userProfileManager = DependencyProvider.default.resolve<UserProfileManager>()

    override var isInitialSetupCompleted: Boolean = false
        get(){
            return userProfileManager.getActive() != null
        }

    override var isLoggedIn: Boolean = true
        set(newValue) {

            val oldValue = isLoggedIn
            field = newValue

            // Do not overwrite the state if currently syncing.
            if (isLoggedIn && (cloudManagerState == UNINITIALIZED || cloudManagerState == NOT_LOGGED_IN)) {
                cloudManagerState = IDLE
            } else if (!isLoggedIn) {
                cloudManagerState = NOT_LOGGED_IN
            }

            // We've gone from logged in to logged out, notify the app.
            if (oldValue && !isLoggedIn) {
                messenger.publish(CloudLoginStateChangedMessage(cloudManagerState))
            }
        }

    override val isEmancipated: Boolean
        get() = userProfileManager.getActive()?.isEmancipated ?: false

    private val logger = Logger("CloudManagerImpl")

    private val timeService = DependencyProvider.default.resolve<TimeService>()

    /**
     * This property indicates whether server time has been retrieved for the current sync.
     */
    private var hasGottenServerTime: Boolean = false

    private val messenger = DependencyProvider.default.resolve<Messenger>()

    private val syncManager = DependencyProvider.default.resolve<SyncManager>()

    private val careProgramManager = DependencyProvider.default.resolve<CareProgramManager>()

    init {
        cloudService.delegate = this
        messenger.subscribe(this)
        onSyncCloud()
    }

    override fun logOut() {
        cloudService.logOut()
    }

    // CloudServiceDelegate implementation

    override fun getServerTimeCompleted(success: Boolean, serverTime: Instant?) {

        logger.log(INFO, "serverTime:${serverTime?.atOffset(ZoneOffset.UTC) ?: "null"}")

        if(!success || serverTime == null) {
            return
        }

        // Update the serverTime in the session state. Do not overwrite existing value if nil.
        CloudSessionState.shared.serverTime = serverTime

        if (userProfileManager.isActiveProfileEmancipated()){
            cloudManagerState = EMANCIPATED
            return
        }

        val now = timeService.now()
        val adjustedServerTime = timeService.getApplicationTime(serverTime)

        val offsetSeconds = Duration.between(now, adjustedServerTime).seconds.toInt()
        logger.log(INFO, "Server Time Offset: $offsetSeconds")

        CloudSessionState.shared.serverTimeOffset = offsetSeconds
        hasGottenServerTime = true
        onSyncCloud()

        messenger.post(ServerTimeOffsetUpdatedMessage(isServerTimeOffsetWithinAcceptableRange()))
    }

    // Message handlers

    /**
     * This method is the message handler for the SyncCloud message that triggers a sync operation with the cloud.
     */
    @Subscribe
    fun onSyncCloudMessage(message: SyncCloudMessage) {
        onSyncCloud()
    }

    private fun onSyncCloud() {
        logger.log(INFO, "onSyncCloud()")

        if (cloudManagerState == UNINITIALIZED) {
            cloudService.initialize()
        }

        if (cloudManagerState == IDLE && isInitialSetupCompleted && isLoggedIn) {

            // Prevent syncing if we did not get ServerTime yet.
            if (!hasGottenServerTime) {
                return cloudService.getServerTimeAsync()
            }

            if(userProfileManager.shouldRefreshActiveUserAppList){
                userProfileManager.refreshActiveUserAppListAsync()
            }

            if (!careProgramManager.hasManagedInitialUserProgramList){
                if (syncManager.hasSynced){
                    careProgramManager.hasManagedInitialUserProgramList = true
                } else {
                    return careProgramManager.getAndManageInitialUserProgramListAsync()
                }
            }

            syncManager.sync()
            hasGottenServerTime = false
        }
    }

    // ServiceTimeService members
    override var serverTimeOffset: Int? = null
        get() {
            return CloudSessionState.shared.serverTimeOffset
        }

    override fun isServerTimeOffsetWithinAcceptableRange(): Boolean {
        return Math.abs(serverTimeOffset ?: 0) <= CloudConstants.maxAcceptableOffsetInSeconds
    }

    override fun clearServerTimeOffset() {
        CloudSessionState.shared.serverTimeOffset = null
    }

}
