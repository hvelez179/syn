package com.teva.respiratoryapp.mvvmframework.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import com.teva.common.services.PermissionManager
import com.teva.utilities.services.DependencyProvider
import java.util.*

/**
 * Helper class that checks permissions provides UI to request permission from the user.
 *
 * @param activity           The activity that owns the permission checker.
 * @param dependencyProvider The dependency injection mechanism.
 */
@SuppressLint("UseSparseArrays")
class PermissionChecker(private val activity: Activity, dependencyProvider: DependencyProvider) {

    private val permissionManager: PermissionManager = dependencyProvider.resolve()

    private var nextPermissionRequestId = PERMISSION_REQUEST + 1
    private val permissionRequests: MutableMap<Int, PermissionRequest>

    init {

        permissionRequests = HashMap<Int, PermissionRequest>()
    }

    /**
     * Checks to see if the app holds the requested permissions.

     * @param permissions The requested permissions.
     * *
     * @return True if the requested permissions are held.
     */
    fun checkPermissions(vararg permissions: String): Boolean {
        return permissionManager.checkPermission(*permissions)
    }

    /**
     * Checks if the UI should display a rationale to the user for the requested permissions.

     * @param permissionList The requested permissions.
     * *
     * @return
     */
    fun shouldShowRationale(vararg permissionList: String): Boolean {
        return permissionList.any {
            ActivityCompat.shouldShowRequestPermissionRationale(activity, it)
        }
    }

    /**
     * Displays UI to request permissions.

     * @param permissions The requested permissions.
     */
    fun requestPermissions(vararg permissions: String) {
        ActivityCompat.requestPermissions(activity,
                permissions,
                PERMISSION_REQUEST)
    }

    /**
     * Displays UI to request permissions and calls a callback method when the permission
     * dialog is dismissed.
     * @param permissions The requested Permissons
     * *
     * @param callback The callback interface to call when the permission dialog is dismissed.
     */
    fun requestPermissions(permissions: Array<String>, callback: PermissionCallback) {
        val request = PermissionRequest(callback)
        permissionRequests.put(request.requestId, request)

        ActivityCompat.requestPermissions(activity,
                permissions,
                request.requestId)
    }

    /**
     * Called by the activity when the Request Permissions result is received by the activity.

     * @param requestCode  The request code for the permission request.
     * *
     * @param permissions  The permissions that were requested.
     * *
     * @param grantResults An array of values indicating whether the corresponding permission was granted.
     */
    internal fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        val permissionsGranted = HashSet<String>()

        var allGranted = true
        for (i in permissions.indices) {
            if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                permissionsGranted.add(permissions[i])
            } else {
                allGranted = false
            }
        }

        // Notify app services about newly granted permissions
        permissionManager.notifyPermissionsChanged(permissionsGranted)

        // Notify requestor that the request has completed.
        val request = permissionRequests[requestCode]
        if (request != null) {
            permissionRequests.remove(requestCode)
            if (request.callback != null) {
                request.callback!!.onPermissionRequestResult(allGranted)
            }
        }
    }

    private inner class PermissionRequest(
            var callback: PermissionCallback?) {
        internal var requestId: Int = 0

        init {
            requestId = nextPermissionRequestId++
        }
    }

    interface PermissionCallback {
        fun onPermissionRequestResult(granted: Boolean)
    }

    companion object {
        private val PERMISSION_REQUEST = 1
    }
}
