package biz.riverone.itsudakke

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import biz.riverone.itsudakke.common.ApplicationControl
import biz.riverone.itsudakke.common.MyCalendarUtil
import biz.riverone.itsudakke.dialog.InputDoneDialog
import biz.riverone.itsudakke.dialog.ResetDialogFragment
import biz.riverone.itsudakke.dialog.SelectTaskDialog
import biz.riverone.itsudakke.dialog.SettingTaskDialog
import biz.riverone.itsudakke.models.DoneItem
import biz.riverone.itsudakke.models.DoneItemList
import biz.riverone.itsudakke.models.TaskItem
import biz.riverone.itsudakke.models.TaskItemList
import biz.riverone.itsudakke.views.CalendarActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import java.util.*

/**
 * Itsudakke: いつだっけ？
 * Copyright(C) 2018 J.Kawahara
 * 2018.1.16 J.Kawahara 新規作成
 * 2018.1.17 J.Kawahara v.1.00 初版公開
 * 2018.1.27 J.Kawahara v.1.01 履歴アクティビティの戻るボタンを削除
 * 2018.2.16 J.Kawahara v.1.02 丸型アイコンを更新
 * 2018.3.21 J.Kawahara v.1.03 カレンダー表示機能を追加
 *           J.Kawahara v.1.04 カレンダー画面のマージンを微調整
 */

class MainActivity : AppCompatActivity() {

    private val taskItemList = TaskItemList()
    private val controllerList = ArrayList<View>()
    private lateinit var mAdView: AdView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 画面をポートレートに固定する
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        // 古いデータを削除する
        DoneItemList.eraseOld(ApplicationControl.database)

        // AdMob
        MobileAds.initialize(this, "ca-app-pub-1882812461462801~4066766979")
        mAdView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
    }

    override fun onStart() {
        super.onStart()

        // データベースからタスク一覧を取得する
        taskItemList.load(ApplicationControl.database)

        initializeControls()
    }

    private fun initializeControls() {
        controllerList.clear()
        val itemCount = taskItemList.size

        // UI の配列を生成する
        (0..itemCount)
                .map { resources.getIdentifier("task" + it, "id", packageName) }
                .mapNotNullTo(controllerList) { findViewById(it) }

        // タスク名と背景色を設定する
        for (i in taskItemList.indices) {
            // 画面表示
            val taskItem = taskItemList[i]
            val view = controllerList[i]
            display(view, taskItem)

            // 入力ボタンの準備をする
            val buttonInput = view.findViewById<Button>(R.id.buttonInput)
            buttonInput.setOnClickListener(onButtonInputClickListener)

            // 履歴表示画面へのリンクの準備をする
            val calcViewer = view.findViewById<View>(R.id.calcViewer)
            calcViewer.setOnClickListener(onTaskClickListener)

            buttonInput.tag = taskItem.id
            calcViewer.tag = taskItem.id
        }
    }

    private fun display(view: View, taskItem: TaskItem) {
        // 科目名を設定する
        val textViewTaskName = view.findViewById<TextView>(R.id.textViewTaskName)
        textViewTaskName.text = taskItem.title

        // 背景色を設定する
        val colors = resources.obtainTypedArray(R.array.colors)
        val defaultColorId = (taskItem.id - 1) % colors.length()
        view.setBackgroundColor(colors.getColor(taskItem.colorId, defaultColorId))
        colors.recycle()

        // 最終実行日を表示する
        val lastDayItem = DoneItem.maxItem(ApplicationControl.database, taskItem.id)
        val strLastDay = if (lastDayItem.date > 0) {
            val month = MyCalendarUtil.toMonth(lastDayItem.date)
            val day = MyCalendarUtil.toDay(lastDayItem.date)
            val week = MyCalendarUtil.toWeekStr(lastDayItem.date)
            "$month/$day ($week)"
        } else {
            ""
        }
        val textViewLastDate = view.findViewById<TextView>(R.id.textViewLastDate)
        textViewLastDate.text = strLastDay

        // 経過日数を表示する
        val strDiff = if (lastDayItem.date > 0) {
            val lastCal = MyCalendarUtil.intToCalendar(lastDayItem.date)
            val diff = MyCalendarUtil.calcDayDiff(lastCal, Calendar.getInstance())
            if (diff <= 0) {
                "今日"
            } else {
                diff.toString() + "日前"
            }
        } else {
            ""
        }
        val textViewElapsed = view.findViewById<TextView>(R.id.textViewElapsed)
        textViewElapsed.text = strDiff
    }

    // 入力ボタンクリック時のリスナ
    private val onButtonInputClickListener = View.OnClickListener {
        sender ->

        if (sender != null && sender.tag != null) {
            val taskId = sender.tag as Int
            val taskItem = taskItemList.find(taskId) ?: TaskItem()
            if (taskItem.id > 0) {
                val doneItem = DoneItem()
                doneItem.taskId = taskId
                doneItem.date = MyCalendarUtil.currentDay()

                val inputDialog = InputDoneDialog.create(doneItem, taskItem)
                inputDialog.show(supportFragmentManager, "dialog")
            }
        }
    }

    // タスク表示部クリック時のリスナ
    private val onTaskClickListener = View.OnClickListener {
        sender ->

        if (sender != null && sender.tag != null) {
            val taskId = sender.tag as Int
            val taskItem = taskItemList.find(taskId) ?: TaskItem()
            if (taskItem.id > 0) {
                val sendIntent = Intent(this, CalendarActivity::class.java)
                sendIntent.putExtra(CalendarActivity.EXTRA_KEY_TASK_ITEM, taskItem)
                startActivity(sendIntent)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    private fun showSettingTaskDialog(taskId: Int) {
        val taskItem = taskItemList.find(taskId) ?: return
        val settingDialog = SettingTaskDialog.create(taskItem)
        settingDialog.show(supportFragmentManager, "dialog")
    }

    override fun onOptionsItemSelected(item: MenuItem?) : Boolean {
        when (item?.itemId) {
            R.id.menu_setting_task -> {
                // 科目の設定
                val selectTaskDialog = SelectTaskDialog.create()
                selectTaskDialog.show(supportFragmentManager, "dialog")
                return true
            }

            R.id.menu_reset -> {
                // リセット
                ResetDialogFragment.show(supportFragmentManager)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == InputDoneDialog.REQUEST_CODE
                && resultCode == RESULT_OK) {
            // できた！入力ダイアログから戻ってきた
            if (data != null && data.hasExtra(InputDoneDialog.EXTRA_KEY_DONE_ITEM)) {
                // 編集内容を登録する
                val doneItem = data.getParcelableExtra<DoneItem>(InputDoneDialog.EXTRA_KEY_DONE_ITEM)
                doneItem.register(ApplicationControl.database)

                val viewIndex = doneItem.taskId - 1
                display(controllerList[viewIndex], taskItemList[viewIndex])
            }
        } else if (requestCode == SelectTaskDialog.REQUEST_CODE
                && resultCode == RESULT_OK) {
            // 設定を変更する科目の選択ダイアログから戻ってきた
            if (data != null && data.hasExtra(SelectTaskDialog.EXTRA_KEY_TASK_ID)) {
                val taskId = data.getIntExtra(SelectTaskDialog.EXTRA_KEY_TASK_ID, 0)
                if (taskId > 0) {
                    // タスクの設定ダイアログを表示する
                    showSettingTaskDialog(taskId)
                }
            }
        } else if (requestCode == SettingTaskDialog.REQUEST_CODE
                && resultCode == RESULT_OK) {
            // タスクの設定ダイアログから戻ってきた
            if (data != null && data.hasExtra(SettingTaskDialog.EXTRA_KEY_TASK_ITEM)) {
                val taskItem = data.getParcelableExtra<TaskItem>(SettingTaskDialog.EXTRA_KEY_TASK_ITEM)
                taskItemList.update(ApplicationControl.database, taskItem)

                val viewIndex = taskItem.id - 1
                display(controllerList[viewIndex], taskItem)
            }
        } else if (requestCode == ResetDialogFragment.REQUEST_CODE
                && resultCode == RESULT_OK) {
            // リセット確認ダイアログから戻ってきた

            // できた！実績データを全て削除する
            DoneItemList.eraseAll(ApplicationControl.database)

            // タスクデータを削除後、初期データを投入する
            TaskItemList.reset(ApplicationControl.database)

            onStart()
            Toast.makeText(this, R.string.reset_message, Toast.LENGTH_SHORT).show()
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}
