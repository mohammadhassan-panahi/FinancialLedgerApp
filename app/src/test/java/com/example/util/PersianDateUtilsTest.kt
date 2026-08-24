package com.example.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.GregorianCalendar

class PersianDateUtilsTest {

    private fun jalaliOf(y: Int, m: Int, d: Int) =
        PersianDateUtils.gregorianToJalali(y, m, d)

    @Test
    fun `known gregorian to jalali conversions`() {
        // Nowruz 1405: 2026-03-21
        assertEquals(PersianDateUtils.JalaliDate(1405, 1, 1), jalaliOf(2026, 3, 21))
        // Day after Nowruz 1404's esfand: 2026-03-20 = 29 esfand 1404 (1404 is not a leap year)
        assertEquals(PersianDateUtils.JalaliDate(1404, 12, 29), jalaliOf(2026, 3, 20))
        // 2026-08-24 = 2 shahrivar 1405
        assertEquals(PersianDateUtils.JalaliDate(1405, 6, 2), jalaliOf(2026, 8, 24))
        // 2026-01-01 = 11 dey 1404
        assertEquals(PersianDateUtils.JalaliDate(1404, 10, 11), jalaliOf(2026, 1, 1))
        // Farvardin 1st of 1403 fell on 2024-03-20 (1403 is a leap year)
        assertEquals(PersianDateUtils.JalaliDate(1403, 1, 1), jalaliOf(2024, 3, 20))
    }

    @Test
    fun `leap years follow the 33-year cycle`() {
        // 1403 and 1408 are leap; 1404 and 1405 are not.
        assertEquals(true, PersianDateUtils.isJalaliLeapYear(1403))
        assertEquals(true, PersianDateUtils.isJalaliLeapYear(1408))
        assertEquals(false, PersianDateUtils.isJalaliLeapYear(1404))
        assertEquals(false, PersianDateUtils.isJalaliLeapYear(1405))
    }

    @Test
    fun `esfand day count matches leap year`() {
        assertEquals(29, PersianDateUtils.daysInJalaliMonth(1404, 12))
        assertEquals(30, PersianDateUtils.daysInJalaliMonth(1403, 12))
        assertEquals(31, PersianDateUtils.daysInJalaliMonth(1405, 6))
        assertEquals(30, PersianDateUtils.daysInJalaliMonth(1405, 7))
    }

    @Test
    fun `formatting uses persian digits and jalali months`() {
        val date = GregorianCalendar(2026, GregorianCalendar.AUGUST, 24, 14, 35).time
        assertEquals("۱۴۰۵/۰۶/۰۲", PersianDateUtils.formatJalaliDate(date))
        assertEquals("۱۴:۳۵", PersianDateUtils.formatTime(date))
        assertEquals("۱۴:۳۵ - ۱۴۰۵/۰۶/۰۲", PersianDateUtils.formatJalaliDateTime(date))
        assertEquals("۲ شهریور ۱۴۰۵", PersianDateUtils.formatJalaliLongDate(date))
    }
}
