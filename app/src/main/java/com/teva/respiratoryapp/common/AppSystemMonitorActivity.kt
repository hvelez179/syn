/*
 *
 *  AppSystemMonitorActivity.kt
 *  app
 *
 *  Copyright © 2018 Teva. All rights reserved.
 *
 */

package com.teva.respiratoryapp.common

import com.teva.common.utilities.SystemMonitorActivity
import org.threeten.bp.Duration

/**
 * This class represents the Application activities included in system monitoring.
 * This class implements the SystemMonitorActivity and is passed as a parameter to the SystemMonitorMessage.
 */
sealed class AppSystemMonitorActivity : SystemMonitorActivity {
    class FirstUse : AppSystemMonitorActivity()
    class Foregrounded : AppSystemMonitorActivity()
    class Backgrounded : AppSystemMonitorActivity()
    class Consent(timingValue: Duration) : AppSystemMonitorActivity() {
        override val timingOnly: Boolean = true
        override val timing: Duration? = timingValue
    }
    class InvalidQRCodeScanned : AppSystemMonitorActivity()
    class ExistingActiveInhalerQRCodeScanned : AppSystemMonitorActivity()
    class ExistingInactiveInhalerReactivated : AppSystemMonitorActivity()
    class InhalerRegisteredViaQRCode(val connectedInhalers: Int) : AppSystemMonitorActivity() {
        override val activityLabel: String? = "Connected Inhalers: $connectedInhalers"
    }
    class InhalerRemoved : AppSystemMonitorActivity()
    class Adding6thInhaler : AppSystemMonitorActivity()
    class LoginPageLoaded : AppSystemMonitorActivity()
    class RegistrationPageLoaded : AppSystemMonitorActivity()
    class ForgotPasswordPageLoaded : AppSystemMonitorActivity()

    override val source: String
        get() = getActivitySource()

    override val activityName: String
        get() = getName()

    override val activityLabel: String? = null

    override val timing: Duration? = null

    override val timingOnly: Boolean = false
    
    private fun getActivitySource(): String {
        return when(this.javaClass) {
            Foregrounded::class.java, Backgrounded::class.java, FirstUse::class.java, Consent::class.java -> "App Use"
            InvalidQRCodeScanned::class.java, InhalerRegisteredViaQRCode::class.java, Adding6thInhaler::class.java, ExistingActiveInhalerQRCodeScanned::class.java, ExistingInactiveInhalerReactivated::class.java, InhalerRemoved::class.java -> "Inhalers"
            LoginPageLoaded::class.java, RegistrationPageLoaded::class.java, ForgotPasswordPageLoaded::class.java -> "DHP"
            else -> ""
        }

    }

    private fun getName(): String {
        return when(this.javaClass) {
            Foregrounded::class.java -> "App will enter foreground"
            Backgrounded::class.java -> "App entered background"
            FirstUse::class.java -> "First time launch"
            Adding6thInhaler::class.java -> "Add inhaler with 5 existing inhalers"
            InhalerRegisteredViaQRCode::class.java -> "Inhaler registered via QR code"
            Consent::class.java -> "Consent"
            InvalidQRCodeScanned::class.java -> "Invalid QR Code Scanned"
            ExistingActiveInhalerQRCodeScanned::class.java -> "Existing Active Inhaler QR Code Scanned"
            ExistingInactiveInhalerReactivated::class.java -> "Existing Inactive Inhaler Reactivated"
            InhalerRemoved::class.java -> "Inhaler Removed"
            LoginPageLoaded::class.java -> "Login Page Loaded"
            RegistrationPageLoaded::class.java -> "Registration Page Loaded"
            ForgotPasswordPageLoaded::class.java -> "Forgot Password Page Loaded"
            else -> ""
        }

    }

}