package biz.riverone.itsudakke.models

import biz.riverone.itsudakke.common.Database
import biz.riverone.itsudakke.common.MyCalendarUtil
import java.util.*

/**
 * 完了データの配列
 * Created by kawahara on 2018/01/16.
 */

class DoneItemList : ArrayList<DoneItem>() {

    companion object {
        private val TABLE_NAME = "done_item"

        fun eraseOld(database: Database?) {
            // 15ヶ月以上前のデータを削除する
            val db = database?.resource ?: return
            val intDate = MyCalendarUtil.calendarToInt(Calendar.getInstance())
            val day = MyCalendarUtil.toDay(intDate)
            if (day % 10 == 0) {
                // 10日、20日、30日に実行する
                var year = MyCalendarUtil.toYear(intDate)
                var month = MyCalendarUtil.toMonth(intDate)
                month -= 15
                while (month < 0) {
                    month += 12
                    year -= 1
                }
                val ymd = MyCalendarUtil.ymdToInt(year, month, day)
                val where = "done_date < ?"
                val whereArg = arrayOf(ymd.toString())
                db.delete(TABLE_NAME, where, whereArg)
            }
        }

        fun eraseAll(database: Database?) {
            val db = database?.resource ?: return
            db.delete(TABLE_NAME, null, null)
        }
    }

    fun load(database: Database?, taskItem: TaskItem, year: Int, month: Int) {
        clear()

        val db = database?.resource ?: return

        // 1日から末日まで
        val startDate = MyCalendarUtil.startOfMonth(year, month)
        val endDate = MyCalendarUtil.endOfMonth(year, month)

        val columns = arrayOf(
                "id",
                "task",
                "done_date",
                "memo",
                "registered"
        )

        val selection = "task = ? AND done_date >= ? AND done_date <= ?"
        val selectionArgs = arrayOf(
                taskItem.id.toString(),
                startDate.toString(),
                endDate.toString()
        )

        val cursor = db.query(
                TABLE_NAME,
                columns,
                selection,
                selectionArgs,
                null, null,
                "done_date, id"
        )

        if (cursor?.moveToFirst() == true) {
            do {
                val item = DoneItem()
                item.id = cursor.getInt(0)
                item.taskId = cursor.getInt(1)
                item.date = cursor.getInt(2)
                item.memo = cursor.getString(3)
                item.registered = cursor.getInt(4)

                this.add(item)
            } while (cursor.moveToNext())
        }
        cursor?.close()
    }
}