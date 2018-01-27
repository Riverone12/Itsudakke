package biz.riverone.itsudakke.models

import android.content.ContentValues
import biz.riverone.itsudakke.common.ApplicationControl.Companion.context
import biz.riverone.itsudakke.common.Database

/**
 * タスクデータの配列
 * Created by kawahara on 2018/01/16.
 */

class TaskItemList : ArrayList<TaskItem>() {

    companion object {
        private val DB_TABLE = "task_item"

        fun reset(database: Database?) {
            val db = database?.resource ?: return
            db.delete(DB_TABLE, null, null)

            database.DBHelper(context).onCreate(database.resource)
        }
    }

    fun find(id: Int): TaskItem? {
        return this.firstOrNull { it.id == id }
    }

    // 全アイテムを読み込む
    fun load(database: Database?) {
        clear()

        val dbResource = database?.resource
        if (dbResource != null) {
            val columns = arrayOf(
                    "id",
                    "title",
                    "color"
            )
            val cursor = dbResource.query(
                    DB_TABLE, columns, null, null, null, null, "id"
            )
            if (cursor?.moveToFirst() == true) {
                do {
                    val item = TaskItem()
                    item.id = cursor.getInt(0)
                    item.title = cursor.getString(1)
                    item.colorId = cursor.getInt(2)

                    this.add(item)
                } while (cursor.moveToNext())
            }
            cursor?.close()
        }
    }

    // データ更新
    fun update(database: Database?, taskItem: TaskItem) : Boolean {
        val dbResource = database?.resource
        var result = false
        if (dbResource != null) {
            val values = ContentValues()
            values.put("title", taskItem.title)
            values.put("color", taskItem.colorId)

            val updatedCount: Int = if (taskItem.id <= 0) {
                dbResource.insert(DB_TABLE, null, values).toInt()
            } else {
                val where = "id = " + taskItem.id
                dbResource.update(DB_TABLE, values, where, null)
            }
            result = (updatedCount > 0)
            if (result) {
                find(taskItem.id)?.from(taskItem)
            }
        }
        return result
    }
}