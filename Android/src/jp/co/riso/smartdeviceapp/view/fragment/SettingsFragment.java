/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * SettingsFragment.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.view.fragment;

import jp.co.riso.android.text.AlphaNumericFilter;
import jp.co.riso.android.util.AppUtils;
import jp.co.riso.smartdeviceapp.AppConstants;
import jp.co.riso.smartdeviceapp.R;
import jp.co.riso.smartdeviceapp.view.base.BaseFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

public class SettingsFragment extends BaseFragment {
    
    /** {@inheritDoc} */
    @Override
    public int getViewLayout() {
        return R.layout.fragment_settings;
    }
    
    /** {@inheritDoc} */
    @Override
    public void initializeFragment(Bundle savedInstanceState) {
        
    }
    
    /** {@inheritDoc} */
    @Override
    public void initializeView(View view, Bundle savedInstanceState) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        InputFilter[] filterArray;
        
        EditText editText = (EditText) view.findViewById(R.id.loginIdEditText);
        
        editText.setActivated(true);
        editText.setText(prefs.getString(AppConstants.PREF_KEY_LOGIN_ID, AppConstants.PREF_DEFAULT_LOGIN_ID));
        editText.addTextChangedListener(new SharedPreferenceTextWatcher(getActivity(), AppConstants.PREF_KEY_LOGIN_ID));

        filterArray = new InputFilter[] {
                new InputFilter.LengthFilter(AppConstants.CONST_LOGIN_ID_LIMIT),
                new AlphaNumericFilter()
        };
        editText.setFilters(filterArray);
        
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
    
    /** {@inheritDoc} */
    @Override
    public void initializeCustomActionBar(View view, Bundle savedInstanceState) {
        TextView textView = (TextView) view.findViewById(R.id.actionBarTitle);
        textView.setText(R.string.ids_lbl_settings);
        
        addActionMenuButton(view);
    }
    
    /** {@inheritDoc} */
    @Override
    public void onClick(View v) {
        super.onClick(v);
        
        switch (v.getId()) {
            case ID_MENU_ACTION_BUTTON:
                AppUtils.hideSoftKeyboard(getActivity());
                break;
        }
    }
    
    // ================================================================================
    // Internal Classes
    // ================================================================================
    
    private class SharedPreferenceTextWatcher implements TextWatcher {
        private Context mContext;
        private String mPrefKey;
        
        /**
         * Constructor
         * 
         * @param context
         *            Context
         * @param prefKey
         *            Shared preference key
         */
        public SharedPreferenceTextWatcher(Context context, String prefKey) {
            mContext = context;
            mPrefKey = prefKey;
        }

        /** {@inheritDoc} */
        @Override
        public synchronized void afterTextChanged(Editable s) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(mPrefKey, s.toString());
            editor.apply();
        }

        /** {@inheritDoc} */
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        /** {@inheritDoc} */
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }
        
    }
}
