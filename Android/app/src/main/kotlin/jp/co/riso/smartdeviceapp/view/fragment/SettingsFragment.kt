/*
 * Copyright (c) 2022 RISO, Inc. All rights reserved.
 *
 * SettingsFragment.kt
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.smartdeviceapp.view.fragment

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import androidx.preference.PreferenceManager
import jp.co.riso.android.util.AppUtils
import jp.co.riso.smartdeviceapp.AppConstants
import jp.co.riso.smartdeviceapp.view.base.BaseFragment
import jp.co.riso.smartprint.R

/**
 * @class SettingsFragment
 *
 * @brief Fragment for Settings Screen
 */
class SettingsFragment : BaseFragment() {

    override val viewLayout: Int
        get() = R.layout.fragment_settings

    override fun initializeFragment(savedInstanceState: Bundle?) {}

    override fun initializeView(view: View, savedInstanceState: Bundle?) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(activity)
        val editText = view.findViewById<EditText>(R.id.loginIdEditText)
        editText.isActivated = true
        editText.setText(
            prefs.getString(
                AppConstants.PREF_KEY_LOGIN_ID,
                AppConstants.PREF_DEFAULT_LOGIN_ID
            )
        )
        editText.addTextChangedListener(
            SharedPreferenceTextWatcher(
                activity,
                AppConstants.PREF_KEY_LOGIN_ID
            )
        )

        // RM#910 for chromebook, virtual keyboard must be hidden manually after ENTER is pressed
        editText.setOnEditorActionListener(OnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_NULL) {
                if (event != null && event.action == KeyEvent.ACTION_UP) {
                    AppUtils.hideSoftKeyboard(requireActivity())
                }
                return@OnEditorActionListener true
            }
            false
        })
        val filterArray: Array<InputFilter> = arrayOf(
            LengthFilter(AppConstants.CONST_LOGIN_ID_LIMIT)
        )
        editText.filters = filterArray
        resizeView(view)
    }

    override fun initializeCustomActionBar(view: View, savedInstanceState: Bundle?) {
        val textView = view.findViewById<TextView>(R.id.actionBarTitle)
        textView.setText(R.string.ids_lbl_settings)
        addActionMenuButton(view)
    }

    override fun onClick(v: View) {
        super.onClick(v)
        val id = v.id
        if (id == R.id.menu_id_action_button) {
            AppUtils.hideSoftKeyboard(requireActivity())
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        resizeView(requireView())
    }
    // ================================================================================
    // Private Methods
    // ================================================================================
    /**
     * @brief Updates the view width.
     *
     * @param view Container of the view to be updated
     */
    private fun resizeView(view: View) {
        if (!isTablet) {
            val screenSize = AppUtils.getScreenDimensions(activity)
            val rootView = view.findViewById<View>(R.id.rootView)
            val params = rootView.layoutParams
            if (screenSize!!.x > screenSize.y) {
                params.width = screenSize.y
            } else {
                params.width = screenSize.x
            }
        }
    }
    // ================================================================================
    // Internal Classes
    // ================================================================================
    /**
     * @class SharedPreferenceTextWatcher
     *
     * @brief Class for monitoring changes in text values
     *
     * @param mContext Context
     * @param mPrefKey Shared preference key
     */
    private class SharedPreferenceTextWatcher (
        private val mContext: Context?,
        private val mPrefKey: String) : TextWatcher {

        @Synchronized
        override fun afterTextChanged(s: Editable) {
            val prefs = PreferenceManager.getDefaultSharedPreferences(mContext)
            val editor = prefs.edit()
            editor.putString(mPrefKey, s.toString())
            editor.apply()
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    }
}