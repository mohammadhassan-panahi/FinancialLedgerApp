package com.example.util

import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar

/**
 * Jalali (Solar Hijri) date conversion + formatting. Every user-facing date in the app goes
 * through here so the whole app shows Shamsi dates with Persian digits — one source of truth.
 *
 * Algorithm: the standard integer Jalali↔Gregorian conversion (Kazimierz M. Borkowski /
 * Roozbeh Pournader & Mohammad Toossi), valid for the whole range we care about (1900–2100).
 */
object PersianDateUtils {

    private val jalaliMonths = arrayOf(
        "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
    )

    data class JalaliDate(val year: Int, val month: Int, val dayOfMonth: Int) {
        val monthName: String get() = jalaliMonths[month - 1]
    }

    fun gregorianToJalali(gy: Int, gm: Int, gd: Int): JalaliDate {
        val gDaysInMonth = intArrayOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334)
        val baseYear = if (gy > 1600) 979 else 0
        val shiftedYear = gy - (if (gy > 1600) 1600 else 0)
        val leapAdjustedYear = if (gm > 2) shiftedYear + 1 else shiftedYear

        var days = 365 * shiftedYear +
            (leapAdjustedYear + 3) / 4 - (leapAdjustedYear + 99) / 100 + (leapAdjustedYear + 399) / 400 -
            80 + gd + gDaysInMonth[gm - 1]

        var jy = baseYear + 33 * (days / 12053)
        days %= 12053
        jy += 4 * (days / 1461)
        days %= 1461
        if (days > 365) {
            jy += (days - 1) / 365
            days = (days - 1) % 365
        }
        val jm = if (days < 186) 1 + days / 31 else 7 + (days - 186) / 30
        val jd = 1 + if (days < 186) days % 31 else (days - 186) % 30
        return JalaliDate(jy, jm, jd)
    }

    /** Number of days in a Jalali month (esfand is 30 in leap years, 29 otherwise). */
    fun daysInJalaliMonth(jy: Int, jm: Int): Int = when {
        jm <= 6 -> 31
        jm <= 11 -> 30
        isJalaliLeapYear(jy) -> 30
        else -> 29
    }

    /** Jalali leap-year check via the 33-year cycle remainder trick. */
    fun isJalaliLeapYear(jy: Int): Boolean {
        val remainders = intArrayOf(1, 5, 9, 13, 17, 22, 26, 30)
        return remainders.contains(rem(jy, 33))
    }

    private fun rem(a: Int, b: Int): Int = ((a % b) + b) % b

    fun toJalali(date: Date): JalaliDate {
        val cal = GregorianCalendar()
        cal.time = date
        return gregorianToJalali(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH))
    }

    /** e.g. "۱۴۰۵/۰۶/۰۲" */
    fun formatJalaliDate(date: Date): String {
        val j = toJalali(date)
        return PersianNumberUtils.toPersianDigits("%04d/%02d/%02d".format(j.year, j.month, j.dayOfMonth))
    }

    /** e.g. "۱۴:۳۵" — hour in 24h, from the device's own clock values. */
    fun formatTime(date: Date): String {
        val cal = GregorianCalendar()
        cal.time = date
        return PersianNumberUtils.toPersianDigits("%02d:%02d".format(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE)))
    }

    /** e.g. "۱۴:۳۵ - ۱۴۰۵/۰۶/۰۲" (history rows). */
    fun formatJalaliDateTime(date: Date): String = "${formatTime(date)} - ${formatJalaliDate(date)}"

    /** e.g. "۲ شهریور ۱۴۰۵" (long form for headers, PDFs, reminders). */
    fun formatJalaliLongDate(date: Date): String {
        val j = toJalali(date)
        return PersianNumberUtils.toPersianDigits("${j.dayOfMonth} ${j.monthName} ${j.year}")
    }
}
