/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * DialogUtils.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.android.dialog;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;

public final class DialogUtils {
    
    /**
     * @brief Removes a dialog fragment with the given tag in a activity using a transaction
     * @param ft
     *            FragmentTransaction to include the removal of fragment
     * @param activity
     *            Activity where the fragment resides
     * @param tag
     *            The tag of fragment for removal
     */
    private static void removeDialogFragment(FragmentTransaction ft, Activity activity, String tag) {
        
        // Get the fragment
        Fragment prev = activity.getFragmentManager().findFragmentByTag(tag);
        
        // Remove the fragment if found
        if (prev != null) {
            ft.remove(prev);
        }
    }
    
    /**
     * @brief Displays a dialog fragment specifying a tag in an activity
     * @param activity
     *            Activity to display the fragment on.
     * @param tag
     *            The assigned tag of the fragment to be added
     * @param newFragment
     *            DialogFragment object to be added
     */
    public static void displayDialog(Activity activity, String tag, DialogFragment newFragment) {
        
        // Create a fragment transaction
        FragmentTransaction ft = activity.getFragmentManager().beginTransaction();
        
        // Remove any instances of fragment
        removeDialogFragment(ft, activity, tag);
        
        // Show the fragment
        newFragment.show(ft, tag);
    }
    
    /**
     * @brief Dismisses a dialog with the given tag in an activity
     * @param activity
     *            Activity where the fragment resides
     * @param tag
     *            The tag of fragment for removal
     */
    public static void dismissDialog(Activity activity, String tag) {
        
        // Create a fragment transaction
        FragmentTransaction ft = activity.getFragmentManager().beginTransaction();
        
        // Remove the fragment
        removeDialogFragment(ft, activity, tag);
        
        // Alllow removal even on paused state
        ft.commitAllowingStateLoss();
    }
    
    // TODO: The following codes should be converted to use Dialog Fragment
    /*
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
    */
}
