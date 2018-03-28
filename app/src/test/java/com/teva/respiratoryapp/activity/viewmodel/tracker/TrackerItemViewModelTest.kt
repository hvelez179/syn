package com.teva.respiratoryapp.activity.viewmodel.tracker

import android.databinding.Observable
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever

import com.teva.analysis.entities.HistoryDose
import com.teva.analysis.model.HistoryDay
import com.teva.respiratoryapp.common.DateTimeLocalization
import com.teva.utilities.services.DependencyProvider
import com.teva.devices.entities.InhaleEvent
import com.teva.medication.entities.Medication
import com.teva.medication.entities.Prescription
import com.teva.respiratoryapp.testutils.BaseTest
import com.teva.userfeedback.entities.DailyUserFeeling
import com.teva.userfeedback.enumerations.UserFeeling

import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDate

import java.util.ArrayList

import org.junit.Assert.*

class TrackerItemViewModelTest : BaseTest() {

    private lateinit var dependencyProvider: DependencyProvider
    private lateinit var historyDay: HistoryDay

    @Before
    fun setup() {
        dependencyProvider = DependencyProvider.default
        dependencyProvider.unregisterAll()

        val medication = Medication()
        medication.drugUID = DRUG_UID
        medication.overdoseInhalationCount = 13
        val prescription = Prescription()
        prescription.medication = medication
        val prescriptionList = ArrayList<Prescription>()
        prescriptionList.add(prescription)

        historyDay = HistoryDay(date)
        historyDay.connectedInhalerCount = CONNECTED_INHALERS
        historyDay.prescriptions = prescriptionList

        val relieverDoses = ArrayList<HistoryDose>()
        relieverDoses.add(HistoryDose(DRUG_ID, ArrayList<InhaleEvent>()))
        relieverDoses.add(HistoryDose(DRUG_ID, ArrayList<InhaleEvent>()))
        historyDay.relieverDoses = relieverDoses

        val dailyUserFeeling = DailyUserFeeling()
        dailyUserFeeling.userFeeling = UserFeeling.GOOD

        historyDay.dailyUserFeeling = dailyUserFeeling

        val dateTimeLocalization: DateTimeLocalization = mock()
        whenever(dateTimeLocalization.toShortDayOfWeek(date)).thenReturn(DAY_OF_WEEK)
        whenever(dateTimeLocalization.toShortMonthDay(date)).thenReturn(MONTH_DAY)

        dependencyProvider.register(DateTimeLocalization::class, dateTimeLocalization)
    }


    @Test
    @Throws(Exception::class)
    fun testIsLoadedReturnsFalseWhenNotLoaded() {
        val viewModel = TrackerItemViewModel(dependencyProvider)

        assertFalse(viewModel.isLoaded)
    }

    @Test
    @Throws(Exception::class)
    fun testIsLoadedReturnsTrueWhenNotLoaded() {
        val viewModel = TrackerItemViewModel(dependencyProvider)
        viewModel.setItem(historyDay)

        assertTrue(viewModel.isLoaded)
    }

    @Test
    @Throws(Exception::class)
    fun testFormattedDateReturnsStringWhenDateIsNotNull() {
        val expectedFormattedDate = MONTH_DAY

        val viewModel = TrackerItemViewModel(dependencyProvider)

        viewModel.date = date

        val formattedDate = viewModel.formattedDate

        assertEquals(expectedFormattedDate, formattedDate)
    }

    @Test
    @Throws(Exception::class)
    fun testFormattedDateReturnsNullWhenDateIsNull() {
        val viewModel = TrackerItemViewModel(dependencyProvider)

        val formattedDate = viewModel.formattedDate

        assertNull(formattedDate)
    }

    @Test
    @Throws(Exception::class)
    fun testConnectedInhalerCountReturnsZeroWhenNotLoaded() {
        val viewModel = TrackerItemViewModel(dependencyProvider)

        val connectedInhalerCount = viewModel.connectedInhalers

        assertEquals(0, connectedInhalerCount.toLong())
    }

    @Test
    @Throws(Exception::class)
    fun testConnectedInhalerCountReturnsValueWhenLoaded() {
        val viewModel = TrackerItemViewModel(dependencyProvider)

        viewModel.setItem(historyDay)

        val connectedInhalerCount = viewModel.connectedInhalers

        assertEquals(CONNECTED_INHALERS.toLong(), connectedInhalerCount.toLong())
    }


    @Test
    @Throws(Exception::class)
    fun testInhalationCountReturnsZeroWhenNotLoaded() {
        val viewModel = TrackerItemViewModel(dependencyProvider)

        val inhalationCount = viewModel.inhalationCount

        assertEquals(0, inhalationCount.toLong())
    }

    @Test
    @Throws(Exception::class)
    fun testInhalationCountReturnsValueWhenLoaded() {
        val viewModel = TrackerItemViewModel(dependencyProvider)

        viewModel.setItem(historyDay)

        val inhalationCount = viewModel.inhalationCount

        assertEquals(INHALATION_COUNT.toLong(), inhalationCount.toLong())
    }

    @Test
    @Throws(Exception::class)
    fun testInhalationCountCriticalReturnsFalseWhenLessThanThreshold() {
        val viewModel = TrackerItemViewModel(dependencyProvider)

        viewModel.setItem(historyDay)

        assertFalse(viewModel.isInhalationCountCritical)
    }

    @Test
    @Throws(Exception::class)
    fun testInhalationCountCriticalReturnsTrueWhenGreaterThanThreshold() {
        val viewModel = TrackerItemViewModel(dependencyProvider)

        val relieverDoses = ArrayList<HistoryDose>()
        for (i in 0..CRITICAL_INHALATION_COUNT + 1 - 1) {
            relieverDoses.add(HistoryDose(DRUG_ID, ArrayList<InhaleEvent>()))
        }
        historyDay.relieverDoses = relieverDoses

        viewModel.setItem(historyDay)

        assertTrue(viewModel.isInhalationCountCritical)
    }

    @Test
    @Throws(Exception::class)
    fun testInhalationCountValidReturnsTrueWhenLoadedAndConnectedInhalers() {
        val viewModel = TrackerItemViewModel(dependencyProvider)

        viewModel.setItem(historyDay)

        assertTrue(viewModel.isInhalationCountValid)
    }

    @Test
    @Throws(Exception::class)
    fun testInhalationCountValidReturnsFalseWhenNotLoaded() {
        val viewModel = TrackerItemViewModel(dependencyProvider)

        assertFalse(viewModel.isInhalationCountValid)
    }

    @Test
    @Throws(Exception::class)
    fun testInhalationCountValidReturnsFalseWhenLoadedAndNoConnectedInhalers() {
        val viewModel = TrackerItemViewModel(dependencyProvider)

        historyDay.connectedInhalerCount = 0
        viewModel.setItem(historyDay)

        assertFalse(viewModel.isInhalationCountValid)
    }


    @Test
    @Throws(Exception::class)
    fun testDailySelfAssessmentReturnsUnknownWhenNotLoaded() {
        val viewModel = TrackerItemViewModel(dependencyProvider)

        val dsa = viewModel.dailySelfAssessment

        assertEquals(UserFeeling.UNKNOWN, dsa)
    }

    @Test
    @Throws(Exception::class)
    fun testDailySelfAssessmentReturnsVal() {
        val viewModel = TrackerItemViewModel(dependencyProvider)

        viewModel.setItem(historyDay)
        val dsa = viewModel.dailySelfAssessment

        assertEquals(UserFeeling.GOOD, dsa)
    }

    @Test
    @Throws(Exception::class)
    fun testSetItemCallsNotifyChange() {
        val viewModel = TrackerItemViewModel(dependencyProvider)

        val callback: Observable.OnPropertyChangedCallback = mock()
        viewModel.addOnPropertyChangedCallback(callback)

        viewModel.setItem(historyDay)

        verify(callback).onPropertyChanged(viewModel, 0)
    }

    companion object {
        private val CRITICAL_INHALATION_COUNT = 12
        private val INHALATION_COUNT = 2

        private val DRUG_ID = "745750"
        private val DAY_OF_WEEK = "FRI"
        private val MONTH_DAY = "Feb 18"
        private val CONNECTED_INHALERS = 2
        private val date = LocalDate.of(2017, 2, 17)
        val DRUG_UID = "745750"
    }
}