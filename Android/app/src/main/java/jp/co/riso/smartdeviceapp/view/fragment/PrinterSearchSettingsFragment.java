/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * AddPrinterFragment.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.view.fragment;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import jp.co.riso.android.util.AppUtils;
import jp.co.riso.smartdeviceapp.AppConstants;
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager;
import jp.co.riso.smartdeviceapp.view.MainActivity;
import jp.co.riso.smartdeviceapp.view.base.BaseFragment;
import jp.co.riso.smartdeviceapp.view.printersearchsettings.SnmpCommunityNameEditText;
import jp.co.riso.smartprint.R;

/**
 * @class AddPrinterFragment
 * 
 * @brief Fragment for Add Printer Screen.
 */
public class PrinterSearchSettingsFragment extends BaseFragment {

    private BroadcastReceiver snmpCommunityNameEditTextPastebroadcastReceiver;
    private SnmpCommunityNameEditText snmpCommunityNameEditText;

    @Override
    public void onStop() {
        super.onStop();

        if(snmpCommunityNameEditTextPastebroadcastReceiver != null) {
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(snmpCommunityNameEditTextPastebroadcastReceiver);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        IntentFilter intentFilter = new IntentFilter(SnmpCommunityNameEditText.SNMP_COMMUNITY_NAME_TEXTFIELD_PASTE_BROADCAST_ID);

        snmpCommunityNameEditTextPastebroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(SnmpCommunityNameEditText.SNMP_COMMUNITY_NAME_TEXTFIELD_PASTE_BROADCAST_ID.equals(intent.getAction())) {
                    showInvalidPasteErrorDialog();
                }
            }
        };

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(snmpCommunityNameEditTextPastebroadcastReceiver, intentFilter);
    }

    @Override
    public int getViewLayout() {
        return R.layout.fragment_printersearchsettings;
    }

    @Override
        public void initializeFragment(Bundle savedInstanceState) {
        setRetainInstance(true);
    }

    @Override
    public void initializeView(View view, Bundle savedInstanceState) {
        snmpCommunityNameEditText = (SnmpCommunityNameEditText) view.findViewById(R.id.inputSnmpCommunityName);
        snmpCommunityNameEditText.setText(PrinterManager.getInstance(getActivity()).getSnmpCommunityNameFromSharedPrefs());
        snmpCommunityNameEditText.addTextChangedListener(new SnmpCommunityNameTextWatcher());

        if (!isTablet()) {
            Point screenSize = AppUtils.getScreenDimensions(getActivity());
            View rootView = view.findViewById(R.id.rootView);
            if (rootView == null) {
                return;
            }
            ViewGroup.LayoutParams params = rootView.getLayoutParams();
            if (screenSize.x > screenSize.y) {
                params.width = screenSize.y;
            } else {
                params.width = screenSize.x;
            }
        }
    }
    
    @Override
    public void initializeCustomActionBar(View view, Bundle savedInstanceState) {
        TextView textView = (TextView) view.findViewById(R.id.actionBarTitle);
        textView.setText(R.string.ids_lbl_search_printers_settings);
        
        if (isTablet()) {
            int leftTextPadding = (int) getResources().getDimension(R.dimen.home_title_padding);            
            textView.setPadding(leftTextPadding, 0, 0, 0);
        } else {
            addMenuButton(view, R.id.leftActionLayout, R.id.menu_id_back_button, R.drawable.selector_actionbar_back, this);
        }
    }
    
    // ================================================================================
    // Private Methods
    // ================================================================================

    /**
     * @brief Close the Search Settings Printer screen
     */
    private void closeScreen() {

        if (isTablet()) {
            if (getActivity() != null && getActivity() instanceof MainActivity) {
                final MainActivity activity = (MainActivity) getActivity();
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        activity.closeDrawers();
                    }
                });
            }
        } else {
            FragmentManager fm = getFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();

            if (fm.getBackStackEntryCount() > 0) {
                fm.popBackStack();
                ft.commit();
            }
        }
        AppUtils.hideSoftKeyboard(getActivity());
    }

    private void showInvalidPasteErrorDialog() {
        Context context = getActivity();

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setTitle(context.getString(R.string.ids_lbl_search_printers_settings));
        alertDialogBuilder.setMessage(context.getString(R.string.ids_err_msg_invalid_community_name));
        alertDialogBuilder.setPositiveButton(context.getString(R.string.ids_lbl_ok), null);
        alertDialogBuilder.create().show();
    }

    private void saveSnmpCommunityNameToSharedPrefs(String snmpCommunityName) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();

        if(snmpCommunityName == null || snmpCommunityName.isEmpty()) {
            snmpCommunityName = AppConstants.PREF_DEFAULT_SNMP_COMMUNITY_NAME;
        }

        editor.putString(AppConstants.PREF_KEY_SNMP_COMMUNITY_NAME, snmpCommunityName);
        editor.commit();
    }

    // ================================================================================
    // INTERFACE - View.OnClickListener
    // ================================================================================

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.menu_id_back_button:
                closeScreen();
                break;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        saveSnmpCommunityNameToSharedPrefs(snmpCommunityNameEditText.getText().toString());
    }

    private class SnmpCommunityNameTextWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            saveSnmpCommunityNameToSharedPrefs(s.toString());
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    }
}