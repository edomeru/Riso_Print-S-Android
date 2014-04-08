/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * PrintingDialogFragment.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.android.dialog;

import jp.co.riso.smartdeviceapp.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

public class PrintingDialogFragment extends DialogFragment {
    
    public PrintingDialogFragment() {
        super();
    }
    
    public static PrintingDialogFragment newInstance() {
        PrintingDialogFragment dialog = null;
        
        dialog = new PrintingDialogFragment();
        return dialog;
    }
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        
        Context context = getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        AlertDialog dialog;
        
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_printing, null);
        
        builder.setView(view);
        
        dialog = builder.create();
        
        return dialog;
    }
}