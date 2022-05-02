/*
 * Copyright (c) 2022 RISO, Inc. All rights reserved.
 *
 * WaitingDialogFragment.kt
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.android.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import jp.co.riso.smartprint.R
import android.view.ContextThemeWrapper
import android.view.KeyEvent
import android.view.View
import android.widget.TextView
import androidx.fragment.app.DialogFragment

/**
 * @class WaitingDialogFragment
 *
 * @brief Custom Dialog Fragment class for waiting dialog
 *
 * @note Generic waiting dialog. To use do the ff:
 * 1. Target Fragment must implement the WaitingDialogFragment
 * 2. In the target fragment, add these snippet:
 * @code
 * WaitingDialogFragment dialog = WaitingDialogFragment.newInstance(<parameters>)
 * dialog.setTargetFragment(this, requestCode)
 * DialogUtils.displayDialog(activity, tag, dialog)
 * @endcode
 * 3. To dismiss, call: DialogUtils.dismissDialog(activity, tag);
 */
class WaitingDialogFragment : DialogFragment() {
    private var _listener: WaitingDialogListener? = null

    companion object {
        private const val KEY_TITLE = "title"
        private const val KEY_MESSAGE = "message"
        private const val KEY_CANCELABLE = "cancelable"
        private const val KEY_NEG_BUTTON = "negButton"
        private var sCancelBackButtonListener: DialogInterface.OnKeyListener? = null

        /**
         * @brief Creates an WaitingDialogFragment instance.
         *
         * @param title The text displayed as the title in the dialog
         * @param message The text displayed as the message in the dialog
         * @param cancelable True if the dialog is cancelable
         * @param buttonTitle The text displayed in the button of the dialog
         *
         * @return WaitingDialogFragment instance
         */
        @JvmStatic
        fun newInstance(
            title: String?,
            message: String?,
            cancelable: Boolean,
            buttonTitle: String?
        ): WaitingDialogFragment {
            val dialog = WaitingDialogFragment()

            // Supply num input as an argument.
            val args = Bundle()
            args.apply {
                putString(KEY_TITLE, title)
                putString(KEY_MESSAGE, message)
                putString(KEY_NEG_BUTTON, buttonTitle)
                putBoolean(KEY_CANCELABLE, cancelable)
            }
            dialog.arguments = args
            return dialog
        }

        init {
            sCancelBackButtonListener =
                DialogInterface.OnKeyListener { _, keyCode, _ -> keyCode == KeyEvent.KEYCODE_BACK }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val title = arguments?.getString(KEY_TITLE)
        val message = arguments?.getString(KEY_MESSAGE)
        val negButton = arguments?.getString(KEY_NEG_BUTTON)
        val cancelable = arguments?.getBoolean(KEY_CANCELABLE)
        val newContext =
            ContextThemeWrapper(activity, android.R.style.TextAppearance_Holo_DialogWindowTitle)
        val builder = AlertDialog.Builder(newContext)
        builder.setView(R.layout.progress_dialog)
        if (title != null) {
            builder.setTitle(title)
        }
        val dialog = builder.create()
        dialog.setCanceledOnTouchOutside(false)
        // http://developer.android.com/reference/android/app/DialogFragment.html#setCancelable(boolean)
        isCancelable = cancelable!!
        if (!cancelable) {
            // Disable the back button
            dialog.setOnKeyListener(sCancelBackButtonListener)
        } else {
            dialog.setButton(
                DialogInterface.BUTTON_NEGATIVE,
                negButton
            ) { dialog, _ -> dialog.cancel() }
        }
        dialog.setOnShowListener {
            if (message != null) {
                (dialog.findViewById<View>(R.id.progressText) as TextView).text = message
            }
        }
        return dialog
    }

    override fun onDestroyView() {
        val dialog = dialog

        // Work around bug:
        // http://code.google.com/p/android/issues/detail?id=17423
        if (dialog != null && retainInstance) {
            dialog.setDismissMessage(null)
        }
        super.onDestroyView()
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        if (targetFragment is WaitingDialogListener || _listener != null) {
            if (_listener == null) {
                _listener = targetFragment as WaitingDialogListener?
            }
            _listener!!.onCancel()
        }
    }

    /**
     * @brief Sets the message displayed in the Progress dialog.
     *
     * @param msg String to be displayed
     */
    fun setMessage(msg: String?) {
        activity?.runOnUiThread {
            if (dialog != null) {
                val dialog = dialog as AlertDialog?
                (dialog!!.findViewById<View>(R.id.progressText) as TextView).text = msg
            }
        }

    }

    fun setButtonText(buttonText: String?) {
        if (dialog != null) {
            val dialog = dialog as AlertDialog?
            dialog!!.getButton(DialogInterface.BUTTON_NEGATIVE).text = buttonText
        }
    }

    // ================================================================================
    // Internal Classes
    // ================================================================================
    /**
     * @interface WaitingDialogListener
     *
     * @brief Interface for WaitingDialog events
     */
    interface WaitingDialogListener {
        /**
         * @brief Called when the button is clicked or when dialog is cancelled
         */
        fun onCancel()
    }
}