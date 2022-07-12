/*
 * Copyright (c) 2022 RISO, Inc. All rights reserved.
 *
 * PrintersFragment.kt
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.smartdeviceapp.view.fragment

import android.content.res.Configuration
import android.os.Bundle
import android.os.Looper
import android.os.Message
import android.os.Parcelable
import android.view.Gravity
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import jp.co.riso.android.dialog.ConfirmDialogFragment
import jp.co.riso.android.dialog.ConfirmDialogFragment.ConfirmDialogListener
import jp.co.riso.android.dialog.DialogUtils
import jp.co.riso.android.dialog.InfoDialogFragment
import jp.co.riso.android.os.pauseablehandler.PauseableHandler
import jp.co.riso.android.os.pauseablehandler.PauseableHandlerCallback
import jp.co.riso.smartdeviceapp.AppConstants
import jp.co.riso.smartdeviceapp.SmartDeviceApp
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager.Companion.getInstance
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager.PrintersCallback
import jp.co.riso.smartdeviceapp.model.Printer
import jp.co.riso.smartdeviceapp.view.MainActivity
import jp.co.riso.smartdeviceapp.view.base.BaseFragment
import jp.co.riso.smartdeviceapp.view.printers.PrinterArrayAdapter
import jp.co.riso.smartdeviceapp.view.printers.PrinterArrayAdapter.PrinterArrayAdapterInterface
import jp.co.riso.smartdeviceapp.view.printers.PrintersListView
import jp.co.riso.smartdeviceapp.view.printers.PrintersScreenTabletView
import jp.co.riso.smartdeviceapp.view.printers.PrintersScreenTabletView.PrintersViewCallback
import jp.co.riso.smartprint.R

/**
 * @class PrintersFragment
 *
 * @brief Fragment for Printers Screen
 */
class PrintersFragment : BaseFragment(), PrintersCallback, PauseableHandlerCallback,
    PrintersViewCallback, ConfirmDialogListener, PrinterArrayAdapterInterface {
    private var _pauseableHandler: PauseableHandler? = null
    private var _deletePrinter: Printer? = null

    // ListView parameters
    private var _listView: ListView? = null
    private var _printer: ArrayList<Printer?>? = null
    private var _printerAdapter: ArrayAdapter<Printer?>? = null

    // Tablet parameters
    private var _printerTabletView: PrintersScreenTabletView? = null

    // Printer Manager
    private var _printerManager: PrinterManager? = null
    private var _deleteItem = PrinterManager.EMPTY_ID
    private var _scrollState: Parcelable? = null
    private var _settingItem = PrinterManager.EMPTY_ID
    private var _updateOnlineStatus: Runnable? = null
    private var _emptyPrintersText: TextView? = null

    override val viewLayout: Int
        get() = R.layout.fragment_printers

    override fun initializeFragment(savedInstanceState: Bundle?) {
        _printerManager = getInstance(SmartDeviceApp.appContext!!)
        if (_pauseableHandler == null) {
            _pauseableHandler = PauseableHandler(Looper.myLooper(), this)
        }
        _updateOnlineStatus = object : Runnable {
            override fun run() {
                /* Update online status*/
                updateOnlineStatus()
                /* Run every 5 seconds */_pauseableHandler!!.postDelayed(
                    this,
                    AppConstants.CONST_UPDATE_INTERVAL.toLong()
                )
            }
        }
    }

    override fun initializeView(view: View, savedInstanceState: Bundle?) {
        val newMessage = Message.obtain(_pauseableHandler, MSG_POPULATE_PRINTERS_LIST)
        if (!isTablet) {
            newMessage.obj = _scrollState
        }
        newMessage.arg1 = _deleteItem
        newMessage.arg2 = _settingItem
        if (_printer == null) {
            _printer = _printerManager!!.savedPrintersList as ArrayList<Printer?>?
        }
        _emptyPrintersText = view.findViewById(R.id.emptyPrintersText)
        if (isTablet) {
            _printerTabletView = view.findViewById(R.id.printerParentView)
            _printerTabletView!!.setPrintersViewCallback(this)
        } else {
            _listView = view.findViewById(R.id.printer_list)
        }
        _printerManager!!.setPrintersCallback(this)
        _pauseableHandler!!.sendMessage(newMessage)
        if (isTablet) {
            _settingItem = PrinterManager.EMPTY_ID
        } else {
            _scrollState = null
        }
        _deleteItem = PrinterManager.EMPTY_ID

        // for chromebook, scrollview must not be focusable when printers list is empty
        // scrollview constructor enables focusable so it can't be disabled in layout.xml
        if (isChromeBook) {
            val scrollView = view.findViewById<View>(R.id.printersTabletScrollView)
            if (scrollView != null) {
                scrollView.isFocusable = false
            }
        }
    }

    override fun initializeCustomActionBar(view: View, savedInstanceState: Bundle?) {
        val textView = view.findViewById<TextView>(R.id.actionBarTitle)
        textView.setText(R.string.ids_lbl_printers)
        addMenuButton(
            view,
            R.id.rightActionLayout,
            R.id.menu_id_action_add_button,
            R.drawable.selector_actionbar_add_printer,
            this
        )
        if (!isChromeBook) {
            addMenuButton(
                view,
                R.id.rightActionLayout,
                R.id.menu_id_action_search_button,
                R.drawable.selector_actionbar_printersearch,
                this
            )
        }
        addMenuButton(
            view,
            R.id.rightActionLayout,
            R.id.menu_id_printer_search_settings_button,
            R.drawable.selector_actionbar_printersearchsettings,
            this
        )
        addActionMenuButton(view)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (isTablet && _printerTabletView != null) {
            _deleteItem = _printerTabletView!!.deleteItemPosition
            _settingItem = _printerTabletView!!.defaultSettingSelected
        } else {
            if (_listView != null) {
                _scrollState = _listView!!.onSaveInstanceState()
                _deleteItem = (_listView as PrintersListView?)!!.deleteItemPosition
            } else {
                _deleteItem = PrinterManager.EMPTY_ID
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val activity = activity as MainActivity?
        if (!activity!!.isDrawerOpen(Gravity.RIGHT) && _printerManager!!.isSearching) {
            _printerManager!!.cancelPrinterSearch()
        }
        if (_updateOnlineStatus != null && _pauseableHandler != null) {
            if (_printerManager!!.savedPrintersList.isEmpty()) {
                showEmptyText()
            } else {
                showPrintersView()
            }
            _pauseableHandler!!.resume()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (_pauseableHandler != null) {
            _pauseableHandler!!.resume()
        }
        if (isTablet && _printerTabletView != null) {
            _printerTabletView!!.requestLayout()
        }
    }

    override fun onPause() {
        super.onPause()
        if (_updateOnlineStatus != null && _pauseableHandler != null) {
            _pauseableHandler!!.removeCallbacks(_updateOnlineStatus!!)
        }
    }

    /**
     * @brief Sets the selected state of a Printer's default setting in tablet view.
     *
     * @param state Set Printer to selected state
     */
    private fun setDefaultSettingSelected(state: Boolean) {
        if (_printerTabletView != null) {
            _printerTabletView!!.setDefaultSettingSelected(PrinterManager.EMPTY_ID, state)
        }
    }

    override fun clearIconStates() {
        super.clearIconStates()
        setDefaultSettingSelected(false)
        setIconState(R.id.menu_id_action_button, false)
        setIconState(R.id.menu_id_action_search_button, false)
        setIconState(R.id.menu_id_printer_search_settings_button, false)
        setIconState(R.id.menu_id_action_add_button, false)
    }
    // ================================================================================
    // Private Methods
    // ================================================================================
    /**
     * @brief Displays the Printer Search Screen.
     */
    private fun displayPrinterSearchFragment() {
        if (_isMaxPrinterCountReached) {
            _pauseableHandler!!.resume()
            return
        }
        if (isTablet) {
            setIconState(R.id.menu_id_action_search_button, true)
        }
        val fragment = PrinterSearchFragment()
        switchToFragment(fragment, FRAGMENT_TAG_PRINTER_SEARCH)
    }

    /**
     * @brief Displays the Add Printer Screen.
     */
    private fun displayAddPrinterFragment() {
        if (_isMaxPrinterCountReached) {
            _pauseableHandler!!.resume()
            return
        }
        if (isTablet) {
            setIconState(R.id.menu_id_action_add_button, true)
        }
        val fragment = AddPrinterFragment()
        switchToFragment(fragment, FRAGMENT_TAG_ADD_PRINTER)
    }

    /**
     * @brief Displays the Printer Search Settings Screen.
     */
    private fun displaySearchPrinterFragment() {
        if (isTablet) {
            setIconState(R.id.menu_id_printer_search_settings_button, true)
        }
        val fragment = PrinterSearchSettingsFragment()
        switchToFragment(fragment, FRAGMENT_TAG_PRINTER_SEARCH_SETTINGS)
    }

    /**
     * @brief Displays the Printer Info Screen.
     *
     * @param printer Printer to be displayed in the PrinterInfo Screen
     */
    private fun displayPrinterInfoFragment(printer: Printer) {
        val fragment = PrinterInfoFragment()
        fragment.setPrinter(printer)
        switchToFragment(fragment, FRAGMENT_TAG_PRINTER_INFO)
    }

    /**
     * @brief Displays the Default Print Settings Screen.
     *
     * @param fragment Default Print Settings Fragment
     */
    private fun displayDefaultPrintSettings(fragment: PrintSettingsFragment) {
        switchToFragment(fragment, PrintPreviewFragment.FRAGMENT_TAG_PRINT_SETTINGS)
    }

    /**
     * @brief Switch to a fragment.
     *
     * @param fragment Fragment object
     * @param tag Fragment tag
     */
    private fun switchToFragment(fragment: BaseFragment, tag: String) {
        val fm = parentFragmentManager
        val ft = fm.beginTransaction()
        if (isTablet) {
            if (activity != null && activity is MainActivity) {
                val activity = activity as MainActivity?
                ft.replace(R.id.rightLayout, fragment, tag)
                ft.commit()
                activity!!.openDrawer(Gravity.RIGHT)
            }
        } else {
            ft.setCustomAnimations(
                R.animator.left_slide_in,
                R.animator.left_slide_out,
                R.animator.right_slide_in,
                R.animator.right_slide_out
            )
            ft.addToBackStack(null)
            ft.replace(R.id.mainLayout, fragment, tag)
            ft.commit()
        }
    }

    /**
     * @brief Determines if the maximum number of saved printers is reached.
     *
     * @retval true Maximum printer count is reached
     * @retval false Maximum printer count is not yet reached
     */
    private val _isMaxPrinterCountReached: Boolean
        get() {
            if (_printerManager!!.printerCount == AppConstants.CONST_MAX_PRINTER_COUNT) {
                val title = resources.getString(R.string.ids_lbl_printers)
                val errMsg: String = resources.getString(R.string.ids_err_msg_max_printer_count)
                val info = InfoDialogFragment.newInstance(
                    title,
                    errMsg,
                    resources.getString(R.string.ids_lbl_ok)
                )
                DialogUtils.displayDialog(requireActivity(), KEY_PRINTER_ERR_DIALOG, info)
                return true
            }
            return false
        }

    /**
     * @brief Updates the online status for the whole view.
     */
    private fun updateOnlineStatus() {
        val childCount: Int
        var position = 0
        if (isTablet && _printerTabletView != null) {
            childCount = _printerTabletView!!.childCount
        } else {
            position = _listView!!.firstVisiblePosition
            childCount = _listView!!.childCount
        }
        for (i in 0 until childCount) {
            var targetView: View? = null
            if (isTablet) {
                if (_printerTabletView != null && _printerTabletView!!.getChildAt(i) != null) {
                    targetView = _printerTabletView!!.getChildAt(i).findViewById(R.id.img_onOff)
                }
            } else {
                if (_listView != null && _listView!!.getChildAt(i) != null) {
                    targetView = _listView!!.getChildAt(i).findViewById(R.id.img_onOff)
                }
            }
            if (targetView != null &&  // RM#914 add safety checking for access to mPrinter array list
                _printer!!.size > i + position
            ) {
                _printerManager!!.updateOnlineStatus(
                    _printer!![i + position]!!.ipAddress,
                    targetView
                )
            }
        }
    }

    /**
     * @brief Displays empty message, hides printers view and stops updates of online status.
     */
    private fun showEmptyText() {
        _pauseableHandler!!.removeCallbacks(_updateOnlineStatus!!)
        _emptyPrintersText!!.visibility = View.VISIBLE
        if (isTablet && _printerTabletView != null) {
            _printerTabletView!!.visibility = View.GONE
        } else if (_listView != null) {
            _listView!!.visibility = View.GONE
        }
    }

    /**
     * @brief Displays printers view, hides empty message and starts updates of online status.
     */
    private fun showPrintersView() {
        _pauseableHandler!!.post(_updateOnlineStatus!!)
        _emptyPrintersText!!.visibility = View.GONE
        if (isTablet && _printerTabletView != null) {
            _printerTabletView!!.visibility = View.VISIBLE
        } else if (_listView != null) {
            _listView!!.visibility = View.VISIBLE
        }
    }

    /**
     * @brief Display dialog during failed database access.
     */
    private fun dialogErrCb() {
        val title = resources.getString(R.string.ids_lbl_printers)
        val errMsg = resources.getString(R.string.ids_err_msg_db_failure)
        val info: DialogFragment =
            InfoDialogFragment.newInstance(title, errMsg, resources.getString(R.string.ids_lbl_ok))
        DialogUtils.displayDialog(requireActivity(), KEY_PRINTER_ERR_DIALOG, info)
    }

    // ================================================================================
    // INTERFACE - View.OnClickListener
    // ================================================================================
    override fun onClick(v: View) {
        val id = v.id
        if (id == R.id.menu_id_action_search_button) {
            if (activity != null && activity is MainActivity) {
                val activity = activity as MainActivity?
                if (!activity!!.isDrawerOpen(Gravity.RIGHT)) {
                    _pauseableHandler!!.sendEmptyMessage(R.id.menu_id_action_search_button)
                } else {
                    activity.closeDrawers()
                }
            }
        } else if (id == R.id.menu_id_action_add_button) {
            if (activity != null && activity is MainActivity) {
                val activity = activity as MainActivity?
                if (!activity!!.isDrawerOpen(Gravity.RIGHT)) {
                    _pauseableHandler!!.sendEmptyMessage(R.id.menu_id_action_add_button)
                } else {
                    activity.closeDrawers()
                }
            }
        } else if (id == R.id.menu_id_printer_search_settings_button) {
            if (activity != null && activity is MainActivity) {
                val activity = activity as MainActivity?
                if (!activity!!.isDrawerOpen(Gravity.RIGHT)) {
                    _pauseableHandler!!.sendEmptyMessage(R.id.menu_id_printer_search_settings_button)
                } else {
                    activity.closeDrawers()
                }
            }
        } else if (id == R.id.menu_id_action_button) {
            if (activity != null && activity is MainActivity) {
                _pauseableHandler!!.sendEmptyMessage(R.id.menu_id_action_button)
            }
        }
    }

    // ================================================================================
    // INTERFACE - PrintersCallback
    // ================================================================================
    override fun onAddedNewPrinter(printer: Printer?, isOnline: Boolean) {
        val newMessage = Message.obtain(_pauseableHandler, MSG_ADD_NEW_PRINTER)
        newMessage.obj = printer
        newMessage.arg1 = if (isOnline) 1 else 0
        _pauseableHandler!!.sendMessage(newMessage)
    }

    // ================================================================================
    // INTERFACE - PrintersViewCallback/PrinterArrayAdapterInterface 
    // ================================================================================
    override fun onPrinterDeleteClicked(printer: Printer?) {
        val title = resources.getString(R.string.ids_lbl_printer)
        val errMsg = resources.getString(R.string.ids_info_msg_delete_jobs)
        val info: DialogFragment
        info = ConfirmDialogFragment.newInstance(
            title,
            errMsg,
            resources.getString(R.string.ids_lbl_ok),
            resources.getString(R.string.ids_lbl_cancel),
            KEY_PRINTERS_DIALOG
        )
        setResultListenerConfirmDialog(requireActivity().supportFragmentManager, this, KEY_PRINTERS_DIALOG)
        _deletePrinter = printer
        DialogUtils.displayDialog(requireActivity(), KEY_PRINTERS_DIALOG, info)
    }

    override fun onPrinterListClicked(printer: Printer?) {
        if (_pauseableHandler != null) {
            val msg = Message.obtain(_pauseableHandler, MSG_SUBMENU_BUTTON)
            msg.obj = printer
            _pauseableHandler!!.sendMessage(msg)
        }
    }

    // ================================================================================
    // INTERFACE - ConfirmDialogListener
    // ================================================================================


    override fun onConfirm() {
        if (isTablet) {
            val relayout = _deletePrinter!!.id == _printerManager!!.defaultPrinter
            if (_printerManager!!.removePrinter(_deletePrinter)) {
                _printerTabletView!!.confirmDeletePrinterView(relayout)
            } else {
                dialogErrCb()
            }
        } else {
            if (_printerManager!!.removePrinter(_deletePrinter)) {
                _printerAdapter!!.notifyDataSetChanged()
            } else {
                dialogErrCb()
            }
            (_listView as PrintersListView?)!!.resetDeleteView(false)
        }
        if (_printerManager!!.savedPrintersList.isEmpty()) {
            showEmptyText()
        }
    }

    override fun onCancel() {
        if (isTablet && _printerTabletView != null) {
            _printerTabletView!!.resetDeletePrinterView()
        } else {
            _deletePrinter = null
            (_printerAdapter as PrinterArrayAdapter?)!!.resetDeletePrinterView()
            (_listView as PrintersListView?)!!.resetDeleteView(true)
        }
    }

    // ================================================================================
    // INTERFACE - PauseableHandlerCallback
    // ================================================================================
    override fun storeMessage(message: Message?): Boolean {
        return message!!.what != R.id.menu_id_action_add_button && message.what != R.id.menu_id_action_search_button && message.what != R.id.menu_id_action_button && message.what != MSG_SUBMENU_BUTTON && message.what != MSG_PRINTSETTINGS_BUTTON
    }

    override fun processMessage(message: Message?) {
        when (val id = message!!.what) {
            MSG_POPULATE_PRINTERS_LIST -> {
                if (isTablet) {
                    _printerTabletView!!.restoreState(_printer, message.arg1, message.arg2)
                    _printerTabletView!!.setPauseableHandler(_pauseableHandler)
                } else {
                    _printerAdapter =
                        PrinterArrayAdapter(activity, R.layout.printers_container_item, _printer)
                    (_printerAdapter as PrinterArrayAdapter?)!!.setPrintersArrayAdapterInterface(this)
                    _listView!!.adapter = _printerAdapter
                    if (message.obj != null) {
                        (_listView as PrintersListView?)!!.onRestoreInstanceState(
                            message.obj as Parcelable,
                            message.arg1
                        )
                    }
                }
                return
            }
            MSG_ADD_NEW_PRINTER -> {
                val printer = message.obj as Printer
                if (isTablet) {
                    _printerTabletView!!.onAddedNewPrinter(printer, message.arg1 > 0)
                } else {
                    _printerAdapter!!.notifyDataSetChanged()
                }
                return
            }
            MSG_SUBMENU_BUTTON -> {
                _pauseableHandler!!.pause()
                displayPrinterInfoFragment(message.obj as Printer)
            }
            MSG_PRINTSETTINGS_BUTTON -> {
                _pauseableHandler!!.pause()
                val fragment = (message.obj as PrintSettingsFragment?)!!
                displayDefaultPrintSettings(fragment)
                _printerTabletView!!.setDefaultSettingSelected(message.arg1, true)
            }
            else -> when (id) {
                R.id.menu_id_action_search_button -> {
                    _pauseableHandler!!.pause()
                    displayPrinterSearchFragment()
                }
                R.id.menu_id_action_add_button -> {
                    _pauseableHandler!!.pause()
                    displayAddPrinterFragment()
                }
                R.id.menu_id_printer_search_settings_button -> {
                    _pauseableHandler!!.pause()
                    displaySearchPrinterFragment()
                }
                R.id.menu_id_action_button -> {
                    _pauseableHandler!!.pause()
                    val activity = activity as MainActivity?
                    activity!!.openDrawer(Gravity.LEFT)
                }
            }
        }
    }

    companion object {
        const val FRAGMENT_TAG_PRINTER_SEARCH = "fragment_printer_search"
        const val FRAGMENT_TAG_ADD_PRINTER = "fragment_add_printer"
        const val FRAGMENT_TAG_PRINTER_SEARCH_SETTINGS = "fragment_tag_printer_search_settings"
        const val FRAGMENT_TAG_PRINTER_INFO = "fragment_printer_info"
        const val KEY_PRINTER_ERR_DIALOG = "printer_err_dialog"
        const val MSG_ADD_NEW_PRINTER = 0x1
        const val MSG_SUBMENU_BUTTON = 0x2
        const val MSG_PRINTSETTINGS_BUTTON = 0x3
        const val KEY_PRINTERS_DIALOG = "printers_dialog"
        private const val MSG_POPULATE_PRINTERS_LIST = 0x0
    }

}