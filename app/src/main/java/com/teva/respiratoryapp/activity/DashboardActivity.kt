package com.teva.respiratoryapp.activity

import android.Manifest
import android.app.Dialog
import android.app.NotificationManager
import android.content.*
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.os.Parcelable
import android.provider.Settings
import android.view.View
import android.view.WindowManager
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.teva.cloud.dataentities.UserProfile
import com.teva.cloud.enumerations.CloudManagerState.EMANCIPATED
import com.teva.cloud.enumerations.CloudManagerState.NOT_LOGGED_IN
import com.teva.cloud.messages.CloudLoginStateChangedMessage
import com.teva.cloud.messages.LeaveProgramMessage
import com.teva.cloud.messages.RemindProgramConsentMessage
import com.teva.cloud.messages.SyncCloudMessage
import com.teva.cloud.models.CloudManager
import com.teva.cloud.models.CloudManagerNotificationId
import com.teva.cloud.models.programmanagement.CareProgramManager
import com.teva.cloud.models.programmanagement.InvitationDetails
import com.teva.common.messages.AppForegroundMessage
import com.teva.common.messages.SystemMonitorMessage
import com.teva.common.services.AlarmService
import com.teva.common.services.AlarmServiceCallback
import com.teva.common.services.TimeService
import com.teva.common.utilities.DataTask
import com.teva.utilities.utilities.Logger.Level.*
import com.teva.common.utilities.Messenger
import com.teva.devices.entities.Device
import com.teva.devices.messages.BluetoothStateChangedMessage
import com.teva.devices.model.DeviceManager
import com.teva.environment.messages.UpdateEnvironmentMessage
import com.teva.location.models.LocationManager
import com.teva.location.services.LocationProvidersChangedMessage
import com.teva.notifications.services.NotificationPresenter
import com.teva.notifications.services.notification.NotificationInfo
import com.teva.respiratoryapp.BuildConfig
import com.teva.respiratoryapp.R
import com.teva.respiratoryapp.activity.model.InhalerRegistrationCommonState
import com.teva.respiratoryapp.activity.view.*
import com.teva.respiratoryapp.activity.view.device.*
import com.teva.respiratoryapp.activity.view.popup.DsaConfirmPopup
import com.teva.respiratoryapp.activity.view.popup.DsaPopup
import com.teva.respiratoryapp.activity.view.popup.InhalerRegistrationPopup
import com.teva.respiratoryapp.activity.view.popup.NotificationPopup
import com.teva.respiratoryapp.activity.view.popup.walkthrough.Walkthrough
import com.teva.respiratoryapp.activity.view.popup.walkthrough.WalkthroughFragment
import com.teva.respiratoryapp.activity.view.programs.AddCareProgramFragment
import com.teva.respiratoryapp.activity.view.programs.CareProgramConsentFragment
import com.teva.respiratoryapp.activity.view.programs.CareProgramsFragment
import com.teva.respiratoryapp.activity.view.setup.AddProfileFragment
import com.teva.respiratoryapp.activity.view.setup.ConsentFragment
import com.teva.respiratoryapp.activity.view.setup.LoginFragment
import com.teva.respiratoryapp.activity.view.setup.ProfileSetupFragment
import com.teva.respiratoryapp.activity.view.tracker.*
import com.teva.respiratoryapp.activity.viewmodel.EmancipatedMessageViewModel
import com.teva.respiratoryapp.activity.viewmodel.LoadingEvents
import com.teva.respiratoryapp.activity.viewmodel.SupportEvents
import com.teva.respiratoryapp.activity.viewmodel.dashboard.AboutAppViewModel
import com.teva.respiratoryapp.activity.viewmodel.dashboard.ContactSupportViewModel
import com.teva.respiratoryapp.activity.viewmodel.dashboard.DashboardStateViewModel
import com.teva.respiratoryapp.activity.viewmodel.dashboard.DashboardViewModel.DashboardEvents
import com.teva.respiratoryapp.activity.viewmodel.dashboard.DashboardViewModel.MENU_ITEM_ID
import com.teva.respiratoryapp.activity.viewmodel.dashboard.DashboardViewModel.MENU_ITEM_ID.*
import com.teva.respiratoryapp.activity.viewmodel.dashboard.IntroViewModel
import com.teva.respiratoryapp.activity.viewmodel.device.*
import com.teva.respiratoryapp.activity.viewmodel.device.ScanIntroViewModel.IntroEvents
import com.teva.respiratoryapp.activity.viewmodel.device.SelectInhalerNameViewModel.SelectInhalerNameEvents
import com.teva.respiratoryapp.activity.viewmodel.popup.NotificationPopupViewModel
import com.teva.respiratoryapp.activity.viewmodel.popup.walkthrough.WalkthroughViewModel
import com.teva.respiratoryapp.activity.viewmodel.programs.AddCareProgramViewModel
import com.teva.respiratoryapp.activity.viewmodel.programs.CareProgramConsentViewModel
import com.teva.respiratoryapp.activity.viewmodel.programs.CareProgramsViewModel
import com.teva.respiratoryapp.activity.viewmodel.setup.ConsentViewModel
import com.teva.respiratoryapp.activity.viewmodel.setup.LoginViewModel
import com.teva.respiratoryapp.activity.viewmodel.setup.ProfileSetupViewModel
import com.teva.respiratoryapp.activity.viewmodel.tracker.ReportEmptyViewModel
import com.teva.respiratoryapp.activity.viewmodel.tracker.TrackerViewModel.TrackerEvents
import com.teva.respiratoryapp.common.AppSystemMonitorActivity
import com.teva.respiratoryapp.models.ApplicationSettings
import com.teva.respiratoryapp.models.engagementbooster.EngagementBoosterNotificationId
import com.teva.respiratoryapp.models.notification.NotificationCategories
import com.teva.respiratoryapp.models.notification.NotificationCategory
import com.teva.respiratoryapp.models.notification.NotificationType
import com.teva.respiratoryapp.mvvmframework.ui.BaseActivity
import com.teva.respiratoryapp.mvvmframework.ui.FragmentAnimation
import com.teva.respiratoryapp.mvvmframework.ui.FragmentInfo
import com.teva.respiratoryapp.mvvmframework.ui.FragmentViewModel.NavigationEvents
import com.teva.respiratoryapp.mvvmframework.ui.PermissionChecker
import com.teva.respiratoryapp.services.AppService
import com.teva.respiratoryapp.services.NotificationPresenterImpl
import com.teva.respiratoryapp.services.alert.AlertButton
import com.teva.respiratoryapp.services.alert.AlertConfiguration
import com.teva.respiratoryapp.services.alert.AlertType
import com.teva.respiratoryapp.services.alert.SystemAlertManager
import com.teva.userfeedback.entities.DailyUserFeeling
import com.teva.userfeedback.enumerations.UserFeeling
import com.teva.userfeedback.model.DSANotificationId
import com.teva.userfeedback.model.UserFeelingManager
import org.greenrobot.eventbus.Subscribe
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import java.util.*


/**
 * Activity class for the Dashboard.
 */
class DashboardActivity : BaseActivity(), AlarmServiceCallback {
    private var inhalerRegistrationCommonState: InhalerRegistrationCommonState? = null
    private var dashboardStateViewModel: DashboardStateViewModel? = null
    private val applicationSettings: ApplicationSettings = dependencyProvider.resolve()
    private var locationManager: LocationManager = dependencyProvider.resolve()
    private val cloudManager: CloudManager = dependencyProvider.resolve()

    private var walkthroughInProgress: Boolean = false
    private var bluetoothDisabledDialog: Dialog? = null
    private var bluetoothDisabledDialogDisplayed = true
    private var locationDialogDisplayed = true
    private var locationDisabledDialog: Dialog? = null
    private val networkChangeReceiver = NetworkChangeReceiver()

    /**
     * This function handles alarm messages sent by the alarm service.
     */
    override fun onAlarm(id: String, data: Parcelable?) {
        if(id == CloudManagerNotificationId.EMANCIPATED) {
            dependencyProvider.resolve<ApplicationSettings>().emancipated = true
            showEmancipationMessage()
        }
    }

    /**
     * This function displays the emancipation message.
     */
    private fun showEmancipationMessage() {
        if(!(currentFragment is EmancipatedMessageFragment)) {
            showView(EmancipatedMessageFragment::class)
        }
    }

    /**
     * This class is a broadcast receiver for receiving updates about changes to internet connection.
     */
    inner class NetworkChangeReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val connectivityManager = context
                    .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

            // if internet connection is available, request for environment information.
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            if (activeNetworkInfo != null && activeNetworkInfo.isConnected) {
                // On some devices such as the Samsung Galaxy S8, request
                // for environment data fails if retrieved immediately after internet
                // is available. The next retry happens after 5 minutes.
                // A short delay is introduced before retrieving the data so that
                // the first request succeeds.
                val ENVIRONMENT_UPDATE_DELAY = 10000L // milliseconds
                val timer = Timer()
                timer.schedule(object:TimerTask() {
                    override fun run() {
                        dependencyProvider.resolve<Messenger>().publish(UpdateEnvironmentMessage())
                        timer.cancel()
                        timer.purge()
                    }
                }, ENVIRONMENT_UPDATE_DELAY)
            }
        }
    }

    /**
     * Android lifecycle method called when the activity is created.

     * @param savedInstanceState The saved state of the activity.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent?.getBooleanExtra(SplashActivity.FROM_SPLASH_KEY, false) == true) {
            overridePendingTransition(R.anim.splash_fade_in, R.anim.splash_fade_out)
        }

        if(!BuildConfig.DEBUG) {
            window.setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                    WindowManager.LayoutParams.FLAG_SECURE)
        }

        setContentView(R.layout.dashboard_activity)

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        val intent = Intent(this, AppService::class.java)
        startService(intent)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)

        // Start the broadcast receiver that monitors for changes to the network connectivity
        val intentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        dependencyProvider.resolve<Context>().registerReceiver(networkChangeReceiver, intentFilter)

        locationManager = dependencyProvider.resolve()

        if (savedInstanceState == null) {
            if (!cloudManager.isInitialSetupCompleted) {
                showView(IntroFragment::class, INTRO_STACK_TAG)
            } else if(!cloudManager.isLoggedIn) {
                showView(LoginFragment::class, FragmentAnimation.FADE)
            }else if (!applicationSettings.isWalkthroughShown) {
                // if walkthrough was not completed, display the walkthrough screen.
                val bundle = WalkthroughFragment.createArguments(Walkthrough.WELCOME, false)
                walkthroughInProgress = true
                if(currentFragmentInfo != null) {
                    replaceView(CLEAR_STACK_TAG, true, WalkthroughFragment::class, FragmentAnimation.FADE, null, WALKTHROUGH_STACK_TAG, bundle)
                } else {
                    showView(WalkthroughFragment::class, FragmentAnimation.FADE, WALKTHROUGH_STACK_TAG, bundle)
                }

            } else {
                // display the dashboard if consent and walkthrough were completed.
                if(currentFragmentInfo != null) {
                    replaceView(CLEAR_STACK_TAG, true, DashboardFragment::class, FragmentAnimation.FADE, null, null, null)
                } else {
                    showView(DashboardFragment::class, FragmentAnimation.FADE)
                }
            }
        } else {
            // if creating from a saved instance, read the flag to indicate if
            // walkthrough was in progress as we would need to continue running
            // the app in the walkthrough mode.
            walkthroughInProgress = savedInstanceState.getBoolean(IS_WALKTHROUGH_IN_PROGRESS_BUNDLE_KEY)
            bluetoothDisabledDialogDisplayed = savedInstanceState.getBoolean(BLUETOOTH_DISABLED_DIALOG_DISPLAYED_KEY)
            locationDialogDisplayed = savedInstanceState.getBoolean(LOCATION_DIALOG_DISPLAYED_KEY)
        }

        dashboardStateViewModel!!.onCreate()
        dependencyProvider.resolve<AlarmService>().register(CloudManagerNotificationId.EMANCIPATED, this)
    }

    /**
     * Android lifecycle method called when a new Intent is delivered to the activity.

     * @param intent The new Intent
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)

        if (resumed) {
            processNotificationIntent()
        }

        logger.log(VERBOSE, "onNewIntent: " + intent.action)
    }

    /**
     * Android lifecycle method called when the activity becomes the currently focused activity.
     */
    override fun onResume() {
        super.onResume()
        checkAndUpdateGooglePlayServices()

        dashboardStateViewModel?.onResume()

        processNotificationIntent()
        intent = null

        checkBluetoothAndLocationState(false)

        if(dependencyProvider.resolve<SharedPreferences>().getBoolean("firstRun", true)) {
            dependencyProvider.resolve<Messenger>().post(SystemMonitorMessage(AppSystemMonitorActivity.FirstUse()))
            dependencyProvider.resolve<SharedPreferences>().edit().putBoolean("firstRun", false)
        }

        val emancipated = dependencyProvider.resolve<ApplicationSettings>().emancipated

        if(emancipated) {
            showEmancipationMessage()
        }
    }

    /**
     * Android lifecycle method called when the activity is not the currently focused activity.
     */
    override fun onPause() {
        super.onPause()

        dashboardStateViewModel!!.onPause()
    }

    /**
     * Message handler for the BluetoothStateChangedMessage.
     */
    @Suppress("UNUSED_PARAMETER")
    @Subscribe
    fun onBluetoothStateChangedMessage(message: BluetoothStateChangedMessage) {
        checkBluetoothAndLocationState(true)
    }

    /**
     * Message handler for the LocationProvidersChangedMessage
     */
    @Suppress("UNUSED_PARAMETER")
    @Subscribe
    fun onLocationProvidersChangedMessage(message: LocationProvidersChangedMessage) {
        checkBluetoothAndLocationState(false)
    }

    /**
     * Checks if Bluetooth is enabled and the Location Services have permission and are
     * enabled.  Dialogs are displayed to allow the user to enable services or provide permission.

     * @param overrideDialogFlags Indicates whether the Bluetooth or Location dialogs should be displayed
     * *                            even if they've already been displayed since the last time the Bluetooth
     * *                            or Location services was on. This is used when the user taps the banner.
     */
    private fun checkBluetoothAndLocationState(overrideDialogFlags: Boolean) {
        val deviceManager = dependencyProvider.resolve<DeviceManager>()

        // We don't display the Bluetooth or Location services dialogs until the first inhaler
        // is scanned, so skip the Bluetooth and Location check unless the FirstInhalerScanned
        // setting is true.  However, if the user clicks on the banner, we want to display the
        // appropriate dialogs even if we haven't scanned an inhaler yet.
        if (overrideDialogFlags) {
            // first check if Bluetooth is enabled
            var checkLocation = true

            if (!deviceManager.isBluetoothEnabled) {
                // If the Bluetooth is disabled, but the "Bluetooth is Disabled" dialog has already been displayed,
                // then don't display the dialog again unless overrideDialogFlags is true. OverrideDialogFlags is true
                // when the user taps on the banner.
                if (!bluetoothDisabledDialogDisplayed || overrideDialogFlags) {
                    showBluetoothDisabledDialog(overrideDialogFlags)
                    bluetoothDisabledDialogDisplayed = true

                    // the bluetooth dialog is being displayed, so don't check the
                    // location services right now. This method will be called again
                    // when the dialog is dismissed.
                    checkLocation = false
                }
            }

            if (checkLocation) {
                checkLocationState(overrideDialogFlags)
            }
        }

        // clean up dialogs if the user changed the states on their own.
        if (deviceManager.isBluetoothEnabled) {
            if (bluetoothDisabledDialog != null && bluetoothDisabledDialog!!.isShowing) {

                bluetoothDisabledDialog!!.dismiss()
            }

            // We only display the dialog once after the transition from Bluetooth ON to Bluetooth OFF.
            // But once we see that Bluetooth is ON again, we reset the flag indicating that the
            // dialog has been displayed so that it will be displayed again the next time the Bluetooth is
            // turned off.
            bluetoothDisabledDialogDisplayed = false
        }

        if (locationManager.isLocationServicesEnabled) {

            if (locationDisabledDialog != null && locationDisabledDialog!!.isShowing) {

                locationDisabledDialog!!.dismiss()
            }

            // We only display the dialog once after the transition from Location Services ON to OFF.
            // But once we see that Location Services are ON again, we reset the flag indicating that the
            // dialog has been displayed so that it will be displayed again the next time the Location Services are
            // turned off.
            locationDialogDisplayed = false
        }
    }

    /**
     * Checks the location permission and enabled state and displays a dialog
     * allowing the user to enable the Location Services or grant permission.
     * @param overrideDialogFlags Override the flags indicating that the dialogs have already been displayed.
     */
    private fun checkLocationState(overrideDialogFlags: Boolean) {
        // Show the Location dialog if the user hasn't granted permissions yet or if the
        // location services are disabled.
        val hasPermissions = permissionChecker!!.checkPermissions(*LOCATION_PERMISSION)
        if (!hasPermissions || !locationManager.isLocationServicesEnabled) {
            showLocationDialog(overrideDialogFlags)
        }
    }

    /**
     * Retrieves the current day's DSA and displays the appropriate DSA screen.
     */
    private fun showDsa() {

        DataTask<Unit, DailyUserFeeling>("DashboardViewModel_CheckIfDSAExists")
                .inBackground {
                    val userFeelingManager: UserFeelingManager = dependencyProvider.resolve()
                    val today = dependencyProvider.resolve<TimeService>().today()

                    userFeelingManager.getUserFeelingAtDate(today)

                }
                .onResult { dsa ->
                    dependencyProvider.resolve<NotificationManager>().cancel(DSANotificationId.DSA_REMINDER, 0)
                    if (dsa == null || dsa?.userFeeling === UserFeeling.UNKNOWN) {
                        val timeService = dependencyProvider.resolve<TimeService>()

                        val dsaBundle = DsaPopup.createArguments(timeService.now())
                        showView(DsaPopup::class, FragmentAnimation.FADE,null,dsaBundle)
                    } else {
                        val confirmBundle = DsaConfirmPopup.createArguments(dsa.userFeeling)
                        showView(DsaConfirmPopup::class, FragmentAnimation.FADE, null, confirmBundle)
                    }
                }
                .execute()
    }

    /**
     * Dispays a dialog informing the user that either permissions haven't been granted
     * for the location services or they are disabled. When the user clicks the Allow button
     * the appropriate system dialogs are displayed to grant permission or enable the services.
     * @param overrideDialogFlags Override the flags indicating that the dialogs have already been displayed.
     */
    private fun showLocationDialog(overrideDialogFlags: Boolean) {
        if (!locationDialogDisplayed || overrideDialogFlags) {

            val message = getLocalizedString(R.string.location_rationale_text)
            locationDialogDisplayed = true
            locationDisabledDialog = systemAlertManager?.showAlert(
                    message = message,
                    primaryButtonTextId = R.string.allow_text, secondaryButtonTextId = R.string.cancel_text,
                    onClick = { button ->
                        if (button == AlertButton.PRIMARY) {
                            requestLocationPermissionsOrEnableLocationServices()
                        }
                    })
        }
    }

    /**
     * This method is called when the user clicks the positive button on the Location Services
     * dialog.  It will display either the Android Location Permission Request dialog, the
     * application settings page, or the Location Services enable request dialog based on the
     * current state of the permissions and Location Sevices.
     */
    private fun requestLocationPermissionsOrEnableLocationServices() {
        // If we don't have permissions, then we either display the permission dialog
        // or send them to the application settings screen.
        // But if we have the permissions, then we try to enable the location services.
        if (!permissionChecker!!.checkPermissions(*LOCATION_PERMISSION)) {

            // if we haven't asked for permissions yet, or if shouldShowRationale returns true,
            // then we request permissions.  However, if we have asked for permissions before
            // and shouldShowRationale() returns false, that means the user clicked the
            // Don't ask me again checkbox on the permission dialog.  Therefore we need to
            // send the user to the application settings screen.
            if (!applicationSettings.locationPermissionRequested || permissionChecker!!.shouldShowRationale(*LOCATION_PERMISSION)) {

                // display the permission dialog
                permissionChecker!!.requestPermissions(LOCATION_PERMISSION, object : PermissionChecker.PermissionCallback {
                    override fun onPermissionRequestResult(granted: Boolean) {
                        // if the permissions were granted, then check and
                        // enable location services.
                        if (granted) {
                            locationManager.enableLocationServices(
                                    this@DashboardActivity)
                        }
                    }
                })

                applicationSettings.locationPermissionRequested = true
            } else {
                // send the user to the application settings screen
                showApplicationSettings()
            }
        } else {
            // The dialog must have been displayed because Location Services are disabled,
            // so try to enable them.
            locationManager.enableLocationServices(this@DashboardActivity)
        }
    }

    /**
     * Launches the device's application settings screen for this app.
     */
    private fun showApplicationSettings() {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    /**
     * Shows the dialog informing the user that Bluetooth is disabled.
     */
    private fun showBluetoothDisabledDialog(overrideDialogFlags: Boolean) {
        bluetoothDisabledDialog = systemAlertManager?.showAlert(
                messageId = R.string.turnOnBluetoothAlertBody_text,
                titleId = R.string.turnOnBluetoothAlertTitle_text,
                primaryButtonTextId = R.string.settings_text,
                secondaryButtonTextId =  R.string.cancel_text,
                onClick = { button ->
                    if (button == AlertButton.PRIMARY) {
                        displayBluetoothSettings()
                    }

                    bluetoothDisabledDialog = null
                    checkLocationState(overrideDialogFlags)
                })
    }

    /**
     * Displays the Bluetooth settings activity
     */
    private fun displayBluetoothSettings() {
        val intentOpenBluetoothSettings = Intent()
        intentOpenBluetoothSettings.action = android.provider.Settings.ACTION_BLUETOOTH_SETTINGS
        startActivity(intentOpenBluetoothSettings)
    }

    /**
     * Checks if the intent is a notification intent received by the activity and handles
     * the display of the notification.
     */
    private fun processNotificationIntent() {

        val emancipated = dependencyProvider.resolve<ApplicationSettings>().emancipated

        if(emancipated) {
            showEmancipationMessage()
            return
        }

        val intent = intent
        if (intent != null) {
            val action = intent.action
            if (action != null && action.startsWith(NotificationPresenterImpl.NOTIFICATION_ACTION)) {

                // if the menu was open, close it.
                if (currentFragment is DashboardFragment) {
                    (currentFragment as DashboardFragment).closeMenu()
                }

                logger.log(INFO, "Showing notification: " + action)

                val notificationInfo = intent.getParcelableExtra<NotificationInfo>(NotificationPresenterImpl.NOTIFICATION_EXTRA)
                val isFromNotification = intent.getBooleanExtra(NotificationPresenterImpl.FROM_NOTIFICATION_EXTRA, false)

                val newNotificationCategory = NotificationCategories.findCategory(notificationInfo)
                val newNotificationPriority = newNotificationCategory.priority
                val fragmentClass = newNotificationCategory.fragmentClass

                if (notificationInfo.categoryId == CloudManagerNotificationId.EMANCIPATION_TOMORROW || notificationInfo.categoryId == CloudManagerNotificationId.EMANCIPATION_IN_7_DAYS){

                    dependencyProvider.resolve<SystemAlertManager>()
                            .showAlert(id = notificationInfo.categoryId,
                                    titleId = newNotificationCategory.headerStringId,
                                    messageId = newNotificationCategory.bodyStringId,
                                    primaryButtonTextId = newNotificationCategory.buttonStringId,
                                    imageId = newNotificationCategory.bodyImageSource,
                                    imageTextId = newNotificationCategory.bodyImageTextId,
                                    onImageClick = { showView(newNotificationCategory.imageFragmentClass!!)},
                                    secondaryButtonTextId = newNotificationCategory.hyperlinkStringId,
                                    onClickClose ={ button ->
                                if (button == AlertButton.SECONDARY) {
                                    showView(fragmentClass)
                                }
                                true

                            })
                    return
                } else if (notificationInfo.categoryId == CloudManagerNotificationId.EMANCIPATED){

                    dependencyProvider.resolve<SystemAlertManager>()
                            .showAlert(titleId = newNotificationCategory.headerStringId,
                                    messageId = newNotificationCategory.bodyStringId,
                                    primaryButtonTextId = null,
                                    secondaryButtonTextId = newNotificationCategory.hyperlinkStringId,
                                    onClickClose = { button ->
                                if (button == AlertButton.SECONDARY) {
                                    showView(fragmentClass)
                                }
                                true
                            })
                    return
                }

                val bundle = NotificationPopup.createArguments(notificationInfo)

                if (newNotificationCategory.categoryId == DSANotificationId.DSA_REMINDER ||
                        newNotificationCategory.categoryId == EngagementBoosterNotificationId.DAILY_SELF_ASSESSMENT_TOOL) {
                    showDsa()
                } else if (isFromNotification) {

                    if (newNotificationCategory.showWhenFromNotification) {
                        systemAlertManager?.closeAllAlerts()
                        // If the user clicked on a notification, show it on top
                        showView(fragmentClass, FragmentAnimation.FADE, newNotificationCategory.stackTag, bundle)
                    }
                } else if (newNotificationCategory.showWhenInForeground) {

                    if (!isNotificationCurrentlyDisplayed()) {
                        systemAlertManager?.closeAllAlerts()
                        // top fragment is not a notification, so display on top.
                        showView(fragmentClass, FragmentAnimation.FADE, newNotificationCategory.stackTag, bundle)

                        // The notification might be in the backstack, for example, if the user has launched IFU
                        // from a previous notification. remove any matching notifications from the the backstack.
                        removeSimilarNotificationsFromBackStack(newNotificationCategory.notificationType)
                    } else {
                        val existingNotificationType = getCurrentNotificationCategory()?.notificationType
                        val newNotificationType = newNotificationCategory.notificationType

                        var addToBackStack: Boolean = false

                        if (NotificationType.shouldNotificationReplaceExisting(newNotificationType, existingNotificationType)) {
                            systemAlertManager?.closeAllAlerts()
                            // if current notifications needs to be replaced by the new one, do so.
                            replaceView(null, false, fragmentClass, FragmentAnimation.FADE, null, newNotificationCategory.stackTag, bundle)
                        } else if (!NotificationType.isExistingNotificationOfHigherPriority(newNotificationType, existingNotificationType)) {
                            systemAlertManager?.closeAllAlerts()
                            // the new notification is of higher priority so display it.
                            showView(fragmentClass, FragmentAnimation.FADE, newNotificationCategory.stackTag, bundle)
                        } else {
                            // mark the new notification to be added to the back stack.
                            addToBackStack = true
                        }

                        // remove any matching notifications from the the backstack.
                        removeSimilarNotificationsFromBackStack(newNotificationType)

                        // add the new notification to the backstack.
                        if (addToBackStack) {

                            val newFragmentInfo = FragmentInfo()
                            newFragmentInfo.fragmentClass = fragmentClass
                            newFragmentInfo.arguments = bundle
                            newFragmentInfo.animation = FragmentAnimation.FADE
                            newFragmentInfo.fragmentTag = backStack.createFragmentTag()
                            newFragmentInfo.isOpaque = true
                            newFragmentInfo.stackTag = newNotificationCategory.stackTag
                            var similarNotificationExists = false
                            backStack.addAfter(newFragmentInfo) { obj ->

                                var shouldAddFragment = false
                                if(similarNotificationExists) {
                                    shouldAddFragment = false
                                    logger.log(INFO, "similar notification is currently on the backstack. Not inserting the new notification of type: $newNotificationType")
                                } else if (obj.fragmentClass.java.isAssignableFrom(fragmentClass.java)) {
                                    val entryNotificationInfo = NotificationPopup.getNotificationDataFromArguments(obj.arguments!!)
                                    if (entryNotificationInfo != null) {
                                        val entryNotificationCategory = NotificationCategories.findCategory(entryNotificationInfo)
                                        if(newNotificationCategory.notificationType == entryNotificationCategory.notificationType) {
                                            similarNotificationExists = true
                                            shouldAddFragment = false
                                        } else {
                                            shouldAddFragment = !NotificationType.isExistingNotificationOfHigherPriority(newNotificationCategory.notificationType, entryNotificationCategory.notificationType)
                                        }
                                        logger.log(INFO, "Existing notification type: ${entryNotificationCategory.notificationType} New notification type: $newNotificationType  Inserting fragment: $shouldAddFragment")
                                    }

                                } else {
                                    logger.log(INFO, "New notification type: $newNotificationType  - Fragment class not assignable: ${obj.fragmentClass}")
                                    shouldAddFragment = true
                                }

                                shouldAddFragment
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * This function determines if a notification is currently displayed.
     *
     * @return - true if the topmost fragment is a notification.
     */
    private fun isNotificationCurrentlyDisplayed(): Boolean {
        return currentFragment is NotificationPopup || currentFragment is DsaPopup || currentFragment is DsaConfirmPopup
    }

    /**
     * This function returns the category of the currently displayed notification.
     *
     * @return - the category of the currently displayed notification.
     */
    private fun getCurrentNotificationCategory(): NotificationCategory? {
        if (!isNotificationCurrentlyDisplayed()) {
            return null
        } else {
            if (currentFragment is DsaPopup || currentFragment is DsaConfirmPopup) {
                return NotificationCategories.findCategory(DSANotificationId.DSA_REMINDER)
            } else {
                val currentNotificationFragment = currentFragment as NotificationPopup?
                return NotificationCategories.findCategory(currentNotificationFragment!!.notificationInfo!!)
            }
        }
    }

    /**
     * This method removes notifications of the specified type from the backstack.
     *
     * @param notificationType - the type of the notifications to be removed.
     */
    private fun removeSimilarNotificationsFromBackStack(notificationType: NotificationType) {
        backStack.remove { obj ->
            // do not remove if fragment is not a notification or does not have data.
            if (obj.arguments == null || (obj.fragmentClass != NotificationPopup::class.java && obj.fragmentClass != DsaPopup::class.java)) {
                return@remove false
            }

            val currentNotificationType: NotificationType

            if(obj.fragmentClass == DsaPopup::class.java || obj.fragmentClass == DsaConfirmPopup::class.java) {
                currentNotificationType = NotificationType.ASK_USER_FEELING
            } else {
                val category = NotificationCategories.findCategory(NotificationPopup.getNotificationDataFromArguments(obj.arguments!!)!!)
                currentNotificationType = category.notificationType
            }

            return@remove NotificationType.shouldNotificationReplaceExisting(notificationType, currentNotificationType)
        }
    }

    /**
     * The ServiceConnection for the Asthma App Android Service that the activity connects to.
     */
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
            // resend AppForegroundMessage to let newly started services know about the foreground state.
            sendAppForegroudMessage()
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            // Not sure we need to do anything here.
        }
    }

    /**
     * Message handler for the AppForegroundMessage
     */
    @Suppress("UNUSED_PARAMETER")
    @Subscribe
    fun onAppForeground(message: AppForegroundMessage) {
        checkForExpiration()
    }

    /**
     * Handler for LeaveProgramMessage
     */
    @Suppress("UNUSED_PARAMETER")
    @Subscribe
    fun onLeaveProgramMessage(message: LeaveProgramMessage){
        dependencyProvider.resolve<Messenger>().post(SyncCloudMessage())
    }

    /**
     * Handler for CloudLoginStateChangedMessage
     */
    @Suppress("UNUSED_PARAMETER")
    @Subscribe
    fun onLoginUpdated(message: CloudLoginStateChangedMessage){
        if (currentFragment?.isLogoutable!!){
            if (message.loginState == NOT_LOGGED_IN){
                replaceView(CLEAR_STACK_TAG, true, LoginFragment::class, FragmentAnimation.FADE, null, null, null)
            } else if(message.loginState == EMANCIPATED){

                dependencyProvider.resolve<SystemAlertManager>()
                        .showAlert(titleId = R.string.emancipatedTitle_text,
                                messageId = R.string.emancipatedBackgroundMessage_text,
                                primaryButtonTextId = null,
                                secondaryButtonTextId = R.string.contact_teva_support, onClickClose = { button ->
                            if (button == AlertButton.SECONDARY) {
                                showView(SupportFragment::class)
                            }
                            true
                        })
            }
        }
    }

    /**
     * Android lifecycle method called when the activity is displayed on the screen.
     */
    override fun onStart() {
        super.onStart()

        dependencyProvider.resolve<Messenger>().subscribe(this)
        dependencyProvider.resolve<NotificationPresenter>().setInForeground(true)

        dashboardStateViewModel!!.onStart()
    }

    /**
     * Android lifecycle method called when the activity is removed from the screen.
     */
    override fun onStop() {
        super.onStop()

        dependencyProvider.resolve<Messenger>().unsubscribeToAll(this)
        dependencyProvider.resolve<NotificationPresenter>().setInForeground(false)

        dashboardStateViewModel!!.onStop()
    }


    /**
     * Android lifecycle method called when the fragment is destroyed.
     */
    override fun onDestroy() {
        super.onDestroy()

        dashboardStateViewModel!!.onDestroy()
        try {
            unregisterReceiver(networkChangeReceiver)
        } catch(exception: IllegalArgumentException) {
            logger.log(ERROR, "Failed attempt to unregister BroadcastReceiver that was not registered.")
        }

        unbindService(serviceConnection)
    }

    /**
     * Displays the customer support number and opens the dialer if requested.
     */
    fun showSupportDialog() {
        val number = BuildConfig.SUPPORT_NUMBER

        systemAlertManager?.showAlert(
                id = CALL_SUPPORT_ALERT_ID,
                message = BuildConfig.SUPPORT_NUMBER,
                titleId = R.string.callCenter_text,
                primaryButtonTextId = R.string.call_text,
                secondaryButtonTextId = R.string.cancel_text,
                onClick = { button ->
                    if (button == AlertButton.PRIMARY) {
                        val callIntent = Intent(Intent.ACTION_DIAL)
                        callIntent.data = Uri.parse("tel:" + number)
                        startActivity(callIntent)
                    }
                })
    }

    /**
     * Creates and initializes the ViewModels used by the activity and
     * adds them to the activity's DependencyProvider.

     * @param savedInstanceState The saved state of the activity
     */
    override fun initViewModels(savedInstanceState: Bundle?) {
        super.initViewModels(savedInstanceState)

        dependencyProvider.register(NavigationEvents::class, object : NavigationEvents {
            override fun onBackPressed(noAnimation: Boolean) {
                handleBackPressed(noAnimation)
            }
        })

        initSetupFragments()

        initDashboardFragments()

        initDeviceFragments(savedInstanceState)

        initNotificationFragments()

        dependencyProvider.register(CheckBluetoothAndLocationEvents::class,
                object : CheckBluetoothAndLocationEvents {
                    override fun checkBluetoothAndLocationStatus() {
                        this@DashboardActivity.checkBluetoothAndLocationState(true)
                    }
                })
    }

    /**
     * Called to ask the activity to save its current dynamic state, so it
     * can later be reconstructed when a new instance of its process is
     * restarted.

     * @param outState Bundle in which to save the activity state.
     */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        // write the flag to indicate if walkthrough was in progress
        // as we would need to continue running the app in the walkthrough
        // mode when resumed.
        outState.putBoolean(IS_WALKTHROUGH_IN_PROGRESS_BUNDLE_KEY, walkthroughInProgress)
        outState.putBoolean(BLUETOOTH_DISABLED_DIALOG_DISPLAYED_KEY, bluetoothDisabledDialogDisplayed)
        outState.putBoolean(LOCATION_DIALOG_DISPLAYED_KEY, locationDialogDisplayed)

        val deviceCommonStateBundle = Bundle()
        inhalerRegistrationCommonState!!.saveInstanceState(deviceCommonStateBundle)
        outState.putBundle(INHALER_REGISTRATION_COMMON_STATE_KEY, deviceCommonStateBundle)

        val dashboardStateBundle = Bundle()
        dashboardStateViewModel!!.saveInstanceState(dashboardStateBundle)
        outState.putBundle(DASHBOARD_STATE_KEY, dashboardStateBundle)
    }

    /**
     * Initializes the events of the Dashboard Screens
     */
    private fun initDashboardFragments() {
        dashboardStateViewModel = DashboardStateViewModel(dependencyProvider)
        dependencyProvider.register(dashboardStateViewModel!!)

        dependencyProvider.register(SupportEvents::class, object : SupportEvents {
            override fun onSupport() {
                showView(ContactSupportFragment::class, FragmentAnimation.NO_ANIMATION)
            }

        })

        dependencyProvider.register(ReportEmptyViewModel.Events::class, object : ReportEmptyViewModel.Events {
            override fun onAddInhaler() {
                inhalerRegistrationCommonState!!.initForAdd(true)
                replaceView(null,
                        false,
                        ScanIntroFragment::class,
                        FragmentAnimation.FADE,
                        null,
                        DEVICE_STACK_TAG,
                        null)
            }
        })

        dependencyProvider.register(TrackerEvents::class, object : TrackerEvents {
            override fun onAddInhaler() {
                inhalerRegistrationCommonState!!.initForAdd(true)
                showView(ScanIntroFragment::class, FragmentAnimation.FADE, DEVICE_STACK_TAG, null)
            }

            override fun onReport() {
                showView(ReportFragment::class, FragmentAnimation.NO_ANIMATION)
            }

            override fun onReportEmpty() {
                showView(ReportEmptyFragment::class, FragmentAnimation.NO_ANIMATION)
            }

            override fun onDailyReport(date: LocalDate) {
                val arguments = DailyReportFragment.createArguments(date)
                showView(DailyReportFragment::class, FragmentAnimation.SLIDE_OVER, null, arguments)
            }
        })

        // Add handlers for the Dashboard's fragment events.
        dependencyProvider.register(DashboardEvents::class, object : DashboardEvents {
            override fun onMenu() {
                if (currentFragment is DashboardFragment) {
                    (currentFragment as DashboardFragment).openMenu()
                }
            }

            override fun onMenuItemClicked(menuItemId: MENU_ITEM_ID) {
                if (currentFragment is DashboardFragment) {
                    (currentFragment as DashboardFragment).closeMenu()
                }

                when (menuItemId) {
                    INSTRUCTIONS_FOR_USE_MENU_ITEM_ID -> showView(InstructionsForUseFragment::class, FragmentAnimation.FADE)

                    HOW_TO_USE_MENU_ITEM_ID -> {
                        walkthroughInProgress = false
                        val bundle = WalkthroughFragment.createArguments(Walkthrough.INHALER_READY, true)
                        showView(WalkthroughFragment::class, FragmentAnimation.FADE, WALKTHROUGH_STACK_TAG, bundle)
                    }

                    VIDEO_LIBRARY_MENU_ITEM_ID -> {

                        systemAlertManager?.showAlert(
                                messageId = R.string.menuConfirmWebRedirect_text,
                                primaryButtonTextId = R.string.continue_text,
                                secondaryButtonTextId = R.string.cancel_text,
                                onClick = { button ->
                                    if (button == AlertButton.PRIMARY) {
                                        val browseIntent = Intent(Intent.ACTION_VIEW, Uri.parse(VIDEO_LIBRARY_URL))
                                        startActivity(browseIntent)
                                    }
                                })
                    }

                    HEALTHCARE_PROGRAM_MENU_ITEM_ID -> showView(CareProgramsFragment::class, FragmentAnimation.SLIDE_OVER, CARE_PROGRAMS_STACK_TAG)

                    NOTIFICATION_SETTINGS_MENU_ITEM_ID -> showView(NotificationSettingsFragment::class, FragmentAnimation.SLIDE_OVER)

                    ABOUT_MENU_ITEM_ID -> showView(AboutTheAppFragment::class, FragmentAnimation.SLIDE_OVER)
                }
            }

            override fun onDeviceList() {
                showDeviceList()
            }

            override fun onTracker() {
                showView(TrackerFragment::class, FragmentAnimation.SLIDE_OVER)
            }

            override fun onEnvironment() {
                showView(EnvironmentFragment::class, FragmentAnimation.SLIDE_OVER)
            }

            override fun onReport() {
                showView(ReportFragment::class, FragmentAnimation.NO_ANIMATION)
            }

            override fun onReportEmpty() {
                showView(ReportEmptyFragment::class, FragmentAnimation.NO_ANIMATION)
            }

            override fun onDsa() {
                showDsa()
            }

            override fun onNotificationSettings() {
                showView(NotificationSettingsFragment::class, FragmentAnimation.SLIDE_OVER)
            }

            override fun onLogOut() {
                if (currentFragment is DashboardFragment) {
                    (currentFragment as DashboardFragment).closeMenu()
                }

                cloudManager.logOut()
            }
        })

        dependencyProvider.register(DeviceListViewModel.DeviceListEvents::class, object : DeviceListViewModel.DeviceListEvents {
            override fun addInhaler(isInhalerListEmpty: Boolean) {
                inhalerRegistrationCommonState!!.initForAdd(isInhalerListEmpty)
                if (isInhalerListEmpty) {
                    // The My Inhalers list is empty, so replace the My Inhalers screen with the Scan Intro screen
                    replaceView(null, false, ScanIntroFragment::class, FragmentAnimation.FADE, null, DEVICE_STACK_TAG, null)
                } else {
                    // Display the My Inhalers screen.
                    showView(ScanIntroFragment::class, FragmentAnimation.FADE, DEVICE_STACK_TAG, null)
                }
            }

            override fun showDeviceInfo(device: Device) {
                val bundle = DeviceInfoFragment.createFragmentArguments(device.serialNumber,
                        DeviceInfoViewModel.Mode.EDIT)

                showView(DeviceInfoFragment::class, FragmentAnimation.FADE, DEVICE_STACK_TAG, bundle)
            }
        })

        dependencyProvider.register(AboutAppViewModel.Events::class,
                object : AboutAppViewModel.Events {
                    override fun onWebsiteClick() {
                        val message = getLocalizedString(R.string.menuConfirmWebRedirect_text)
                        val title = ""

                        systemAlertManager?.showAlert(
                                id = WEB_NAVIGATION_ID,
                                message = message,
                                primaryButtonTextId = R.string.continue_text,
                                secondaryButtonTextId = R.string.cancel_text,
                                onClick = { button ->
                                    if (button == AlertButton.PRIMARY) {
                                        val browseIntent = Intent(Intent.ACTION_VIEW, Uri.parse(WEB_LINK_URL))
                                        startActivity(browseIntent)
                                    }
                                })


                    }

                    override fun onTermsOfUseClick() {
                        showView(TermsOfUseFragment::class)
                    }

                    override fun onPrivacyNoticeClick() {
                        showView(PrivacyNoticeFragment::class)
                    }

                    override fun onLicensesClick() {
                        showView(LicensesFragment::class)
                    }

                    override fun onInstructionsForUseClick() {
                        showView(InstructionsForUseFragment::class, FragmentAnimation.FADE)
                    }
                })

        dependencyProvider.register(WalkthroughViewModel.Events::class,
                object : WalkthroughViewModel.Events {
                    override fun onNext(walkthrough: Walkthrough) {
                        val bundle = WalkthroughFragment.createArguments(walkthrough, !walkthroughInProgress)
                        showView(WalkthroughFragment::class, FragmentAnimation.FADE, null, bundle)
                    }

                    override fun onScanInhaler() {
                        if (walkthroughInProgress) {
                            walkthroughInProgress = false
                            applicationSettings.isWalkthroughShown = true

                            // replace the walkthrough screens with the scan intro fragment.
                            // add the dashboard fragment to the backstack so that it is
                            // displayed if the user decides to skip the scan.
                            // the insertion needs to be done after the replace else the dashboard
                            // fragment would also be popped off during the view replacement.
                            inhalerRegistrationCommonState!!.initForAdd(true)
                            replaceView(WALKTHROUGH_STACK_TAG, true, ScanIntroFragment::class, FragmentAnimation.FADE, null, DEVICE_STACK_TAG, null)
                            insertView(DashboardFragment::class, FragmentAnimation.FADE, null, null)
                        }
                    }

                    override fun onDone() {
                        // if running in walkthrough mode, display the dashboard, else
                        // if running "how to use the app", pop the walkthrough screens
                        // off the back stack.
                        if (walkthroughInProgress) {
                            walkthroughInProgress = false
                            applicationSettings.isWalkthroughShown = true
                            replaceView(WALKTHROUGH_STACK_TAG, true, DashboardFragment::class, FragmentAnimation.FADE, null, null, null)
                        } else {
                            popViews(WALKTHROUGH_STACK_TAG, true)
                        }
                    }
                })
        dependencyProvider.register(EmancipatedMessageViewModel.Events::class, object : EmancipatedMessageViewModel.Events {
            override fun onSupportClicked() {
                showView(ContactSupportFragment::class)
            }
        })
    }

    private fun initSetupFragments() {
        dependencyProvider.register(ConsentViewModel.Events::class,
                object : ConsentViewModel.Events {
                    override fun onConfirm() {
                        dependencyProvider.resolve<ApplicationSettings>().hasUserAcceptedTermsOfUse = true
                        showView(LoginFragment::class, FragmentAnimation.FADE)
                    }

                    override fun onPrivacyNotice() {
                        showView(PrivacyNoticeFragment::class, FragmentAnimation.SLIDE_OVER)
                    }

                    override fun onTermsOfUse() {
                        showView(TermsOfUseFragment::class, FragmentAnimation.SLIDE_OVER)
                    }

                })

        dependencyProvider.register(LoginViewModel.Events::class, object : LoginViewModel.Events {
            override fun loginComplete() {
                if(!cloudManager.isInitialSetupCompleted) {
                    showView(ProfileSetupFragment::class, FragmentAnimation.FADE)
                } else if (!applicationSettings.isWalkthroughShown) {
                    dependencyProvider?.resolve<LoadingEvents>()?.hideLoadingIndicator()
                    // if walkthrough was not completed, display the walkthrough screen.
                    val bundle = WalkthroughFragment.createArguments(Walkthrough.WELCOME, false)
                    walkthroughInProgress = true
                    replaceView(CLEAR_STACK_TAG, true, WalkthroughFragment::class, FragmentAnimation.FADE, null, WALKTHROUGH_STACK_TAG, bundle)

                } else {
                    dependencyProvider?.resolve<LoadingEvents>()?.hideLoadingIndicator()

                    // display the dashboard if consent and walkthrough were completed.
                    replaceView(CLEAR_STACK_TAG, true, DashboardFragment::class, FragmentAnimation.FADE, null, null, null)
                }
            }

        })

        dependencyProvider.register(ProfileSetupViewModel.Events::class, object : ProfileSetupViewModel.Events {
            override fun onNext() {
                val bundle = WalkthroughFragment.createArguments(Walkthrough.WELCOME, false)
                walkthroughInProgress = true
                replaceView(INTRO_STACK_TAG, true, WalkthroughFragment::class, FragmentAnimation.FADE, null, WALKTHROUGH_STACK_TAG, bundle)
            }

            override fun addDependent(existingProfiles: ArrayList<UserProfile>) {
                val bundle = AddProfileFragment.createArguments(existingProfiles)
                showView(AddProfileFragment::class, FragmentAnimation.FADE, null, bundle)
            }

            override fun failedToRetrieveProfiles() {
                replaceView(LoginFragment::class, FragmentAnimation.FADE)
            }
        })

        dependencyProvider.register(CareProgramsViewModel.Events::class, object : CareProgramsViewModel.Events {
            override fun addProgram() {
                showView(AddCareProgramFragment::class, FragmentAnimation.FADE)
            }

        })

        dependencyProvider.register(AddCareProgramViewModel.Events::class, object : AddCareProgramViewModel.Events {
            override fun onSignUp(invitationDetails: InvitationDetails) {
                val arguments = CareProgramConsentFragment.createFragmentArguments(invitationDetails)
                showView(CareProgramConsentFragment::class, FragmentAnimation.FADE, bundle = arguments)
            }
        })

        dependencyProvider.register(CareProgramConsentViewModel.Events::class, object : CareProgramConsentViewModel.Events {
            override fun onConsentOrDecline() {
                popViews(CARE_PROGRAMS_STACK_TAG, false)
            }
        })

        dependencyProvider.register(ContactSupportViewModel.Events::class, object : ContactSupportViewModel.Events {
            override fun onContactSupport() {
                showView(CustomerSupportFragment::class, FragmentAnimation.FADE)
            }
        })
    }



    /**
     * Message handler for the RemindProgramConsentMessage.

     * @param message The message received.
     */
    @Suppress("UNUSED_PARAMETER")
    @Subscribe
    fun onRemindProgramConsentMessage(message: RemindProgramConsentMessage) {

        var programName = message.programData?.programName!!

        var appListString = createAppListText(programName, message.consentedApps!!)

        var title = getString(R.string.continueSharingDataTitle_text, mapOf(Pair("AppList",appListString)))


        val substitutionMap = HashMap<String, Any>()
        substitutionMap.put("AppName", getString(R.string.app_name))
        substitutionMap.put("Program", programName)
        var messageText = getString(R.string.continueSharingDataConsent_text, substitutionMap)


        dependencyProvider.resolve<SystemAlertManager>()
                .showAlert(title = title,
                        message = messageText,
                        primaryButtonTextId = R.string.continueSharingData_text,
                        secondaryButtonTextId = R.string.leaveProgram_text, onClickClose = { button ->
                    if (button == AlertButton.PRIMARY) {
                        continueConsent(message.programData?.programId!!)
                    } else if (button == AlertButton.SECONDARY) {
                        substitutionMap.put("AppList", appListString)
                        presentAreYouSure(message.programData?.programId!!, substitutionMap)
                    }

                    true
                })
    }

    /**
     * Come here on continued consent
     */
    private fun continueConsent(programId : String){
        dependencyProvider.resolve<CareProgramManager>().continueConsent(programId)
        dependencyProvider.resolve<Messenger>().post(SyncCloudMessage())
    }

    /**
     * Come here on leave program
     */
    private fun presentAreYouSure(programId: String, substitutionMap: HashMap<String,Any>) {
        val title = getString(R.string.continueSharingDataAreYouSureTitle_text)
        val message = getString(R.string.continueSharingDataAreYouSureText_text, substitutionMap)



        dependencyProvider.resolve<SystemAlertManager>()
                .showAlert(title = title,
                        message = message,
                        primaryButtonTextId = R.string.continueSharingDataLeaveProgram_text,
                        secondaryButtonTextId = R.string.dismiss_text, onClickClose = { button ->
                    if (button == AlertButton.PRIMARY) {
                        leaveCareProgramAsync(programId)
                    }

                    true
                })

    }

    /**
     * Begin leave program
     */
    private fun leaveCareProgramAsync(programId: String) {
        dependencyProvider.resolve<CareProgramManager>().leaveCareProgramAsync(programId)
    }

    /**
     * Helper function to format Consent Message
     */
    private fun createAppListText(programName: String, consentedApps: List<String>) : String {
        var appListString = ""
        val appText = getString(R.string.app_text)
        val andText = getString(R.string.and_text)
        val appCount = consentedApps.size
        for (index in 0..(appCount - 1)) {
            appListString += consentedApps[index] + " " + appText
            if (appCount >= 3) {
                if (index < appCount - 1) {
                    appListString += ", "
                }
                if (index == appCount - 2) {
                    appListString += andText
                    appListString += " "
                }
            } else if (appCount == 2 && index == 0) {
                appListString += " "
                appListString += andText
                appListString += " "
            }
        }
        return appListString
    }

    /**
     * Checks if any devices are registered and displays the My Inhalers screen or the Scan Inhaler Intro screen.
     */
    private fun showDeviceList() {
        showView(DeviceListFragment::class, FragmentAnimation.SLIDE_OVER)
    }

    /**
     * Initialize the events for the Device screens
     *
     * @param savedInstanceState The arguments/savedState Bundle
     */
    private fun initDeviceFragments(savedInstanceState: Bundle?) {
        // Common state for device screens
        inhalerRegistrationCommonState = InhalerRegistrationCommonState(dependencyProvider)
        if (savedInstanceState != null) {
            // restore the state from the bundle
            inhalerRegistrationCommonState!!.loadInstanceState(
                    savedInstanceState.getBundle(INHALER_REGISTRATION_COMMON_STATE_KEY))
        }

        dependencyProvider.register(inhalerRegistrationCommonState!!)

        dependencyProvider.register(IntroViewModel.Events::class,
                object : IntroViewModel.Events {
                    override fun onNext() {
                        if(!dependencyProvider.resolve<ApplicationSettings>().hasUserAcceptedTermsOfUse) {
                            showView(ConsentFragment::class, FragmentAnimation.FADE)
                        } else {
                            showView(LoginFragment::class, FragmentAnimation.FADE)
                        }
                    }
                })

        // Add handlers for the Device fragment events.
        dependencyProvider.register(IntroEvents::class,
                object : IntroEvents {
                    override fun onStartScan() {
                        showView(ScanFragment::class, FragmentAnimation.FADE)
                    }
                })

        dependencyProvider.register(ScanViewModel.ScanEvents::class,
                object : ScanViewModel.ScanEvents {
                    override fun onQRCodeScanned(isNewDevice: Boolean) {
                        if (isNewDevice) {
                            showView(SelectInhalerNameFragment::class, FragmentAnimation.FADE)
                        } else {
                            // the re-activated device might have changes, save the device.
                            saveDevice(InhalerRegistrationCommonState.Mode.Reactivate)

                            // a previously deleted device was scanned and reactivated, so go to the
                            // Device Info screen
                            val arguments = DeviceInfoFragment.createFragmentArguments(
                                    inhalerRegistrationCommonState!!.serialNumber!!,
                                    if (inhalerRegistrationCommonState!!.isFirstAdd) DeviceInfoViewModel.Mode.FIRST_ADD else DeviceInfoViewModel.Mode.ADD)

                            replaceView(DEVICE_STACK_TAG, true, DeviceInfoFragment::class,
                                    FragmentAnimation.FADE, null, null, arguments)
                        }
                    }

                    override fun onTroubleScanning() {
                        showView(ContactSupportFragment::class)
                    }
                })

        dependencyProvider.register(SelectInhalerNameEvents::class,
                object : SelectInhalerNameEvents {
                    override fun chooseCustomName() {
                        showView(CustomDeviceNameFragment::class, FragmentAnimation.FADE)
                    }

                    override fun next() {
                        val arguments = DeviceInfoFragment.createFragmentArguments(
                                inhalerRegistrationCommonState!!.serialNumber!!,
                                if (inhalerRegistrationCommonState!!.isFirstAdd) DeviceInfoViewModel.Mode.FIRST_ADD else DeviceInfoViewModel.Mode.ADD)

                        replaceView(DEVICE_STACK_TAG, true, DeviceInfoFragment::class,
                                FragmentAnimation.FADE, null, null, arguments)
                    }
                })

        dependencyProvider.register(CustomDeviceNameViewModel.NameEvents::class,
                object : CustomDeviceNameViewModel.NameEvents {
                    override fun onNameComplete() {
                        if(inhalerRegistrationCommonState!!.mode == InhalerRegistrationCommonState.Mode.Edit) {
                            popView()
                        } else {
                            inhalerRegistrationCommonState!!.save()
                            dependencyProvider.resolve<SelectInhalerNameEvents>().next()
                        }
                    }
                })

        dependencyProvider.register(DeviceInfoViewModel.InfoEvents::class,
                object : DeviceInfoViewModel.InfoEvents {
                    override fun onEditDeviceName(serialNumber: String) {
                        inhalerRegistrationCommonState!!.initForEdit(serialNumber)
                        showView(SelectInhalerNameFragment::class, FragmentAnimation.FADE, DEVICE_STACK_TAG, null)
                    }

                    override fun onDone(mode: DeviceInfoViewModel.Mode) {
                        if (mode == DeviceInfoViewModel.Mode.FIRST_ADD) {
                            insertView(DeviceListFragment::class, FragmentAnimation.SLIDE_OVER, null, null)
                        }

                        if (mode == DeviceInfoViewModel.Mode.FIRST_ADD || mode == DeviceInfoViewModel.Mode.ADD) {
                            // This was the first add, so we skipped the My Inhalers screen, but now
                            // we want to display it when we close the add inhaler screens.
                            replaceView(null, false, InhalerRegistrationPopup::class,
                                    FragmentAnimation.FADE, null, null, null)
                        } else {
                            // EDIT or DELETED or DELTED_LAST mode

                            //if an inhalation feedback is displayed when the inhaler is being
                            // deleted, the device info is on the backstack, check and pop it or remove it from the backstack
                            if(currentFragment is DeviceInfoFragment) {
                                popView()
                            } else {
                                backStack.remove { obj -> obj.fragmentClass == DeviceInfoFragment::class.java }
                            }
                        }
                    }
                })
    }

    private fun saveDevice(mode: InhalerRegistrationCommonState.Mode) {
        // the re-activated device might have changes, save the device.
        DataTask<Unit, Device>("DashboardActivity_saveDevice")
                .inBackground {
                    dependencyProvider.resolve<InhalerRegistrationCommonState>().mode = mode
                    dependencyProvider.resolve<InhalerRegistrationCommonState>().save()
                }
                .execute()
    }

    /**
     * Initialize the events for the Device screens
     */
    private fun initNotificationFragments() {
        dependencyProvider.register(NotificationPopupViewModel.Events::class,
                object : NotificationPopupViewModel.Events {
                    override fun onClose(notificationInfo: NotificationInfo) {
                        onBackPressed()
                    }

                    override fun onButton(notificationInfo: NotificationInfo) {
                        val notificationCategory = NotificationCategories.findCategory(notificationInfo)
                        val buttonNavigationClass = notificationCategory.buttonNavigationClass
                        if (buttonNavigationClass != null) {
                            //if the notification had a stack tag, set that information
                            replaceView(null, false, buttonNavigationClass, FragmentAnimation.FADE, null, notificationCategory.stackTag, null)
                        } else {
                            popView()
                        }
                    }

                    override fun onHyperlink(notificationInfo: NotificationInfo) {
                        val notificationCategory = NotificationCategories.findCategory(notificationInfo)
                        val hyperlinkNavigationClass = notificationCategory.hyperlinkNavigationClass
                        if (hyperlinkNavigationClass != null) {
                            // if the view being shown such as the IFU is in the back stack,
                            // remove it from the back stack before showing it again.
                            backStack.remove { obj -> obj.fragmentClass == hyperlinkNavigationClass }
                            showView(hyperlinkNavigationClass, FragmentAnimation.FADE)
                        } else {
                            popView()
                        }
                    }
                })
    }

    /**
     * This method checks if the application has expired and if it has,
     * displays a message and closes the application.
     */
    private fun checkForExpiration() {
        val expirationDate = LocalDate.parse(BuildConfig.EXPIRATION_DATE, DateTimeFormatter.ofPattern("M/d/yyyy"))

        // Use real OS time for app expiration, not hypertime
        val currentDate = LocalDate.now()

        if (currentDate.isAfter(expirationDate)) {
            val substitutionMap = HashMap<String, Any>()
            substitutionMap.put("AppName", getString(R.string.app_name))
            val expiration_message
                    = getLocalizedString(R.string.appIsExpired_text, substitutionMap)

            systemAlertManager?.showAlert(message = expiration_message) {
                finishAndRemoveTask()
                true
            }
        }
    }

    /**
     * This method checks the status of google play services and
     * updates them if necessary.
     */
    private fun checkAndUpdateGooglePlayServices() {
        val response = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this)

        if (response != ConnectionResult.SUCCESS) {
            val dialog = GoogleApiAvailability.getInstance().getErrorDialog(this, response, 1)
            dialog.show()
        }
    }

    /**
     * Gets the layout id for an alert.
     *
     * @param id The id of the dialog
     * @return The layout id to use or null to use the default layout.
     */
    override fun getAlertConfiguration(id: String?): AlertConfiguration? {
        var config = super.getAlertConfiguration(id)

        if (config == null) {
            if (id == CALL_SUPPORT_ALERT_ID) {
                config = AlertConfiguration(AlertType.ALERT_DIALOG)
            } else if(id == CloudManagerNotificationId.EMANCIPATION_TOMORROW || id == CloudManagerNotificationId.EMANCIPATION_IN_7_DAYS) {
                config = AlertConfiguration(AlertType.FULL_SCREEN_WITH_IMAGE, R.layout.alert_fragment_with_image_and_hyperlink)
            }
        }

        return config
    }

    companion object {
        private val INHALER_REGISTRATION_COMMON_STATE_KEY = "inhalerRegistrationCommonState"
        private val DASHBOARD_STATE_KEY = "dashboardState"

        private val DEVICE_STACK_TAG = "device"

        val WALKTHROUGH_STACK_TAG = "walkthrough"
        val INTRO_STACK_TAG = "intro"
        val CARE_PROGRAMS_STACK_TAG = "AddCareProgram"

        // This stack tag is not set anywhere. It is used to clear all the fragments from the backstack.
        val CLEAR_STACK_TAG = "clear"

        private val IS_WALKTHROUGH_IN_PROGRESS_BUNDLE_KEY = "IsWalkthroughInProgress"
        private val VIDEO_LIBRARY_URL = "https://myproair.com/respiclick/asthma-resources/videos.aspx"
        private val BLUETOOTH_DISABLED_DIALOG_DISPLAYED_KEY = "BluetoothDisabledDialogDisplayed"
        private val LOCATION_DIALOG_DISPLAYED_KEY = "LocationDialogDisplayed"
        private val WEB_LINK_URL = "https://myproair.com/respiclick/default.aspx"

        // From Api 27, requesting a permission does not grant related permissions. So,
        // include fine location permission in addition to coarse location permission.
        private val LOCATION_PERMISSION = arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)

        val CALL_SUPPORT_ALERT_ID = "CallSupport"
        val WEB_NAVIGATION_ID = "NavigateToWeb"

    }
}
