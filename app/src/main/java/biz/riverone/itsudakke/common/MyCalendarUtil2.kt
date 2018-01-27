package biz.riverone.itsudakke.common

import java.util.*

/**
 * カレンダー関連の関数群
 * Created by kawahara on 2018/01/16.
 */

object MyCalendarUtil2 {

    fun calcClosingDate(year: Int, month: Int, closingDay: Int): Int {
        val cal = Calendar.getInstance()
        cal.clear()
        cal.set(year, month - 1, 1)

        val lastDay = cal.getActualMaximum(Calendar.DATE)

        val day = if (closingDay == 0 || closingDay >= lastDay) {
            lastDay
        } else {
            closingDay
        }

        cal.set(year, month - 1, day)
        return MyCalendarUtil.calendarToInt(cal)
    }

    fun calcStartDate(year: Int, month: Int, closingDay: Int): Int {
        var tYear = year
        var tMonth = month - 1
        if (tMonth < 0) {
            tMonth = 12
            tYear -= 1
        }

        val lastClosingDate = calcClosingDate(tYear, tMonth, closingDay)
        val y = MyCalendarUtil.toYear(lastClosingDate)
        val m = MyCalendarUtil.toMonth(lastClosingDate)
        val d = MyCalendarUtil.toDay(lastClosingDate)

        val cal = Calendar.getInstance()
        cal.clear()
        cal.set(y, m - 1, d + 1)

        return MyCalendarUtil.calendarToInt(cal)
    }

    // 今月度の開始日
    fun currentStartDate(closingDay: Int) : Int {
        val lastClosing = lastClosingDate(closingDay)
        val y = MyCalendarUtil.toYear(lastClosing)
        val m = MyCalendarUtil.toMonth(lastClosing)
        val d = MyCalendarUtil.toDay(lastClosing)

        val cal = Calendar.getInstance()
        cal.clear()
        cal.set(y, m - 1, d + 1)

        return MyCalendarUtil.calendarToInt(cal)
    }

    // 先月度の開始日
    fun lastStartDate(closingDay: Int) : Int {
        val currentClosingDay = currentClosingDate(closingDay)
        val year = MyCalendarUtil.toYear(currentClosingDay)
        val month = MyCalendarUtil.toMonth(currentClosingDay) - 2
        val intVal = calcClosingDate(year, month, closingDay)

        val y = MyCalendarUtil.toYear(intVal)
        val m = MyCalendarUtil.toMonth(intVal)
        val d = MyCalendarUtil.toDay(intVal)
        val cal = Calendar.getInstance()
        cal.clear()
        cal.set(y, m - 1, d + 1)

        return MyCalendarUtil.calendarToInt(cal)
    }

    // 今月度の締め日
    fun currentClosingDate(closingDay: Int) : Int {
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        val day = cal.get(Calendar.DAY_OF_MONTH)

        var closingDate = calcClosingDate(year, month, closingDay)
        if (day > (closingDate % 100)) {
            closingDate = calcClosingDate(year, month + 1, closingDay)
        }
        return closingDate
    }

    // 先月度の締め日
    fun lastClosingDate(closingDay: Int) : Int {
        val currentClosingDay = currentClosingDate(closingDay)
        val year = MyCalendarUtil.toYear(currentClosingDay)
        val month = MyCalendarUtil.toMonth(currentClosingDay) - 1
        return calcClosingDate(year, month, closingDay)
    }

}