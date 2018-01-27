package biz.riverone.itsudakke.dialog

import android.app.Activity
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.TextView
import biz.riverone.itsudakke.R
import biz.riverone.itsudakke.common.MyCalendarUtil
import biz.riverone.itsudakke.models.DoneItem
import biz.riverone.itsudakke.models.TaskItem

/**
 * 「できた！」実績入力ダイアログ
 * Created by kawahara on 2018/01/17.
 */
class InputDoneDialog : SettingDialogBase(), DatePickerDialog.OnDateSetListener {

    companion object {
        const val REQUEST_CODE: Int = 100
        const val EXTRA_KEY_DONE_ITEM = "doneItem"
        const val EXTRA_KEY_TASK_ITEM = "taskItem"

        fun create(doneItem: DoneItem, taskItem: TaskItem): InputDoneDialog {
            val dialog = InputDoneDialog()
            val bundle = Bundle()
            bundle.putParcelable(EXTRA_KEY_DONE_ITEM, doneItem)
            bundle.putParcelable(EXTRA_KEY_TASK_ITEM, taskItem)
            dialog.arguments = bundle
            dialog.setTargetFragment(null, REQUEST_CODE)
            return dialog
        }
    }

    override val layoutId = R.layout.dialog_input_done
    override val dialogTitleResourceId: Int get() { return R.string.empty }

    private var doneItem: DoneItem? = null
    private var textViewDoneDay: TextView? = null
    private var memoEdit: EditText? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        // ソフトウェアキーボードを表示する
        memoEdit?.requestFocus()
        memoEdit?.selectAll()
        dialog.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)

        return view
    }

    override fun initializeControls(v: View) {
        val taskItem: TaskItem = if (arguments != null && arguments.containsKey(EXTRA_KEY_TASK_ITEM)) {
            arguments.getParcelable(EXTRA_KEY_TASK_ITEM)
        } else {
            TaskItem()
        }

        if (arguments != null && arguments.containsKey(EXTRA_KEY_DONE_ITEM)) {
            doneItem = arguments.getParcelable(EXTRA_KEY_DONE_ITEM)
        }

        if (doneItem == null) {
            doneItem = DoneItem()
            doneItem?.date = MyCalendarUtil.currentDay()
        }

        // 背景色を設定する
        val colors = resources.obtainTypedArray(R.array.colors)
        v.setBackgroundColor(colors.getColor(taskItem.colorId, 0))
        colors.recycle()

        // タイトルを設定する
        val titleView = v.findViewById<TextView>(R.id.textViewTitle)
        val title = taskItem.title + resources.getString(R.string.dialogPostTitle)
        titleView.text = title

        // 日付入力の準備
        textViewDoneDay = v.findViewById(R.id.textViewTradeDate)
        textViewDoneDay?.setOnClickListener {
            val currentDate = MyCalendarUtil.currentDay()
            val y = MyCalendarUtil.toYear(doneItem?.date ?: MyCalendarUtil.toYear(currentDate))
            val m = MyCalendarUtil.toMonth(doneItem?.date ?: MyCalendarUtil.toMonth(currentDate))
            val d = MyCalendarUtil.toDay(doneItem?.date ?: MyCalendarUtil.toDay(currentDate))

            val datePickerDialog = DatePickerDialog(context, this, y, m - 1, d)
            datePickerDialog.show()
        }

        val currentDay = MyCalendarUtil.currentDay()
        if (doneItem?.date ?: 0 <= 0) {
            doneItem?.date = currentDay
        }
        displayDoneDate(doneItem?.date ?: currentDay)

        // メモ欄の準備
        memoEdit = v.findViewById(R.id.editTextMemo)
        memoEdit?.setText(doneItem?.memo)

        // 削除ボタンの準備
        val eraseController = v.findViewById<View>(R.id.eraseController)
        if (doneItem?.id ?: 0 > 0) {
            val buttonErase = v.findViewById<Button>(R.id.buttonErase)
            buttonErase.setOnClickListener(buttonEraseClickListener)
        } else {
            eraseController.visibility = View.GONE
        }
    }

    // 日付選択ダイアログから戻ってきたときのリスナ
    override fun onDateSet(datePicker: DatePicker?, year: Int, month: Int, day: Int) {
        doneItem?.date = (year * 10000) + ((month + 1) * 100) + day
        displayDoneDate(doneItem?.date?: MyCalendarUtil.currentDay())
    }

    override fun putResult(result: Intent): Intent {
        // 日付は、onDateSet() にて設定済みのはず

        // メモを取得する
        doneItem?.memo = memoEdit?.text.toString()

        result.putExtra(EXTRA_KEY_DONE_ITEM, doneItem)
        return result
    }

    private fun displayDoneDate(value: Int) {
        val caption = DateFormat.format("yyyy/MM/dd", MyCalendarUtil.intToCalendar(value))
        textViewDoneDay?.text = caption
    }

    // 削除ボタンクリック時の動作
    private val buttonEraseClickListener = View.OnClickListener {
        AlertDialog.Builder(activity)
                .setTitle(R.string.confirmEraseTitle)
                .setMessage(R.string.confirmErase)
                .setPositiveButton(R.string.captionOk, eraseOkButtonClickListener)
                .setNegativeButton(R.string.captionCancel, null)
                .show()
    }

    private val eraseOkButtonClickListener = DialogInterface.OnClickListener {
        _, _ ->

        // 削除フラグを立てる
        doneItem?.erased = 1

        val result = Intent()
        putResult(result)

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