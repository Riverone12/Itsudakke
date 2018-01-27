package biz.riverone.itsudakke.common

import android.app.Application
import android.content.Context

/**
 * ApplicationControl.kt: 開始時と終了時の処理
 * Created by kawahara on 2018/01/16.
 */

class ApplicationControl : Application() {

    companion object {
        private lateinit var instance: ApplicationControl

        val context: Context
            get() {
                return instance.applicationContext
            }

        val database: Database?
            get() {
                return instance.database
            }
    }

    private var database: Database? = null

    override fun onCreate() {
        super.onCreate()

        instance = this

        // データベース接続
        database = Database(context)
        database?.openWritable()
    }

    override fun onTerminate() {
        super.onTerminate()

        database?.close()
    }
}