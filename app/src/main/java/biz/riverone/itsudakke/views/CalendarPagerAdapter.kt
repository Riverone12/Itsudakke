package biz.riverone.itsudakke.views

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import biz.riverone.itsudakke.models.TaskItem

/**
 * CalendarPagerAdapter.kt: スワイプでページを切り替える仕組み
 * Created by kawahara on 2018/03/21.
 */
class CalendarPagerAdapter(fragmentManager: FragmentManager)
    : TaskPagerAdapterBase(fragmentManager) {
    override fun createFragment(accountItem: TaskItem, year: Int, month: Int): Fragment {
        return CalendarFragment.create(accountItem, year, month)
    }
}