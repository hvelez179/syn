//
// AsthmaApplication.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp

import android.app.ActivityManager
import android.app.AlarmManager
import android.app.Application
import android.app.NotificationManager
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.SharedPreferences
import android.location.Geocoder
import android.location.LocationManager
import android.support.annotation.MainThread
import android.util.Log
import com.jakewharton.threetenabp.AndroidThreeTen
import com.teva.analysis.model.AnalyzedDataProvider
import com.teva.analysis.model.AnalyzedDataProviderImpl
import com.teva.analysis.model.HistoryCollator
import com.teva.analysis.model.HistoryCollatorImpl
import com.teva.analysis.model.datamonitors.SummaryMessageQueue
import com.teva.analysis.model.datamonitors.SummaryMessageQueueImpl
import com.teva.cloud.dataquery.ConsentDataQuery
import com.teva.cloud.dataquery.ProgramDataQuery
import com.teva.cloud.dataquery.UserAccountQuery
import com.teva.cloud.models.*
import com.teva.common.services.*
import com.teva.common.utilities.*
import com.teva.utilities.utilities.Logger.Level.INFO
import com.teva.utilities.utilities.Logger.Level.VERBOSE
import com.teva.utilities.utilities.rest.RestClient
import com.teva.utilities.utilities.rest.RestClientImpl
import com.teva.devices.dataquery.ConnectionMetaDataQuery
import com.teva.devices.dataquery.DeviceDataQuery
import com.teva.devices.dataquery.InhaleEventDataQuery
import com.teva.devices.model.*
import com.teva.environment.models.DailyEnvironmentalReminderManager
import com.teva.environment.models.EnvironmentMonitor
import com.teva.environment.models.EnvironmentMonitorFactory
import com.teva.location.models.LocationManagerFactory
import com.teva.location.services.LocationSettings
import com.teva.location.services.LocationSettingsImpl
import com.teva.medication.dataquery.MedicationDataQuery
import com.teva.medication.dataquery.PrescriptionDataQuery
import com.teva.notifications.dataquery.ReminderDataQuery
import com.teva.notifications.models.NotificationsFactory
import com.teva.notifications.services.NotificationPresenter
import com.teva.notifications.services.NotificationService
import com.teva.notifications.services.NotificationServiceImpl
import com.teva.respiratoryapp.common.DateTimeLocalization
import com.teva.respiratoryapp.models.ApplicationSettings
import com.teva.respiratoryapp.models.SystemManager
import com.teva.respiratoryapp.models.dataquery.encrypted.*
import com.teva.respiratoryapp.models.engagementbooster.EngagementBoosterManager
import com.teva.respiratoryapp.models.engagementbooster.EngagementBoosterManagerImpl
import com.teva.respiratoryapp.services.NotificationPresenterImpl
import com.teva.respiratoryapp.services.data.DataService
import com.teva.respiratoryapp.services.data.encrypteddata.EncryptedDataServiceImpl
import com.teva.userfeedback.UserFeedbackFactory
import com.teva.userfeedback.dataquery.DailyUserFeelingDataQuery
import com.teva.userfeedback.model.DailyAssessmentReminderManager
import com.teva.userfeedback.model.UserFeelingManager
import org.greenrobot.eventbus.EventBus
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import java.io.IOException
import com.teva.common.services.analytics.AnalyticsService
import com.teva.common.services.analytics.AnalyticsServiceImpl
import com.teva.utilities.services.DependencyProvider
import com.teva.utilities.utilities.Logger

/**
 * This class is the main object that represents the Android application process.
 */
@MainThread
class AsthmaApplication : Application() {

    private var deviceDataQuery: DeviceDataQuery? = null

    init {
        Log.v("AsthmaApplication", "Constructor")
    }

    override fun onCreate() {
        super.onCreate()

        configureLogger()

        var currentProcName = ""
        val pid = android.os.Process.myPid()
        val manager = this.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (processInfo in manager.runningAppProcesses) {
            if (processInfo.pid == pid) {
                currentProcName = processInfo.processName
                break
            }
        }

        if (currentProcName.endsWith(":remote")) {
            // This is the alarm process. It exists only to allow
            // the alarms to start the app even if the user has explicitly
            // killed the app.
            // Don't initialize the rest of the app.
            logger?.log(VERBOSE, "onCreate: alarm process")
        } else {
            // This is the main process. Start up all the services.
            logger?.log(VERBOSE, "onCreate: main process")
            initialize()
        }
    }

    /**
     * This method performs the application initialization.
     */
    fun initialize() {
        AndroidThreeTen.init(this)

        logDisplayMetrics()

        createServices(DependencyProvider.default)
    }

    /**
     * This method configures the Logger class with the log levels specified in logconfig.json.
     */
    private fun configureLogger() {
        val assetManager = assets
        try {
            val inputStream = assetManager.open("logconfig.json")

            Logger.configure(inputStream)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        logger = Logger(AsthmaApplication::class)
    }

    /**
     * This method logs the display metrics of the phone.
     */
    private fun logDisplayMetrics() {
        if (logger!!.isEnabled(INFO)) {
            val metrics = resources.displayMetrics
            val stringBuilder = StringBuilder("Display metrics:")
                    .append("\nWidth: ").append(metrics.widthPixels)
                    .append("\nHeight: ").append(metrics.heightPixels)
                    .append("\nDensity: ").append(metrics.density)
                    .append("\nDensityDpi: ").append(metrics.densityDpi)
                    .append("\nScaledDensity: ").append(metrics.scaledDensity)
                    .append("\nXdpi: ").append(metrics.xdpi)
                    .append("\nYdpi: ").append(metrics.ydpi)
            logger!!.log(INFO, stringBuilder.toString())
        }
    }

    /**
     * This method adds the application services to the DependencyProvider.
     */
    private fun createServices(dependencyProvider: DependencyProvider) {
        registerAndroidServiceFactories(dependencyProvider)

        dependencyProvider.register(Context::class, this)
        dependencyProvider.register(AnalyticsService::class, AnalyticsServiceImpl(dependencyProvider))

        dependencyProvider.register(assets)

        val eventBus = EventBus.builder()
                .build()

        dependencyProvider.register(Messenger::class, MessengerImpl(eventBus))
        dependencyProvider.register(LocalizationService::class, LocalizationServiceImpl(this))

        dependencyProvider.register(DateTimeLocalization(dependencyProvider, BuildConfig.US_LOCALE_ONLY))

        dependencyProvider.register(PermissionManager::class, PermissionManagerImpl(dependencyProvider, this))

        dependencyProvider.register(AlarmService::class, AlarmServiceImpl(dependencyProvider))

        dependencyProvider.register(TimeService::class, TimeServiceImpl(dependencyProvider))
        dependencyProvider.register(ApplicationSettings::class, ApplicationSettings(dependencyProvider))
        initializeDatabase(dependencyProvider)
        initializeCloud(dependencyProvider)


        // Create and register the DeviceManager and DeviceQuery.
        val deviceManagerFactory = DeviceManagerFactory(dependencyProvider, deviceDataQuery!!)

        val deviceManager = deviceManagerFactory.deviceManager
        val deviceQuery = deviceManagerFactory.deviceQuery
        dependencyProvider.register(DeviceManager::class, deviceManager)
        dependencyProvider.register(DeviceDataQuery::class, deviceQuery)
        dependencyProvider.register(DeviceQuery::class, deviceQuery)

        dependencyProvider.register(QRCodeParser::class, QRCodeParserImpl())

        dependencyProvider.register(LocationSettings::class, LocationSettingsImpl(dependencyProvider))

        dependencyProvider.register(NotificationPresenter::class, NotificationPresenterImpl(dependencyProvider))
        dependencyProvider.register(NotificationService::class, NotificationServiceImpl(dependencyProvider))
        dependencyProvider.register(com.teva.notifications.models.NotificationManager::class, NotificationsFactory.getNotificationManager(dependencyProvider))

        dependencyProvider.register(UserFeelingManager::class, UserFeedbackFactory.dailyAssessmentManager)
        dependencyProvider.register(DailyAssessmentReminderManager::class, UserFeedbackFactory.dailyAssessmentReminderManager)

        dependencyProvider.register(com.teva.location.models.LocationManager::class, LocationManagerFactory.locationManager)

        dependencyProvider.register(SummaryMessageQueue::class, SummaryMessageQueueImpl(dependencyProvider))
        dependencyProvider.register(EnvironmentMonitor::class, EnvironmentMonitorFactory.environmentMonitor)
        dependencyProvider.register(DailyEnvironmentalReminderManager::class, EnvironmentMonitorFactory.environmentalReminderManager)


        dependencyProvider.register(HistoryCollator::class, HistoryCollatorImpl(dependencyProvider))
        dependencyProvider.register(AnalyzedDataProvider::class,
                AnalyzedDataProviderImpl(dependencyProvider, HISTORY_CACHE_SIZE))

        dependencyProvider.register(SystemManager::class, SystemManager(dependencyProvider))
        dependencyProvider.register(EngagementBoosterManager::class, EngagementBoosterManagerImpl(dependencyProvider))

        dependencyProvider.register(RestClient::class, RestClientImpl())
    }

    /**
     * Opens or creates the database file and reset the query caches.
     */
    fun initializeDatabase(dependencyProvider: DependencyProvider) {
        val encryptedDataService = EncryptedDataServiceImpl(dependencyProvider)

        dependencyProvider.register(DataService::class, encryptedDataService)

        dependencyProvider.register(EncryptedDailyAirQualityMapper())
        dependencyProvider.register(EncryptedMedicationMapper(dependencyProvider))
        dependencyProvider.register(EncryptedPrescriptionMapper(dependencyProvider))
        dependencyProvider.register(EncryptedDeviceMapper(dependencyProvider))
        dependencyProvider.register(EncryptedInhaleEventMapper(dependencyProvider))
        dependencyProvider.register(EncryptedConnectionMetaMapper(dependencyProvider))
        dependencyProvider.register(EncryptedVASMapper())
        dependencyProvider.register(EncryptedNotificationSettingMapper())
        dependencyProvider.register(EncryptedConsentDataMapper())
        dependencyProvider.register(EncryptedUserProfileDataMapper())
        dependencyProvider.register(EncryptedUserAccountMapper())
        dependencyProvider.register(EncryptedProgramDataMapper())

        encryptedDataService.initializeDatabase("")

        // Don't register DeviceDataQuery here.  Register the DeviceManager one instead.
        deviceDataQuery = EncryptedDeviceQuery(dependencyProvider)

        dependencyProvider.register(MedicationDataQuery::class, EncryptedMedicationQuery(dependencyProvider))
        dependencyProvider.register(PrescriptionDataQuery::class, EncryptedPrescriptionQuery(dependencyProvider))
        dependencyProvider.register(InhaleEventDataQuery::class, EncryptedInhaleEventQuery(dependencyProvider))
        dependencyProvider.register(ConnectionMetaDataQuery::class, EncryptedConnectionMetaQuery(dependencyProvider))
        dependencyProvider.register(ReminderDataQuery::class, EncryptedNotificationSettingQuery(dependencyProvider))
        dependencyProvider.register(DailyUserFeelingDataQuery::class, EncryptedVASQuery(dependencyProvider))
        dependencyProvider.register(ConsentDataQuery::class, EncryptedConsentDataQuery(dependencyProvider))
        dependencyProvider.register(UserAccountQuery::class, EncryptedUserAccountQuery(dependencyProvider))
        dependencyProvider.register(ProgramDataQuery::class, EncryptedProgramDataQuery(dependencyProvider))
    }

    private fun initializeCloud(dependencyProvider: DependencyProvider) {
        val cloudManager = CloudFactory.setupAndGetCloudManager(false, EncryptedUserProfileQuery(dependencyProvider))
        dependencyProvider.register(CloudManager::class, cloudManager)
        dependencyProvider.register(ServerTimeService::class, cloudManager)
        dependencyProvider.register(WebLoginManager::class, CloudFactory.getWebLoginManager())
    }

    /**
     * This method adds factory objects for the Android services to the DependencyProvider.
     * @param dependencyProvider The DependencyProvider to add the factories to.
     */
    private fun registerAndroidServiceFactories(dependencyProvider: DependencyProvider) {
        dependencyProvider.register(SharedPreferences::class) { instanceName->
            getSharedPreferences(instanceName, Context.MODE_PRIVATE)
        }

        dependencyProvider.register(LocationManager::class) {
            getSystemService(Context.LOCATION_SERVICE)
        }

        dependencyProvider.register(AlarmManager::class) {
            getSystemService(Context.ALARM_SERVICE)
        }

        dependencyProvider.register(NotificationManager::class) {
            getSystemService(Context.NOTIFICATION_SERVICE)
        }

        dependencyProvider.register(BluetoothManager::class) {
            getSystemService(Context.BLUETOOTH_SERVICE)
        }

        dependencyProvider.register(ZoneId::class) {
            ZoneId.systemDefault()
        }

        // dependency factory for now to support unit testing
        dependencyProvider.register(Instant::class) {
            Instant.now()
        }

        dependencyProvider.register(Geocoder::class) {
            Geocoder(this)
        }
    }

    companion object {
        private var logger: Logger? = null

        val HISTORY_CACHE_SIZE = 14
    }
}
