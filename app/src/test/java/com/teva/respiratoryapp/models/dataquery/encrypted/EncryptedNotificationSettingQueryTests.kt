package com.teva.respiratoryapp.models.dataquery.encrypted

import com.nhaarman.mockito_kotlin.*
import com.teva.common.services.TimeService
import com.teva.utilities.services.DependencyProvider
import com.teva.common.utilities.Messenger
import com.teva.notifications.entities.ReminderSetting
import com.teva.notifications.enumerations.RepeatType
import com.teva.respiratoryapp.services.data.DataService
import com.teva.respiratoryapp.services.data.QueryInfo
import com.teva.respiratoryapp.services.data.SearchCriteria
import com.teva.respiratoryapp.services.data.encrypteddata.entities.NotificationSettingDataEncrypted
import com.teva.respiratoryapp.testutils.BaseTest
import com.teva.respiratoryapp.testutils.Entity
import com.teva.respiratoryapp.testutils.Model
import com.teva.respiratoryapp.testutils.ModelMatcher.matchesReminderSetting
import com.teva.respiratoryapp.testutils.ModelMatcher.matchesReminderSettingList
import com.teva.respiratoryapp.testutils.matches
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.threeten.bp.Instant
import java.util.*

/**
 * This class defines the unit tests for the EncryptedNotificationSettingQuery class.
 */
class EncryptedNotificationSettingQueryTests : BaseTest() {

    private lateinit var dependencyProvider: DependencyProvider
    private lateinit var dataService: DataService
    private lateinit var messenger: Messenger
    private lateinit var timeService: TimeService

    /**
     * This method sets up the mocks for the classes and methods needed for the test execution.
     */
    @Before
    fun setup() {
        dependencyProvider = DependencyProvider.default
        dataService = mock()
        messenger = mock()
        timeService = mock()

        val reminderName = "Reminder1"
        dataService = mock()
        whenever(dataService.create(NotificationSettingDataEncrypted::class.java)).thenAnswer {
            NotificationSettingDataEncrypted()
        }


        messenger = mock()
        timeService = mock()

        //dependencyProvider = new DependencyProvider();
        dependencyProvider = DependencyProvider.default
        dependencyProvider.register(Messenger::class, messenger)
        dependencyProvider.register(DataService::class, dataService)
        dependencyProvider.register(TimeService::class, timeService)
        dependencyProvider.register(EncryptedNotificationSettingMapper())

        whenever(timeService.now()).thenAnswer {
            Instant.ofEpochSecond(1491226442L)
        }

        val notificationSettings = ArrayList<NotificationSettingDataEncrypted>()
        notificationSettings.add(Entity.NotificationSettingDataEncrypted("Reminder1", 1, 1, 32400, true))
        notificationSettings.add(Entity.NotificationSettingDataEncrypted("Reminder2", 1, 1, 36000, true))

        val queryInfo1 = QueryInfo(SearchCriteria("name = %@", reminderName))
        val queryInfo2 = QueryInfo(SearchCriteria("enabled = %@", 1))

        val searchCriteria = SearchCriteria("name = %@", reminderName)
        whenever(dataService.fetchRequest(eq(NotificationSettingDataEncrypted::class.java), argThat{matches(queryInfo1)})).thenAnswer {
            notificationSettings
        }
        whenever(dataService.fetchRequest(eq(NotificationSettingDataEncrypted::class.java), argThat{matches(queryInfo2)})).thenAnswer {
            notificationSettings
        }

        whenever(dataService.getCount(eq(NotificationSettingDataEncrypted::class.java), argThat{matches(searchCriteria)})).thenReturn(1)
    }

    @Test
    fun testInsertOneNotificationSettingIntoDatabase() {
        // create expectations
        val expectedEntities = ArrayList<NotificationSettingDataEncrypted>()
        expectedEntities.add(Entity.NotificationSettingDataEncrypted("Reminder1", 1, 1, 32400, true))

        val expectedSearchCriteria = ArrayList<SearchCriteria>()
        expectedSearchCriteria.add(SearchCriteria("name = %@", expectedEntities[0].name))

        // perform operation
        val query = EncryptedNotificationSettingQuery(dependencyProvider)

        val reminder = Model.ReminderSetting("Reminder1", true, RepeatType.ONCE_PER_DAY, 32400, true)

        query.insert(reminder)

        // test expectations
        verify(dataService).save(eq(NotificationSettingDataEncrypted::class.java), argThat{matches(expectedEntities)}, argThat{matches(expectedSearchCriteria)})
    }

    @Test
    fun testGetNotificationSettingByNameFromDatabase() {
        // Initialize test data.
        val reminderName = "Reminder1"

        // create expectations
        val expectedReminder = Model.ReminderSetting("Reminder1", true, RepeatType.ONCE_PER_DAY, 32400, true)
        val expectedSearchCriteria = SearchCriteria("name = %@", reminderName)

        // perform operation
        val query = EncryptedNotificationSettingQuery(dependencyProvider)
        val returnedReminder = query.get(reminderName)

        // test expectations
        val queryInfo1 = QueryInfo(expectedSearchCriteria)
        verify(dataService).fetchRequest(eq(NotificationSettingDataEncrypted::class.java), argThat{matches(queryInfo1)})
        assertThat<ReminderSetting>(returnedReminder, matchesReminderSetting(expectedReminder))
    }

    @Test
    fun testQueryNotificationSettingWithInvalidNameFromDatabase() {
        // Initialize test data.
        val reminderName = "NonExistentReminder"

        // create expectations
        val expectedSearchCriteria = SearchCriteria("name = %@", reminderName)

        // perform operation
        val query = EncryptedNotificationSettingQuery(dependencyProvider)
        val returnedReminder = query.get(reminderName)

        // test expectations
        val queryInfo1 = QueryInfo(expectedSearchCriteria)
        verify(dataService).fetchRequest(eq(NotificationSettingDataEncrypted::class.java), argThat{matches(queryInfo1)})
        assertNull(returnedReminder)
    }

    @Test
    fun testGetEnabledNotificationSettingsFromDatabase() {

        // create expectations
        val expectedReminders = ArrayList<ReminderSetting>()
        expectedReminders.add(Model.ReminderSetting("Reminder1", true, RepeatType.ONCE_PER_DAY, 32400, true))
        expectedReminders.add(Model.ReminderSetting("Reminder2", true, RepeatType.ONCE_PER_DAY, 36000, true))

        val expectedSearchCriteria = SearchCriteria("enabled = %@", 1)

        // perform operation
        val query = EncryptedNotificationSettingQuery(dependencyProvider)
        val returnedReminders = query.allEnabled

        // test expectations
        val queryInfo1 = QueryInfo(expectedSearchCriteria)
        verify(dataService).fetchRequest(eq(NotificationSettingDataEncrypted::class.java), argThat{matches(queryInfo1)})
        assertThat(returnedReminders, matchesReminderSettingList(expectedReminders))
    }

    @Test
    fun testQueryEnabledNotificationSettingsFromDatabaseWhenNothingIsEnabled() {

        // create expectations
        val expectedSearchCriteria = SearchCriteria("enabled = %@", 1)
        val queryInfo1 = QueryInfo(expectedSearchCriteria)


        // perform operation
        whenever(dataService.fetchRequest(eq(NotificationSettingDataEncrypted::class.java), argThat{matches(queryInfo1)})).thenReturn(ArrayList<NotificationSettingDataEncrypted>())
        val query = EncryptedNotificationSettingQuery(dependencyProvider)
        val returnedReminders = query.allEnabled


        // test expectations
        verify(dataService).fetchRequest(eq(NotificationSettingDataEncrypted::class.java), argThat{matches(queryInfo1)})
        assertEquals(0, returnedReminders.size.toLong())
    }

    @Test
    fun testVerifyNotificationSettingWithNameExistsInDatabase() {
        // Initialize test data.
        val reminderName = "Reminder1"

        // create expectations
        val expectedSearchCriteria = SearchCriteria("name = %@", reminderName)

        // perform operation
        val query = EncryptedNotificationSettingQuery(dependencyProvider)
        val reminderExists = query.hasData(reminderName)

        // test expectations
        verify(dataService).getCount(eq(NotificationSettingDataEncrypted::class.java), argThat{matches(expectedSearchCriteria)})
        assertTrue(reminderExists)
    }

    @Test
    fun testVerifyNonExistentNotificationSettingExistsInDatabase() {
        // Initialize test data.
        val reminderName = "NonExistentReminder"

        // create expectations
        val expectedSearchCriteria = SearchCriteria("name = %@", reminderName)

        // perform operation
        val query = EncryptedNotificationSettingQuery(dependencyProvider)
        val reminderExists = query.hasData(reminderName)

        // test expectations
        verify(dataService).getCount(eq(NotificationSettingDataEncrypted::class.java), argThat{matches(expectedSearchCriteria)})
        assertFalse(reminderExists)
    }

}
