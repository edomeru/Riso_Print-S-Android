/*
 * Copyright (c) 2022 RISO, Inc. All rights reserved.
 *
 * InfoDialogFragment.kt
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.android.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.ContextThemeWrapper
import androidx.fragment.app.DialogFragment

/**
 * @class InfoDialogFragment
 *
 * @brief Custom Dialog Fragment class for information display dialog
 *
 * @note Generic information dialog. To use do the ff:
 * 1. Target Fragment must implement the InfoDialogFragment
 * 2. In the target fragment, add these snippet:
 * @code
 * InfoDialogFragment dialog = InfoDialogFragment.newInstance(<parameters>)
 * DialogUtils.displayDialog(activity, tag, dialog);
 * @endcode
 */
class InfoDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val title = arguments?.getString(KEY_TITLE)
        val message = arguments?.getString(KEY_MESSAGE)
        val buttonTitle = arguments?.getString(KEY_BUTTON)
        val newContext =
            ContextThemeWrapper(activity, android.R.style.TextAppearance_Holo_DialogWindowTitle)
        val builder = AlertDialog.Builder(newContext)
        if (title != null) {
            builder.setTitle(title)
        }
        if (message != null) {
            builder.setMessage(message)
        }
        if (buttonTitle != null) {
            builder.setNegativeButton(buttonTitle, null)
        }
        return builder.create()
    }

    companion object {
        private const val KEY_TITLE = "title"
        private const val KEY_MESSAGE = "message"
        private const val KEY_BUTTON = "button"

        /**
         * @brief Creates an InfoDialogFragment instance.
         *
         * @param message The text displayed as the message in the dialog
         * @param buttonTitle The text displayed in the button of the dialog
         *
         * @return InfoDialogFragment instance
         */
        @JvmStatic
        fun newInstance(
            message: String?,
            buttonTitle: String?
        ): InfoDialogFragment {
            return newInstance(null, message, buttonTitle)
        }

        /**
         * @brief Creates an InfoDialogFragment instance.
         *
         * @param title The text displayed as the title in the dialog
         * @param message The text displayed as the message in the dialog
         * @param buttonTitle The text displayed in the button of the dialog
         *
         * @return InfoDialogFragment instance
         */
        @JvmStatic
        fun newInstance(
            title: String?,
            message: String?,
            buttonTitle: String?
        ): InfoDialogFragment {
            val dialog = InfoDialogFragment()

            // Supply num input as an argument.
            val args = Bundle()
            args.apply{
                putString(KEY_TITLE, title)
                putString(KEY_MESSAGE, message)
                putString(KEY_BUTTON, buttonTitle)
            }
            dialog.arguments = args
            return dialog
        }
    }
}