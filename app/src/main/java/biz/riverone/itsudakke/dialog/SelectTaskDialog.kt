package biz.riverone.itsudakke.dialog

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.view.View
import android.widget.TextView
import biz.riverone.itsudakke.R
import biz.riverone.itsudakke.common.ApplicationControl
import biz.riverone.itsudakke.models.TaskItemList

/**
 * SelectTaskDialog.kt: 設定するタスクの選択ダイアログ
 * Created by kawahara on 2018/01/17.
 */
class SelectTaskDialog : SettingDialogBase() {

    companion object {
        const val REQUEST_CODE = 2
        const val EXTRA_KEY_TASK_ID = "taskId"

        fun create() : SelectTaskDialog {
            val dialog = SelectTaskDialog()
            dialog.setTargetFragment(null, REQUEST_CODE)
            dialog.showOkButton = false
            return dialog
        }
    }

    override val layoutId = R.layout.dialog_select_task
    override val dialogTitleResourceId: Int
        get() = R.string.selectTaskDialogTitle

    private val taskItemList = TaskItemList()
    private val controllerList = ArrayList<View>()

    override fun initializeControls(v: View) {
        // データベースからタスクデータを取得する
        taskItemList.load(ApplicationControl.database)

        // 選択肢コントロールの準備
        controllerList.clear()
        val itemCount = taskItemList.size
        val packageName = activity.packageName

        (0..itemCount)
                .map { resources.getIdentifier("task" + it, "id", packageName) }
                .mapNotNullTo(controllerList) { v.findViewById(it) }

        // タスク名と背景色を設定する
        val colors = resources.obtainTypedArray(R.array.colors)
        for (i in taskItemList.indices) {
            val taskItem = taskItemList[i]
            val view = controllerList[i]

            // タスク名を表示する
            val textViewTaskName = view.findViewById<TextView>(R.id.textViewTaskName)
            textViewTaskName.text = taskItem.title

            // 背景色を設定する
            val defaultColorId = (taskItem.id - 1) % colors.length()
            view.setBackgroundColor(colors.getColor(taskItem.colorId, defaultColorId))

            view.tag = taskItem.id
            view.setOnClickListener(onClickListener)
        }
        colors.recycle()
    }

    private val onClickListener = View.OnClickListener {
        sender ->

        val result = Intent()
        val taskId = sender?.tag as Int
        result.putExtra(EXTRA_KEY_TASK_ID, taskId)
        if (targetFragment != null) {
            targetFragment.onActivityResult(targetRequestCode, Activity.RESULT_OK, result)
        } else {
            val pendingIntent = activity.createPendingResult(targetRequestCode, result, PendingIntent.FLAG_ONE_SHOT)
            try {
                pendingIntent.send(Activity.RESULT_OK)
            } catch (ex: PendingIntent.CanceledException) {
                ex.printStackTrace()
            }
        }
        dismiss()
    }
}