//
// CustomDeviceNameViewModelTest.java
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.activity.viewmodel.device

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import com.teva.utilities.services.DependencyProvider
import com.teva.common.utilities.LocalizationService
import com.teva.common.utilities.Messenger
import com.teva.devices.entities.Device
import com.teva.devices.enumerations.InhalerNameType
import com.teva.devices.model.DeviceQuery
import com.teva.respiratoryapp.R
import com.teva.respiratoryapp.activity.model.InhalerRegistrationCommonState
import com.teva.respiratoryapp.mocks.MockedLocalizationService
import com.teva.respiratoryapp.testutils.BaseTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.*

class CustomDeviceNameViewModelTest : BaseTest() {

    private lateinit var commonState: InhalerRegistrationCommonState
    private lateinit var deviceQuery: DeviceQuery
    private lateinit var nameEvents: CustomDeviceNameViewModel.NameEvents
    private lateinit var messenger: Messenger
    private lateinit var dependencyProvider: DependencyProvider

    private var deviceList: MutableList<Device>? = null


    @Before
    @Throws(Exception::class)
    fun setUp() {
        dependencyProvider = DependencyProvider.default
        dependencyProvider.unregisterAll()

        messenger = mock()
        dependencyProvider.register(Messenger::class, messenger)

        // initialize common state
        commonState = mock()
        whenever(commonState.serialNumber).thenReturn(CURRENT_SERIAL_NUMBER)
        dependencyProvider.register(InhalerRegistrationCommonState::class, commonState)

        // initialize device query
        createDeviceList()
        deviceQuery = mock()
        whenever(deviceQuery.getAll()).thenReturn(deviceList)
        dependencyProvider.register(DeviceQuery::class, deviceQuery)

        // initialize localization service
        val localizationService = MockedLocalizationService()
        dependencyProvider.register(LocalizationService::class, localizationService)
        localizationService.add(R.string.addDeviceInvalidCustomNicknameInvalidCharacters_text, CUSTOM_NAME_CHARACTER_ERROR)
        localizationService.add(R.string.addDeviceInvalidCustomNicknameExceedsMaxLength_text, CUSTOM_NAME_LENGTH_ERROR)
        localizationService.add(R.string.addDeviceInvalidCustomNicknameAlreadyExists_text, CUSTOM_NAME_ALREADY_EXISTS)

        nameEvents = mock()
        dependencyProvider.register(CustomDeviceNameViewModel.NameEvents::class, nameEvents)
    }

    private fun createDevice(serialNumber: String, nameType: InhalerNameType, nickname: String): Device {
        val device = Device()
        device.serialNumber = serialNumber
        device.inhalerNameType = nameType
        device.nickname = nickname

        return device
    }

    private fun createDeviceList() {
        deviceList = ArrayList<Device>()

        deviceList!!.add(createDevice("0987654321", InhalerNameType.HOME, "Home"))
        deviceList!!.add(createDevice("0987654321", InhalerNameType.WORK, "Work"))
        deviceList!!.add(createDevice("0987654321", InhalerNameType.CARRY_WITH_ME, "Travel"))
        deviceList!!.add(createDevice(CURRENT_SERIAL_NUMBER, InhalerNameType.SPORTS, "Sports"))
    }

    @Test
    fun testOnStartAddsCommonStateListener() {
        val viewmodel = CustomDeviceNameViewModel(dependencyProvider)

        viewmodel.onStart()

        verify(commonState).addListener(viewmodel)
    }

    @Test
    fun testOnStartRetrievesExistingNames() {
        val initialNickname = TOO_LONG_NICKNAME
        whenever(commonState.customNickname).thenReturn(initialNickname)
        val viewmodel = CustomDeviceNameViewModel(dependencyProvider)

        assertEquals(initialNickname, viewmodel.nickname)

        viewmodel.onStart()

        // verify all nicknames except current devices nickname is in the existingNames set.
        for (device in deviceList!!) {
            if (device.serialNumber != CURRENT_SERIAL_NUMBER) {
                assertTrue(viewmodel.existingNames!!.contains(device.nickname))
            }
        }
    }

    @Test
    fun testOnStartValidatesNickname() {
        val initialNickname = TOO_LONG_NICKNAME
        whenever(commonState.customNickname).thenReturn(initialNickname)
        val viewmodel = CustomDeviceNameViewModel(dependencyProvider)

        viewmodel.onStart()

        // verify validation error was set to the too many characters error
        assertEquals(viewmodel.validationError, CUSTOM_NAME_LENGTH_ERROR)
    }

    @Test
    fun testOnStopRemovesCommonStateListener() {
        val viewmodel = CustomDeviceNameViewModel(dependencyProvider)

        viewmodel.onStop()

        verify(commonState).removeListener(viewmodel)
    }

    @Test
    fun testValidationSucceedsIfNicknameValid() {
        val viewmodel = CustomDeviceNameViewModel(dependencyProvider)
        viewmodel.onStart()

        viewmodel.nickname = GOOD_NICKNAME
        assertNull(viewmodel.validationError)
    }

    @Test
    fun testValidationFailsIfNicknameTooLong() {
        val viewmodel = CustomDeviceNameViewModel(dependencyProvider)
        viewmodel.onStart()

        viewmodel.nickname = TOO_LONG_NICKNAME
        assertEquals(CUSTOM_NAME_LENGTH_ERROR, viewmodel.validationError)
    }

    @Test
    fun testValidationFailsIfNicknameNotAlphaNumeric() {
        val viewmodel = CustomDeviceNameViewModel(dependencyProvider)
        viewmodel.onStart()

        viewmodel.nickname = WITH_SYMBOLS_NICKNAME
        assertEquals(CUSTOM_NAME_CHARACTER_ERROR, viewmodel.validationError)
    }

    @Test
    fun testLengthErrorIsDisplayedIfNicknameHasInvalidCharactersAndIsTooLong() {
        val viewmodel = CustomDeviceNameViewModel(dependencyProvider)
        viewmodel.onStart()

        viewmodel.nickname = TOO_LONG_INVALID_NICKNAME
        assertEquals(CUSTOM_NAME_CHARACTER_ERROR, viewmodel.validationError)
    }

    @Test
    fun testErrorIsClearedIfInvalidNicknameIsCleared() {
        val viewmodel = CustomDeviceNameViewModel(dependencyProvider)
        viewmodel.onStart()

        viewmodel.nickname = WITH_SYMBOLS_NICKNAME
        assertEquals(CUSTOM_NAME_CHARACTER_ERROR, viewmodel.validationError)
        viewmodel.nickname = ""
        assertNull(viewmodel.validationError)
    }

    @Test
    fun testValidationFailsIfNicknameAlreadyExists() {
        val viewmodel = CustomDeviceNameViewModel(dependencyProvider)
        viewmodel.onStart()

        viewmodel.nickname = "Home"
        assertEquals(CUSTOM_NAME_ALREADY_EXISTS, viewmodel.validationError)
    }

    @Test
    fun testValidationSucceedsIfNicknameMatchesSpecialNameThatsNotInUse() {
        // remove the first device in the deviceList, which is the one with the "Home" nickname.
        deviceList!!.removeAt(0)

        val viewmodel = CustomDeviceNameViewModel(dependencyProvider)
        viewmodel.onStart()

        viewmodel.nickname = "Home"
        assertNull(viewmodel.validationError)
    }

    @Test
    fun testCustomDeviceNameIsNotAcceptedIfNoNickname() {
        val viewmodel = CustomDeviceNameViewModel(dependencyProvider)

        viewmodel.onStart()

        viewmodel.nickname = ""

        viewmodel.onEditorActionButton()

        verify(nameEvents, never()).onNameComplete()
    }

    @Test
    fun testCustomDeviceNameIsNotAcceptedIfValidationFails() {
        val viewmodel = CustomDeviceNameViewModel(dependencyProvider)

        viewmodel.onStart()

        viewmodel.nickname = TOO_LONG_NICKNAME

        viewmodel.onEditorActionButton()

        verify(nameEvents, never()).onNameComplete()
    }

    @Test
    fun testCustomDeviceNameIsAcceptedIfValidationSucceeds() {
        val viewmodel = CustomDeviceNameViewModel(dependencyProvider)

        viewmodel.onStart()

        viewmodel.nickname = GOOD_NICKNAME

        viewmodel.onEditorActionButton()

        verify(nameEvents).onNameComplete()
    }

    companion object {
        private val CURRENT_SERIAL_NUMBER = "1234567890"
        private val TOO_LONG_NICKNAME = "TooLong"
        private val TOO_LONG_INVALID_NICKNAME = "TooLong?"
        private val GOOD_NICKNAME = "Gooood"
        private val WITH_SYMBOLS_NICKNAME = "Sym#."

        private val CUSTOM_NAME_CHARACTER_ERROR = "custom_name_character_error"
        private val CUSTOM_NAME_LENGTH_ERROR = "custom_name_length_error"
        private val CUSTOM_NAME_ALREADY_EXISTS = "custom_name_already_exists"
    }
}