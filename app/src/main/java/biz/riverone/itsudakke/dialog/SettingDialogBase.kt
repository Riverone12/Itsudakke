package biz.riverone.itsudakke.dialog

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.app.PendingIntent
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.support.v7.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import biz.riverone.itsudakke.R

/**
 * 設定ダイアログの基底クラス
 * Created by kawahara on 2018/01/17.
 */
abstract class SettingDialogBase : DialogFragment() {

    companion object {
        const val TEXT_SIZE_SMALL = 18.0f
        const val TEXT_SIZE_MIDDLE = 22.0f
        const val TEXT_SIZE_LARGE = 28.0f

        // const val MATCH_PARENT = ViewGroup.LayoutParams.MATCH_PARENT
        const val WRAP_CONTENT = ViewGroup.LayoutParams.WRAP_CONTENT
    }

    private var container: ViewGroup? = null
    protected open val layoutId: Int = R.layout.fragment_setting_dialog_base
    protected abstract val dialogTitleResourceId: Int
    protected abstract fun initializeControls(v: View)

    protected var showOkButton = true
    protected var showCancelButton = true

    interface OnCloseListener {
        fun onClose(dialog: SettingDialogBase)
    }

    private var closeListener: OnCloseListener? = null
    private var resultCode: Int = Activity.RESULT_CANCELED

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        this.container = container
        return null
    }
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val layoutParameters = dialog.window.attributes
        val metrics = resources.displayMetrics
        val dialogWidth = metrics.widthPixels * 0.8
        val dialogHeight =  metrics.heightPixels * 0.9
        layoutParameters.width = dialogWidth.toInt()
        layoutParameters.height = dialogHeight.toInt()
        dialog.window.attributes = layoutParameters
    }

    @SuppressLint("RestrictedApi")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = activity.layoutInflater.inflate(layoutId, container, false)
        initializeControls(view)

        val themeContext = ContextThemeWrapper(context, R.style.AppTheme)
        val builder = AlertDialog.Builder(themeContext)

        val title = resources.getString(dialogTitleResourceId)
        if (title.isNotEmpty()) {
            builder.setTitle(title)
        }

        if (showOkButton) {
            builder.setPositiveButton(
                    resources.getString(R.string.captionOk),
                    onButtonOkClickListener
            )
        }

        if (showCancelButton) {
            builder.setNegativeButton(
                    resources.getString(R.string.captionCancel)
            ) { _, _ -> dismiss() }
        }

        builder.setView(view)

        return builder.create()
    }

    // 結果をIntent に詰め込む
    protected open fun putResult(result: Intent): Intent = result

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        if (context is OnCloseListener) {
            closeListener = context
        }
    }

    override fun onPause() {
        super.onPause()
        closeListener?.onClose(this)
    }

    // OKボタンクリック時のイベントハンドラ
    private val onButtonOkClickListener = DialogInterface.OnClickListener {
        // dialog, which ->
        _, _ ->

        resultCode = Activity.RESULT_OK

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