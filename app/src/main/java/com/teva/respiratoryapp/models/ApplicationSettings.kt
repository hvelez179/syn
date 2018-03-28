///
// ApplicationSettings.java
// app
//
// Copyright © 2017 Teva. All rights reserved
///

package com.teva.respiratoryapp.models

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.support.annotation.MainThread

import com.teva.utilities.services.DependencyProvider
import com.teva.common.utilities.MessageHandler

/**
 * This class retrieves and saves the application settings for the app.
 *
 * @param dependencyProvider The dependency injection mechanism
 */
@MainThread
@SuppressLint("CommitPrefEdits")
class ApplicationSettings(dependencyProvider: DependencyProvider) : MessageHandler.MessageListener {

    private val sharedPreferences: SharedPreferences = dependencyProvider.resolve()
    private val editor: SharedPreferences.Editor
    private val handler: MessageHandler = MessageHandler(this)

    init {
        editor = sharedPreferences.edit()
    }

    /**
     * A value indicating whether the value proposition has been shown.
     */
    var hasActiveProfile: Boolean
        get() = sharedPreferences.getBoolean(HAS_ACTIVE_PROFILE, false)
        set(value) {
            editor.putBoolean(HAS_ACTIVE_PROFILE, value)
            postCommit()
        }

    /**
     * A value indicating whether the first inhaler has been scanned.
     */
    var firstInhalerScanned: Boolean
        get() = sharedPreferences.getBoolean(FIRST_INHALER_SCANNED, false)
        set(value) {
            editor.putBoolean(FIRST_INHALER_SCANNED, value)
            postCommit()
        }

    /**
     * A value indicating whether the user has been asked for the camera permission.
     */
    var cameraPermissionRequested: Boolean
        get() = sharedPreferences.getBoolean(CAMERA_PERMISSION_REQUESTED, false)
        set(value) {
            editor.putBoolean(CAMERA_PERMISSION_REQUESTED, value)
            postCommit()
        }

    /**
     * A value indicating whether the user has been asked for the camera permission.
     */
    var locationPermissionRequested: Boolean
        get() = sharedPreferences.getBoolean(LOCATION_PERMISSION_REQUESTED, false)
        set(value) {
            editor.putBoolean(LOCATION_PERMISSION_REQUESTED, value)
            postCommit()
        }

    /**
     * A value indicating if the walkthrough was shown to the user.
     */
    var isWalkthroughShown: Boolean
        get() = sharedPreferences.getBoolean(WALKTHROUGH_SHOWN, false)
        set(value) {
            editor.putBoolean(WALKTHROUGH_SHOWN, value)
            postCommit()
        }

    /**
     * A value indicating if the user has consented to the terms of using the app.
     */
    var hasUserAcceptedTermsOfUse: Boolean
        get() = sharedPreferences.getBoolean(USER_ACCEPTED_TERMS_OF_USE, false)
        set(value) {
            editor.putBoolean(USER_ACCEPTED_TERMS_OF_USE, value)
            postCommit()
        }

    /**
     * The encrypted database password.
     */
    var encryptedDatabasePassword: String
        get() = sharedPreferences.getString(DATABASE_PASSWORD, "")
        set(value) {
            editor.putString(DATABASE_PASSWORD, value)
            postCommit()
        }

    /**
     * A value indicating if the user account should not be usable due to emancipation.
     */
    var emancipated: Boolean
        get() = sharedPreferences.getBoolean(EMANCIPATED, false)
        set(value) {
            editor.putBoolean(EMANCIPATED, value)
            postCommit()
        }

    /**
     * This method posts the commit message to the message handler.
     */
    private fun postCommit() {
        handler.sendMessageIfNotQueued(COMMIT_MESSAGE)
    }

    /**
     * Method called when a message is received by the MessageHandler.
     *
     * @param message The id of the message.
     */
    override fun onMessage(message: Int) {
        if (message == COMMIT_MESSAGE) {
            editor.commit()
        }
    }

    companion object {
        private val COMMIT_MESSAGE = 1

        private val LOCATION_PERMISSION_REQUESTED = "LocationPermissionRequested"
        private val CAMERA_PERMISSION_REQUESTED = "CameraPermissionRequested"
        private val WALKTHROUGH_SHOWN = "WalkthroughShown"
        private val FIRST_INHALER_SCANNED = "FirstInhalerScanned"
        private val HAS_ACTIVE_PROFILE = "HasActiveProfile"
        private val USER_ACCEPTED_TERMS_OF_USE = "UserAcceptedTermsOfUse"
        private val AES_KEY = "AESKey"
        private val DATABASE_PASSWORD = "DatabasePassword"
        private val EMANCIPATED = "Emancipated"
    }
}
