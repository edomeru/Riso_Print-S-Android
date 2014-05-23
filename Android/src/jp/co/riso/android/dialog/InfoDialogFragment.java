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

public class InfoDialogFragment extends DialogFragment {
    
    public static final String KEY_TITLE = "title";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_BUTTON = "button";
    
    
    /**
     * @param message
     * @param buttonTitle
     * @return InfoDialogFragment instance
     */
    public static InfoDialogFragment newInstance(String message, String buttonTitle) {
        return InfoDialogFragment.newInstance(null, message, buttonTitle);
    }
    
    /**
     * @param title
     * @param message
     * @param buttonTitle
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
    
    /** {@inheritDoc} */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    
    /** {@inheritDoc} */
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
        
        AlertDialog dialog = null;
        dialog = builder.create();
        
        return dialog;
    }
}
