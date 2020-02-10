/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * InfoDialogFragment.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.android.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
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
 *      InfoDialogFragment.showdisplayDialog(activity, tag, dialog);
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
    public static InfoDialogFragment newInstance(int message, int buttonTitle) {
        return InfoDialogFragment.newInstance(0, message, buttonTitle);
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
    public static InfoDialogFragment newInstance(int title, int message, int buttonTitle) {
        InfoDialogFragment dialog = new InfoDialogFragment();
        
        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putInt(KEY_TITLE, title);
        args.putInt(KEY_MESSAGE, message);
        args.putInt(KEY_BUTTON, buttonTitle);
        
        dialog.setArguments(args);

        return dialog;
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int title = getArguments().getInt(KEY_TITLE);
        int message = getArguments().getInt(KEY_MESSAGE);
        int buttonTitle = getArguments().getInt(KEY_BUTTON);

        ContextThemeWrapper newContext = new ContextThemeWrapper(getActivity(), android.R.style.TextAppearance_Holo_DialogWindowTitle);
        AlertDialog.Builder builder = new AlertDialog.Builder(newContext);
        
        if (title != 0) {
            builder.setTitle(title);
        }
        
        if (message != 0) {
            builder.setMessage(message);
        }
        
        if (buttonTitle != 0) {
            builder.setNegativeButton(buttonTitle, null);
        }
        
        AlertDialog dialog = null;
        dialog = builder.create();
        
        return dialog;
    }
}
