/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * DialogUtils.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.android.dialog;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

/**
 * @class DialogUtils
 * 
 * @brief Helper class for displaying and dismissing dialog fragments.
 */
public final class DialogUtils {
    
    /**
     * @brief Removes a dialog fragment with the given tag in a activity using a transaction
     * 
     * @param ft FragmentTransaction to include the removal of fragment
     * @param activity Activity where the fragment resides
     * @param tag The tag of fragment for removal
     */
    private static void removeDialogFragment(FragmentTransaction ft, FragmentActivity activity, String tag) {
        
        // Get the fragment
        Fragment prev = activity.getSupportFragmentManager().findFragmentByTag(tag);
        
        // Remove the fragment if found
        if (prev != null) {
            ft.remove(prev);
        }
    }
    
    /**
     * @brief Displays a dialog fragment specifying a tag in an activity
     * 
     * @param activity Activity to display the fragment on.
     * @param tag The assigned tag of the fragment to be added
     * @param newFragment DialogFragment object to be added
     */
    public static void displayDialog(FragmentActivity activity, String tag, DialogFragment newFragment) {
        
        // Create a fragment transaction
        FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
        
        // Remove any instances of fragment
        removeDialogFragment(ft, activity, tag);
        
        // Show the fragment
        newFragment.show(ft, tag);
    }
    
    /**
     * @brief Dismisses a dialog with the given tag in an activity
     * 
     * @param activity Activity where the fragment resides
     * @param tag The tag of fragment for removal
     */
    public static void dismissDialog(FragmentActivity activity, String tag) {
        
        // Create a fragment transaction
        FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
        
        // Remove the fragment
        removeDialogFragment(ft, activity, tag);
        
        // Allow removal even on paused state
        ft.commitAllowingStateLoss();
    }

}
