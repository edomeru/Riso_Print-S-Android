/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * InfoDialogFragment.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.android.dialog;

import androidx.fragment.app.DialogFragment;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.ContextThemeWrapper;

/**
 * @class InfoDialogFragment
 *
 * @brief Custom Dialog Fragment class for information display dialog
 *
 * @note Generic information dialog. To use do the ff:
 * 1. Target Fragment must implement the WaitingDialogFragment
 * 2. In the target fragment, add these snippet:
 *      @code
 *      InfoDialogFragment dialog = new InfoDialogFragment(<parameters>):
 *      InfoDialogFragment.showDisplayDialog(activity, tag, dialog);
 *      @endcode
 */
public class InfoDialogFragment extends DialogFragment {

    private static final String KEY_TITLE = "title";
    private static final String KEY_MESSAGE = "message";
    private static final String KEY_BUTTON = "button";


    /**
     * @brief Creates an InfoDialogFragment instance.
     *
     * @param message The text displayed as the message in the dialog
     * @param buttonTitle The text displayed in the button of the dialog
     *
     * @return InfoDialogFragment instance
     */
    public static InfoDialogFragment newInstance(String message, String buttonTitle) {
        return InfoDialogFragment.newInstance(null, message, buttonTitle);
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
    public static InfoDialogFragment newInstance(String title, String message, String buttonTitle) {
        InfoDialogFragment dialog = new InfoDialogFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putString(KEY_TITLE, title);
        args.putString(KEY_MESSAGE, message);
        args.putString(KEY_BUTTON, buttonTitle);

        dialog.setArguments(args);

        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String title = getArguments().getString(KEY_TITLE);
        String message = getArguments().getString(KEY_MESSAGE);
        String buttonTitle = getArguments().getString(KEY_BUTTON);

        ContextThemeWrapper newContext = new ContextThemeWrapper(getActivity(), android.R.style.TextAppearance_Holo_DialogWindowTitle);
        AlertDialog.Builder builder = new AlertDialog.Builder(newContext);

        if (title != null) {
            builder.setTitle(title);
        }

        if (message != null) {
            builder.setMessage(message);
        }

        if (buttonTitle != null) {
            builder.setNegativeButton(buttonTitle, null);
        }

        AlertDialog dialog;
        dialog = builder.create();

        return dialog;
    }
}
