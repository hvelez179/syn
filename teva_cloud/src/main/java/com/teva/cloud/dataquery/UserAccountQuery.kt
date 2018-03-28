//
// UserAccountQuery.kt
// teva_cloud
//
// Copyright © 2017 Teva. All rights reserved.
//

package com.teva.cloud.dataquery

import com.teva.cloud.dataentities.UserAccount
import com.teva.common.dataquery.DataQueryForTrackedModels


/**
 * Classes that implement this interface support persistence of user account information.
 */
interface UserAccountQuery : DataQueryForTrackedModels<UserAccount> {
    /**
     * Returns information about the user account.
     */
    fun getUserAccount(): UserAccount?
}