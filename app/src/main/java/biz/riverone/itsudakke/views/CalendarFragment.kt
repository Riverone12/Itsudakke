package biz.riverone.itsudakke.views

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import biz.riverone.itsudakke.R
import biz.riverone.itsudakke.common.ApplicationControl
import biz.riverone.itsudakke.common.EqualWidthHeightTextView
import biz.riverone.itsudakke.common.MyCalendarUtil
import biz.riverone.itsudakke.models.DoneItem
import biz.riverone.itsudakke.models.DoneItemList
import biz.riverone.itsudakke.models.TaskItem
import java.util.*

/**
 * CalendarFragment.kt: 1ヶ月分のカレンダー表示フラグメント
 * Copyright (C) 2018 J.Kawahara
 * 2018.3.21 J.Kawahara 新規作成
 */
class CalendarFragment : Fragment() {

    private var rowCache = ArrayList<View>()

    private val doneItemList = DoneItemList()
    private lateinit var taskItem: TaskItem
    private var targetYear: Int = 0
    private var targetMonth: Int = 0

    private var defaultTextColor = -1

    companion object {

        private const val ARG_KEY_TASK_ITEM = "taskItem"
        private const val ARG_KEY_TARGET_YEAR = "targetYear"
        private const val ARG_KEY_TARGET_MONTH = "targetMonth"

        private const val WRAP_CONTENT = ViewGroup.LayoutParams.WRAP_CONTENT
        private const val MATCH_PARENT = ViewGroup.LayoutParams.MATCH_PARENT

        fun create(taskItem: TaskItem, year: Int, month: Int): CalendarFragment {

            val arguments = Bundle()
            arguments.putParcelable(ARG_KEY_TASK_ITEM, taskItem)
            arguments.putInt(ARG_KEY_TARGET_YEAR, year)
            arguments.putInt(ARG_KEY_TARGET_MONTH, month)

            val fragment = CalendarFragment()
            fragment.arguments = arguments

            return fragment
        }
    }

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


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val v = inflater!!.inflate(R.layout.fragment_calendar, container, false)

        getParameters()
        initializeControls(v)

        return v
    }

    private fun initializeControls(v: View) {

        // カレンダーを表示する
        val intCal = MyCalendarUtil.ymdToInt(targetYear, targetMonth, 1)
        val cal = MyCalendarUtil.intToCalendar(intCal)
        createCalendarView(v, cal)
    }

    private fun createCalendarView(v: View, cal: Calendar) {
        val calendarLayout = v.findViewById<TableLayout>(R.id.calendarLayout)

        // 背景色を設定する
        val colors = resources.obtainTypedArray(R.array.colors)
        val defaultColorId = (taskItem.id - 1) % colors.length()
        calendarLayout.setBackgroundColor(colors.getColor(taskItem.colorId, defaultColorId))
        colors.recycle()

        // すでに表示済みの行がある場合、削除する
        for (row in rowCache) {
            calendarLayout.removeView(row)
        }
        rowCache.clear()
        /*
        if (!isAdded) {
            return
        }
        */
        // 対象期間の「できた！」データを読み込む
        doneItemList.load(ApplicationControl.database, taskItem, targetYear, targetMonth)

        // カレンダー表示の最初の日を特定する（当月1日が日曜日でない場合、前月最後の日曜日）
        while (cal.get(Calendar.DAY_OF_WEEK) > 1) {
            cal.add(Calendar.DATE, -1)
        }

        // 行のレイアウトパラメータ
        val rowLayoutParams = TableLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        rowLayoutParams.weight = 1.0f

        // 列のレイアウトパラメータ
        val colLayoutParams = TableRow.LayoutParams(WRAP_CONTENT, MATCH_PARENT)
        colLayoutParams.weight = 1.0f
        val marginLayout = colLayoutParams as ViewGroup.MarginLayoutParams
        marginLayout.setMargins(12, 12, 12, 12)

        val textSizeLarge = resources.getDimension(R.dimen.textSizeLarge)

        for (rowIndex in 0..5) {
            val currentMonth = cal.get(Calendar.MONTH) + 1
            if (currentMonth > targetMonth) {
                if (targetMonth != 1 || currentMonth != 12) {
                    break
                }
            }
            val row = TableRow(context)

            for (i in 0..6) {
                val month = cal.get(Calendar.MONTH) + 1
                val dt = MyCalendarUtil.calendarToInt(cal)
                val child = EqualWidthHeightTextView(context)
                if (defaultTextColor < 0) {
                    defaultTextColor = child.currentTextColor
                }

                if (month == targetMonth) {
                    child.text = cal.get(Calendar.DATE).toString()
                    child.gravity = Gravity.CENTER
                    child.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSizeLarge)

                    child.tag = dt
                    var doneItem: DoneItem? = null
                    for (item in doneItemList) {
                        if (item.date == dt) {
                            doneItem = item
                            break
                        }
                    }
                    setBackground(child, doneItem != null)
                }

                row.addView(child, colLayoutParams)
                cal.add(Calendar.DATE, 1)
            }
            calendarLayout.addView(row, rowLayoutParams)
            rowCache.add(row)
        }
    }

    private fun setBackground(textView: TextView, isDone: Boolean) {
        if (isDone) {
            textView.setBackgroundResource(R.drawable.calendar_done_background)
            textView.setTextColor(ContextCompat.getColor(context, R.color.colorLightGray))
        } else {
            textView.setBackgroundResource(R.drawable.calendar_not_done_background)
            textView.setTextColor(defaultTextColor)
        }
    }
}
