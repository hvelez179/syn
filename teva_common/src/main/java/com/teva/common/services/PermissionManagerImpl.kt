///
// PermissionManagerImpl.kt
// teva_common
//
// Copyright © 2017 Teva. All rights reserved
///

package com.teva.common.services

import android.content.Context
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import com.teva.common.messages.PermissionUpdateMessage
import com.teva.utilities.services.DependencyProvider
import com.teva.utilities.utilities.Logger
import com.teva.common.utilities.Messenger

/**
 * Checks permissions at runtime.
 *
 * @param dependencyProvider The dependency injection mechanism.
 * @param appContext The application context.
 */
class PermissionManagerImpl(
        private val dependencyProvider: DependencyProvider,
        private val appContext: Context) : PermissionManager {

    private val logger = Logger(PermissionManager::class)

    /**
     * Checks if the app holds the specified permissions
     *
     * @param permissionList The collection of permissions to check.
     * @return True if all of the permissions are held by the app, false otherwise.
     */
    override fun checkPermission(vararg permissionList: String): Boolean {
        var result = true

        for (permission in permissionList) {
            val state = ActivityCompat.checkSelfPermission(appContext, permission)

            if (state != PackageManager.PERMISSION_GRANTED) {
                logger.log(Logger.Level.DEBUG, "Failed permission check: " + permission)
                result = false
                break
            }
        }

        return result
    }

    /**
     * Broadcasts a message indicating that the permissions have been updated.
     *
     * @param permissionsGranted  The new permissions that have been granted.
     */
    override fun notifyPermissionsChanged(permissionsGranted: Set<String>) {
        val message = PermissionUpdateMessage(permissionsGranted)
        dependencyProvider.resolve<Messenger>().publish(message)
    }
}
