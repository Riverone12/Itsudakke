package biz.riverone.itsudakke.common

import java.time.DayOfWeek
import java.util.*

/**
 * カレンダー関連の関数群
 * Created by kawahara on 2018/01/16.
 */

object MyCalendarUtil {

    fun calendarToInt(calendar: Calendar) : Int {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val date = calendar.get(Calendar.DATE)

        return (year * 10000) + (month * 100) + date
    }

    fun intToCalendar(value: Int) : Calendar {
        val calendar = Calendar.getInstance()
        calendar.clear()
        val year = value / 10000
        val month = (value % 10000) / 100
        val date = value % 100
        calendar.set(year, month - 1, date)

        return calendar
    }

    fun currentDay(): Int {
        return calendarToInt(Calendar.getInstance())
    }

    fun toYear(value: Int) : Int { return value / 10000 }
    fun toMonth(value: Int) : Int { return (value % 10000) / 100 }
    fun toDay(value: Int) : Int { return value % 100 }

    fun calcDayDiff(from: Calendar, to: Calendar): Int {
        val diff = to.timeInMillis - from.timeInMillis
        return (diff / (1000 * 60 * 60 * 24)).toInt()
    }

    fun startOfMonth(year: Int, month: Int) : Int {
        return ymdToInt(year, month, 1)
    }

    fun ymdToInt(year: Int, month: Int, day: Int) : Int {
        return (year * 10000 + month * 100 + day)
    }

    fun endOfMonth(year: Int, month: Int) : Int {
        val cal = intToCalendar(year * 10000 + month * 100 + 1)
        cal.add(Calendar.MONTH, 1)
        cal.add(Calendar.DAY_OF_MONTH, -1)
        return calendarToInt(cal)
    }

    fun nextDay(ymd: Int) : Int {
        val cal = intToCalendar(ymd)
        cal.add(Calendar.DAY_OF_MONTH, 1)
        return calendarToInt(cal)
    }

    fun toWeekStr(ymd: Int): String {
        val cal = intToCalendar(ymd)
        val w = cal.get(Calendar.DAY_OF_WEEK)
        val result =
                when (w) {
                    1 -> "日"
                    2 -> "月"
                    3 -> "火"
                    4 -> "水"
                    5 -> "木"
                    6 -> "金"
                    7 -> "土"
                    else -> ""
                }
        return result
    }
}