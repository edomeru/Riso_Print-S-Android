package com.scanlibrary;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
// aLINK edit - Start
// android.app.DialogFragment was deprecated in API level 28
// Use androidx.fragment.app.DialogFragment instead
import androidx.fragment.app.DialogFragment;
// aLINK edit - End
import android.os.Bundle;

@SuppressLint("ValidFragment")
public class SingleButtonDialogFragment extends DialogFragment {

    protected int positiveButtonTitle;
    protected String message;
    protected String title;
    protected boolean isCancelable;

    public SingleButtonDialogFragment(int positiveButtonTitle,
                                      String message, String title, boolean isCancelable) {
        this.positiveButtonTitle = positiveButtonTitle;
        this.message = message;
        this.title = title;
        this.isCancelable = isCancelable;
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setCancelable(isCancelable)
                .setMessage(message)
                .setPositiveButton(positiveButtonTitle,
                        (dialog, which) -> {

                        });

        return builder.create();
    }
}