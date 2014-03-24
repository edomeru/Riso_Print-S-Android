/*
 * Copyright (c) 2014 All rights reserved.
 *
 * ConfirmDialogFragment.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.android.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;

public class ConfirmDialogFragment  extends DialogFragment {
    
    private static final String KEY_TITLE = "title";
    private static final String KEY_MESSAGE = "message";
    private static final String KEY_POS_BUTTON = "posButton";
    private static final String KEY_NEG_BUTTON = "negButton";
    
    private OnClickListener mListener;
    
    public static ConfirmDialogFragment newInstance(String message, String buttonPosTitle, String buttonNegTitle) {
        return ConfirmDialogFragment.newInstance(null, message, buttonPosTitle, buttonNegTitle);
    }
    
    public static ConfirmDialogFragment newInstance(String title, String message, String buttonPosTitle, String buttonNegTitle) {
        ConfirmDialogFragment dialog = new ConfirmDialogFragment();
        
        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putString(KEY_TITLE, title);
        args.putString(KEY_MESSAGE, message);
        args.putString(KEY_POS_BUTTON, buttonPosTitle);
        args.putString(KEY_NEG_BUTTON, buttonNegTitle);
        
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
        String buttonPosTitle = getArguments().getString(KEY_POS_BUTTON);
        String buttonNegTitle = getArguments().getString(KEY_NEG_BUTTON);
        
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        
        if (title != null) {
            builder.setTitle(title);
        }
        
        if (message != null) {
            builder.setMessage(message);
        }
        
        if (buttonPosTitle != null) {
            builder.setPositiveButton(buttonPosTitle, mListener);
        }
        
        if (buttonNegTitle != null) {
            builder.setNegativeButton(buttonNegTitle, null);
        }
        
        AlertDialog dialog = null;
        dialog = builder.create();
        
        return dialog;
    }
    
    public void setListener(OnClickListener dialogListener){
        mListener = dialogListener;
    }
    
}
