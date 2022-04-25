/*
 * Copyright (c) 2022 RISO, Inc. All rights reserved.
 *
 * ConfirmDialogFragment.kt
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.android.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.ContextThemeWrapper
import androidx.fragment.app.DialogFragment

/**
 * @class ConfirmDialogFragment
 *
 * @brief Custom Dialog Fragment class for confirmation dialog
 *
 * @note Generic confirmation dialog. To use do the ff:
 * 1. Target Fragment must implement the ConfirmDialogListener
 * 2. In the target fragment, add these snippet:
 * @code
 * ConfirmDialogFragment dialog = ConfirmDialogFragment.newInstance(<parameters>)
 * dialog.setTargetFragment(this, requestCode)
 * DialogUtils.showdisplayDialog(activity, tag, dialog)
 * @endcode
 */
class ConfirmDialogFragment : DialogFragment(), DialogInterface.OnClickListener {
    private var _listener: ConfirmDialogListener? = null
    private var _alertDialog: AlertDialog? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val title = arguments?.getString(KEY_TITLE)
        val message = arguments?.getString(KEY_MESSAGE)
        val buttonPosTitle = arguments?.getString(KEY_POS_BUTTON)
        val buttonNegTitle = arguments?.getString(KEY_NEG_BUTTON)
        val newContext =
            ContextThemeWrapper(activity, android.R.style.TextAppearance_Holo_DialogWindowTitle)
        val builder = AlertDialog.Builder(newContext)
        if (title != null) {
            builder.setTitle(title)
        }
        if (message != null) {
            builder.setMessage(message)
        }
        if (buttonPosTitle != null) {
            builder.setPositiveButton(buttonPosTitle, this)
        }
        if (buttonNegTitle != null) {
            builder.setNegativeButton(buttonNegTitle, this)
        }
        _alertDialog = builder.create()
        return _alertDialog!!
    }

    override fun onClick(dialog: DialogInterface, which: Int) {
        if (_listener == null && targetFragment is ConfirmDialogListener) {
            _listener = targetFragment as ConfirmDialogListener?
        }
        if (_listener != null) {
            when (which) {
                Dialog.BUTTON_POSITIVE -> {
                    _listener!!.onConfirm()
                    _alertDialog = null
                }
                Dialog.BUTTON_NEGATIVE -> {
                    _listener!!.onCancel()
                    _alertDialog = null
                }
            }
        }
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        if (_listener == null && targetFragment is ConfirmDialogListener) {
            _listener = targetFragment as ConfirmDialogListener?
        }
        if (_listener != null) {
            _listener!!.onCancel()
            _alertDialog = null
        }
    }

    // ================================================================================
    // Public methods
    // ================================================================================
    val isShowing: Boolean
        get() = if (_alertDialog != null) {
            _alertDialog!!.isShowing
        } else false
    // ================================================================================
    // Internal Classes
    // ================================================================================
    /**
     * @interface ConfirmDialogListener
     *
     * @brief Interface ConfirmDialogFragment events
     */
    interface ConfirmDialogListener {
        /**
         * @brief Called when positive button is clicked
         */
        fun onConfirm()

        /**
         * @brief Called when negative button is clicked
         */
        fun onCancel()
    }

    companion object {
        private const val KEY_TITLE = "title"
        private const val KEY_MESSAGE = "message"
        private const val KEY_POS_BUTTON = "posButton"
        private const val KEY_NEG_BUTTON = "negButton"

        /**
         * @brief Creates a ConfirmDialogFragment instance.
         *
         * @param message The text displayed as the message in the dialog
         * @param buttonPosTitle The text displayed in the positive button of the dialog
         * @param buttonNegTitle The text displayed in the negative button of the dialog
         *
         * @return ConfirmDialogFragment instance
         */
        @JvmStatic
        fun newInstance(
            message: String?,
            buttonPosTitle: String?,
            buttonNegTitle: String?
        ): ConfirmDialogFragment {
            return newInstance(null, message, buttonPosTitle, buttonNegTitle)
        }

        /**
         * @brief Creates a ConfirmDialogFragment instance.
         *
         * @param title The text displayed as the title in the dialog
         * @param message The text displayed as the message in the dialog
         * @param buttonPosTitle The text displayed in the positive button of the dialog
         * @param buttonNegTitle The text displayed in the negative button of the dialog
         *
         * @return ConfirmDialogFragment instance
         */
        @JvmStatic
        fun newInstance(
            title: String?,
            message: String?,
            buttonPosTitle: String?,
            buttonNegTitle: String?
        ): ConfirmDialogFragment {
            val dialog = ConfirmDialogFragment()

            // Supply num input as an argument.
            val args = Bundle()
            args.apply {
                putString(KEY_TITLE, title)
                putString(KEY_MESSAGE, message)
                putString(KEY_POS_BUTTON, buttonPosTitle)
                putString(KEY_NEG_BUTTON, buttonNegTitle)
            }
            dialog.arguments = args
            return dialog
        }
    }
}