/*
 * Copyright (c) 2022 RISO, Inc. All rights reserved.
 *
 * PrinterSearchSettingsFragment.kt
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.smartdeviceapp.view.fragment

import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import jp.co.riso.android.util.AppUtils.getScreenDimensions
import jp.co.riso.android.util.AppUtils.hideSoftKeyboard
import jp.co.riso.smartdeviceapp.AppConstants
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager.Companion.getInstance
import jp.co.riso.smartdeviceapp.view.MainActivity
import jp.co.riso.smartdeviceapp.view.base.BaseFragment
import jp.co.riso.smartdeviceapp.view.printersearchsettings.SnmpCommunityNameEditText
import jp.co.riso.smartprint.R
import kotlin.math.min

/**
 * @class AddPrinterFragment
 *
 * @brief Fragment for Add Printer Screen.
 */
class PrinterSearchSettingsFragment : BaseFragment() {
    private var _snmpCommunityNameEditTextPasteBroadcastReceiver: BroadcastReceiver? = null
    private var _snmpCommunityNameEditText: SnmpCommunityNameEditText? = null
    override fun onStop() {
        super.onStop()
        if (_snmpCommunityNameEditTextPasteBroadcastReceiver != null) {
            LocalBroadcastManager.getInstance(requireActivity()).unregisterReceiver(
                _snmpCommunityNameEditTextPasteBroadcastReceiver!!
            )
        }
    }

    override fun onStart() {
        super.onStart()
        val intentFilter =
            IntentFilter(SnmpCommunityNameEditText.SNMP_COMMUNITY_NAME_TEXTFIELD_PASTE_BROADCAST_ID)
        _snmpCommunityNameEditTextPasteBroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (SnmpCommunityNameEditText.SNMP_COMMUNITY_NAME_TEXTFIELD_PASTE_BROADCAST_ID == intent.action) {
                    showInvalidPasteErrorDialog()
                } else if (SnmpCommunityNameEditText.SNMP_COMMUNITY_NAME_SAVE_ON_BACK_BROADCAST_ID == intent.action) {
                    saveSnmpCommunityNameToSharedPrefs(if (_snmpCommunityNameEditText != null) _snmpCommunityNameEditText!!.text.toString() else AppConstants.PREF_DEFAULT_SNMP_COMMUNITY_NAME)
                }
            }
        }
        LocalBroadcastManager.getInstance(requireActivity())
            .registerReceiver(_snmpCommunityNameEditTextPasteBroadcastReceiver as BroadcastReceiver, intentFilter)
    }

    override val viewLayout: Int
        get() = R.layout.fragment_printersearchsettings

    override fun initializeFragment(savedInstanceState: Bundle?) {}

    override fun initializeView(view: View, savedInstanceState: Bundle?) {
        _snmpCommunityNameEditText = view.findViewById(R.id.inputSnmpCommunityName)
        _snmpCommunityNameEditText?.setText(getInstance(requireActivity())!!.snmpCommunityNameFromSharedPrefs)
        _snmpCommunityNameEditText?.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                saveSnmpCommunityNameToSharedPrefs(v.text.toString())
            } else if (actionId == EditorInfo.IME_NULL) {
                // RM#910 for chromebook, virtual keyboard must be hidden manually after ENTER key is pressed
                if (event != null && event.action == KeyEvent.ACTION_UP) {
                    saveSnmpCommunityNameToSharedPrefs(v.text.toString())
                    hideSoftKeyboard(requireActivity())
                }
                return@OnEditorActionListener true
            }
            false
        })

        // RM#911 when display size is changed, layout can change from tablet to phone
        // if layout is phone, also check if fragment is not currently open in the right drawer
        // if it is, do not expand to fit the screen to prevent clipping
        if (!isTablet && !isOnRightDrawer) {
            val screenSize = getScreenDimensions(activity)
            val rootView = view.findViewById<View>(R.id.rootView) ?: return
            val params = rootView.layoutParams
            params.width = min(screenSize!!.x, screenSize.y)
        }
    }

    override fun initializeCustomActionBar(view: View, savedInstanceState: Bundle?) {
        val textView = view.findViewById<TextView>(R.id.actionBarTitle)
        textView.setText(R.string.ids_lbl_search_printers_settings)

        // RM#911 when display size is changed, layout can change from tablet to phone
        // even if layout is not tablet, check if fragment is currently open in the right drawer
        // if it is, use action bar for tablet
        if (isTablet || isOnRightDrawer) {
            val leftTextPadding = resources.getDimension(R.dimen.home_title_padding)
                .toInt()
            textView.setPadding(leftTextPadding, 0, 0, 0)
        } else {
            addMenuButton(
                view,
                R.id.leftActionLayout,
                R.id.menu_id_back_button,
                R.drawable.selector_actionbar_back,
                this
            )
        }
    }
    // ================================================================================
    // Private Methods
    // ================================================================================
    /**
     * @brief Close the Search Settings Printer screen
     */
    private fun closeScreen() {
        if (isTablet) {
            if (activity != null && activity is MainActivity) {
                val activity = activity as MainActivity?
                activity!!.runOnUiThread { activity.closeDrawers() }
            }
        } else {
            val fm = parentFragmentManager
            val ft = fm.beginTransaction()
            if (fm.backStackEntryCount > 0) {
                fm.popBackStack()
                ft.commit()
            }
        }
        hideSoftKeyboard(requireActivity())
    }

    private fun showInvalidPasteErrorDialog() {
        val context: Context? = activity
        val alertDialogBuilder = AlertDialog.Builder(context)
        alertDialogBuilder.setTitle(context!!.getString(R.string.ids_lbl_search_printers_settings))
        alertDialogBuilder.setMessage(context.getString(R.string.ids_err_msg_invalid_community_name))
        alertDialogBuilder.setPositiveButton(context.getString(R.string.ids_lbl_ok), null)
        alertDialogBuilder.create().show()
    }

    private fun saveSnmpCommunityNameToSharedPrefs(snmpCommunityName: String) {
        _snmpCommunityNameEditText!!.saveValueToSharedPrefs(snmpCommunityName)
    }

    // ================================================================================
    // INTERFACE - View.OnClickListener
    // ================================================================================
    override fun onClick(v: View) {
        super.onClick(v)
        val id = v.id
        if (id == R.id.menu_id_back_button) {
            closeScreen()
        }
    }

    override fun onDetach() {
        super.onDetach()
        saveSnmpCommunityNameToSharedPrefs(_snmpCommunityNameEditText!!.text.toString())
    }

    override fun onRightFragmentDrawerClosed() {
        super.onRightFragmentDrawerClosed()
        // Save community name in shared prefs when Printer Search Settings drawer is closed
        saveSnmpCommunityNameToSharedPrefs(_snmpCommunityNameEditText!!.text.toString())
    }
}