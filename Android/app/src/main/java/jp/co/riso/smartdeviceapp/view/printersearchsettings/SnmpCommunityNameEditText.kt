/*
 * Copyright (c) 2015 Ricoh Company, Ltd. All rights reserved.
 *
 * PrinterSearchSettingsEditText.java
 * Tamago Clicker
 * Created by: a-LINK Group
 */
package jp.co.riso.smartdeviceapp.view.printersearchsettings

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.text.InputFilter.LengthFilter
import android.util.AttributeSet
import android.view.KeyEvent
import android.widget.EditText
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.PreferenceManager
import jp.co.riso.android.text.SnmpCommunityNameFilter
import jp.co.riso.android.text.SnmpCommunityNameFilter.InvalidInputObserver
import jp.co.riso.smartdeviceapp.AppConstants
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager.Companion.getInstance


@SuppressLint("AppCompatCustomView")
class SnmpCommunityNameEditText : EditText {
    private var _context: Context? = null
    private val snmpCommunityNameFilter =
        SnmpCommunityNameFilter( // filter catches invalid case for pasting in context menu or from keyboard clipboard
            object : InvalidInputObserver {
                override fun onInvalidInput(showError: Boolean) {
                    if (context != null && showError) {
                        val intent = Intent(SNMP_COMMUNITY_NAME_TEXTFIELD_PASTE_BROADCAST_ID)
                        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
                        text = text
                    }
                }
            })


    constructor(context: Context?) : super(context) {
        this._context = context
        initializeIntentFilter()
    }

    constructor(context: Context?, attributeSet: AttributeSet?) : super(context, attributeSet) {
        this._context = context
        initializeIntentFilter()
    }

    constructor(context: Context?, attributeSet: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attributeSet,
        defStyleAttr
    ) {
        this._context = context
        initializeIntentFilter()
    }

    private fun initializeIntentFilter() {
        filters = arrayOf(
            snmpCommunityNameFilter,
            LengthFilter(AppConstants.CONST_COMMUNITY_NAME_LIMIT)
        )
    }

    override fun onKeyPreIme(keyCode: Int, event: KeyEvent): Boolean {
        if (event.keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
            val intent = Intent(SNMP_COMMUNITY_NAME_SAVE_ON_BACK_BROADCAST_ID)
            LocalBroadcastManager.getInstance(context!!).sendBroadcast(intent)
            // Save community name in shared prefs keyboard is dismissed via back button
            saveValueToSharedPrefs(this.text.toString())
        }
        return super.dispatchKeyEvent(event)
    }

    fun saveValueToSharedPrefs(snmpCommunityName: String?) {
        var snmpCommunityName = snmpCommunityName
        val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
        if (snmpCommunityName == null || snmpCommunityName.isEmpty()) {
            snmpCommunityName = getInstance(context!!)!!.snmpCommunityNameFromSharedPrefs
        }
        editor.putString(AppConstants.PREF_KEY_SNMP_COMMUNITY_NAME, snmpCommunityName)
        editor.commit()
    }

    companion object {
        const val SNMP_COMMUNITY_NAME_TEXTFIELD_PASTE_BROADCAST_ID =
            "snmp_community_name_textfield_paste_broadcast_id"
        const val SNMP_COMMUNITY_NAME_SAVE_ON_BACK_BROADCAST_ID =
            "snmp_community_name_save_on_back_broadcast_id"
    }
}