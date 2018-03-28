//
// ServerTimeOffsetUpdatedMessage.kt
// teva_cloud
//
// Copyright © 2017 Teva. All rights reserved.
//

package com.teva.cloud.messages


/**
 * This class represents a message published to indicate that the
 * server time offset is updated.
 */
class ServerTimeOffsetUpdatedMessage (var acceptable: Boolean = false)