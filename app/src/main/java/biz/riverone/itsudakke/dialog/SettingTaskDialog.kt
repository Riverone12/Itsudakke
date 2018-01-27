package biz.riverone.itsudakke.dialog

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import biz.riverone.itsudakke.R
import biz.riverone.itsudakke.models.TaskItem

/**
 * タスクの設定ダイアログ
 * Created by kawahara on 2018/01/17.
 */
class SettingTaskDialog : SettingDialogBase() {

    companion object {
        const val REQUEST_CODE = 10
        const val EXTRA_KEY_TASK_ITEM = "taskItem"

        fun create(taskItem: TaskItem): SettingTaskDialog {
            val dialog = SettingTaskDialog()
            val bundle = Bundle()
            bundle.putParcelable(EXTRA_KEY_TASK_ITEM, taskItem)
            dialog.arguments = bundle
            dialog.setTargetFragment(null, REQUEST_CODE)
            return dialog
        }
    }

    override val layoutId = R.layout.dialog_setting_task
    override val dialogTitleResourceId: Int
        get() = R.string.empty

    private var taskItem: TaskItem? = null

    private var editName: EditText? = null

    override fun initializeControls(v: View) {
        if (arguments != null && arguments.containsKey(EXTRA_KEY_TASK_ITEM)) {
            taskItem = arguments.getParcelable(EXTRA_KEY_TASK_ITEM)
        }
        if (taskItem == null) {
            taskItem = TaskItem()
        }

        // タイトルを設定する
        val title = resources.getString(R.string.settingMenuTask)
        val titleView = v.findViewById<TextView>(R.id.textViewSettingTaskTitle)
        titleView.text = title

        // 名称の入力欄の準備をする
        editName = v.findViewById<EditText>(R.id.editTextTaskName)
        editName?.setText(taskItem?.title)
    }

    override fun putResult(result: Intent): Intent {
        taskItem?.title = editName?.text.toString()
        result.putExtra(EXTRA_KEY_TASK_ITEM, taskItem)
        return result
    }
}