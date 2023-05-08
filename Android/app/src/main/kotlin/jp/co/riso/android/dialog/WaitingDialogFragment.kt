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
import android.view.ContextThemeWrapper
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import jp.co.riso.smartprint.R

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
    private var _requestKey: String? = null

    companion object {
        private const val KEY_TITLE = "title"
        private const val KEY_MESSAGE = "message"
        private const val KEY_CANCELABLE = "cancelable"
        private const val KEY_NEG_BUTTON = "negButton"
        private const val KEY_REQUEST = "requestKey"

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
            buttonTitle: String?,
            requestKey: String?
        ): WaitingDialogFragment {
            val dialog = WaitingDialogFragment()

            // Supply num input as an argument.
            val args = Bundle()
            args.apply {
                putString(KEY_TITLE, title)
                putString(KEY_MESSAGE, message)
                putString(KEY_NEG_BUTTON, buttonTitle)
                putBoolean(KEY_CANCELABLE, cancelable)
                putString(KEY_REQUEST, requestKey)
            }
            dialog.arguments = args
            return dialog
        }

        init {
            sCancelBackButtonListener =
                DialogInterface.OnKeyListener { _, keyCode, _ -> keyCode == KeyEvent.KEYCODE_BACK }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val title = arguments?.getString(KEY_TITLE)
        val message = arguments?.getString(KEY_MESSAGE)
        val negButton = arguments?.getString(KEY_NEG_BUTTON)
        val cancelable = arguments?.getBoolean(KEY_CANCELABLE)
        _requestKey = arguments?.getString(KEY_REQUEST)

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
            ) { dialogInterface, _ -> dialogInterface.cancel() }
        }
        dialog.setOnShowListener {
            if (message != null) {
                (dialog.findViewById<View>(R.id.progressText) as TextView).text = message
            }
        }
        return dialog
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        requireActivity().supportFragmentManager.setFragmentResult(
            _requestKey!!,
            bundleOf( DialogUtils.DIALOG_RESULT to DialogUtils.DIALOG_RESULT_CANCEL)
        )
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

    /**
     * @brief Sets the layout height of the progress dialog when wake up message is displayed
     */
    fun setLayoutHeightForWakingStatus() {
        activity?.runOnUiThread {
            if (dialog != null) {
                val dialog = dialog as AlertDialog?
                val progressText = (dialog!!.findViewById<View>(R.id.progressText) as TextView)
                val progressTextParams = progressText.layoutParams

                progressTextParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                progressText.layoutParams = progressTextParams
            }
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

        /**
         * @brief Called to setup fragment result listener
         */
        fun setResultListenerWaitingDialog(fm: FragmentManager, lifecycleOwner: LifecycleOwner, requestKey: String){
            fm.setFragmentResultListener(
                requestKey,
                lifecycleOwner
            ) { _, bundle: Bundle ->
                val result = bundle.getString(DialogUtils.DIALOG_RESULT)
                if (result == DialogUtils.DIALOG_RESULT_CANCEL) {
                    onCancel()
                }
            }
        }
    }
}