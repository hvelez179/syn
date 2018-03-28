//
// PermissionUpdateMessage.kt
// teva_common
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.common.messages

/**
 * Message sent to verify that the app has a permission.
 *
 * @param permissionSet The set of permissions that were updated.
 */
class PermissionUpdateMessage(private val permissionSet: Set<String>) {

    /**
     * Checks if the updated permissions include the specified permission.
     * @param permission The permission to check.
     * *
     * @return True if the updated permissions contains the permission, false otherwise.
     */
    fun hasPermission(permission: String): Boolean {
        return permissionSet.contains(permission)
    }

    /**
     * Checks if the updated permissions includes any of the specfied permissions.
     * @param permissions The permissions to check.
     * *
     * @return True if the update permissions contain any of the specified permissions, false otherwise.
     */
    fun hasAnyPermission(vararg permissions: String): Boolean {

        return permissions.any { hasPermission(it) }
    }

}
