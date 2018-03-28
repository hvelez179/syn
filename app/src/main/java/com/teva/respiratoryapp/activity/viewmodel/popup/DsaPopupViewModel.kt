//
// DsaPopupViewModel.kt
// app
//
// Copyright © 2017 Teva. All rights reserved
//

package com.teva.respiratoryapp.activity.viewmodel.popup

import android.app.NotificationManager
import android.databinding.Bindable
import android.util.Log
import com.teva.analysis.messages.UpdateAnalysisDataMessage
import com.teva.common.services.TimeService
import com.teva.common.utilities.DataTask
import com.teva.utilities.services.DependencyProvider
import com.teva.common.utilities.Messenger
import com.teva.respiratoryapp.BR
import com.teva.respiratoryapp.models.engagementbooster.EngagementBoosterNotificationId
import com.teva.userfeedback.enumerations.UserFeeling
import com.teva.userfeedback.model.DSANotificationId
import com.teva.userfeedback.model.UserFeelingManager
import org.threeten.bp.Instant

/**
 * Viewmodel for the DSA popup.
 *
 * @param dependencyProvider The dependency injection mechanism.
 */
class DsaPopupViewModel(dependencyProvider: DependencyProvider,now:Instant)
    : DashboardPopupViewModel(dependencyProvider) {

    val dsaTS = now;

    @get:Bindable
    var userFeeling = UserFeeling.UNKNOWN

        set(value) {
            field = value
            notifyPropertyChanged(BR.userFeeling)
        }

    init {

        popupColor = PopupColor.WHITE
        headerBarVisible = false
        arrowState = PopupDashboardButton.DSA
        buttonState = PopupDashboardButton.ALL
        buttonsDimmed = false
    }

    /**
     * Click handler for the DSA choices.
     * @param dsa The DSA value chosen by the user.
     */
    fun onDsa(dsa: UserFeeling) {
        userFeeling = dsa
    }

    override fun onButton() {
        saveDsa()
    }

    /**
     * Save the DSA value in the database.
     */
    private fun saveDsa() {
        val dsaToSave = userFeeling
        val dsaTimeStamp = dsaTS;

        Log.w("save data","dsaToSave==>"+dsaToSave)
        Log.w("save data","dsaTimeStamp==>"+dsaTimeStamp)


        DataTask<Unit, Unit>("DSAConfirmPopupViewModel_save")
                .inBackground {
                    val userFeelingManager = dependencyProvider.resolve<UserFeelingManager>()
                    userFeelingManager.saveUserFeeling(dsaToSave, dsaTimeStamp)
                    dependencyProvider.resolve<Messenger>().publish(UpdateAnalysisDataMessage(ArrayList<Any>()))
                }
                .onResult {
                    // When DSA is saved, clear the DSA reminder and engagement booster.
                    dependencyProvider.resolve<NotificationManager>().cancel(DSANotificationId.DSA_REMINDER, 0)
                    dependencyProvider.resolve<NotificationManager>().cancel(EngagementBoosterNotificationId.DAILY_SELF_ASSESSMENT_TOOL, 0)
                    onBackPressed()
                }
                .execute()
    }
}
