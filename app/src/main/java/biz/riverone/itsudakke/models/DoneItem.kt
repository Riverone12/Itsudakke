package biz.riverone.itsudakke.models

import android.content.ContentValues
import android.os.Parcel
import android.os.Parcelable
import biz.riverone.itsudakke.common.Database
import biz.riverone.itsudakke.common.MyCalendarUtil
/**
 * 完了データ
 * Created by kawahara on 2018/01/16.
 */

class DoneItem() : Parcelable {
    var id: Int = 0
    var taskId: Int = 0
    var date: Int = 0
    var memo: String = ""
    var registered: Int = 0

    var erased: Int = 0

    fun clear() {
        id = 0
        taskId = 0
        date = 0
        memo = ""
        registered = 0

        erased = 0
    }

    fun find(database: Database?, id: Int): Boolean {
        clear()
        val db = database?.resource ?: return false

        val selection = "id = ?"
        val selectionArgs = arrayOf(id.toString())

        val cursor = db.query(
                TABLE_NAME,
                columns,
                selection,
                selectionArgs,
                null, null, null
        )

        var result = false
        if (cursor.moveToFirst()) {
            this.id = cursor.getInt(0)
            taskId = cursor.getInt(1)
            date = cursor.getInt(2)
            memo = cursor.getString(3)
            registered = cursor.getInt(4)

            result = true
        }
        cursor.close()
        return result
    }

    private fun insert(database: Database?) {
        registered = MyCalendarUtil.currentDay()
        id = getNextId(database)

        val contentValues = ContentValues()
        contentValues.put("id", id)
        contentValues.put("task", taskId)
        contentValues.put("done_date", date)
        contentValues.put("memo", memo)
        contentValues.put("registered", registered)

        val db = database?.resource
        db?.insert(TABLE_NAME, null, contentValues)
    }

    private fun update(database: Database?) {
        registered = MyCalendarUtil.currentDay()

        val whereClause = "id = ?"
        val whereArgs = arrayOf(id.toString())

        val contentValues = ContentValues()
        contentValues.put("task", taskId)
        contentValues.put("done_date", date)
        contentValues.put("memo", memo)
        contentValues.put("registered", registered)

        val db = database?.resource
        db?.update(TABLE_NAME, contentValues, whereClause, whereArgs)
    }

    fun register(database: Database?) {
        val temp = DoneItem()
        if (id > 0 && temp.find(database, id)) {
            update(database)
        } else {
            insert(database)
        }
    }

    fun erase(database: Database?) {
        val db = database?.resource
        db?.delete(TABLE_NAME, "id=?", arrayOf(id.toString()))
    }

    constructor(parcel: Parcel) : this() {
        id = parcel.readInt()
        taskId = parcel.readInt()
        date = parcel.readInt()
        memo = parcel.readString()
        registered = parcel.readInt()

        erased = parcel.readInt()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeInt(taskId)
        parcel.writeInt(date)
        parcel.writeString(memo)
        parcel.writeInt(registered)

        parcel.writeInt(erased)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {
        const val TABLE_NAME= "done_item"
        private val columns = arrayOf(
                "id",
                "task",
                "done_date",
                "memo",
                "registered"
        )

        private fun getNextId(database: Database?): Int {
            val db = database?.resource ?: return -1
            val columns = arrayOf("COALESCE(MAX(id), 0) + 1")
            val cursor = db.query(
                    TABLE_NAME,
                    columns,
                    null, null,
                    null, null, null, null
            )
            var result = -1
            if (cursor.moveToFirst()) {
                result = cursor.getInt(0)
            }
            cursor.close()
            return result
        }

        fun maxItem(database: Database?, taskId: Int, limitDate: Int = 0): DoneItem {
            val db = database?.resource ?: return DoneItem()

            val limit = if (limitDate < 0) {
                limitDate
            } else {
                MyCalendarUtil.currentDay()
            }

            val columns = arrayOf("id")
            val selection = "task = ? AND done_date <= ?"
            val selectionArgs = arrayOf(taskId.toString(), limit.toString())
            val cursor = db.query(
                    TABLE_NAME,
                    columns,
                    selection,
                    selectionArgs,
                    null, null, "done_date DESC"
            )
            val id =
                    if (cursor.moveToFirst()) {
                        cursor.getInt(0)
                    } else {
                        0
                    }
            cursor.close()

            val result = DoneItem()
            result.find(database, id)
            return result
        }

        @JvmField
        val CREATOR : Parcelable.Creator<DoneItem>
                = object : Parcelable.Creator<DoneItem> {
            override fun createFromParcel(parcel: Parcel): DoneItem {
                return DoneItem(parcel)
            }

            override fun newArray(size: Int): Array<DoneItem?> {
                return arrayOfNulls(size)
            }
        }
    }
}