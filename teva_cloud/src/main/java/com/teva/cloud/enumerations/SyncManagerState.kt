//
// SyncManagerState.kt
// teva_cloud
//
// Copyright © 2017 Teva. All rights reserved.
//

package com.teva.cloud.enumerations


/**
 * Enumeration representing the current state of the sync manager.
 */
enum class SyncManagerState {
    IDLE,
    SYNCING,
    UPLOADING,
    DOWNLOADING
}