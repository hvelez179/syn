//
//  DHPManagerFactory.kt
//  Teva_DHP
//
//  Copyright © 2017 Teva. All rights reserved.
//

package com.teva.dhp.models

/**
 This class provides the concrete implementation of the DHPManager.
 */
open class DHPManagerFactory {

    companion object {
	    private var dhpManagerImpl: DHPManagerImpl? = null

		fun getDHPManager(loginInfo: DHPLoginInfo): DHPManager {

            if (dhpManagerImpl == null) {

                // TODO: Uncomment this registration when we stop using Deprecated Rest Client.
                // Register a RestClient to be used by the DHPManager.
                // DependencyProvider.registerSingleton(TevaUtilitiesFactory.createRestClient())

                // TODO: Consider adding has() to DependencyProvider

                dhpManagerImpl = DHPManagerImpl(loginInfo)
            }

            return dhpManagerImpl as DHPManagerImpl
        }
    }

    /**
     This method returns the DHPManager, or creates it if it does not exist.
     - Returns: Returns the DHPManager
     */
}
