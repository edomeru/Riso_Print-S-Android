/*
 * Copyright (c) 2015 Ricoh Company, Ltd. All rights reserved.
 *
 * PrinterSearchSettingsEditText.java
 * Tamago Clicker
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.view.printersearchsettings;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.text.ClipboardManager;
import android.text.InputFilter;
import android.util.AttributeSet;
import android.widget.EditText;

import jp.co.riso.android.text.SnmpCommunityNameFilter;
import jp.co.riso.smartdeviceapp.AppConstants;

public class SnmpCommunityNameEditText extends EditText {

    public static final String SNMP_COMMUNITY_NAME_TEXTFIELD_PASTE_BROADCAST_ID = "snmp_community_name_textfield_paste_broadcast_id";

    private final Context context;
    private final SnmpCommunityNameFilter snmpCommunityNameFilter = new SnmpCommunityNameFilter();

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
    public boolean onTextContextMenuItem(int id) {
        switch (id) {
            case android.R.id.paste:
                onTextPaste();
                break;
        }

        return super.onTextContextMenuItem(id);
    }

    private void onTextPaste() {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        CharSequence clipboardText = clipboard.getText();
        CharSequence originalText = getText();

        if(!snmpCommunityNameFilter.isValid(clipboardText)) {
            Intent intent = new Intent(SNMP_COMMUNITY_NAME_TEXTFIELD_PASTE_BROADCAST_ID);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
            setText(originalText);
        }
    }
}
