//
// SyncManagerImplTests.kt
// teva_cloud
//
// Copyright © 2018 Teva. All rights reserved.
//

package com.teva.cloud.models.sync

import android.content.SharedPreferences
import com.nhaarman.mockito_kotlin.*
import com.teva.cloud.dataentities.UserProfile
import com.teva.cloud.dataquery.UserAccountQuery
import com.teva.cloud.dataquery.UserProfileQuery
import com.teva.cloud.messages.CloudSyncCompleteMessage
import com.teva.cloud.models.CloudConstants
import com.teva.cloud.models.CloudManagerNotificationId
import com.teva.cloud.models.CloudSessionState
import com.teva.cloud.models.programmanagement.CareProgramManager
import com.teva.cloud.models.userprofile.UserProfileManager
import com.teva.cloud.services.CloudObjectContainer
import com.teva.cloud.services.sync.SyncCloudService
import com.teva.common.services.TimeService
import com.teva.utilities.services.DependencyProvider
import com.teva.common.utilities.Messenger
import com.teva.devices.dataquery.DeviceDataQuery
import com.teva.devices.dataquery.InhaleEventDataQuery
import com.teva.devices.entities.Device
import com.teva.devices.entities.InhaleEvent
import com.teva.devices.enumerations.InhalerNameType
import com.teva.devices.model.DeviceManager
import com.teva.devices.model.DeviceQuery
import com.teva.dhp.models.DHPManager
import com.teva.environment.models.DailyEnvironmentalReminderManager
import com.teva.medication.dataquery.PrescriptionDataQuery
import com.teva.medication.entities.Medication
import com.teva.medication.entities.Prescription
import com.teva.medication.enumerations.MedicationClassification
import com.teva.notifications.dataquery.ReminderDataQuery
import com.teva.notifications.entities.ReminderSetting
import com.teva.notifications.enumerations.RepeatType
import com.teva.notifications.models.NotificationManager
import com.teva.userfeedback.dataquery.DailyUserFeelingDataQuery
import com.teva.userfeedback.entities.DailyUserFeeling
import com.teva.userfeedback.enumerations.UserFeeling
import com.teva.userfeedback.model.DailyAssessmentReminderManager
import junit.framework.Assert.assertEquals
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.eq
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime


/**
 * This class defines unit tests for the SyncManagerImpl class.
 */
class SyncManagerImplTests {
    private var dependencyProvider: DependencyProvider = DependencyProvider.default

    private val prescriptionDataQuery: PrescriptionDataQuery = mock()
    private val deviceDataQuery: DeviceDataQuery = mock()
    private val dailyUserFeelingDataQuery: DailyUserFeelingDataQuery = mock()
    private val reminderDataQuery: ReminderDataQuery = mock()
    private val inhaleEventDataQuery: InhaleEventDataQuery = mock()
    private val userProfileDataQuery: UserProfileQuery = mock()
    private val userProfileManager: UserProfileManager = mock()
    private val sharedPreferences: SharedPreferences = mock()
    private val sharedPreferencesEditor: SharedPreferences.Editor = mock()
    private val messenger: Messenger = mock()
    private val dhpManager: DHPManager = mock()
    private val userAccountQuery: UserAccountQuery = mock()

    private val timeService: TimeService = mock()

    private val prescriptions = ArrayList<Prescription>()
    private val devices = ArrayList<Device>()
    private val inhaleEvents = ArrayList<InhaleEvent>()
    private val reminders = ArrayList<ReminderSetting>()
    private val dailyUserFeelings = ArrayList<DailyUserFeeling>()
    private val profiles = ArrayList<UserProfile>()

    private val syncCloudService: SyncCloudService = mock()


    @Before
    @Throws(Exception::class)
    fun setup() {
        dependencyProvider.register(PrescriptionDataQuery::class, prescriptionDataQuery)
        dependencyProvider.register(DeviceDataQuery::class, deviceDataQuery)
        dependencyProvider.register(InhaleEventDataQuery::class, inhaleEventDataQuery)
        dependencyProvider.register(ReminderDataQuery::class, reminderDataQuery)
        dependencyProvider.register(DailyUserFeelingDataQuery::class, dailyUserFeelingDataQuery)
        dependencyProvider.register(UserProfileQuery::class, userProfileDataQuery)
        dependencyProvider.register(UserProfileManager::class, userProfileManager)

        whenever(sharedPreferences.edit()).thenReturn(sharedPreferencesEditor)
        dependencyProvider.register(SharedPreferences::class, sharedPreferences)

        dependencyProvider.register(Messenger::class, messenger)
        dependencyProvider.register(DHPManager::class, dhpManager)
        dependencyProvider.register(UserAccountQuery::class, userAccountQuery)

        whenever(timeService.now()).thenReturn(Instant.ofEpochMilli(1514903638316))
        dependencyProvider.register(TimeService::class, timeService)
    }

    @After
    @Throws(Exception::class)
    fun tearDown() {
        DependencyProvider.default.unregisterAll()
        prescriptions.clear()
        devices.clear()
        inhaleEvents.clear()
        reminders.clear()
        dailyUserFeelings.clear()
    }

    @Test
    fun testSyncMethodInvokesSyncServiceUploadWithChangedData() {
        // arrange
        createUploadData()

        whenever(prescriptionDataQuery.getAllChanged()).thenReturn(prescriptions)
        whenever(deviceDataQuery.getAllChanged()).thenReturn(devices)
        whenever(inhaleEventDataQuery.getAllChanged()).thenReturn(inhaleEvents)
        whenever(reminderDataQuery.getAll()).thenReturn(reminders)
        whenever(dailyUserFeelingDataQuery.getAllChanged()).thenReturn(dailyUserFeelings)
        whenever(userProfileManager.getAllChangedProfiles()).thenReturn(profiles)

        CloudSessionState.shared.serverTimeOffset = 2
        CloudSessionState.shared.serverTime = Instant.ofEpochMilli(1514903636316)

        val cloudObjectContainerArgumentCaptor = argumentCaptor<CloudObjectContainer>()
        whenever(syncCloudService.isFirstSync).thenReturn(false)

        val careProgramManager: CareProgramManager = mock()
        dependencyProvider.register(CareProgramManager::class, careProgramManager)

        // act
        val syncManager = SyncManagerImpl()
        val clazz = syncManager.javaClass
        val syncServiceField = clazz.getDeclaredField("syncService")
        syncServiceField.isAccessible = true
        syncServiceField.set(syncManager, syncCloudService)
        syncManager.sync()

        // assert
        verify(syncCloudService).uploadAsync(cloudObjectContainerArgumentCaptor.capture())

        val uploadData = cloudObjectContainerArgumentCaptor.firstValue

        assertEquals(prescriptions.size,  uploadData.prescriptions.size)
        for(i in 0 until prescriptions.size) {
            assertEquals(prescriptions[i], uploadData.prescriptions[i])
        }

        assertEquals(devices.size,  uploadData.devices.size)
        for(i in 0 until devices.size) {
            assertEquals(devices[i], uploadData.devices[i])
        }

        assertEquals(inhaleEvents.size,  uploadData.inhaleEvents.size)
        for(i in 0 until inhaleEvents.size) {
            assertEquals(inhaleEvents[i], uploadData.inhaleEvents[i])
        }

        assertEquals(reminders.size,  uploadData.settings.size)
        for(i in 0 until reminders.size) {
            assertEquals(reminders[i], uploadData.settings[i])
        }

        assertEquals(dailyUserFeelings.size,  uploadData.dsas.size)
        for(i in 0 until dailyUserFeelings.size) {
            assertEquals(dailyUserFeelings[i], uploadData.dsas[i])
        }

        assertEquals(profiles.size,  uploadData.profiles.size)
        for(i in 0 until profiles.size) {
            assertEquals(profiles[i], uploadData.profiles[i])
        }
    }

    @Test
    fun testSyncManagerContinuesUploadingIfMoreDataToUploadExistsAfterSuccessfulUpload() {
        // arrange
        createUploadData()

        whenever(prescriptionDataQuery.getAllChanged()).thenReturn(prescriptions)
        whenever(deviceDataQuery.getAllChanged()).thenReturn(devices)
        whenever(inhaleEventDataQuery.getAllChanged()).thenReturn(inhaleEvents)
        whenever(reminderDataQuery.getAll()).thenReturn(reminders)
        whenever(dailyUserFeelingDataQuery.getAllChanged()).thenReturn(dailyUserFeelings)
        whenever(userProfileManager.getAllChangedProfiles()).thenReturn(profiles)

        CloudSessionState.shared.serverTimeOffset = 2
        CloudSessionState.shared.serverTime = Instant.ofEpochMilli(1514903636316)

        val cloudObjectContainerArgumentCaptor = argumentCaptor<CloudObjectContainer>()
        whenever(syncCloudService.isFirstSync).thenReturn(false)

        val careProgramManager: CareProgramManager = mock()
        dependencyProvider.register(CareProgramManager::class, careProgramManager)

        // act
        val syncManager = SyncManagerImpl()
        val clazz = syncManager.javaClass
        val syncServiceField = clazz.getDeclaredField("syncService")
        syncServiceField.isAccessible = true
        syncServiceField.set(syncManager, syncCloudService)
        val syncStartTimeField = clazz.getDeclaredField("syncStartTime")
        syncStartTimeField.isAccessible = true
        syncStartTimeField.set(syncManager, Instant.ofEpochMilli(1514903637316))
        syncManager.uploadCompleted(true)

        // assert
        verify(syncCloudService).uploadAsync(cloudObjectContainerArgumentCaptor.capture())

        val uploadData = cloudObjectContainerArgumentCaptor.firstValue

        assertEquals(prescriptions.size,  uploadData.prescriptions.size)
        for(i in 0 until prescriptions.size) {
            assertEquals(prescriptions[i], uploadData.prescriptions[i])
        }

        assertEquals(devices.size,  uploadData.devices.size)
        for(i in 0 until devices.size) {
            assertEquals(devices[i], uploadData.devices[i])
        }

        assertEquals(inhaleEvents.size,  uploadData.inhaleEvents.size)
        for(i in 0 until inhaleEvents.size) {
            assertEquals(inhaleEvents[i], uploadData.inhaleEvents[i])
        }

        assertEquals(reminders.size,  uploadData.settings.size)
        for(i in 0 until reminders.size) {
            assertEquals(reminders[i], uploadData.settings[i])
        }

        assertEquals(dailyUserFeelings.size,  uploadData.dsas.size)
        for(i in 0 until dailyUserFeelings.size) {
            assertEquals(dailyUserFeelings[i], uploadData.dsas[i])
        }

        assertEquals(profiles.size,  uploadData.profiles.size)
        for(i in 0 until profiles.size) {
            assertEquals(profiles[i], uploadData.profiles[i])
        }
    }

    @Test
    fun testSyncManagerResetsChangedFlagOfUploadedDataIfUploadFails() {
        // arrange
        createUploadData()

        whenever(prescriptionDataQuery.getAllChanged()).thenReturn(prescriptions)
        whenever(deviceDataQuery.getAllChanged()).thenReturn(devices)
        whenever(inhaleEventDataQuery.getAllChanged()).thenReturn(inhaleEvents)
        whenever(reminderDataQuery.getAll()).thenReturn(reminders)
        whenever(dailyUserFeelingDataQuery.getAllChanged()).thenReturn(dailyUserFeelings)
        whenever(userProfileManager.getAllChangedProfiles()).thenReturn(profiles)

        CloudSessionState.shared.serverTimeOffset = 2
        CloudSessionState.shared.serverTime = Instant.ofEpochMilli(1514903636316)
        whenever(sharedPreferences.getLong(eq(CloudConstants.lastSuccessfulSyncDateKey), any())).thenReturn(1514903606)
        whenever(sharedPreferences.getLong(eq(CloudConstants.lastFailedSyncDateKey), any())).thenReturn(1514903616)


        whenever(syncCloudService.isFirstSync).thenReturn(false)

        val careProgramManager: CareProgramManager = mock()
        dependencyProvider.register(CareProgramManager::class, careProgramManager)

        // act
        val syncManager = SyncManagerImpl()
        val clazz = syncManager.javaClass
        val syncServiceField = clazz.getDeclaredField("syncService")
        syncServiceField.isAccessible = true
        syncServiceField.set(syncManager, syncCloudService)
        syncManager.sync()
        syncManager.uploadCompleted(false)

        // assert
        for(i in 0 until prescriptions.size) {
            verify(prescriptionDataQuery).resetChangedFlag(prescriptions[i], true)
        }

        for(i in 0 until devices.size) {
            verify(deviceDataQuery).resetChangedFlag(devices[i], true)
        }

        for(i in 0 until inhaleEvents.size) {
            verify(inhaleEventDataQuery).resetChangedFlag(inhaleEvents[i], true)
        }

        for(i in 0 until reminders.size) {
            verify(reminderDataQuery).resetChangedFlag(reminders[i], true)
        }

        for(i in 0 until dailyUserFeelings.size) {
            verify(dailyUserFeelingDataQuery).resetChangedFlag(dailyUserFeelings[i], true)
        }

        for(i in 0 until profiles.size) {
            verify(userProfileManager).update(profiles[i], true)
        }
    }

    @Test
    fun testSyncMethodStartsDownloadingDevicesAndPrescriptionsIfTheyHaveNotBeenDownloadedAndThereIsNoDataToUpload() {
        // arrange
        whenever(prescriptionDataQuery.getAllChanged()).thenReturn(prescriptions)
        whenever(deviceDataQuery.getAllChanged()).thenReturn(devices)
        whenever(inhaleEventDataQuery.getAllChanged()).thenReturn(inhaleEvents)
        whenever(reminderDataQuery.getAll()).thenReturn(reminders)
        whenever(dailyUserFeelingDataQuery.getAllChanged()).thenReturn(dailyUserFeelings)
        whenever(userProfileManager.getAllChangedProfiles()).thenReturn(profiles)

        CloudSessionState.shared.serverTimeOffset = 2
        CloudSessionState.shared.serverTime = Instant.ofEpochMilli(1514903636316)

        whenever(syncCloudService.isFirstSync).thenReturn(true)

        val careProgramManager: CareProgramManager = mock()
        dependencyProvider.register(CareProgramManager::class, careProgramManager)

        // act
        val syncManager = SyncManagerImpl()
        val clazz = syncManager.javaClass
        val syncServiceField = clazz.getDeclaredField("syncService")
        syncServiceField.isAccessible = true
        syncServiceField.set(syncManager, syncCloudService)
        syncManager.sync()

        // assert
        verify(syncCloudService).downloadPrescriptionsAndDevicesAsync()
    }

    @Test
    fun testSyncMethodStartsDownloadingDataIfThereIsNoDataToUploadAndDevicesAndPrescriptionsHaveBeenDownloaded() {
        // arrange
        whenever(prescriptionDataQuery.getAllChanged()).thenReturn(prescriptions)
        whenever(deviceDataQuery.getAllChanged()).thenReturn(devices)
        whenever(inhaleEventDataQuery.getAllChanged()).thenReturn(inhaleEvents)
        whenever(reminderDataQuery.getAll()).thenReturn(reminders)
        whenever(dailyUserFeelingDataQuery.getAllChanged()).thenReturn(dailyUserFeelings)
        whenever(userProfileManager.getAllChangedProfiles()).thenReturn(profiles)

        CloudSessionState.shared.serverTimeOffset = 2
        CloudSessionState.shared.serverTime = Instant.ofEpochMilli(1514903636316)

        whenever(syncCloudService.isFirstSync).thenReturn(false)

        val careProgramManager: CareProgramManager = mock()
        dependencyProvider.register(CareProgramManager::class, careProgramManager)

        // act
        val syncManager = SyncManagerImpl()
        val clazz = syncManager.javaClass
        val syncServiceField = clazz.getDeclaredField("syncService")
        syncServiceField.isAccessible = true
        syncServiceField.set(syncManager, syncCloudService)
        val prescriptionsAndDevicesDownloadedField = clazz.getDeclaredField("hasDownloadedPrescriptionsAndDevices")
        prescriptionsAndDevicesDownloadedField.isAccessible = true
        prescriptionsAndDevicesDownloadedField.set(syncManager, true)
        syncManager.sync()

        // assert
        verify(syncCloudService).downloadAsync()
    }

    @Test
    fun testSyncManagerInsertsDownloadedDataIfItDoesNotCurrentlyExistInTheDatabase() {
        // arrange
        val downloadData = createDownloadData()
        val existingDevice: Device = mock()
        val deviceQuery: DeviceQuery = mock()
        dependencyProvider.register(DeviceQuery::class, deviceQuery)
        val deviceManager: DeviceManager = mock()
        dependencyProvider.register(DeviceManager::class, deviceManager)
        val dailyAssessmentReminderManager: DailyAssessmentReminderManager = mock()
        dependencyProvider.register(DailyAssessmentReminderManager::class, dailyAssessmentReminderManager)
        val dailyEnvironmentalReminderManager: DailyEnvironmentalReminderManager = mock()
        dependencyProvider.register(DailyEnvironmentalReminderManager::class, dailyEnvironmentalReminderManager)
        val careProgramManager: CareProgramManager = mock()
        dependencyProvider.register(CareProgramManager::class, careProgramManager)


        whenever(prescriptionDataQuery.getAll()).thenReturn(prescriptions)
        whenever(deviceQuery.get("123456789")).thenReturn(null)
        whenever(deviceDataQuery.get("987654321")).thenReturn(existingDevice)
        whenever(inhaleEventDataQuery.get(any(), any<Device>())).thenReturn(null)
        whenever(reminderDataQuery.get(any())).thenReturn(null)
        whenever(dailyUserFeelingDataQuery.get(any())).thenReturn(null)

        // act
        val syncManager = SyncManagerImpl()
        val clazz = syncManager.javaClass
        val syncServiceField = clazz.getDeclaredField("syncService")
        syncServiceField.isAccessible = true
        syncServiceField.set(syncManager, syncCloudService)
        syncManager.downloadCompleted(true, downloadData, false, null)

        // assert
        for(i in 0 until downloadData.prescriptions.size) {
            verify(prescriptionDataQuery).insert(downloadData.prescriptions[i], false)
        }
        for(i in 0 until downloadData.dsas.size) {
            verify(dailyUserFeelingDataQuery).insert(downloadData.dsas[i], false)
        }
        for(i in 0 until downloadData.devices.size) {
            verify(deviceQuery).insert(downloadData.devices[i], false)
        }
        for(i in 0 until downloadData.inhaleEvents.size) {
            verify(inhaleEventDataQuery).insert(downloadData.inhaleEvents[i], false)
        }
    }

    @Test
    fun testSyncManagerUpdatesTheDatabaseIfDownloadedDataCurrentlyExistsInTheDatabase() {
        // arrange
        createExistingData()
        val downloadData = createDownloadData()
        val existingDevice: Device = mock()
        val deviceQuery: DeviceQuery = mock()
        val existingAccountOwner: UserProfile = mock()

        whenever(existingAccountOwner.isAccountOwner).thenReturn(true)
        whenever(existingAccountOwner.dateOfBirth).thenReturn(LocalDate.of(1970, 2, 2))

        dependencyProvider.register(DeviceQuery::class, deviceQuery)
        val deviceManager: DeviceManager = mock()
        dependencyProvider.register(DeviceManager::class, deviceManager)
        val dailyAssessmentReminderManager: DailyAssessmentReminderManager = mock()
        dependencyProvider.register(DailyAssessmentReminderManager::class, dailyAssessmentReminderManager)
        val dailyEnvironmentalReminderManager: DailyEnvironmentalReminderManager = mock()
        dependencyProvider.register(DailyEnvironmentalReminderManager::class, dailyEnvironmentalReminderManager)
        val careProgramManager: CareProgramManager = mock()
        dependencyProvider.register(CareProgramManager::class, careProgramManager)


        whenever(prescriptionDataQuery.getAll()).thenReturn(prescriptions)
        whenever(deviceQuery.get("123456789")).thenReturn(devices[0])
        whenever(deviceDataQuery.get("987654321")).thenReturn(existingDevice)
        whenever(inhaleEventDataQuery.get(any(), any<Device>())).thenReturn(inhaleEvents[0])
        whenever(reminderDataQuery.get("VAS_REMINDER")).thenReturn(reminders[0])
        whenever(reminderDataQuery.get("DailyEnvironmentalReminder")).thenReturn(reminders[1])
        whenever(dailyUserFeelingDataQuery.get(any())).thenReturn(dailyUserFeelings[0])
        whenever(userProfileManager.getAccountOwner()).thenReturn(existingAccountOwner)

        // act
        val syncManager = SyncManagerImpl()
        val clazz = syncManager.javaClass
        val syncServiceField = clazz.getDeclaredField("syncService")
        syncServiceField.isAccessible = true
        syncServiceField.set(syncManager, syncCloudService)
        syncManager.downloadCompleted(true, downloadData, false, null)

        // assert
        for(i in 0 until downloadData.prescriptions.size) {
            verify(prescriptionDataQuery).update(downloadData.prescriptions[i], false)
        }
        for(i in 0 until downloadData.dsas.size) {
            verify(dailyUserFeelingDataQuery).update(downloadData.dsas[i], false)
        }
        for(i in 0 until downloadData.devices.size) {
            verify(deviceQuery).update(downloadData.devices[i], false)
        }
        for(i in 0 until downloadData.inhaleEvents.size) {
            verify(inhaleEventDataQuery).update(downloadData.inhaleEvents[i], false)
        }
        verify(dailyAssessmentReminderManager).enableReminder(downloadData.settings[0].isEnabled)
        verify(dailyEnvironmentalReminderManager).enableReminder(downloadData.settings[1].isEnabled)

        verify(userProfileManager).update(existingAccountOwner, false)
    }

    @Test
    fun testSyncManagerTriggersAnotherDownloadIfMoreDataToDownloadExistsAfterFirstDownloadIsComplete() {
        // arrange
        val downloadData = createDownloadData()
        val existingDevice: Device = mock()
        val deviceQuery: DeviceQuery = mock()
        dependencyProvider.register(DeviceQuery::class, deviceQuery)
        val deviceManager: DeviceManager = mock()
        dependencyProvider.register(DeviceManager::class, deviceManager)
        val dailyAssessmentReminderManager: DailyAssessmentReminderManager = mock()
        dependencyProvider.register(DailyAssessmentReminderManager::class, dailyAssessmentReminderManager)
        val dailyEnvironmentalReminderManager: DailyEnvironmentalReminderManager = mock()
        dependencyProvider.register(DailyEnvironmentalReminderManager::class, dailyEnvironmentalReminderManager)
        val careProgramManager: CareProgramManager = mock()
        dependencyProvider.register(CareProgramManager::class, careProgramManager)

        whenever(prescriptionDataQuery.getAll()).thenReturn(prescriptions)
        whenever(deviceQuery.get("123456789")).thenReturn(null)
        whenever(deviceDataQuery.get("987654321")).thenReturn(existingDevice)
        whenever(inhaleEventDataQuery.get(any(), any<Device>())).thenReturn(null)
        whenever(reminderDataQuery.get(any())).thenReturn(null)
        whenever(dailyUserFeelingDataQuery.get(any())).thenReturn(null)

        // act
        val syncManager = SyncManagerImpl()
        val clazz = syncManager.javaClass
        val syncServiceField = clazz.getDeclaredField("syncService")
        syncServiceField.isAccessible = true
        syncServiceField.set(syncManager, syncCloudService)
        syncManager.downloadCompleted(true, downloadData, true, null)

        // assert
        verify(syncCloudService).downloadAsync()
    }

    @Test
    fun testSyncManagerRaisesAnEventIfThereWasNoSyncFor14Days() {
        // arrange
        createUploadData()
        val notificationManager: NotificationManager = mock()
        dependencyProvider.register(NotificationManager::class, notificationManager)

        val careProgramManager: CareProgramManager = mock()
        dependencyProvider.register(CareProgramManager::class, careProgramManager)

        whenever(prescriptionDataQuery.getAllChanged()).thenReturn(prescriptions)
        whenever(deviceDataQuery.getAllChanged()).thenReturn(devices)
        whenever(inhaleEventDataQuery.getAllChanged()).thenReturn(inhaleEvents)
        whenever(reminderDataQuery.getAll()).thenReturn(reminders)
        whenever(dailyUserFeelingDataQuery.getAllChanged()).thenReturn(dailyUserFeelings)
        whenever(userProfileManager.getAllChangedProfiles()).thenReturn(profiles)

        CloudSessionState.shared.serverTimeOffset = 2
        CloudSessionState.shared.serverTime = Instant.ofEpochMilli(1514903636316)
        whenever(sharedPreferences.getLong(eq(CloudConstants.lastSuccessfulSyncDateKey), any())).thenReturn(1513607606)
        whenever(sharedPreferences.getLong(eq(CloudConstants.lastFailedSyncDateKey), any())).thenReturn(1513607616)

        whenever(syncCloudService.isFirstSync).thenReturn(false)
        val categoryIDArgumentCaptor = argumentCaptor<String>()

        // act
        val syncManager = SyncManagerImpl()
        val clazz = syncManager.javaClass
        val syncServiceField = clazz.getDeclaredField("syncService")
        syncServiceField.isAccessible = true
        syncServiceField.set(syncManager, syncCloudService)
        syncManager.sync()
        syncManager.uploadCompleted(false)

        // assert
        verify(notificationManager).setNotification(categoryIDArgumentCaptor.capture(), any())
        assertEquals(CloudManagerNotificationId.NO_CLOUD_SYNC_FOR_14_DAYS, categoryIDArgumentCaptor.firstValue)
    }

    @Test
    fun testSyncManagerTriggersCloudSyncCompleteMessageIfThereIsNoDataToUploadOrDownload() {
        // arrange
        whenever(prescriptionDataQuery.getAllChanged()).thenReturn(prescriptions)
        whenever(deviceDataQuery.getAllChanged()).thenReturn(devices)
        whenever(inhaleEventDataQuery.getAllChanged()).thenReturn(inhaleEvents)
        whenever(reminderDataQuery.getAll()).thenReturn(reminders)
        whenever(dailyUserFeelingDataQuery.getAllChanged()).thenReturn(dailyUserFeelings)
        whenever(userProfileManager.getAllChangedProfiles()).thenReturn(profiles)

        CloudSessionState.shared.serverTimeOffset = 2
        CloudSessionState.shared.serverTime = Instant.ofEpochMilli(1514903636316)

        val careProgramManager: CareProgramManager = mock()
        dependencyProvider.register(CareProgramManager::class, careProgramManager)
        // act
        val syncManager = SyncManagerImpl()
        val clazz = syncManager.javaClass
        val syncServiceField = clazz.getDeclaredField("syncService")
        syncServiceField.isAccessible = true
        syncServiceField.set(syncManager, syncCloudService)
        val prescriptionsAndDevicesDownloadedField = clazz.getDeclaredField("hasDownloadedPrescriptionsAndDevices")
        prescriptionsAndDevicesDownloadedField.isAccessible = true
        prescriptionsAndDevicesDownloadedField.set(syncManager, true)
        val downloadedField = clazz.getDeclaredField("hasDownloaded")
        downloadedField.isAccessible = true
        downloadedField.set(syncManager, true)
        syncManager.sync()

        // assert
        verify(messenger).post(any<CloudSyncCompleteMessage>())
    }


    private fun createUploadData() {
        val medication = Medication("745750", "Brand", "Name", MedicationClassification.RELIEVER, 4, 180, 180, 200, 12)
        val prescription = Prescription(2, 1, Instant.ofEpochMilli(1514903538316), medication)
        prescription.changeTime = Instant.ofEpochMilli(1514903538316)
        prescription.hasChanged = true
        prescriptions.add(prescription)
        val device = Device("123456789", "1111111111", medication, "Home", "Teva", "1.0", "1.0", "", "", LocalDate.of(2019, 1, 1), 1, Instant.ofEpochMilli(1514903538316), InhalerNameType.HOME, true, true, null)
        device.changeTime = Instant.ofEpochMilli(1514903538316)
        device.hasChanged = true
        devices.add(device)
        val inhaleEvent = InhaleEvent(10, 10, 2, 3, 100, 1, true)
        inhaleEvent.changeTime = Instant.ofEpochMilli(1514903538316)
        inhaleEvent.hasChanged = true
        inhaleEvents.add(inhaleEvent)
        val dailyUserFeeling = DailyUserFeeling(Instant.ofEpochMilli(1514903538316), UserFeeling.GOOD)
        dailyUserFeeling.changeTime = Instant.ofEpochMilli(1514903538316)
        dailyUserFeeling.hasChanged = true
        dailyUserFeelings.add(dailyUserFeeling)
        val reminder = ReminderSetting(true, "VAS_REMINDER", RepeatType.ONCE_PER_DAY, LocalTime.of(8, 0))
        reminder.serverTimeOffset = 0
        reminder.changeTime = Instant.ofEpochMilli(1514903538316)
        reminder.hasChanged = true
        reminders.add(reminder)
        val userProfile =UserProfile("FED-1234", "John", "Doe", true, true, true, LocalDate.of(1970, 1, 1), Instant.ofEpochMilli(1514903538316))
        userProfile.changeTime = Instant.ofEpochMilli(1514903538316)
        userProfile.hasChanged = true
        profiles.add(userProfile)
    }

    private fun createDownloadData(): CloudObjectContainer {
        val downloadData = CloudObjectContainer()

        val medication = Medication("745750", "Brand", "Name", MedicationClassification.RELIEVER, 4, 180, 180, 200, 12)
        val prescription = Prescription(2, 1, Instant.ofEpochMilli(1514903538316), medication)
        prescription.changeTime = Instant.ofEpochMilli(1514903538316)
        prescription.hasChanged = true
        downloadData.prescriptions.add(prescription)
        val device = Device("123456789", "1111111111", medication, "Home", "Teva", "1.0", "1.0", "", "", LocalDate.of(2019, 1, 1), 1, Instant.ofEpochMilli(1514903538316), InhalerNameType.HOME, true, true, null)
        device.changeTime = Instant.ofEpochMilli(1514903538316)
        device.hasChanged = true
        downloadData.devices.add(device)
        val inhaleEvent = InhaleEvent(10, 10, 2, 3, 100, 1, true)
        inhaleEvent.deviceSerialNumber = "987654321"
        inhaleEvent.changeTime = Instant.ofEpochMilli(1514903538316)
        inhaleEvent.eventTime = Instant.ofEpochMilli(1514903538316)
        inhaleEvent.hasChanged = true
        downloadData.inhaleEvents.add(inhaleEvent)
        val dailyUserFeeling = DailyUserFeeling(Instant.ofEpochMilli(1514903538316), UserFeeling.GOOD)
        dailyUserFeeling.changeTime = Instant.ofEpochMilli(1514903538316)
        dailyUserFeeling.hasChanged = true
        downloadData.dsas.add(dailyUserFeeling)
        val reminder = ReminderSetting(true, "VAS_REMINDER", RepeatType.ONCE_PER_DAY, LocalTime.of(8, 0))
        reminder.serverTimeOffset = 0
        reminder.changeTime = Instant.ofEpochMilli(1514903538316)
        reminder.hasChanged = true
        downloadData.settings.add(reminder)
        val reminder2 = ReminderSetting(true, "DailyEnvironmentalReminder", RepeatType.ONCE_PER_DAY, LocalTime.of(8, 30))
        reminder2.serverTimeOffset = 0
        reminder2.changeTime = Instant.ofEpochMilli(1514903538316)
        reminder2.hasChanged = true
        downloadData.settings.add(reminder2)
        val userProfile =UserProfile("FED-1234", "John", "Doe", true, true, true, LocalDate.of(1970, 1, 1), Instant.ofEpochMilli(1514903538316))
        userProfile.changeTime = Instant.ofEpochMilli(1514903538316)
        userProfile.hasChanged = true
        downloadData.profiles.add(userProfile)

        return downloadData
    }

    private fun createExistingData(): CloudObjectContainer {
        val downloadData = CloudObjectContainer()

        val medication = Medication("745750", "Brand", "Name", MedicationClassification.RELIEVER, 4, 180, 180, 200, 12)
        val prescription = Prescription(2, 1, Instant.ofEpochMilli(1514903538316), medication)
        prescription.changeTime = Instant.ofEpochMilli(1514903530316)
        prescription.hasChanged = true
        prescriptions.add(prescription)
        val device = Device("123456789", "1111111111", medication, "Home", "Teva", "1.0", "1.0", "", "", LocalDate.of(2019, 1, 1), 1, Instant.ofEpochMilli(1514903538316), InhalerNameType.HOME, true, true, null)
        device.changeTime = Instant.ofEpochMilli(1514903530316)
        device.hasChanged = true
        devices.add(device)
        val inhaleEvent = InhaleEvent(10, 10, 2, 3, 100, 1, true)
        inhaleEvent.deviceSerialNumber = "987654321"
        inhaleEvent.changeTime = Instant.ofEpochMilli(1514903530316)
        inhaleEvent.eventTime = Instant.ofEpochMilli(1514903530316)
        inhaleEvent.hasChanged = true
        inhaleEvents.add(inhaleEvent)
        val dailyUserFeeling = DailyUserFeeling(Instant.ofEpochMilli(1514903538316), UserFeeling.GOOD)
        dailyUserFeeling.changeTime = Instant.ofEpochMilli(1514903530316)
        dailyUserFeeling.hasChanged = true
        dailyUserFeelings.add(dailyUserFeeling)
        val reminder1 = ReminderSetting(true, "VAS_REMINDER", RepeatType.ONCE_PER_DAY, LocalTime.of(8, 0))
        reminder1.serverTimeOffset = 0
        reminder1.changeTime = Instant.ofEpochMilli(1514903530316)
        reminder1.hasChanged = true
        reminders.add(reminder1)
        val reminder2 = ReminderSetting(true, "DailyEnvironmentalReminder", RepeatType.ONCE_PER_DAY, LocalTime.of(8, 30))
        reminder2.serverTimeOffset = 0
        reminder2.changeTime = Instant.ofEpochMilli(1514903530316)
        reminder2.hasChanged = true
        reminders.add(reminder2)
        val userProfile =UserProfile("FED-1234", "John", "Doe", true, true, true, LocalDate.of(1970, 1, 1), Instant.ofEpochMilli(1514903538316))
        userProfile.changeTime = Instant.ofEpochMilli(1514903530316)
        userProfile.hasChanged = true
        profiles.add(userProfile)

        return downloadData
    }
}