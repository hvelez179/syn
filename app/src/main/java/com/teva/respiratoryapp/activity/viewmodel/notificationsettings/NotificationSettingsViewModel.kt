//
// NotificationSettingsViewModel.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.activity.viewmodel.notificationsettings

import android.databinding.Observable
import android.databinding.ObservableField
import com.teva.utilities.services.DependencyProvider
import com.teva.environment.models.DailyEnvironmentalReminderManager
import com.teva.respiratoryapp.mvvmframework.ui.FragmentViewModel
import com.teva.userfeedback.model.DailyAssessmentReminderManager

/**
 * This class represents the ViewModel for the NotificationSettings screen.
 *
 * @param dependencyProvider The dependency injection mechanism.
 */

class NotificationSettingsViewModel(dependencyProvider: DependencyProvider)
    : FragmentViewModel(dependencyProvider) {

    var selfAssessmentNotificationEnabled = ObservableField<Boolean>()
    var environmentNotificationEnabled = ObservableField<Boolean>()

    /**
     * Method called when the fragment's onStart() lifecycle method is called.
     * This method retrieves the current notification setting values and adds handlers for
     * handling changes to notification settings.
     */
    override fun onStart() {
        val dsaReminderManager = dependencyProvider.resolve<DailyAssessmentReminderManager>()
        val environmentalReminderManager = dependencyProvider.resolve<DailyEnvironmentalReminderManager>()

        val dsaReminderSetting = dsaReminderManager.reminderSetting
        val environmentReminderSetting = environmentalReminderManager.reminderSetting

        if (dsaReminderSetting == null || !dsaReminderSetting.isEnabled) {
            selfAssessmentNotificationEnabled.set(false)
        } else {
            selfAssessmentNotificationEnabled.set(true)
        }

        if (environmentReminderSetting == null || !environmentReminderSetting.isEnabled) {
            environmentNotificationEnabled.set(false)
        } else {
            environmentNotificationEnabled.set(true)
        }

        selfAssessmentNotificationEnabled.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(observable: Observable, i: Int) {
                dsaReminderManager.enableReminder(selfAssessmentNotificationEnabled.get()!!)
            }
        })

        environmentNotificationEnabled.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(observable: Observable, i: Int) {
                environmentalReminderManager.enableReminder(environmentNotificationEnabled.get()!!)
            }
        })
    }
}
