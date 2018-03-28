//
// UserAppListMessage.kt
// teva_cloud
//
// Copyright © 2018 Teva. All rights reserved.
//

package com.teva.cloud.messages

import com.teva.cloud.dataentities.CloudAppData


/**
 * This message class is posted to indicate when UserAppList is retrieved.
 */
class UserAppListMessage (var success: Boolean, var userAppList: List<CloudAppData>)