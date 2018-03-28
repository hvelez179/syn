//
// teva_cloud
// CloudFactory.kt
//
// Copyright © 2017 Teva. All rights reserved.
//

package com.teva.cloud.models

import com.teva.cloud.dataquery.UserAccountQuery
import com.teva.cloud.dataquery.UserProfileQuery
import com.teva.cloud.models.programmanagement.CareProgramManager
import com.teva.cloud.models.programmanagement.CareProgramManagerImpl
import com.teva.cloud.models.sync.SyncManager
import com.teva.cloud.models.sync.SyncManagerImpl
import com.teva.cloud.models.userprofile.UserProfileManager
import com.teva.cloud.models.userprofile.UserProfileManagerImpl
import com.teva.cloud.services.DHPCloudService
import com.teva.utilities.services.DependencyProvider
import com.teva.dhp.models.DHPManager
import com.teva.dhp.models.DHPManagerFactory
import com.teva.dhp.models.DHPSession

//import com.teva.dhp.models.DHPManager
//import com.teva.dhp.models.DHPManagerFactory
//import com.teva.dhp.models.DHPSession


/**
 * This class provides instances of the cloud manager and login manager.
 */
class CloudFactory {

    companion object {
        private var cloudManagerImpl: CloudManager? = null
        private var webLoginManagerImpl: WebLoginManager? = null
        private var careProgramManagerImpl: CareProgramManager? = null
        private var userProfileManagerImpl: UserProfileManagerImpl? = null
        private var syncManagerImpl: SyncManagerImpl? = null

        /**
         * This method returns the CloudManager, or creates it if it does not exist.
         * It also registers internal dependencies.
         *
         * @return: Returns the CloudManager
         */
        fun setupAndGetCloudManager(isClinical: Boolean, userProfileQuery: UserProfileQuery): CloudManager {

            if (cloudManagerImpl == null) {
                val dhpManager = DHPManagerFactory.getDHPManager(CloudConstants.loginInfo)
                val cloudService = DHPCloudService(dhpManager)//(dhpManager)

                DependencyProvider.default.register(DHPManager::class, dhpManager)

                userProfileManagerImpl = UserProfileManagerImpl()
                userProfileManagerImpl!!.setQuery(userProfileQuery)
                DependencyProvider.default.register(UserProfileManager::class, userProfileManagerImpl!!)

                careProgramManagerImpl = CareProgramManagerImpl()
                DependencyProvider.default.register(CareProgramManager::class, careProgramManagerImpl!!)

                syncManagerImpl = SyncManagerImpl()
                DependencyProvider.default.register(SyncManager::class, syncManagerImpl!!)

                cloudManagerImpl = CloudManagerImpl(cloudService)

                CloudSessionState.shared.isClinical = isClinical


                // Set tokens on the DHPSession, if available.
                val userAccountQuery = DependencyProvider.default.resolve<UserAccountQuery>()
                val userAccount = userAccountQuery.getUserAccount()
                if(userAccount != null && cloudManagerImpl?.isInitialSetupCompleted == true) {
                    DHPSession.shared.dhpAccessToken = userAccount.DHPAccessToken ?: ""
                    DHPSession.shared.dhpRefreshToken = userAccount.DHPRefreshToken ?: ""
                    DHPSession.shared.identityHubAccessToken = userAccount.identityHubAccessToken ?: ""
                    DHPSession.shared.identityHubRefreshToken = userAccount.identityHubRefreshToken ?: ""
                    DHPSession.shared.identityHubIDToken = userAccount.identityHubIdToken ?: ""
                    DHPSession.shared.identityHubProfileURL = userAccount.identityHubProfileUrl ?: ""
                    DHPSession.shared.federationId = userAccount.federationId ?: ""
                    DHPSession.shared.username = userAccount.username ?: ""
               }
            }

            return cloudManagerImpl!!
        }

        /**
         * This method returns the WebLoginManager, or creates it if it does not exist.
         * @return: Returns the WebLoginManager
         */
        fun getWebLoginManager(): WebLoginManager {
            if (webLoginManagerImpl == null) {
                webLoginManagerImpl = WebLoginManagerImpl()
            }

            return webLoginManagerImpl!!
        }
    }
}