/*
 * Copyright (c) 2022 RISO, Inc. All rights reserved.
 *
 * DialogUtils.kt
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.android.dialog

import androidx.fragment.app.*

/**
 * @class DialogUtils
 *
 * @brief Helper class for displaying and dismissing dialog fragments.
 */

object DialogUtils {
    /**
     * @brief Removes a dialog fragment with the given tag in a activity using a transaction
     *
     * @param ft FragmentTransaction to include the removal of fragment
     * @param activity Activity where the fragment resides
     * @param tag The tag of fragment for removal
     */
    @JvmStatic
    private fun removeDialogFragment(
        ft: FragmentTransaction,
        activity: FragmentActivity,
        tag: String
    ) {

        // Get the fragment
        val prev = activity.supportFragmentManager.findFragmentByTag(tag)

        // Remove the fragment if found
        if (prev != null) {
            ft.remove(prev)
        }
    }

    /**
     * @brief Displays a dialog fragment specifying a tag in an activity
     *
     * @param activity Activity to display the fragment on.
     * @param tag The assigned tag of the fragment to be added
     * @param newFragment DialogFragment object to be added
     */
    @JvmStatic
    fun displayDialog(activity: FragmentActivity, tag: String, newFragment: DialogFragment) {

        // Create a fragment transaction
        val ft = activity.supportFragmentManager.beginTransaction()

        // Remove any instances of fragment
        removeDialogFragment(ft, activity, tag)

        // Show the fragment
        newFragment.show(ft, tag)
    }

    /**
     * @brief Dismisses a dialog with the given tag in an activity
     *
     * @param activity Activity where the fragment resides
     * @param tag The tag of fragment for removal
     */
    @JvmStatic
    fun dismissDialog(activity: FragmentActivity, tag: String) {

        // Create a fragment transaction
        val ft = activity.supportFragmentManager.beginTransaction()

        // Remove the fragment
        removeDialogFragment(ft, activity, tag)

        // Allow removal even on paused state
        ft.commitAllowingStateLoss()
    }
}