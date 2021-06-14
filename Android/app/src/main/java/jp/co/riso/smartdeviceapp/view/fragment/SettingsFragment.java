/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * SettingsFragment.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.view.fragment;

import jp.co.riso.android.util.AppUtils;
import jp.co.riso.smartdeviceapp.AppConstants;
import jp.co.riso.smartdeviceapp.view.base.BaseFragment;
import jp.co.riso.smartprint.R;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

/**
 * @class SettingsFragment
 * 
 * @brief Fragment for Settings Screen
 */
public class SettingsFragment extends BaseFragment {
    
    @Override
    public int getViewLayout() {
        return R.layout.fragment_settings;
    }
    
    @Override
    public void initializeFragment(Bundle savedInstanceState) {
        
    }
    
    @Override
    public void initializeView(View view, Bundle savedInstanceState) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        InputFilter[] filterArray;
        
        EditText editText = (EditText) view.findViewById(R.id.loginIdEditText);
        
        editText.setActivated(true);
        editText.setText(prefs.getString(AppConstants.PREF_KEY_LOGIN_ID, AppConstants.PREF_DEFAULT_LOGIN_ID));
        editText.addTextChangedListener(new SharedPreferenceTextWatcher(getActivity(), AppConstants.PREF_KEY_LOGIN_ID));

        // RM#910 for chromebook, virtual keyboard must be hidden manually after ENTER is pressed
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_NULL) {
                    AppUtils.hideSoftKeyboard(getActivity());
                    return true;
                }
                return false;
            }
        });

        filterArray = new InputFilter[] {
                new InputFilter.LengthFilter(AppConstants.CONST_LOGIN_ID_LIMIT)
        };
        editText.setFilters(filterArray);
        
        resizeView(view);
    }
    
    @Override
    public void initializeCustomActionBar(View view, Bundle savedInstanceState) {
        TextView textView = (TextView) view.findViewById(R.id.actionBarTitle);
        textView.setText(R.string.ids_lbl_settings);
        
        addActionMenuButton(view);
    }
    
    @Override
    public void onClick(View v) {
        super.onClick(v);
        
        switch (v.getId()) {
            case R.id.menu_id_action_button:
                AppUtils.hideSoftKeyboard(getActivity());
                break;
        }
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        resizeView(getView());
    }

    // ================================================================================
    // Private Methods
    // ================================================================================
    
    /**
     * @brief Updates the view width.
     * 
     * @param view Container of the view to be updated
     */
    private void resizeView(View view) {
        if (!isTablet()) {
            Point screenSize = AppUtils.getScreenDimensions(getActivity());
            View rootView = view.findViewById(R.id.rootView);
            if (rootView == null) {
                return;
            }
            ViewGroup.LayoutParams params = rootView.getLayoutParams();
            if (screenSize.x > screenSize.y) {
                params.width = screenSize.y;
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
     */
    private class SharedPreferenceTextWatcher implements TextWatcher {
        private Context mContext;
        private String mPrefKey;
        
        /**
         * @brief Constructor.
         * 
         * @param context Context
         * @param prefKey Shared preference key
         */
        public SharedPreferenceTextWatcher(Context context, String prefKey) {
            mContext = context;
            mPrefKey = prefKey;
        }

        @Override
        public synchronized void afterTextChanged(Editable s) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(mPrefKey, s.toString());
            editor.apply();
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }
        
    }
}
