//
// SyncCloudService.kt
// teva_cloud
//
// Copyright © 2017 Teva. All rights reserved.
//

package com.teva.cloud.services.sync

import com.teva.cloud.services.CloudObjectContainer


/**
 * This is the service used by the SyncManager to upload and download data in the cloud.
 */
internal interface SyncCloudService {

    /**
     * This property returns true if this is the first time the app is uploading or downloading data.
     */
    var isFirstSync: Boolean

    /**
     * Callback for the download async call.
     */
    var didDownload: ((success: Boolean, data: CloudObjectContainer, moreDataToDownloadExists: Boolean, didProcessDownloadedData: (() -> Unit)?) -> Unit)?

    /**
     * Callback for the upload async call.
     */
    var didUpload: ((success: Boolean) -> Unit)?

    /**
     * This method converts and uploads the data provided.
     * @param data: the app data to upload in a container.
     */
    fun uploadAsync(data: CloudObjectContainer)

    /**
     * This method downloads prescription and device data from the cloud and converts it into objects the app can understand.
     */
    fun downloadPrescriptionsAndDevicesAsync()

    /**
     * This method downloads data from the cloud and converts it into objects the app can understand.
     */
    fun downloadAsync()

}