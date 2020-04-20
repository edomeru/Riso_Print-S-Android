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
import android.content.DialogInterface.OnKeyListener;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;

/**
 * @class WaitingDialogFragment
 * 
 * @brief Custom Dialog Fragment class for waiting dialog
 * 
 * @note Generic waiting dialog. To use do the ff:
 * 1. Target Fragment must implement the WaitingDialogFragment
 * 2. In the target fragment, add these snippet:
 *      @code 
 *      WaitingDialogFragment dialog = WaitingDialogFragment.newInstance(<parameters>):
 *      dialog.setTargetFragment(this, requestCode);
 *      DialogUtils.showdisplayDialog(activity, tag, dialog); 
 *      @endcode
 * 3. To dismiss, call: DialogUtils.dismissDialog(activity, tag);
 */
public class WaitingDialogFragment extends DialogFragment {
    private static final String KEY_TITLE = "title";
    private static final String KEY_MESSAGE = "message";
    private static final String KEY_CANCELABLE = "cancelable";
    private static final String KEY_NEG_BUTTON = "negButton";
    
    private static final OnKeyListener sCancelBackButtonListener;
    private WaitingDialogListener mListener = null;
    
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
    public static WaitingDialogFragment newInstance(String title, String message, boolean cancelable, String buttonTitle) {
        WaitingDialogFragment dialog = new WaitingDialogFragment();
        
        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putString(KEY_TITLE, title);
        args.putString(KEY_MESSAGE, message);
        args.putBoolean(KEY_CANCELABLE, cancelable);
        args.putString(KEY_NEG_BUTTON, buttonTitle);
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
        String negButton = getArguments().getString(KEY_NEG_BUTTON);
        
        boolean cancelable = getArguments().getBoolean(KEY_CANCELABLE);

        ContextThemeWrapper newContext = new ContextThemeWrapper(getActivity(), android.R.style.TextAppearance_Holo_DialogWindowTitle);
        final ProgressDialog dialog = new ProgressDialog(newContext);
        
        if (title != null) {
            dialog.setTitle(title);
        }
        
        if (message != null) {
            dialog.setMessage(message);
        }
        
        dialog.setCanceledOnTouchOutside(false);
        dialog.setIndeterminate(true);
        // http://developer.android.com/reference/android/app/DialogFragment.html#setCancelable(boolean)
        setCancelable(cancelable);
        
        if (!cancelable) {
            // Disable the back button
            dialog.setOnKeyListener(sCancelBackButtonListener);
        } else {
            dialog.setButton(DialogInterface.BUTTON_NEGATIVE, negButton, new DialogInterface.OnClickListener() {
                
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            
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
    
    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        if (getTargetFragment() instanceof WaitingDialogListener) {
            if (mListener == null) {
                mListener = (WaitingDialogListener) getTargetFragment();
            }
            mListener.onCancel();
        }
    }
    
    /**
     * @brief Sets the message displayed in the Progress dialog.
     * 
     * @param msg String to be displayed
     */
    public void setMessage(final String msg) {
        if (getActivity() != null) {
            
            getActivity().runOnUiThread(new Runnable() {
                
                @Override
                public void run() {
                    if (getDialog() != null) {
                        ProgressDialog dialog = (ProgressDialog) getDialog();
                        dialog.setMessage(msg);
                    }
                }
            });
        }
        
    }
    
    public void setButtonText(final String buttonText){
        if (getDialog() != null) {
            ProgressDialog dialog = (ProgressDialog) getDialog();
            dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setText(buttonText);
        }
    }

    // ================================================================================
    // Public methods
    // ================================================================================

    public void setListener(WaitingDialogListener listener) {
        mListener = listener;
    }
    
    // ================================================================================
    // Internal Classes
    // ================================================================================
    
    /**
     * @interface WaitingDialogListener
     * 
     * @brief Interface for WaitingDialog events
     */
    public interface WaitingDialogListener {
        /**
         * @brief Called when the button is clicked or when dialog is cancelled
         */
        public void onCancel();
    }
}
