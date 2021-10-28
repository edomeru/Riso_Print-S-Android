/*
 * Copyright (c) 2015 Ricoh Company, Ltd. All rights reserved.
 *
 * PrinterSearchSettingsEditText.java
 * Tamago Clicker
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.view.printersearchsettings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.text.InputFilter;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;

import jp.co.riso.android.text.SnmpCommunityNameFilter;
import jp.co.riso.smartdeviceapp.AppConstants;
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager;

@SuppressLint("AppCompatCustomView")
public class SnmpCommunityNameEditText extends EditText {

    public static final String SNMP_COMMUNITY_NAME_TEXTFIELD_PASTE_BROADCAST_ID = "snmp_community_name_textfield_paste_broadcast_id";
    public static final String SNMP_COMMUNITY_NAME_SAVE_ON_BACK_BROADCAST_ID = "snmp_community_name_save_on_back_broadcast_id";

    private final Context context;
    private final SnmpCommunityNameFilter snmpCommunityNameFilter = new SnmpCommunityNameFilter(
            // filter catches invalid case for pasting in context menu or from keyboard clipboard
            new SnmpCommunityNameFilter.InvalidInputObserver() {
                @Override
                public void onInvalidInput(boolean showError) {
                    if (context != null && showError) {
                        Intent intent = new Intent(SNMP_COMMUNITY_NAME_TEXTFIELD_PASTE_BROADCAST_ID);
                        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                        setText(getText());
                    }
                }
    });

    public SnmpCommunityNameEditText(Context context) {
        super(context);
        this.context = context;
        initializeIntentFilter();
    }

    public SnmpCommunityNameEditText(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.context = context;
        initializeIntentFilter();
    }

    public SnmpCommunityNameEditText(Context context, AttributeSet attributeSet, int defStyleAttr) {
        super(context, attributeSet, defStyleAttr);
        this.context = context;
        initializeIntentFilter();
    }

    private void initializeIntentFilter() {
        setFilters(new InputFilter[]{snmpCommunityNameFilter, new InputFilter.LengthFilter(AppConstants.CONST_COMMUNITY_NAME_LIMIT)});
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
            Intent intent = new Intent(SNMP_COMMUNITY_NAME_SAVE_ON_BACK_BROADCAST_ID);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
            // Save community name in shared prefs keyboard is dismissed via back button
            saveValueToSharedPrefs(this.getText().toString());
        }
        return super.dispatchKeyEvent(event);
    }

    public void saveValueToSharedPrefs(String snmpCommunityName) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();

        if(snmpCommunityName == null || snmpCommunityName.isEmpty()) {
            snmpCommunityName = PrinterManager.getInstance(context).getSnmpCommunityNameFromSharedPrefs();
        }

        editor.putString(AppConstants.PREF_KEY_SNMP_COMMUNITY_NAME, snmpCommunityName);
        //noinspection 'ApplySharedPref'
        editor.commit();
    }
}
