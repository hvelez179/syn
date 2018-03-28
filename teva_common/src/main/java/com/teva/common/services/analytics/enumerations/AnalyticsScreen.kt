package com.teva.common.services.analytics.enumerations

import com.teva.common.utilities.splitFromCamelCase

/**
 * This enumeration lists the screen type for analytics.
 */
sealed class AnalyticsScreen() {
    class Dashboard : AnalyticsScreen()
    class Environment : AnalyticsScreen()
    class MyInhalers : AnalyticsScreen()
    class ScanningInstructions : AnalyticsScreen()
    class ScanInhaler : AnalyticsScreen()
    class InhalerName : AnalyticsScreen()
    class NameYourInhaler : AnalyticsScreen()
    class EditInhaler : AnalyticsScreen()
    class Tracker : AnalyticsScreen()
    class DailyReport : AnalyticsScreen()
    class DailySelfAssessment : AnalyticsScreen()
    class UserReport : AnalyticsScreen()
    class TevaSupport : AnalyticsScreen()
    class Menu : AnalyticsScreen()
    class InstructionsForUse : AnalyticsScreen()
    class About : AnalyticsScreen()
    class PrivacyNotice : AnalyticsScreen()
    class ThirdPartyLicenses : AnalyticsScreen()
    class TermsOfUse : AnalyticsScreen()
    class NotificationSettings : AnalyticsScreen()
    class Notification(type: String) : AnalyticsScreen() {
        override val screenName: String = "Notification: ${type.splitFromCamelCase()}"
    }
    class Alert(title: String) : AnalyticsScreen() {
        override val screenName: String = "Alert: ${title.replace("_text", "").replace("Title", "").splitFromCamelCase()}"
    }
    class Walkthrough : AnalyticsScreen()
    class Login : AnalyticsScreen()
    class Introduction : AnalyticsScreen()
    class CareProgram : AnalyticsScreen()
    class AddCareProgram : AnalyticsScreen()
    class ConsentToShareData : AnalyticsScreen()
    class SetupProfile : AnalyticsScreen()
    class AddDependent : AnalyticsScreen()
    class ClinicalSetupQrCode : AnalyticsScreen()
    class ClinicalSetupPseudoName : AnalyticsScreen()

    open val screenName: String = this.javaClass.simpleName.splitFromCamelCase()

}