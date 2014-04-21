/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * WaitingDialogFragment.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.android.dialog;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnKeyListener;
import android.os.Bundle;
import android.view.KeyEvent;

/**
 * To use do the ff:
 * 1. Target Fragment must implement the ConfirmDialogListener
 * 2. In the target fragment, add these snippet:
 *      WaitingDialogFragment dialog = new WaitingDialogFragment(<parameters>):
 *      DialogUtils.showdisplayDialog(activity, tag, dialog);
 * 3. To dismiss, simply call: dialog.dismiss();
 */
public class WaitingDialogFragment extends DialogFragment implements OnClickListener {
    public static final String KEY_TITLE = "title";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_BUTTON = "button";
    
    public static final OnKeyListener sCancelBackButtonListener;
    
    static {
        sCancelBackButtonListener = new OnKeyListener() {
            
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    return true;
                }
                return false;
            }
        };
    }
    
    public static WaitingDialogFragment newInstance(String title, String message, String buttonTitle) {
        WaitingDialogFragment dialog = new WaitingDialogFragment();
        
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
        
        setRetainInstance(true);
    }
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String title = getArguments().getString(KEY_TITLE);
        String message = getArguments().getString(KEY_MESSAGE);
        String buttonTitle = getArguments().getString(KEY_BUTTON);
        boolean cancelable = (buttonTitle != null);
        
        final ProgressDialog dialog = new ProgressDialog(getActivity());
        
        if (title != null) {
            dialog.setTitle(title);
        }
        
        if (message != null) {
            dialog.setMessage(message);
        }
        
        dialog.setIndeterminate(true);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(cancelable);
        
        if (!cancelable) {
            // Disable the back button
            dialog.setOnKeyListener(sCancelBackButtonListener);
        } else {
            dialog.setButton(DialogInterface.BUTTON_NEGATIVE, buttonTitle, this);
        }
        
        return dialog;
    }
    
    @Override
    public void onDestroyView() {
        Dialog dialog = getDialog();
        
        // Work around bug:
        // http://code.google.com/p/android/issues/detail?id=17423
        if ((dialog != null) && getRetainInstance()) {
            dialog.setDismissMessage(null);
        }
        
        super.onDestroyView();
    }
    
    // ================================================================================
    // INTERFACE - OnClickListener
    // ================================================================================
    
    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_NEGATIVE) {
            dismiss();
        }
    }
}
