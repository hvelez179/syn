//
// LoadingEvents.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.activity.viewmodel

/**
 * Interface used to show and hide the loading indicator.
 */
interface LoadingEvents {
    /**
     * Starts the animation that shows the loading indicator.
     */
    fun showLoadingIndicator()

    /**
     * Stops the animation that shows the loading indicator.
     */
    fun hideLoadingIndicator()
}