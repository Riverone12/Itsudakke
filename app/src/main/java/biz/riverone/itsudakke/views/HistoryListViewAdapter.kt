package biz.riverone.itsudakke.views

import android.content.Context
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import biz.riverone.itsudakke.R
import biz.riverone.itsudakke.common.MyCalendarUtil
import biz.riverone.itsudakke.models.DoneItem
import biz.riverone.itsudakke.models.DoneItemList

/**
 * HistoryListViewAdapter.kt: 履歴リストビュー表示用のアダプタ
 * Created by kawahara on 2018/01/17.
 */
class HistoryListViewAdapter(context: Context, resource: Int, objects: DoneItemList)
    : ArrayAdapter<DoneItem>(context, resource, objects) {

    private var layoutInflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private val doneItemList = objects
    private val resourceId: Int = resource

    override fun getCount(): Int {
        return doneItemList.size
    }

    override fun getItemId(position: Int): Long {
        return (doneItemList[position].id).toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: layoutInflater.inflate(resourceId, null)
        val context = view.context

        val doneItem = doneItemList[position]

        val textViewDate = view.findViewById<TextView>(R.id.textViewDate)
        val m = MyCalendarUtil.toMonth(doneItem.date)
        val d = MyCalendarUtil.toDay(doneItem.date)
        val strDate = "$m/$d"
        textViewDate.text = strDate

        val textViewMemo = view.findViewById<TextView>(R.id.textViewMemo)
        textViewMemo.text = doneItem.memo

        // 背景色を変更する
        var dayIdx = 0
        var currentDate = 0
        for (i in doneItemList.indices) {
            if (doneItemList[i].date > doneItem.date) {
                break
            }
            if (currentDate != doneItemList[i].date) {
                dayIdx += 1
                currentDate = doneItemList[i].date
            }
        }
        val bkColor = if (dayIdx % 2 == 0) {
            ContextCompat.getColor(context, R.color.list_even)
        } else {
            ContextCompat.getColor(context, R.color.list_odd)
        }
        view.setBackgroundColor(bkColor)

        return view
    }
}