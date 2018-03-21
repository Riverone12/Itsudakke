package biz.riverone.itsudakke.views

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import biz.riverone.itsudakke.common.MyCalendarUtil
import biz.riverone.itsudakke.common.MyFragmentPagerAdapterBase
import biz.riverone.itsudakke.models.TaskItem
import java.util.ArrayList

/**
 * TaskPagerAdapterBase.kt: スワイプでページを切り替える仕組み
 * Created by kawahara on 2018/03/21.
 */
abstract class TaskPagerAdapterBase(fragmentManager: FragmentManager)
    : MyFragmentPagerAdapterBase<String>(fragmentManager){
    private val fragmentList = ArrayList<Fragment>()

    abstract fun createFragment(accountItem: TaskItem, year: Int, month: Int): Fragment

    fun initialize(accountItem: TaskItem, monthsCount: Int, minYmd: Int) {

        val cYmd = MyCalendarUtil.currentDay()
        val currentLastDate = MyCalendarUtil.endOfMonth(MyCalendarUtil.toYear(cYmd), MyCalendarUtil.toMonth(cYmd))

        var year = MyCalendarUtil.toYear(currentLastDate)
        var month = MyCalendarUtil.toMonth(currentLastDate)

        month -= (monthsCount - 1)
        while (month <= 0) {
            month += 12
            year -= 1
        }

        var my = 0
        var mm = 0

        if (minYmd > 0) {
            // もっとも古い取引日の月度を取得する(my年mm月度)
            my = MyCalendarUtil.toYear(minYmd)
            mm = MyCalendarUtil.toMonth(minYmd)
            val md = MyCalendarUtil.toDay(minYmd)
            val lastDate = MyCalendarUtil.endOfMonth(my, mm)
            val cd = MyCalendarUtil.toDay(lastDate)
            if (md > cd) {
                mm += 1
                if (mm > 12) {
                    my += 1
                    mm = 1
                }
            }
        }

        for (i in 1..monthsCount) {
            if (i < monthsCount - 1
                    && (minYmd == 0 || year < my || (year == my && month < mm))) {
                // 今月度とその1月前は必ず表示する
                // 2か月以上前はデータが存在する場合に限り表示する
            } else {
                val fragment = createFragment(accountItem, year, month)
                fragmentList.add(fragment)

                val title = year.toString() + "年" + month.toString() + "月"
                add(title)
            }
            month += 1
            if (month > 12) {
                year += 1
                month = 1
            }
        }
    }

    private fun getFragmentByPosition(position: Int): Fragment {
        if (position < fragmentList.size) {
            return fragmentList[position]
        }
        return fragmentList[0]
    }

    override fun getFragment(item: String?, position: Int): Fragment {
        return getFragmentByPosition(position)
    }

    override fun getPageTitle(position: Int): CharSequence {
        return getItem(position)
    }
}