package com.teva.respiratoryapp.activity.viewmodel.setup

import android.databinding.Bindable
import android.support.annotation.MainThread
import com.teva.cloud.dataentities.UserProfile
import com.teva.cloud.enumerations.UserProfileStatusCode
import com.teva.cloud.messages.SyncCloudMessage
import com.teva.cloud.messages.UserProfileMessage
import com.teva.cloud.models.CloudManagerNotificationId
import com.teva.cloud.models.userprofile.UserProfileManager
import com.teva.common.services.AlarmService
import com.teva.common.services.TimeService
import com.teva.utilities.services.DependencyProvider
import com.teva.utilities.utilities.Logger.Level.ERROR
import com.teva.utilities.utilities.Logger.Level.VERBOSE
import com.teva.common.utilities.Messenger
import com.teva.common.utilities.toLocalDate
import com.teva.notifications.enumerations.RepeatType
import com.teva.notifications.models.NotificationManager
import com.teva.respiratoryapp.BR
import com.teva.respiratoryapp.R
import com.teva.respiratoryapp.activity.viewmodel.LoadingEvents
import com.teva.respiratoryapp.activity.viewmodel.SupportEvents
import com.teva.respiratoryapp.activity.viewmodel.dashboard.ContactSupportViewModel
import com.teva.respiratoryapp.activity.viewmodel.setup.ProfileItemViewModel.ItemType.ADD_ANOTHER_DEPENDENT
import com.teva.respiratoryapp.activity.viewmodel.setup.ProfileItemViewModel.ItemType.PROFILE
import com.teva.respiratoryapp.common.DateTimeLocalization
import com.teva.respiratoryapp.mvvmframework.ui.FragmentListViewModel
import com.teva.respiratoryapp.mvvmframework.utils.ObservableHashSet
import com.teva.respiratoryapp.services.alert.SystemAlertManager
import org.greenrobot.eventbus.Subscribe
import org.threeten.bp.LocalTime
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime

/**
 * ViewModel for the Profile Setup Screen that chooses the current user of teh application.
 */
class ProfileSetupViewModel(dependencyProvider : DependencyProvider)
    : FragmentListViewModel<ProfileItemViewModel>(dependencyProvider) {

    override val selectedItemSet = ObservableHashSet<ProfileItemViewModel>()
    private val dateTimeLocalization: DateTimeLocalization = dependencyProvider.resolve()
    private val userProfileManager = dependencyProvider.resolve<UserProfileManager>()
    private val userProfiles: ArrayList<UserProfile> = ArrayList()

    private var maxDependentAge = 18
    private val timeService: TimeService = dependencyProvider.resolve()

    /**
     * The enable state of the Next button.
     */
    @get:Bindable
    @get:JvmName("getNextEnabled")
    var isNextEnabled: Boolean = false
        get() { return field }
        set(value) {
            field = value

            notifyPropertyChanged(BR.nextEnabled)
        }

    /**
     * Gets the list of items.
     */
    override var items: List<ProfileItemViewModel> = ArrayList()
        set(value) {
            field = value
            notifyListChanged()
        }

    /**
     * Method called by the BaseFragment when the fragment's onStart() lifecycle method is called.
     */
    override fun onStart() {
        super.onStart()
        dependencyProvider.resolve<Messenger>().subscribe(this)
        userProfileManager.getAllProfilesAsync()
        dependencyProvider?.resolve<LoadingEvents>()?.showLoadingIndicator()
    }

    /**
     * Method called by the BaseFragment when the fragment's onStop() lifecycle method is called.
     */
    override fun onStop() {
        super.onStop()
        dependencyProvider.resolve<Messenger>().unsubscribeToAll(this)
    }

    /**
     * This method is the handler for the user profile message.
     */
    @Subscribe
    @MainThread
    fun onUserProfileMessage(userProfileMessage: UserProfileMessage) {
        when(userProfileMessage.messageCode) {
            UserProfileStatusCode.DID_GET_ALL_PROFILES -> {
                dependencyProvider.resolve<LoadingEvents>().hideLoadingIndicator()
                getAllProfilesCompleted(userProfileMessage.profileData.toList(), userProfileMessage.messageCode)
            }
            UserProfileStatusCode.DID_SETUP_ACTIVE_PROFILE -> {
                val profile = userProfileMessage.profileData.first()
                if (profile.isAccountOwner != true){
                    scheduleEmancipationNotifications(profile)
                }
                dependencyProvider.resolve<LoadingEvents>().hideLoadingIndicator()
                // if valid and saved, then navigate to next screen
                dependencyProvider.resolve<Events>().onNext()
                dependencyProvider.resolve<Messenger>().post(SyncCloudMessage())
            }
            UserProfileStatusCode.ERROR_DURING_GET_ALL_PROFILES -> {
                dependencyProvider.resolve<LoadingEvents>().hideLoadingIndicator()
                dependencyProvider.resolve<SystemAlertManager>().showAlert(
                        id = ProfileSetupViewModel.PROFILE_RETRIEVAL_FAILED_ALERT_ID,
                        messageId = R.string.profile_retrieval_failed_alert_message,
                        titleId = R.string.profile_retrieval_failed_alert_title,
                        primaryButtonTextId = R.string.ok_text,
                        secondaryButtonTextId = R.string.contact_teva_support
                )
                dependencyProvider.resolve<Events>().failedToRetrieveProfiles()
            }
            UserProfileStatusCode.ERROR_DURING_SETUP_ACTIVE_PROFILE -> {
                dependencyProvider.resolve<LoadingEvents>().hideLoadingIndicator()
                //Todo - Show error message.
                dependencyProvider.resolve<SystemAlertManager>().showAlert(
                        id = ProfileSetupViewModel.ACTIVE_PROFILE_FAILED_ALERT_ID,
                        messageId = R.string.profile_setup_failed_alert_message,
                        titleId = R.string.profile_setup_failed_alert_title,
                        primaryButtonTextId = R.string.ok_text,
                        secondaryButtonTextId = R.string.contact_teva_support
                )
            }
        }
    }

    private fun getAllProfilesCompleted(profiles: List<UserProfile>, status: UserProfileStatusCode) {
        if(status != UserProfileStatusCode.DID_GET_ALL_PROFILES) {
            logger.log(ERROR, "ProfileSetupViewModel::getAllProfilesCompleted failed")
            return
        }
        userProfiles.addAll(profiles)

        // Do not allow emancipated user profiles to be added to the list
        val validProfiles = profiles.filter { !isEmancipated(it) }
        updateProfileList(validProfiles)
    }

    private fun updateProfileList(profiles: List<UserProfile>) {
        val newList = ArrayList<ProfileItemViewModel>()

        newList.addAll(profiles.map {
            ProfileItemViewModel(it.profileId, it.firstName + " " + it.lastName, dateTimeLocalization.toShortMonthDayYear(it.dateOfBirth!!), it)
        })


        // user and max 5 dependents
        if (newList.size < 6) {
            newList.add(ProfileItemViewModel(ADD_ANOTHER_DEPENDENT))
        }

        logger.log(VERBOSE, "Here I am?")
        items = newList
    }


    /**
     * Returns true if the given profile has been emancipated.
     * Emancipated is defined as being a dependent and then turning 18.
     */
    private fun isEmancipated(profile: UserProfile) : Boolean {
        var emancipated = false
        val dob = profile.dateOfBirth
        if (dob == null) {
            return true
        }
        val today = timeService.now()
        val maxAccountOwnerBirthDate = today.toLocalDate().minusYears(maxDependentAge.toLong())
        if (dob <= maxAccountOwnerBirthDate && !(profile.isAccountOwner ?: false)) {
            emancipated = true
        }
        return emancipated
    }

    /**
     * Schedules notifications informing the user they will soon be emancipated; encouraging them to export their data.
     * Notifications are scheduled for 7 days and 1 day before the date the user turns 18, using application time.
     * @param profile: the profile for which to schedule notifications
     */
    private fun scheduleEmancipationNotifications(profile: UserProfile) {
        val emancipationAge = 18L
        val emancipationNotificationDate = profile.dateOfBirth?.plusYears(emancipationAge)
        if (emancipationNotificationDate == null) {
            return
        }
        val emancipationIn7DaysNotificationDate = emancipationNotificationDate.plusDays(-7)
        val emancipationTomorrowNotificationDate = emancipationNotificationDate.plusDays(-1)
        val now = timeService.now().toLocalDate()

        val daysFromNow7Day = emancipationIn7DaysNotificationDate.compareTo(now)
        val daysFromNowTomorrow = emancipationTomorrowNotificationDate.compareTo(now)
        val daysFromNowEmancipated = emancipationNotificationDate.compareTo(now)

//        val tradeName = BaseViewModel.tradeNameStringAndValue
//        val notificationData: Map<String, Any> = mapOf(tradeName.key to tradeName.value as Any)
        if (daysFromNow7Day >= 0)
            dependencyProvider.resolve<NotificationManager>().setNotification(CloudManagerNotificationId.EMANCIPATION_IN_7_DAYS, mapOf(), daysFromNow = daysFromNow7Day,timeOfDay = LocalTime.NOON, repeatType = RepeatType.NONE)
        if (daysFromNowTomorrow >= 0)
            dependencyProvider.resolve<NotificationManager>().setNotification(CloudManagerNotificationId.EMANCIPATION_TOMORROW, mapOf(), daysFromNow = daysFromNowTomorrow,timeOfDay = LocalTime.NOON, repeatType = RepeatType.NONE)
        if (daysFromNowEmancipated >= 0) {
            dependencyProvider.resolve<AlarmService>().setAlarm(CloudManagerNotificationId.EMANCIPATED, ZonedDateTime.of(emancipationNotificationDate, LocalTime.NOON, ZoneId.systemDefault()).toInstant(), null)
        }

    }


    /**
     * Called when an item is clicked.
     *
     * @param item The item that was clicked.
     */
    override fun onItemClicked(item: ProfileItemViewModel) {
        selectedItemSet.clear()

        if (item.itemType == PROFILE) {
            selectedItemSet.add(item)
        } else {
            dependencyProvider.resolve<Events>().addDependent(userProfiles)
        }

        isNextEnabled = selectedItemSet.isNotEmpty()
    }


    /**
     * Click handler for the Next CTA button
     */
    fun onNext() {
        selectedItemSet.first().let { selectedProfile ->
            userProfileManager.setupActiveProfileAsync(selectedProfile.userProfile!!)
            dependencyProvider.resolve<LoadingEvents>().showLoadingIndicator()
        }
        //Todo: Display a spinner
    }

    /**
     * Click handler for the Contact Teva Support hyperlink
     */
    fun onContactSupport() {
        dependencyProvider.resolve<ContactSupportViewModel.Events>().onContactSupport()
    }

    /**
     * Viewmodel events interface
     */
    interface Events {
        fun onNext()

        fun addDependent(existingProfiles: ArrayList<UserProfile>)

        fun failedToRetrieveProfiles()
    }

    companion object {
        val ACTIVE_PROFILE_FAILED_ALERT_ID = "ActiveProfileFailedAlert"
        val PROFILE_RETRIEVAL_FAILED_ALERT_ID = "ProfileRetrievalFailedAlert"
    }
}