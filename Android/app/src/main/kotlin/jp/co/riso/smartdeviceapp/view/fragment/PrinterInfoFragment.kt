/*
 * Copyright (c) 2022 RISO, Inc. All rights reserved.
 *
 * PrinterInfoFragment.kt
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
    private var _printer: Printer? = null
    private var _printerName: TextView? = null
    private var _ipAddress: TextView? = null
    private var _macAddress: TextView? = null
    private var _port: Spinner? = null
    private var _defaultPrinter: Spinner? = null
    private var _printerManager: PrinterManager? = null
    private var _printSettingsFragment: PrintSettingsFragment? = null
    private var _pauseableHandler: PauseableHandler? = null
    private var _defaultPrinterAdapter: DefaultPrinterArrayAdapter? = null

    override val viewLayout: Int
        get() = R.layout.fragment_printerinfo

    override fun initializeFragment(savedInstanceState: Bundle?) {
        _printerManager = getInstance(SmartDeviceApp.appContext!!)
        _pauseableHandler = PauseableHandler(Looper.myLooper(), this)
    }

    override fun initializeView(view: View, savedInstanceState: Bundle?) {
        _printerName = view.findViewById(R.id.inputPrinterName)
        _ipAddress = view.findViewById(R.id.infoIpAddress)
        _macAddress = view.findViewById(R.id.infoMacAddress)
        _port = view.findViewById(R.id.inputPort)
        _port!!.onItemSelectedListener = this
        _defaultPrinter = view.findViewById(R.id.defaultPrinter)
        _defaultPrinter!!.onItemSelectedListener = this
        if (savedInstanceState != null) {
            if (_printer == null) {
                val printersList = _printerManager!!.savedPrintersList
                val printerId = savedInstanceState.getInt(KEY_PRINTER_INFO_ID)
                for (printer in printersList) {
                    if (printer!!.id == printerId) {
                        _printer = printer
                    }
                }
            }
        }
        val portAdapter = ArrayAdapter<String>(requireActivity(), R.layout.printerinfo_port_item)
        // Assumption is that LPR is always available
        portAdapter.add(getString(R.string.ids_lbl_port_lpr))
        if (_printer!!.config!!.isRawAvailable) {
            portAdapter.add(getString(R.string.ids_lbl_port_raw))
            portAdapter.setDropDownViewResource(R.layout.printerinfo_port_dropdownitem)
        } else {
            _port!!.visibility = View.GONE
            // Port setting is always displayed as LPR
            view.findViewById<View>(R.id.defaultPort).visibility = View.VISIBLE
        }
        _port!!.adapter = portAdapter
        _port!!.setSelection(_printer!!.portSetting!!.ordinal)
        _defaultPrinterAdapter =
            DefaultPrinterArrayAdapter(activity, R.layout.printerinfo_port_item)
        _defaultPrinterAdapter!!.add(getString(R.string.ids_lbl_yes))
        _defaultPrinterAdapter!!.add(getString(R.string.ids_lbl_no))
        _defaultPrinterAdapter!!.setDropDownViewResource(R.layout.printerinfo_port_dropdownitem)
        _defaultPrinter!!.adapter = _defaultPrinterAdapter
        if (_printerManager!!.defaultPrinter == _printer!!.id) {
            _defaultPrinter!!.setSelection(0) //yes
        } else _defaultPrinter!!.setSelection(1) //no
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
        var printerName = _printer!!.name
        if (printerName == null || printerName.isEmpty()) {
            printerName = requireActivity().resources.getString(R.string.ids_lbl_no_name)
        }
        _printerName!!.text = printerName
        _ipAddress!!.text = _printer!!.ipAddress

        if (_printer!!.macAddress == null || _printer!!.macAddress == "") {
            _macAddress!!.text = "-"
        } else {
            _macAddress!!.text = _printer!!.macAddress
        }

        if (_printerManager!!.defaultPrinter == _printer!!.id) {
            _defaultPrinter!!.setSelection(0)
        } else _defaultPrinter!!.setSelection(1)
        _port!!.setSelection(_printer!!.portSetting!!.ordinal)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(KEY_PRINTER_INFO_ID, _printer!!.id)
        super.onSaveInstanceState(outState)
    }

    override fun clearIconStates() {
        super.clearIconStates()
        setIconState(R.id.menu_id_action_print_settings_button, false)
    }

    override fun onResume() {
        super.onResume()
        _pauseableHandler!!.resume()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (_pauseableHandler != null) {
            _pauseableHandler!!.resume()
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
        _printer = printer
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
            _pauseableHandler!!.pause()
            if (activity != null && activity is MainActivity?) {
                val activity = activity as MainActivity?
                if (!activity!!.isDrawerOpen(Gravity.RIGHT)) {
                    val v = message.obj as View?
                    val fm = requireActivity().supportFragmentManager
                    setIconState(v!!.id, true)
                    _printSettingsFragment = null
                    val ft = fm.beginTransaction()
                    _printSettingsFragment = PrintSettingsFragment()
                    ft.replace(
                        R.id.rightLayout,
                        _printSettingsFragment!!,
                        PrintPreviewFragment.FRAGMENT_TAG_PRINT_SETTINGS
                    )
                    ft.commit()
                    _printSettingsFragment!!.setPrinterId(_printer!!.id)
                    // use new print settings retrieved from the database
                    _printSettingsFragment!!.setPrintSettings(
                        PrintSettings(
                            _printer!!.id,
                            _printer!!.printerType!!
                        )
                    )

                    // isolate update to Default Printer Settings screen (update should not affect Print Settings)
                    _printSettingsFragment!!.setTargetFragmentPrinters()

                    activity.openDrawer(Gravity.RIGHT, false)
                } else {
                    activity.closeDrawers()
                }
            }
        } else if (id == R.id.menu_id_back_button) {
            _pauseableHandler!!.pause()
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
                Message.obtain(_pauseableHandler, R.id.menu_id_action_print_settings_button)
            newMessage.obj = v
            _pauseableHandler!!.sendMessage(newMessage)
        } else if (id == R.id.menu_id_back_button) {
            _pauseableHandler!!.sendEmptyMessage(R.id.menu_id_back_button)
        }
    }

    // ================================================================================
    // INTERFACE - OnItemSelectedListener
    // ================================================================================
    override fun onItemSelected(
        parentView: AdapterView<*>,
        selectedItemView: View?,
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
            _printer!!.portSetting = port
            _printerManager!!.updatePortSettings(_printer!!.id, port)
        } else if (parentId == R.id.defaultPrinter) {
            when (position) {
                0 -> {
                    if (_printerManager!!.setDefaultPrinter(_printer)) {
                        _defaultPrinterAdapter!!.isNoDisabled = true
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
        private const val KEY_PRINTER_INFO_ID = "fragment_printer_info_id"
        private const val KEY_PRINTER_INFO_ERR_DIALOG = "printer_info_err_dialog"
    }
}