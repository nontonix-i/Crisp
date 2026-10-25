package com.example.utils

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object FormatUtils {
    private val rupiahLocale = Locale("id", "ID")

    private val numberFormatThreadLocal = ThreadLocal.withInitial {
        NumberFormat.getNumberInstance(rupiahLocale)
    }

    private val todayKeyFormatThreadLocal = ThreadLocal.withInitial {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    }

    private val dateFormatThreadLocal = ThreadLocal.withInitial {
        SimpleDateFormat("dd MMM yyyy", rupiahLocale)
    }

    private val dateTimeFormatThreadLocal = ThreadLocal.withInitial {
        SimpleDateFormat("dd MMM yyyy, HH:mm", rupiahLocale)
    }

    private val timeFormatThreadLocal = ThreadLocal.withInitial {
        SimpleDateFormat("HH:mm", rupiahLocale)
    }

    fun formatRupiah(amount: Long): String {
        val formatter = numberFormatThreadLocal.get() ?: NumberFormat.getNumberInstance(rupiahLocale)
        return "Rp ${formatter.format(amount)}"
    }

    fun formatRupiah(amount: Int): String {
        return formatRupiah(amount.toLong())
    }

    fun formatBarang(count: Int): String {
        val formatter = numberFormatThreadLocal.get() ?: NumberFormat.getNumberInstance(rupiahLocale)
        return "${formatter.format(count)} pack"
    }

    fun getTodayDateKey(): String {
        val sdf = todayKeyFormatThreadLocal.get() ?: SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    fun formatDate(timestamp: Long): String {
        val sdf = dateFormatThreadLocal.get() ?: SimpleDateFormat("dd MMM yyyy", rupiahLocale)
        return sdf.format(Date(timestamp))
    }

    fun formatDateTime(timestamp: Long): String {
        val sdf = dateTimeFormatThreadLocal.get() ?: SimpleDateFormat("dd MMM yyyy, HH:mm", rupiahLocale)
        return sdf.format(Date(timestamp))
    }

    fun formatTime(timestamp: Long): String {
        val sdf = timeFormatThreadLocal.get() ?: SimpleDateFormat("HH:mm", rupiahLocale)
        return sdf.format(Date(timestamp))
    }

    fun getStartOfDay(timestamp: Long = System.currentTimeMillis()): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timestamp
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun getEndOfDay(timestamp: Long = System.currentTimeMillis()): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timestamp
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        return cal.timeInMillis
    }

    fun getStartOfWeek(): Long {
        val cal = Calendar.getInstance()
        cal.firstDayOfWeek = Calendar.MONDAY
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun getStartOfMonth(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}
