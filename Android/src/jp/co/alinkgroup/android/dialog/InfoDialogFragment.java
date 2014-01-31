/*
 * Copyright (c) 2014 All rights reserved.
 *
 * InfoDialogFragment.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.alinkgroup.android.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;

public class InfoDialogFragment extends DialogFragment {
    
    public static final String KEY_TITLE = "title";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_BUTTON = "button";
    
    public static InfoDialogFragment newInstance(String message, String buttonTitle) {
        return InfoDialogFragment.newInstance(null, message, buttonTitle);
    }
    
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
        
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        
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
