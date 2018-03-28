//
// CustomDeviceNameViewModel.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.activity.viewmodel.device

import android.databinding.Bindable
import android.os.Bundle
import com.teva.common.utilities.DataTask
import com.teva.utilities.services.DependencyProvider
import com.teva.common.utilities.isAlphaNumeric
import com.teva.devices.enumerations.InhalerNameType
import com.teva.devices.model.DeviceQuery
import com.teva.respiratoryapp.BR
import com.teva.respiratoryapp.R
import com.teva.respiratoryapp.activity.model.InhalerRegistrationCommonState
import com.teva.respiratoryapp.activity.viewmodel.TextEntryViewModel

/**
 * ViewModel for the Scan Device Name screen
 *
 * @param dependencyProvider Dependency Injection object.
 */
class CustomDeviceNameViewModel(dependencyProvider: DependencyProvider) : TextEntryViewModel(dependencyProvider), InhalerRegistrationCommonState.Listener {

    private val NICKNAME = "nickname"
    /**
     * The validation error.
     */
    @get:Bindable
    var validationError: String? = null
        private set(value) {
            field = value
            notifyPropertyChanged(BR.validationError)
        }

    private val commonState: InhalerRegistrationCommonState = dependencyProvider.resolve()

    /**
     * The device nickname
     */
    @get:Bindable
    var nickname: String = ""
        set(value) {
            field = value
            validateNickname()
            notifyPropertyChanged(BR.nickname)
            isClearButtonVisible = nickname.isNotEmpty()
        }

    /**
     * A boolean value indicating if clear button should be visible.
     */
    @get:Bindable
    var isClearButtonVisible: Boolean = false
        private set(clearButtonVisible) {
            field = clearButtonVisible
            notifyPropertyChanged(BR.clearButtonVisible)
        }

    var existingNames: Set<String>? = null

    init {

        nickname = commonState.customNickname ?: ""
    }

    /**
     * Method called by the BaseFragment when the fragment's onStart() lifecycle method is called.
     */
    override fun onStart() {
        super.onStart()

        commonState.addListener(this)

        // load the existing names
        existingNames = null

        checkExistingNames(commonState.serialNumber)

        validateNickname()
        notifyPropertyChanged(BR.nickname)
    }

    /**
     * Method called by the BaseFragment when the fragment's onStop() lifecycle method is called.
     */
    override fun onStop() {
        super.onStop()

        commonState.removeListener(this)
    }

    /**
     * Called by the fragment when the keyboard's action button is pressed.
     */
    override fun onEditorActionButton() {
        next()
    }

    /**
     * Handler for the next menu option.
     */
    operator fun next() {
        if (validateNickname()) {
            commonState.customNickname = nickname
            commonState.inhalerNameType = InhalerNameType.CUSTOM
            dependencyProvider.resolve<NameEvents>().onNameComplete()
        }
    }

    /**
     * Saves the current state of the fragment into a saved instance state.
     *
     * @param savedInstanceState The Bundle to save the state into.
     */
    override fun saveInstanceState(savedInstanceState: Bundle) {
        super.saveInstanceState(savedInstanceState)

        savedInstanceState.putString(NICKNAME, nickname)
    }

    /**
     * Restores the current state of the fragment from a saved instance state.
     *
     * @param savedInstanceState The Bundle to restore the state from.
     */
    override fun restoreInstanceState(savedInstanceState: Bundle?) {
        super.restoreInstanceState(savedInstanceState)

        nickname = savedInstanceState?.getString(NICKNAME, "") ?: ""
    }


    /**
     * Validates that the nickname is not empty and has appropriate characters.

     * @return true if the nickname is valid, false otherwise.
     */
    private fun validateNickname(): Boolean {
        var isValid = false

        // make sure the existing names set has been loaded first.
        if (existingNames != null && nickname.isNotEmpty()) {
            var errorMessage: String? = null

            if (!nickname.isAlphaNumeric()) {
                errorMessage = getString(R.string.addDeviceInvalidCustomNicknameInvalidCharacters_text)
            } else if (existingNames!!.contains(nickname)) {
                errorMessage = getString(R.string.addDeviceInvalidCustomNicknameAlreadyExists_text)
            } else if (nickname.length > MAX_NAME_LENGTH) {
                errorMessage = getString(R.string.addDeviceInvalidCustomNicknameExceedsMaxLength_text)
            } else {
                isValid = true
            }

            validationError = errorMessage
        } else {
            validationError = null
        }

        return isValid
    }

    /**
     * Indicates that the device property has changed.
     */
    override fun onDeviceLoaded() {
        nickname = commonState.customNickname ?: ""
        notifyPropertyChanged(BR.nickname)
        validateNickname()
    }

    private fun checkExistingNames(serialNumber: String?) {
        DataTask<String, Set<String>>("CustomInhalerName_existingNames")
                .inBackground {
                    val query = dependencyProvider.resolve<DeviceQuery>()
                    val deviceList = query.getAll()
                    val names = deviceList
                            .filter { serialNumber != it.serialNumber }
                            .map { it.nickname }
                            .toSet()

                    return@inBackground names
                }
                .onResult { nameSet ->
                    existingNames = nameSet
                    validateNickname()
                }
                .execute()
    }

    /**
     * Events produced by the ViewModel to request actions by the Activity.
     */
    interface NameEvents {
        /**
         * Indicates that the device name was entered.
         */
        fun onNameComplete()
    }

    companion object {
        private val MAX_NAME_LENGTH = 6
    }
}
