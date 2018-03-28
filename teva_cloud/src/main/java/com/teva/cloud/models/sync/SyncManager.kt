//
// SyncManager.kt
// teva_cloud
//
// Copyright © 2017 Teva. All rights reserved.
//

package com.teva.cloud.models.sync


/**
 * This interface supports syncing the App data.
 */
internal interface SyncManager {
    /**
     * Indicates whether a sync has ever been completed.
     */
    val hasSynced: Boolean

    /**
     * This method initiates the bulk sync.
     */
    fun sync()

    /**
     * This method retrieves the list of changed objects from the database, provided they are not in the future, based on the server time.
     * It also applies the current server time offset to changed objects that do not have an offset applied.
     * @param uploadChangedObjects: are the changed objects being gathered for upload. If set to false, this method only checks for serverTimeOffsets.
     */
    fun applyServerTimeOffset(uploadChangedObjects: Boolean)
}