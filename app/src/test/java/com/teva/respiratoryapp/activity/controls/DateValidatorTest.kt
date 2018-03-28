package com.teva.respiratoryapp.activity.controls

import android.text.Editable
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import com.nhaarman.mockito_kotlin.*
import com.teva.common.services.TimeService
import com.teva.utilities.services.DependencyProvider
import com.teva.respiratoryapp.mocks.MockEditable
import com.teva.respiratoryapp.testutils.BaseTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDate

/**
 * Test class for the DateValidator
 */
class DateValidatorTest : BaseTest() {
    val timeService: TimeService = mock()

    @Before
    fun setUp() {
        whenever(timeService.today()).thenReturn(LocalDate.of(2017, 1, 1))
        DependencyProvider.default.register(TimeService::class, timeService)
    }

    /**
     * Create a spy of the MockEditable class that stubs out the Spannable methods
     * used by these tests.
     */
    private fun createEditable(initialString: String): Editable {
        val editable = spy(MockEditable(initialString))

        // stub out the Spannable methods
        doNothing().whenever(editable).clearSpans()
        doReturn(arrayOf<ForegroundColorSpan>()).whenever(editable).getSpans(any(), any(), any<Class<ForegroundColorSpan>>())
        doNothing().whenever(editable).removeSpan(any())
        doNothing().whenever(editable).setSpan(any(), any(), any(), any())

        return editable
    }

    /**
     * Tests that a separator is appended to a string that ends with a
     * complete field value
     */
    @Test
    fun testAfterTextChangedAddsSeparatorAfterFields() {
        val dateValidator = DateValidator()
        dateValidator.format = DateValidator.DateFormat.MONTH_DAY_YEAR

        // check after first field
        var editable = createEditable("12")
        dateValidator.afterTextChanged(editable)
        assertEquals("12 / ", editable.toString())

        // check after second field
        editable = createEditable("12")
        dateValidator.afterTextChanged(editable)
        assertEquals("12 / ", editable.toString())
    }

    /**
     * Test that separators are inserted between fields when they are provided in pasted text.
     */
    @Test
    fun testAfterTextChangedAddsSeparatorsBetweenFieldsOfPastedText() {
        val dateValidator = DateValidator()
        dateValidator.format = DateValidator.DateFormat.MONTH_DAY_YEAR

        var editable = createEditable("1208")
        dateValidator.afterTextChanged(editable)
        assertEquals("12 / 08 / ", editable.toString())

        editable = createEditable("12081972")
        dateValidator.afterTextChanged(editable)
        assertEquals("12 / 08 / 1972", editable.toString())
    }

    /**
     * Test that extra digits are removed from the text.
     */
    @Test
    fun testAfterTextChangedRemovesExtraDigits() {
        val dateValidator = DateValidator()
        dateValidator.format = DateValidator.DateFormat.MONTH_DAY_YEAR

        val editable = createEditable("1208197212345")
        dateValidator.afterTextChanged(editable)
        assertEquals("12 / 08 / 1972", editable.toString())
    }

    /**
     * Test that invalid characters are removed from the text.
     */
    @Test
    fun testAfterTextChangedRemovesInvalidCharacters() {
        val dateValidator = DateValidator()
        dateValidator.format = DateValidator.DateFormat.MONTH_DAY_YEAR

        val editable = createEditable("12 / ab#$%d08")

        dateValidator.afterTextChanged(editable)

        assertEquals("12 / 08 / ", editable.toString())
    }

    /**
     * Test that month errors are identified and flagged in Month/Day/Year format.
     */
    @Test
    fun testAfterTextChangedIdentifiesMonthErrorsInMonthDayYearFormat() {
        val dateValidator = DateValidator()
        dateValidator.format = DateValidator.DateFormat.MONTH_DAY_YEAR

        var editable = createEditable("4")
        dateValidator.afterTextChanged(editable)
        assertEquals("4", editable.toString())
        verify(editable).setSpan(any<ForegroundColorSpan>(), eq(0), eq(1), eq(Spanned.SPAN_EXCLUSIVE_EXCLUSIVE))

        editable = createEditable("14")
        dateValidator.afterTextChanged(editable)
        assertEquals("14 / ", editable.toString())
        verify(editable).setSpan(any<ForegroundColorSpan>(), eq(0), eq(5), eq(Spanned.SPAN_EXCLUSIVE_EXCLUSIVE))

        editable = createEditable("14 / 05")
        dateValidator.afterTextChanged(editable)
        assertEquals("14 / 05 / ", editable.toString())
        verify(editable).setSpan(any<ForegroundColorSpan>(), eq(0), eq(10), eq(Spanned.SPAN_EXCLUSIVE_EXCLUSIVE))
    }

    /**
     * Test that month errors are identified and flagged in Day/Month/Year format.
     */
    @Test
    fun testAfterTextChangedIdentifiesMonthErrorsInDayMonthYearFormat() {
        val dateValidator = DateValidator()
        dateValidator.format = DateValidator.DateFormat.DAY_MONTH_YEAR

        var editable = createEditable("05 / 4")
        dateValidator.afterTextChanged(editable)
        assertEquals("05 / 4", editable.toString())
        verify(editable).setSpan(any<ForegroundColorSpan>(), eq(0), eq(6), eq(Spanned.SPAN_EXCLUSIVE_EXCLUSIVE))

        editable = createEditable("05 / 14")
        dateValidator.afterTextChanged(editable)
        assertEquals("05 / 14 / ", editable.toString())
        verify(editable).setSpan(any<ForegroundColorSpan>(), eq(0), eq(10), eq(Spanned.SPAN_EXCLUSIVE_EXCLUSIVE))

        editable = createEditable("05 / 14 / 1972")
        dateValidator.afterTextChanged(editable)
        assertEquals("05 / 14 / 1972", editable.toString())
        verify(editable).setSpan(any<ForegroundColorSpan>(), eq(0), eq(14), eq(Spanned.SPAN_EXCLUSIVE_EXCLUSIVE))
    }

    /**
     * Test that day errors are identified and flagged in Month/Day/Year format.
     */
    @Test
    fun testAfterTextChangedIdentifiesDayErrorsInMonthDayYearFormat() {
        val dateValidator = DateValidator()
        dateValidator.format = DateValidator.DateFormat.MONTH_DAY_YEAR

        var editable = createEditable("12 / 4")
        dateValidator.afterTextChanged(editable)
        assertEquals("12 / 4", editable.toString())
        verify(editable).setSpan(any<ForegroundColorSpan>(), eq(0), eq(6), eq(Spanned.SPAN_EXCLUSIVE_EXCLUSIVE))

        editable = createEditable("12 / 41")
        dateValidator.afterTextChanged(editable)
        assertEquals("12 / 41 / ", editable.toString())
        verify(editable).setSpan(any<ForegroundColorSpan>(), eq(0), eq(10), eq(Spanned.SPAN_EXCLUSIVE_EXCLUSIVE))

        editable = createEditable("12 / 41 / 1972")
        dateValidator.afterTextChanged(editable)
        assertEquals("12 / 41 / 1972", editable.toString())
        verify(editable).setSpan(any<ForegroundColorSpan>(), eq(0), eq(14), eq(Spanned.SPAN_EXCLUSIVE_EXCLUSIVE))
    }

    /**
     * Test that day errors are identified and flagged in Day/Month/Year format.
     */
    @Test
    fun testAfterTextChangedIdentifiesDayErrorsInDayMonthYearFormat() {
        val dateValidator = DateValidator()
        dateValidator.format = DateValidator.DateFormat.DAY_MONTH_YEAR

        var editable = createEditable("4")
        dateValidator.afterTextChanged(editable)
        assertEquals("4", editable.toString())
        verify(editable).setSpan(any<ForegroundColorSpan>(), eq(0), eq(1), eq(Spanned.SPAN_EXCLUSIVE_EXCLUSIVE))

        editable = createEditable("41")
        dateValidator.afterTextChanged(editable)
        assertEquals("41 / ", editable.toString())
        verify(editable).setSpan(any<ForegroundColorSpan>(), eq(0), eq(5), eq(Spanned.SPAN_EXCLUSIVE_EXCLUSIVE))

        editable = createEditable("41 / 12")
        dateValidator.afterTextChanged(editable)
        assertEquals("41 / 12 / ", editable.toString())
        verify(editable).setSpan(any<ForegroundColorSpan>(), eq(0), eq(10), eq(Spanned.SPAN_EXCLUSIVE_EXCLUSIVE))
    }

    /**
     * Test that day errors are identified and flagged.
     */
    @Test
    fun testAfterTextChangedIdentifiesYearErrors() {
        val dateValidator = DateValidator()
        dateValidator.format = DateValidator.DateFormat.MONTH_DAY_YEAR

        var editable = createEditable("12 / 08 / 0")
        dateValidator.afterTextChanged(editable)
        assertEquals("12 / 08 / 0", editable.toString())
        verify(editable).setSpan(any<ForegroundColorSpan>(), eq(0), eq(11), eq(Spanned.SPAN_EXCLUSIVE_EXCLUSIVE))

        editable = createEditable("12 / 08 / 3")
        dateValidator.afterTextChanged(editable)
        assertEquals("12 / 08 / 3", editable.toString())
        verify(editable).setSpan(any<ForegroundColorSpan>(), eq(0), eq(11), eq(Spanned.SPAN_EXCLUSIVE_EXCLUSIVE))

        editable = createEditable("12 / 08 / 2")
        dateValidator.afterTextChanged(editable)
        assertEquals("12 / 08 / 2", editable.toString())
        verify(editable, never()).setSpan(any<ForegroundColorSpan>(), any(), any(), any())

        editable = createEditable("12 / 08 / 18")
        dateValidator.afterTextChanged(editable)
        assertEquals("12 / 08 / 18", editable.toString())
        verify(editable).setSpan(any<ForegroundColorSpan>(), eq(0), eq(12), eq(Spanned.SPAN_EXCLUSIVE_EXCLUSIVE))

        editable = createEditable("12 / 08 / 19")
        dateValidator.afterTextChanged(editable)
        assertEquals("12 / 08 / 19", editable.toString())
        verify(editable, never()).setSpan(any<ForegroundColorSpan>(), any(), any(), any())

        editable = createEditable("12 / 08 / 20")
        dateValidator.afterTextChanged(editable)
        assertEquals("12 / 08 / 20", editable.toString())
        verify(editable, never()).setSpan(any<ForegroundColorSpan>(), any(), any(), any())

        editable = createEditable("12 / 08 / 21")
        dateValidator.afterTextChanged(editable)
        assertEquals("12 / 08 / 21", editable.toString())
        verify(editable).setSpan(any<ForegroundColorSpan>(), eq(0), eq(12), eq(Spanned.SPAN_EXCLUSIVE_EXCLUSIVE))

        editable = createEditable("12 / 08 / 2017")
        dateValidator.afterTextChanged(editable)
        assertEquals("12 / 08 / 2017", editable.toString())
        verify(editable, never()).setSpan(any<ForegroundColorSpan>(), any(), any(), any())

        editable = createEditable("12 / 08 / 2018")
        dateValidator.afterTextChanged(editable)
        assertEquals("12 / 08 / 2018", editable.toString())
        verify(editable).setSpan(any<ForegroundColorSpan>(), eq(0), eq(14), eq(Spanned.SPAN_EXCLUSIVE_EXCLUSIVE))
    }

    /**
     * Test that a properly formatted string is returned when a date is formatted.
     */
    @Test
    fun testTextForDateReturnsProperlyFormattedDateText() {
        val dateValidator = DateValidator()
        dateValidator.format = DateValidator.DateFormat.MONTH_DAY_YEAR

        var text = dateValidator.textFromDate(LocalDate.of(2017, 3, 15))
        assertEquals("03 / 15 / 2017", text)

        dateValidator.format = DateValidator.DateFormat.DAY_MONTH_YEAR
        text = dateValidator.textFromDate(LocalDate.of(2017, 3, 15))
        assertEquals("15 / 03 / 2017", text)
    }

    /**
     * Tests that the date property is updated to a value that matches the input text.
     */
    @Test
    fun testAfterTextChangedUpdatesDate() {
        val dateValidator = DateValidator()
        dateValidator.format = DateValidator.DateFormat.MONTH_DAY_YEAR

        val expectedDate = LocalDate.of(1972, 12, 8)

        var editable = createEditable("12 / 08 / 1972")
        dateValidator.afterTextChanged(editable)
        assertEquals(expectedDate, dateValidator.date)

        editable = createEditable("12 / 08 /")
        dateValidator.afterTextChanged(editable)
        assertNull(dateValidator.date)

        dateValidator.format = DateValidator.DateFormat.DAY_MONTH_YEAR

        editable = createEditable("08 / 12 / 1972")
        dateValidator.afterTextChanged(editable)
        assertEquals(expectedDate, dateValidator.date)

        editable = createEditable("08 / 12 /")
        dateValidator.afterTextChanged(editable)
        assertNull(dateValidator.date)
    }
}
