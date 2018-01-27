package biz.riverone.itsudakke.common

import android.content.Context
import android.content.ContextWrapper
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader

/**
 * データベース接続
 * Created by kawahara on 2018/01/16.
 */

class Database(context: Context) : ContextWrapper(context) {
    private val DB_NAME = "itsudakke"
    private val DB_VERSION = 1
    private var mySQLiteDatabase: SQLiteDatabase? = null

    val resource: SQLiteDatabase?
        get() = mySQLiteDatabase

    fun openWritable() {
        if (mySQLiteDatabase != null) {
            close()
        }
        mySQLiteDatabase = DBHelper(baseContext).writableDatabase
    }

    @Suppress("unused")
    fun openReadable() {
        if (mySQLiteDatabase != null) {
            close()
        }
        mySQLiteDatabase = DBHelper(baseContext).readableDatabase
    }

    fun close() {
        mySQLiteDatabase?.close()
        mySQLiteDatabase = null
    }

    // インナークラス
    inner class DBHelper(val context: Context)
        : SQLiteOpenHelper(
            context,
            DB_NAME,
            null,
            DB_VERSION) {

        override fun onCreate(db: SQLiteDatabase?) {
            if (db != null) {
                // テーブルを作成する
                execFileSQL(db, "create_table.sql")

                // 初期データを投入する
                execFileSQL(db, "insert_table.sql")
            }
        }

        override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
            // db?.execSQL("DROP TABLE IF EXISTS " + DB_TABLE)
            // onCreate(db)
        }

        private fun execFileSQL(db: SQLiteDatabase, filename: String) {
            var inputStream: InputStream? = null
            var inputReader: InputStreamReader? = null
            var reader: BufferedReader? = null

            try {
                // 文字コード（UTF-8）を指定して、ファイルを読み込む
                inputStream = context.assets.open(filename)
                inputReader = InputStreamReader(inputStream, "UTF-8")
                reader = BufferedReader(inputReader)

                // ファイル内のすべての行を処理
                var s: String? = reader.readLine()
                while (s != null && s.isNotEmpty()) {
                    // 戦闘と末尾の空白除去
                    s = s.trim()

                    // 文字が存在する場合（空白行は処理しない）
                    if (s.isNotEmpty()) {
                        // SQL実行
                        db.execSQL(s)
                    }

                    s = reader.readLine()
                }
            }
            catch (e: IOException) {
                e.printStackTrace()
            }
            finally {
                inputStream?.close()
                inputReader?.close()
                reader?.close()
            }
        }
    }
}