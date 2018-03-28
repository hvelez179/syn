//
// DashboardViewModel.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.activity.viewmodel.dashboard

import android.databinding.Bindable
import com.teva.analysis.enumerations.SummaryTextId
import com.teva.analysis.enumerations.SummaryTextId.*
import com.teva.analysis.messages.SummaryUpdatedMessage
import com.teva.analysis.model.AnalyzedDataProvider
import com.teva.common.utilities.DataTask
import com.teva.utilities.services.DependencyProvider
import com.teva.common.utilities.Messenger
import com.teva.respiratoryapp.BR
import com.teva.respiratoryapp.R
import com.teva.respiratoryapp.activity.viewmodel.SupportEvents
import com.teva.respiratoryapp.activity.viewmodel.environment.EnvironmentViewModel
import com.teva.respiratoryapp.mvvmframework.ui.FragmentListViewModel
import org.greenrobot.eventbus.Subscribe
import java.util.*

/**
 * ViewModel for the Dashboard screen.
 *
 * @param dependencyProvider Dependency Injection object.
 */
class DashboardViewModel(dependencyProvider: DependencyProvider) : FragmentListViewModel<MenuItem>(dependencyProvider) {

    enum class MENU_ITEM_ID {
        INSTRUCTIONS_FOR_USE_MENU_ITEM_ID,
        HOW_TO_USE_MENU_ITEM_ID,
        NOTIFICATION_SETTINGS_MENU_ITEM_ID,
        HEALTHCARE_PROGRAM_MENU_ITEM_ID,
        VIDEO_LIBRARY_MENU_ITEM_ID,
        ABOUT_MENU_ITEM_ID
    }


    @get:Bindable
    var summaryCard: SummaryTextId = NEUTRAL_MESSAGE
        set(card) {
            field = card
            notifyPropertyChanged(BR.summaryCard)
        }

    /**
     * The summary message text.
     */
    @get:Bindable
    var summaryMessage: String? = null
        set(summaryMessage) {
            field = summaryMessage
            notifyPropertyChanged(BR.summaryMessage)
        }

    var closeMenuCallback: (()->Unit)? = null

    private val menuItems = ArrayList<MenuItem>()

    val weatherSummaryViewModel: EnvironmentViewModel = EnvironmentViewModel(dependencyProvider)

    init {
        populateMenuItems()
    }

    override val items: List<MenuItem>
        get() = menuItems

    override fun onItemClicked(item: MenuItem) {
        dependencyProvider.resolve<DashboardEvents>().onMenuItemClicked(item.id)
    }

    /**
     * This method creates the menu items and adds them to the menu item list.
     */
    private fun populateMenuItems() {
        menuItems.add(MenuItem(MENU_ITEM_ID.INSTRUCTIONS_FOR_USE_MENU_ITEM_ID,
                getString(R.string.menuInstructionsForUseItem_text)))
        menuItems.add(MenuItem(MENU_ITEM_ID.HOW_TO_USE_MENU_ITEM_ID,
                getString(R.string.menuHowToUseAppItem_text)))
        menuItems.add(MenuItem(MENU_ITEM_ID.VIDEO_LIBRARY_MENU_ITEM_ID,
                getString(R.string.menuVideoLibraryItem_text)))
        menuItems.add(MenuItem(MENU_ITEM_ID.HEALTHCARE_PROGRAM_MENU_ITEM_ID,
                getString(R.string.menuHealthcareProgram_text)))
        menuItems.add(MenuItem(MENU_ITEM_ID.NOTIFICATION_SETTINGS_MENU_ITEM_ID,
                getString(R.string.remindersTitle_text)))
        menuItems.add(MenuItem(MENU_ITEM_ID.ABOUT_MENU_ITEM_ID,
                getString(R.string.menuAboutItem_text)))
    }

    /**
     * Touch handler for the History button.
     */
    fun showHistory() {
        dependencyProvider.resolve<DashboardEvents>().onTracker()
    }

    /**
     * Touch handler for the My Inhalers button.
     */
    fun showDevices() {
        dependencyProvider.resolve<DashboardEvents>().onDeviceList()
    }

    /**
     * Touch handler for Environment button.
     */
    fun showEnvironment() {
        dependencyProvider.resolve<DashboardEvents>().onEnvironment()
    }

    /**
     * Touch handler for Menu icon button.
     */
    fun showMenu() {
        dependencyProvider.resolve<DashboardEvents>().onMenu()
    }

    /**
     * Touch handler for Report icon button.
     */
    fun showReport() {
        DataTask<Void, Boolean>("DashboardViewModel_UserReportDataCheck")
                .inBackground {
                    dependencyProvider.resolve<AnalyzedDataProvider>().trackingStartDate != null
                }
                .onResult { dataExists ->
                    if (dataExists ?: false) {
                        dependencyProvider.resolve<DashboardEvents>().onReport()
                    } else {
                        dependencyProvider.resolve<DashboardEvents>().onReportEmpty()
                    }
                }
                .execute()
    }

    /**
     * Touch handler for DSA icon button.
     */
    fun showDsa() {
        dependencyProvider.resolve<DashboardEvents>().onDsa()
    }

    /**
     * Touch handler for Support icon button.
     */
    fun showSupport() {
        dependencyProvider.resolve<SupportEvents>().onSupport()
    }

    /**
     * Method called by the BaseFragment when the fragment's onStart() lifecycle method is called.
     */
    override fun onStart() {
        super.onStart()

        dependencyProvider.resolve<Messenger>().subscribe(this)
        weatherSummaryViewModel.onStart()

        updateSummaryMessage()
    }

    /**
     * Method called by the BaseFragment when the fragment's onStop() lifecycle method is called.
     */
    override fun onStop() {
        super.onStop()
        weatherSummaryViewModel.onStop()
        dependencyProvider.resolve<Messenger>().unsubscribeToAll(this)
    }

    fun onLogOut(id: Int) {
        dependencyProvider.resolve<DashboardEvents>().onLogOut()
    }

    /**
     * Message handler for the SummaryUpdatedMessage.

     * @param message The message received.
     */
    @Subscribe
    fun onSummaryUpdateMessage(message: SummaryUpdatedMessage) {
        updateSummaryMessage()
    }

    /**
     * Updates the summary message properties
     */
    fun updateSummaryMessage() {
        val summaryInfo = dependencyProvider.resolve<AnalyzedDataProvider>().summaryInfo

        summaryCard = summaryInfo?.id ?: NEUTRAL_MESSAGE

        summaryMessage = when(summaryCard) {
            OVERUSE -> getString(R.string.dashboardOveruse_text, summaryInfo?.message)
            NO_INHALERS -> getString(R.string.dashboardNoInhalers_text, summaryInfo?.message)
            EMPTY_INHALER -> getString(R.string.dashboardEmptyInhaler_text, summaryInfo?.message)
            ENVIRONMENT_MESSAGE -> null
            NEUTRAL_MESSAGE -> getString(R.string.dashboardNeutralMessage_text, summaryInfo?.message)
        }
    }

    /**
     * SelectInhalerNameEvents produced by the viewmodel to request actions by the activity.
     */
    interface DashboardEvents {
        /**
         * Requests the opening of the menu.
         */
        fun onMenu()

        /**
         * Handles the click event of a menu item.

         * @param menuItemId - the ID of the menu item that was clicked.
         */
        fun onMenuItemClicked(menuItemId: MENU_ITEM_ID)

        /**
         * Requests that the My Inhalers screen be displayed.
         */
        fun onDeviceList()

        /**
         * Requests that the Tracker screen be displayed.
         */
        fun onTracker()

        /**
         * Requests that the Environment screen be displayed.
         */
        fun onEnvironment()

        /**
         * Requests that the Report screen be displayed.
         */
        fun onReport()

        /**
         * Requests that the screen indicating that the report is empty is displayed.
         */
        fun onReportEmpty()

        /**
         * Requests that the DSA popup be displayed.
         */
        fun onDsa()

        /**
         * Requests that the Notification Settings screen be displayed.
         */
        fun onNotificationSettings()

        /**
         * Resquests to log out.
         */
        fun onLogOut()
    }
}
