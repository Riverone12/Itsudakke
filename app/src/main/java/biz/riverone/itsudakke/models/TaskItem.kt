package biz.riverone.itsudakke.models

import android.os.Parcel
import android.os.Parcelable
import biz.riverone.itsudakke.common.Database

/**
 * TaskItem.kt: タスクデータ
 * Created by kawahara on 2018/01/16.
 */

class TaskItem() : Parcelable {
    var id: Int = 0
    var title: String = ""
    var colorId: Int = 0

    constructor(parcel: Parcel) : this() {
        id = parcel.readInt()
        title = parcel.readString()
        colorId = parcel.readInt()
    }

    fun from(c: TaskItem) {
        id = c.id
        title = c.title
        colorId = c.colorId
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(title)
        parcel.writeInt(colorId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {

        fun minimamDoneDate(database: Database?, id: Int) : Int {
            val db = database?.resource ?: return 0

            val columns = arrayOf("MIN(done_date)")
            val selection = "task = ?"
            val selectionArgs = arrayOf(id.toString())

            val cursor = db.query(
                    "done_item",
                    columns,
                    selection,
                    selectionArgs,
                    null, null, null
            )

            var ymd = 0
            if (cursor.moveToFirst()) {
                ymd = cursor.getInt(0)
            }
            cursor.close()
            return ymd
        }

        @JvmField
        val CREATOR : Parcelable.Creator<TaskItem>
                = object: Parcelable.Creator<TaskItem> {
            override fun createFromParcel(parcel: Parcel): TaskItem {
                return TaskItem(parcel)
            }

            override fun newArray(size: Int): Array<TaskItem?> {
                return arrayOfNulls(size)
            }
        }
    }
}