package biz.riverone.itsudakke.views

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ListView
import android.widget.TextView
import biz.riverone.itsudakke.R
import biz.riverone.itsudakke.common.ApplicationControl
import biz.riverone.itsudakke.dialog.InputDoneDialog
import biz.riverone.itsudakke.models.DoneItem
import biz.riverone.itsudakke.models.DoneItemList
import biz.riverone.itsudakke.models.TaskItem

/**
 * HistoryFragment.kt: 科目別の実績履歴表示用フラグメント
 * Copyright (C) 2018 J.Kawahara
 * 2018.3.21 J.Kawahara ソースコードの微修正
 */
class HistoryFragment : Fragment() {

    companion object {

        private const val ARG_KEY_TASK_ITEM = "taskItem"
        private const val ARG_KEY_TARGET_YEAR = "targetYear"
        private const val ARG_KEY_TARGET_MONTH = "targetMonth"

        fun create(taskItem: TaskItem, year: Int, month: Int): HistoryFragment {
            val arguments = Bundle()
            arguments.putParcelable(ARG_KEY_TASK_ITEM, taskItem)
            arguments.putInt(ARG_KEY_TARGET_YEAR, year)
            arguments.putInt(ARG_KEY_TARGET_MONTH, month)

            val fragment = HistoryFragment()
            fragment.arguments = arguments
            return fragment
        }
    }

    private var taskItem: TaskItem? = null
    private var targetYear: Int = 0
    private var targetMonth: Int = 0

    private var textViewTotal: TextView? = null
    private var listView: ListView? = null
    private var textViewNoData: TextView? = null

    private var doneItemList = DoneItemList()
    private var listAdapter: HistoryListViewAdapter? = null

    private fun getParameters() {
        targetYear =
                if (arguments.containsKey(ARG_KEY_TARGET_YEAR)) {
                    arguments.getInt(ARG_KEY_TARGET_YEAR)
                } else {
                    0
                }
        targetMonth =
                if (arguments.containsKey(ARG_KEY_TARGET_MONTH)) {
                    arguments.getInt(ARG_KEY_TARGET_MONTH)
                } else {
                    0
                }

        taskItem =
                if (arguments.containsKey(ARG_KEY_TASK_ITEM)) {
                    arguments.getParcelable(ARG_KEY_TASK_ITEM)
                } else {
                    TaskItem()
                }
    }

    val title: String get() {
        getParameters()
        return targetYear.toString() + "年" + targetMonth.toString() + "月"
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = inflater!!.inflate(R.layout.fragment_history, container, false)

        getParameters()
        initializeControls(v)

        return v
    }

    private fun initializeControls(v: View) {
        // タイトル
        val textViewMonthlyTitle = v.findViewById<TextView>(R.id.textViewMonthlyTitle)
        textViewMonthlyTitle.text = title

        listView = v.findViewById(R.id.listView)
        listView?.onItemClickListener = listViewItemClickListener

        textViewTotal = v.findViewById(R.id.textViewTotal)
        textViewNoData = v.findViewById(R.id.textViewNoData)

        reload()
    }

    private fun reload() {
        // 対象期間の「できた！」データを取得する
        doneItemList.load(ApplicationControl.database, taskItem!!, targetYear, targetMonth)

        // 対象月度の合計回数
        val strYen = "%,d回".format(doneItemList.size)
        textViewTotal?.text = strYen

        // リストビューの準備
        listAdapter = HistoryListViewAdapter(context, R.layout.history_list_row, doneItemList)
        listView?.adapter = listAdapter

        if (doneItemList.size > 0) {
            textViewNoData?.visibility = View.GONE
            listView?.visibility = View.VISIBLE
        } else {
            textViewNoData?.visibility = View.VISIBLE
            listView?.visibility = View.GONE
        }
    }

    private val listViewItemClickListener = AdapterView.OnItemClickListener {
        _, _, _, l ->

        val id = l.toInt()
        val doneItem = DoneItem()
        if (doneItem.find(ApplicationControl.database, id)) {
            // 編集ダイアログを表示する
            val inputDialog = InputDoneDialog.create(doneItem, taskItem!!)
            inputDialog.setTargetFragment(this, InputDoneDialog.REQUEST_CODE)
            inputDialog.show(activity.supportFragmentManager, "dialog")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == InputDoneDialog.REQUEST_CODE
                && resultCode == RESULT_OK) {
            if (data != null && data.hasExtra(InputDoneDialog.EXTRA_KEY_DONE_ITEM)) {
                val doneItem = data.getParcelableExtra<DoneItem>(InputDoneDialog.EXTRA_KEY_DONE_ITEM)
                if (doneItem.erased > 0) {
                    // 削除する
                    doneItem.erase(ApplicationControl.database)
                } else {
                    // 更新する
                    doneItem.register(ApplicationControl.database)
                }
                // 再表示
                reload()
            }
        }
    }
}

