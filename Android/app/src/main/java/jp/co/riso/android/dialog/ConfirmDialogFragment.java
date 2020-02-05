/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * ConfirmDialogFragment.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.android.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.ContextThemeWrapper;

/**
 * @class ConfirmDialogFragment
 * 
 * @brief Custom Dialog Fragment class for confirmation dialog
 * 
 * @note Generic confirmation dialog. To use do the ff:
 * 1. Target Fragment must implement the ConfirmDialogListener
 * 2. In the target fragment, add these snippet:
 *      @code 
 *      ConfirmDialogFragment dialog = ConfirmDialogFragment.newInstance(<parameters>):
 *      dialog.setTargetFragment(this, requestCode);
 *      DialogUtils.showdisplayDialog(activity, tag, dialog); 
 *      @endcode
 */
public class ConfirmDialogFragment extends DialogFragment implements OnClickListener {
    
    private static final String KEY_TITLE = "title";
    private static final String KEY_MESSAGE = "message";
    private static final String KEY_POS_BUTTON = "posButton";
    private static final String KEY_NEG_BUTTON = "negButton";
    
    /**
     * @brief Creates a ConfirmDialogFragment instance.
     *      
     * @param message The text displayed as the message in the dialog
     * @param buttonPosTitle The text displayed in the positive button of the dialog
     * @param buttonNegTitle The text displayed in the negative button of the dialog
     * 
     * @return ConfirmDialogFragment instance
     */
    public static ConfirmDialogFragment newInstance(int message, int buttonPosTitle, int buttonNegTitle) {
        return ConfirmDialogFragment.newInstance(0, message, buttonPosTitle, buttonNegTitle);
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
    public static ConfirmDialogFragment newInstance(int title, int message, int buttonPosTitle, int buttonNegTitle) {
        ConfirmDialogFragment dialog = new ConfirmDialogFragment();
        
        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putInt(KEY_TITLE, title);
        args.putInt(KEY_MESSAGE, message);
        args.putInt(KEY_POS_BUTTON, buttonPosTitle);
        args.putInt(KEY_NEG_BUTTON, buttonNegTitle);
        
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
        int buttonPosTitle = getArguments().getInt(KEY_POS_BUTTON);
        int buttonNegTitle = getArguments().getInt(KEY_NEG_BUTTON);
        
        ContextThemeWrapper newContext = new ContextThemeWrapper(getActivity(), android.R.style.TextAppearance_Holo_DialogWindowTitle);
        AlertDialog.Builder builder = new AlertDialog.Builder(newContext);
        
        if (title != 0) {
            builder.setTitle(title);
        }
        
        if (message != 0) {
            builder.setMessage(message);
        }
        
        if (buttonPosTitle != 0) {
            builder.setPositiveButton(buttonPosTitle, this);
        }
        
        if (buttonNegTitle != 0) {
            builder.setNegativeButton(buttonNegTitle, this);
        }
        
        AlertDialog dialog = null;
        dialog = builder.create();
        
        return dialog;
    }
    
    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (getTargetFragment() instanceof ConfirmDialogListener) {
            ConfirmDialogListener listener = (ConfirmDialogListener) getTargetFragment();
            switch (which) {
                case Dialog.BUTTON_POSITIVE:
                    listener.onConfirm();
                    break;
                case Dialog.BUTTON_NEGATIVE:
                    listener.onCancel();
                    break;
            }
        }
    }
    
    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        if (getTargetFragment() instanceof ConfirmDialogListener) {
            ConfirmDialogListener listener = (ConfirmDialogListener) getTargetFragment();
            listener.onCancel();
        }
    }

    // ================================================================================
    // Internal Classes
    // ================================================================================
    
    /**
     * @interface ConfirmDialogListener
     * 
     * @brief Interface ConfirmDialogFragment events
     */
    public interface ConfirmDialogListener {
        /**
         * @brief Called when positive button is clicked
         */
        public void onConfirm();
        
        /**
         * @brief Called when negative button is clicked
         */
        public void onCancel();
    }
}
