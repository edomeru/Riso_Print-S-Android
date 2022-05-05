/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * PrinterInfoFragment.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.smartdeviceapp.view.fragment

import android.content.res.Configuration
import android.os.Bundle
import android.os.Looper
import android.os.Message
import android.view.Gravity
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import jp.co.riso.android.dialog.DialogUtils
import jp.co.riso.android.dialog.InfoDialogFragment
import jp.co.riso.android.os.pauseablehandler.PauseableHandler
import jp.co.riso.android.os.pauseablehandler.PauseableHandlerCallback
import jp.co.riso.smartdeviceapp.SmartDeviceApp
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager.Companion.getInstance
import jp.co.riso.smartdeviceapp.model.Printer
import jp.co.riso.smartdeviceapp.model.Printer.PortSetting
import jp.co.riso.smartdeviceapp.model.printsettings.PrintSettings
import jp.co.riso.smartdeviceapp.view.MainActivity
import jp.co.riso.smartdeviceapp.view.base.BaseFragment
import jp.co.riso.smartdeviceapp.view.printers.DefaultPrinterArrayAdapter
import jp.co.riso.smartprint.R

/**
 * @class PrinterInfo
 *
 * @brief Fragment for Printer Info Screen
 */
class PrinterInfoFragment : BaseFragment(), OnItemSelectedListener, PauseableHandlerCallback {
    private var mPrinter: Printer? = null
    private var mPrinterName: TextView? = null
    private var mIpAddress: TextView? = null
    private var mPort: Spinner? = null
    private var mDefaultPrinter: Spinner? = null
    private var mPrinterManager: PrinterManager? = null
    private var mPrintSettingsFragment: PrintSettingsFragment? = null
    private var mPauseableHandler: PauseableHandler? = null
    private var mDefaultPrinterAdapter: DefaultPrinterArrayAdapter? = null

    override val viewLayout: Int
        get() = R.layout.fragment_printerinfo

    override fun initializeFragment(savedInstanceState: Bundle?) {
        mPrinterManager = getInstance(SmartDeviceApp.appContext!!)
        mPauseableHandler = PauseableHandler(Looper.myLooper(), this)
    }

    override fun initializeView(view: View, savedInstanceState: Bundle?) {
        mPrinterName = view.findViewById(R.id.inputPrinterName)
        mIpAddress = view.findViewById(R.id.inputIpAddress)
        mPort = view.findViewById(R.id.inputPort)
        mPort?.setOnItemSelectedListener(this)
        mDefaultPrinter = view.findViewById(R.id.defaultPrinter)
        mDefaultPrinter?.setOnItemSelectedListener(this)
        if (savedInstanceState != null) {
            if (mPrinter == null) {
                val printersList = mPrinterManager!!.savedPrintersList
                val printerId = savedInstanceState.getInt(KEY_PRINTER_INFO_ID)
                for (printer in printersList!!) {
                    if (printer!!.id == printerId) {
                        mPrinter = printer
                    }
                }
            }
        }
        val portAdapter = ArrayAdapter<String>(requireActivity(), R.layout.printerinfo_port_item)
        // Assumption is that LPR is always available
        portAdapter.add(getString(R.string.ids_lbl_port_lpr))
        if (mPrinter!!.config!!.isRawAvailable) {
            portAdapter.add(getString(R.string.ids_lbl_port_raw))
            portAdapter.setDropDownViewResource(R.layout.printerinfo_port_dropdownitem)
        } else {
            mPort?.setVisibility(View.GONE)
            // Port setting is always displayed as LPR
            view.findViewById<View>(R.id.defaultPort).visibility = View.VISIBLE
        }
        mPort?.setAdapter(portAdapter)
        mPort?.setSelection(mPrinter!!.portSetting!!.ordinal)
        mDefaultPrinterAdapter =
            DefaultPrinterArrayAdapter(activity, R.layout.printerinfo_port_item)
        mDefaultPrinterAdapter!!.add(getString(R.string.ids_lbl_yes))
        mDefaultPrinterAdapter!!.add(getString(R.string.ids_lbl_no))
        mDefaultPrinterAdapter!!.setDropDownViewResource(R.layout.printerinfo_port_dropdownitem)
        mDefaultPrinter?.setAdapter(mDefaultPrinterAdapter)
        if (mPrinterManager!!.defaultPrinter == mPrinter!!.id) {
            mDefaultPrinter?.setSelection(0) //yes
        } else mDefaultPrinter?.setSelection(1) //no
    }

    override fun initializeCustomActionBar(view: View, savedInstanceState: Bundle?) {
        val textView = view.findViewById<TextView>(R.id.actionBarTitle)
        textView.setText(R.string.ids_lbl_printer_info)
        addMenuButton(
            view,
            R.id.rightActionLayout,
            R.id.menu_id_action_print_settings_button,
            R.drawable.selector_actionbar_printerinfo,
            this
        )
        addMenuButton(
            view,
            R.id.leftActionLayout,
            R.id.menu_id_back_button,
            R.drawable.selector_actionbar_back,
            this
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var printerName = mPrinter!!.name
        if (printerName == null || printerName.isEmpty()) {
            printerName = requireActivity().resources.getString(R.string.ids_lbl_no_name)
        }
        mPrinterName!!.text = printerName
        mIpAddress!!.text = mPrinter!!.ipAddress
        if (mPrinterManager!!.defaultPrinter == mPrinter!!.id) {
            mDefaultPrinter!!.setSelection(0)
        } else mDefaultPrinter!!.setSelection(1)
        mPort!!.setSelection(mPrinter!!.portSetting!!.ordinal)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(KEY_PRINTER_INFO_ID, mPrinter!!.id)
        super.onSaveInstanceState(outState)
    }

    override fun clearIconStates() {
        super.clearIconStates()
        setIconState(R.id.menu_id_action_print_settings_button, false)
    }

    override fun onResume() {
        super.onResume()
        mPauseableHandler!!.resume()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (mPauseableHandler != null) {
            mPauseableHandler!!.resume()
        }
    }
    // ================================================================================
    // Public Methods
    // ================================================================================
    /**
     * @brief Sets the printer object to be displayed by the Printer Info Screen.
     *
     * @param printer Printer object
     */
    fun setPrinter(printer: Printer?) {
        mPrinter = printer
    }

    // ================================================================================
    // INTERFACE - PauseableHandlerCallback
    // ================================================================================
    override fun storeMessage(message: Message?): Boolean {
        return false
    }

    override fun processMessage(message: Message?) {
        val id = message!!.what
        if (id == R.id.menu_id_action_print_settings_button) {
            mPauseableHandler!!.pause()
            if (activity != null && activity is MainActivity) {
                val activity = activity as MainActivity?
                if (!activity!!.isDrawerOpen(Gravity.RIGHT)) {
                    val v = message.obj as View
                    val fm = requireActivity().supportFragmentManager
                    setIconState(v.id, true)
                    mPrintSettingsFragment = null
                    val ft = fm.beginTransaction()
                    mPrintSettingsFragment = PrintSettingsFragment()
                    ft.replace(
                        R.id.rightLayout,
                        mPrintSettingsFragment!!,
                        PrintPreviewFragment.FRAGMENT_TAG_PRINT_SETTINGS
                    )
                    ft.commit()
                    mPrintSettingsFragment!!.setPrinterId(mPrinter!!.id)
                    // use new print settings retrieved from the database
                    mPrintSettingsFragment!!.setPrintSettings(
                        PrintSettings(
                            mPrinter!!.id,
                            mPrinter!!.printerType!!
                        )
                    )
                    val printersFragment =
                        fm.findFragmentByTag(FRAGMENT_TAG_PRINTERS) as PrintersFragment?
                    mPrintSettingsFragment!!.setTargetFragment(printersFragment, 0)
                    activity.openDrawer(Gravity.RIGHT, false)
                } else {
                    activity.closeDrawers()
                }
            }
        } else if (id == R.id.menu_id_back_button) {
            mPauseableHandler!!.pause()
            val fm = requireActivity().supportFragmentManager
            val ft = fm.beginTransaction()
            if (fm.backStackEntryCount > 0) {
                fm.popBackStack()
                ft.commit()
            }
        }
    }

    // ================================================================================
    // INTERFACE - View.OnClickListener
    // ================================================================================
    override fun onClick(v: View) {
        val id = v.id
        if (id == R.id.menu_id_action_print_settings_button) {
            val newMessage =
                Message.obtain(mPauseableHandler, R.id.menu_id_action_print_settings_button)
            newMessage.obj = v
            mPauseableHandler!!.sendMessage(newMessage)
        } else if (id == R.id.menu_id_back_button) {
            mPauseableHandler!!.sendEmptyMessage(R.id.menu_id_back_button)
        }
    }

    // ================================================================================
    // INTERFACE - OnItemSelectedListener
    // ================================================================================
    override fun onItemSelected(
        parentView: AdapterView<*>,
        selectedItemView: View,
        position: Int,
        id: Long
    ) {
        val parentId = parentView.id
        if (parentId == R.id.inputPort) {
            var port = PortSetting.LPR
            when (position) {
                1 -> port = PortSetting.RAW
                else -> {}
            }
            mPrinter!!.portSetting = port
            mPrinterManager!!.updatePortSettings(mPrinter!!.id, port)
        } else if (parentId == R.id.defaultPrinter) {
            when (position) {
                0 -> {
                    if (mPrinterManager!!.setDefaultPrinter(mPrinter)) {
                        mDefaultPrinterAdapter!!.isNoDisabled = true
                    } else {
                        val info = InfoDialogFragment.newInstance(
                            requireActivity().getString(R.string.ids_lbl_printer_info),
                            requireActivity().getString(R.string.ids_err_msg_db_failure),
                            requireActivity().getString(R.string.ids_lbl_ok)
                        )
                        DialogUtils.displayDialog(requireActivity(), KEY_PRINTER_INFO_ERR_DIALOG, info)
                    }
                }
                else -> {}
            }
        }
    }

    override fun onNothingSelected(parentView: AdapterView<*>?) {
        // Do nothing
    }

    companion object {
        private const val FRAGMENT_TAG_PRINTERS = "fragment_printers"
        private const val KEY_PRINTER_INFO_ID = "fragment_printer_info_id"
        private const val KEY_PRINTER_INFO_ERR_DIALOG = "printer_info_err_dialog"
    }
}