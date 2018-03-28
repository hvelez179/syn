//
// PermissionManager.kt
// teva_common
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.common.services

/**
 * Checks permissions at runtime.
 */
interface PermissionManager {

    /**
     * Checks if the app holds the specified permissions
     *
     * @param permissionList The collection of permissions to check.
     * @return True if all of the permissions are held by the app, false otherwise.
     */
    fun checkPermission(vararg permissionList: String): Boolean

    /**
     * Broadcasts a message indicating that the permissions have been updated.
     *
     * @param permissionsGranted  The new permissions that have been granted.
     */
    fun notifyPermissionsChanged(permissionsGranted: Set<String>)
}
