/*
 * Copyright (c) 2014 All rights reserved.
 *
 * DialogUtils.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.alinkgroup.android.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;

public final class DialogUtils {
    
    private static void removeDialogFragment(FragmentTransaction ft, Activity activity, String tag) {
        Fragment prev = activity.getFragmentManager().findFragmentByTag(tag);
        if (prev != null) {
            ft.remove(prev);
        }
    }
    
    /**
     * Displays a dialog fragment
     */
    public static void displayDialog(Activity activity, String tag, DialogFragment newFragment) {
        FragmentTransaction ft = activity.getFragmentManager().beginTransaction();
        removeDialogFragment(ft, activity, tag);
        
        newFragment.show(ft, tag);
    }
    
    /**
     * Dismisses a dialog fragment
     */
    public static void dismissDialog(Activity activity, String tag) {
        FragmentTransaction ft = activity.getFragmentManager().beginTransaction();
        removeDialogFragment(ft, activity, tag);
        ft.commitAllowingStateLoss();
        
    }
    
    public static void showAlertDialog(Context context, int titleId, String[] options, OnClickListener listener, int checkedItem) {
        if (context == null) {
            return;
        }
        
        AlertDialog.Builder builder = new Builder(context);
        builder.setTitle(titleId);
        builder.setSingleChoiceItems(options, checkedItem, listener);
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }
    
    public static void showAlertDialog(Context context, String title, String[] options, OnClickListener listener, int checkedItem) {
        if (context == null) {
            return;
        }
        
        AlertDialog.Builder builder = new Builder(context);
        builder.setTitle(title);
        builder.setSingleChoiceItems(options, checkedItem, listener);
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }
    
    public static void showErrorAlertDialog(Context context, String title, String message, String negative, OnClickListener listener) {
        if (context == null) {
            return;
        }
        
        AlertDialog.Builder builder = new Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setNegativeButton(negative, listener);
        
        builder.show();
    }
    
    public static void showErrorAlertDialog(Context context, int titleId, int messageId, int negativeId, OnClickListener listener) {
        if (context == null) {
            return;
        }
        
        AlertDialog.Builder builder = new Builder(context);
        builder.setTitle(titleId);
        builder.setMessage(messageId);
        builder.setNegativeButton(negativeId, listener);
        
        builder.show();
    }
}
