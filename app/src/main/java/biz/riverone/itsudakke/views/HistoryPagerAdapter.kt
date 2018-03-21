package biz.riverone.itsudakke.views

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import biz.riverone.itsudakke.models.TaskItem

/**
 * HistoryPagerAdapter: スワイプでページを切り替える仕組み
 * Created by kawahara on 2018/01/08.
 * 2018.3.21 J.Kawahara TaskPagerAdapterBaseから派生に変更
 */

class HistoryPagerAdapter(fragmentManager: FragmentManager)
    : TaskPagerAdapterBase(fragmentManager) {

    override fun createFragment(accountItem: TaskItem, year: Int, month: Int): Fragment {
        return HistoryFragment.create(accountItem, year, month)
    }
}
