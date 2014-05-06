/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * SettingsFragment.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.view.fragment;

import jp.co.riso.android.text.AlphaNumericFilter;
import jp.co.riso.android.text.InvalidCharacterFilter;
import jp.co.riso.android.util.AppUtils;
import jp.co.riso.smartdeviceapp.AppConstants;
import jp.co.riso.smartdeviceapp.R;
import jp.co.riso.smartdeviceapp.view.base.BaseFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class SettingsFragment extends BaseFragment {
    
    public static final int CARD_ID_LIMIT = 128;
    public static final int READ_COMM_NAME_LIMIT = 15;
    public static final String READ_COMM_INVALID_CHARS = " \\'\"#";
    
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
        
        EditText editText = (EditText) view.findViewById(R.id.cardIdEditText);
        editText.setText(prefs.getString(AppConstants.PREF_KEY_CARD_ID, AppConstants.PREF_DEFAULT_CARD_ID));
        editText.addTextChangedListener(new SharedPreferenceTextWatcher(getActivity(), AppConstants.PREF_KEY_CARD_ID));

        filterArray = new InputFilter[] {
                new InputFilter.LengthFilter(CARD_ID_LIMIT),
                new AlphaNumericFilter()
        };
        editText.setFilters(filterArray);
        
        editText = (EditText) view.findViewById(R.id.readCommNameEditText);
        editText.setText(prefs.getString(AppConstants.PREF_KEY_READ_COMM_NAME, AppConstants.PREF_DEFAULT_READ_COMM_NAME));
        editText.addTextChangedListener(new SharedPreferenceTextWatcher(getActivity(), AppConstants.PREF_KEY_READ_COMM_NAME));
        filterArray = new InputFilter[] {
                new InputFilter.LengthFilter(READ_COMM_NAME_LIMIT),
                new InvalidCharacterFilter(READ_COMM_INVALID_CHARS)
        };
        editText.setFilters(filterArray);
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
            editor.commit();
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
