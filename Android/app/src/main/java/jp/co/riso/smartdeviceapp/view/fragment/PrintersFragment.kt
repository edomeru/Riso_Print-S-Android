/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * PrintersFragment.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.smartdeviceapp.view.fragment

import android.content.res.Configuration
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager.Companion.getInstance
//import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager.savedPrintersList
//import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager.setPrintersCallback
//import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager.isSearching
//import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager.cancelPrinterSearch
//import jp.co.riso.android.os.pauseablehandler.PauseableHandler.resume
//import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager.printerCount
//import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager.updateOnlineStatus
//import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager.defaultPrinter
//import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager.removePrinter
//import jp.co.riso.android.os.pauseablehandler.PauseableHandler.pause
import jp.co.riso.smartdeviceapp.view.base.BaseFragment
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager.PrintersCallback
import jp.co.riso.android.os.pauseablehandler.PauseableHandlerCallback
import jp.co.riso.smartdeviceapp.view.printers.PrintersScreenTabletView.PrintersViewCallback
import jp.co.riso.android.dialog.ConfirmDialogFragment.ConfirmDialogListener
import jp.co.riso.smartdeviceapp.view.printers.PrinterArrayAdapter.PrinterArrayAdapterInterface
import jp.co.riso.android.os.pauseablehandler.PauseableHandler
import android.widget.ArrayAdapter
import jp.co.riso.smartdeviceapp.view.printers.PrintersScreenTabletView
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager
import android.os.Parcelable
import android.widget.TextView
import jp.co.riso.smartprint.R
import android.os.Bundle
import jp.co.riso.smartdeviceapp.SmartDeviceApp
import android.os.Looper
import android.os.Message
import jp.co.riso.smartdeviceapp.AppConstants
import jp.co.riso.smartdeviceapp.view.printers.PrintersListView
import jp.co.riso.smartdeviceapp.view.MainActivity
import android.view.Gravity
import android.view.View
import android.widget.ListView
import androidx.fragment.app.DialogFragment
import jp.co.riso.android.dialog.InfoDialogFragment
import jp.co.riso.android.dialog.DialogUtils
import jp.co.riso.android.dialog.ConfirmDialogFragment
import jp.co.riso.smartdeviceapp.model.Printer
import jp.co.riso.smartdeviceapp.view.printers.PrinterArrayAdapter
import java.util.ArrayList

/**
 * @class PrintersFragment
 *
 * @brief Fragment for Printers Screen
 */
class PrintersFragment : BaseFragment(), PrintersCallback, PauseableHandlerCallback,
    PrintersViewCallback, ConfirmDialogListener, PrinterArrayAdapterInterface {
    private var mPauseableHandler: PauseableHandler? = null
    private var mDeletePrinter: Printer? = null

    // ListView parameters
    private var mListView: ListView? = null
    private var mPrinter: ArrayList<Printer?>? = null
    private var mPrinterAdapter: ArrayAdapter<Printer?>? = null

    // Tablet parameters
    private var mPrinterTabletView: PrintersScreenTabletView? = null

    // Printer Manager
    private var mPrinterManager: PrinterManager? = null
    private var mDeleteItem = PrinterManager.EMPTY_ID
    private var mScrollState: Parcelable? = null
    private var mSettingItem = PrinterManager.EMPTY_ID
    private var mUpdateOnlineStatus: Runnable? = null
    private var mEmptyPrintersText: TextView? = null

    override val viewLayout: Int
        get() = R.layout.fragment_printers

    override fun initializeFragment(savedInstanceState: Bundle?) {
        retainInstance = true
        mPrinterManager = getInstance(SmartDeviceApp.appContext!!)
        if (mPauseableHandler == null) {
            mPauseableHandler = PauseableHandler(Looper.myLooper(), this)
        }
        mUpdateOnlineStatus = object : Runnable {
            override fun run() {
                /* Update online status*/
                updateOnlineStatus()
                /* Run every 5 seconds */mPauseableHandler!!.postDelayed(
                    this,
                    AppConstants.CONST_UPDATE_INTERVAL.toLong()
                )
            }
        }
    }

    override fun initializeView(view: View, savedInstanceState: Bundle?) {
        val newMessage = Message.obtain(mPauseableHandler, MSG_POPULATE_PRINTERS_LIST)
        if (!isTablet) {
            newMessage.obj = mScrollState
        }
        newMessage.arg1 = mDeleteItem
        newMessage.arg2 = mSettingItem
        if (mPrinter == null) {
            mPrinter = mPrinterManager!!.savedPrintersList as ArrayList<Printer?>?
        }
        mEmptyPrintersText = view.findViewById(R.id.emptyPrintersText)
        if (isTablet) {
            mPrinterTabletView = view.findViewById(R.id.printerParentView)
            mPrinterTabletView?.setPrintersViewCallback(this)
        } else {
            mListView = view.findViewById(R.id.printer_list)
        }
        mPrinterManager!!.setPrintersCallback(this)
        mPauseableHandler!!.sendMessage(newMessage)
        if (isTablet) {
            mSettingItem = PrinterManager.EMPTY_ID
        } else {
            mScrollState = null
        }
        mDeleteItem = PrinterManager.EMPTY_ID

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

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)
        if (isTablet && mPrinterTabletView != null) {
            mDeleteItem = mPrinterTabletView!!.deleteItemPosition
            mSettingItem = mPrinterTabletView!!.defaultSettingSelected
        } else {
            if (mListView != null) {
                mScrollState = mListView!!.onSaveInstanceState()
                mDeleteItem = (mListView as PrintersListView).deleteItemPosition
            } else {
                mDeleteItem = PrinterManager.EMPTY_ID
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val activity = activity as MainActivity?
        if (!activity!!.isDrawerOpen(Gravity.RIGHT) && mPrinterManager!!.isSearching) {
            mPrinterManager!!.cancelPrinterSearch()
        }
        if (mUpdateOnlineStatus != null && mPauseableHandler != null) {
            if (mPrinterManager!!.savedPrintersList!!.isEmpty()) {
                showEmptyText()
            } else {
                showPrintersView()
            }
            mPauseableHandler!!.resume()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (mPauseableHandler != null) {
            mPauseableHandler!!.resume()
        }
        if (isTablet && mPrinterTabletView != null) {
            mPrinterTabletView!!.requestLayout()
        }
    }

    override fun onPause() {
        super.onPause()
        if (mUpdateOnlineStatus != null && mPauseableHandler != null) {
            mPauseableHandler!!.removeCallbacks(mUpdateOnlineStatus!!)
        }
    }

    /**
     * @brief Sets the selected state of a Printer's default setting in tablet view.
     *
     * @param state Set Printer to selected state
     */
    fun setDefaultSettingSelected(state: Boolean) {
        if (mPrinterTabletView != null) {
            mPrinterTabletView!!.setDefaultSettingSelected(PrinterManager.EMPTY_ID, state)
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
        if (isMaxPrinterCountReached) {
            mPauseableHandler!!.resume()
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
        if (isMaxPrinterCountReached) {
            mPauseableHandler!!.resume()
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
    private val isMaxPrinterCountReached: Boolean
        private get() {
            if (mPrinterManager!!.printerCount == AppConstants.CONST_MAX_PRINTER_COUNT) {
                val title = resources.getString(R.string.ids_lbl_printers)
                val errMsg: String
                errMsg = resources.getString(R.string.ids_err_msg_max_printer_count)
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
        if (isTablet && mPrinterTabletView != null) {
            childCount = mPrinterTabletView!!.childCount
        } else {
            position = mListView!!.firstVisiblePosition
            childCount = mListView!!.childCount
        }
        for (i in 0 until childCount) {
            var targetView: View? = null
            if (isTablet) {
                if (mPrinterTabletView != null && mPrinterTabletView!!.getChildAt(i) != null) {
                    targetView = mPrinterTabletView!!.getChildAt(i).findViewById(R.id.img_onOff)
                }
            } else {
                if (mListView != null && mListView!!.getChildAt(i) != null) {
                    targetView = mListView!!.getChildAt(i).findViewById(R.id.img_onOff)
                }
            }
            if (targetView != null &&  // RM#914 add safety checking for access to mPrinter array list
                mPrinter!!.size > i + position
            ) {
                mPrinterManager!!.updateOnlineStatus(
                    mPrinter!![i + position]!!.ipAddress,
                    targetView
                )
            }
        }
    }

    /**
     * @brief Displays empty message, hides printers view and stops updates of online status.
     */
    private fun showEmptyText() {
        mPauseableHandler!!.removeCallbacks(mUpdateOnlineStatus!!)
        mEmptyPrintersText!!.visibility = View.VISIBLE
        if (isTablet && mPrinterTabletView != null) {
            mPrinterTabletView!!.visibility = View.GONE
        } else if (mListView != null) {
            mListView!!.visibility = View.GONE
        }
    }

    /**
     * @brief Displays printers view, hides empty message and starts updates of online status.
     */
    private fun showPrintersView() {
        mPauseableHandler!!.post(mUpdateOnlineStatus!!)
        mEmptyPrintersText!!.visibility = View.GONE
        if (isTablet && mPrinterTabletView != null) {
            mPrinterTabletView!!.visibility = View.VISIBLE
        } else if (mListView != null) {
            mListView!!.visibility = View.VISIBLE
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
                    mPauseableHandler!!.sendEmptyMessage(R.id.menu_id_action_search_button)
                } else {
                    activity.closeDrawers()
                }
            }
        } else if (id == R.id.menu_id_action_add_button) {
            if (activity != null && activity is MainActivity) {
                val activity = activity as MainActivity?
                if (!activity!!.isDrawerOpen(Gravity.RIGHT)) {
                    mPauseableHandler!!.sendEmptyMessage(R.id.menu_id_action_add_button)
                } else {
                    activity.closeDrawers()
                }
            }
        } else if (id == R.id.menu_id_printer_search_settings_button) {
            if (activity != null && activity is MainActivity) {
                val activity = activity as MainActivity?
                if (!activity!!.isDrawerOpen(Gravity.RIGHT)) {
                    mPauseableHandler!!.sendEmptyMessage(R.id.menu_id_printer_search_settings_button)
                } else {
                    activity.closeDrawers()
                }
            }
        } else if (id == R.id.menu_id_action_button) {
            if (activity != null && activity is MainActivity) {
                mPauseableHandler!!.sendEmptyMessage(R.id.menu_id_action_button)
            }
        }
    }

    // ================================================================================
    // INTERFACE - PrintersCallback
    // ================================================================================
    override fun onAddedNewPrinter(printer: Printer?, isOnline: Boolean) {
        val newMessage = Message.obtain(mPauseableHandler, MSG_ADD_NEW_PRINTER)
        newMessage.obj = printer
        newMessage.arg1 = if (isOnline) 1 else 0
        mPauseableHandler!!.sendMessage(newMessage)
    }

    // ================================================================================
    // INTERFACE - PrintersViewCallback/PrinterArrayAdapterInterface 
    // ================================================================================
    override fun onPrinterDeleteClicked(printer: Printer?) {
        val title = resources.getString(R.string.ids_lbl_printer)
        val errMsg = resources.getString(R.string.ids_info_msg_delete_jobs)
        val info: DialogFragment
        info = ConfirmDialogFragment.newInstance(
            title, errMsg, resources.getString(R.string.ids_lbl_ok),
            resources.getString(R.string.ids_lbl_cancel)
        )
        info.setTargetFragment(this, 0)
        mDeletePrinter = printer
        DialogUtils.displayDialog(requireActivity(), KEY_PRINTERS_DIALOG, info)
    }

    override fun onPrinterListClicked(printer: Printer?) {
        if (mPauseableHandler != null) {
            val msg = Message.obtain(mPauseableHandler, MSG_SUBMENU_BUTTON)
            msg.obj = printer
            mPauseableHandler!!.sendMessage(msg)
        }
    }

    // ================================================================================
    // INTERFACE - ConfirmDialogListener
    // ================================================================================
    override fun onConfirm() {
        if (isTablet) {
            val relayout = mDeletePrinter!!.id == mPrinterManager!!.defaultPrinter
            if (mPrinterManager!!.removePrinter(mDeletePrinter)) {
                mPrinterTabletView!!.confirmDeletePrinterView(relayout)
            } else {
                dialogErrCb()
            }
        } else {
            if (mPrinterManager!!.removePrinter(mDeletePrinter)) {
                mPrinterAdapter!!.notifyDataSetChanged()
            } else {
                dialogErrCb()
            }
            (mListView as PrintersListView?)!!.resetDeleteView(false)
        }
        if (mPrinterManager!!.savedPrintersList!!.isEmpty()) {
            showEmptyText()
        }
    }

    override fun onCancel() {
        if (isTablet && mPrinterTabletView != null) {
            mPrinterTabletView!!.resetDeletePrinterView()
        } else {
            mDeletePrinter = null
            (mPrinterAdapter as PrinterArrayAdapter?)!!.resetDeletePrinterView()
            (mListView as PrintersListView?)!!.resetDeleteView(true)
        }
    }

    // ================================================================================
    // INTERFACE - PauseableHandlerCallback
    // ================================================================================
    override fun storeMessage(message: Message?): Boolean {
        return message!!.what != R.id.menu_id_action_add_button && message.what != R.id.menu_id_action_search_button && message.what != R.id.menu_id_action_button && message.what != MSG_SUBMENU_BUTTON && message.what != MSG_PRINTSETTINGS_BUTTON
    }

    override fun processMessage(msg: Message?) {
        val id = msg!!.what
        when (id) {
            MSG_POPULATE_PRINTERS_LIST -> {
                if (isTablet) {
                    mPrinterTabletView!!.restoreState(mPrinter, msg.arg1, msg.arg2)
                    mPrinterTabletView!!.setPauseableHandler(mPauseableHandler)
                } else {
                    mPrinterAdapter =
                        PrinterArrayAdapter(activity, R.layout.printers_container_item, mPrinter)
                    (mPrinterAdapter as PrinterArrayAdapter).setPrintersArrayAdapterInterface(this)
                    mListView!!.adapter = mPrinterAdapter
                    if (msg.obj != null) {
                        (mListView as PrintersListView?)!!.onRestoreInstanceState(
                            msg.obj as Parcelable,
                            msg.arg1
                        )
                    }
                }
                return
            }
            MSG_ADD_NEW_PRINTER -> {
                val printer = msg.obj as Printer
                if (isTablet) {
                    mPrinterTabletView!!.onAddedNewPrinter(printer, msg.arg1 > 0)
                } else {
                    mPrinterAdapter!!.notifyDataSetChanged()
                }
                return
            }
            MSG_SUBMENU_BUTTON -> {
                mPauseableHandler!!.pause()
                displayPrinterInfoFragment(msg.obj as Printer)
            }
            MSG_PRINTSETTINGS_BUTTON -> {
                mPauseableHandler!!.pause()
                val fragment = msg.obj as PrintSettingsFragment
                displayDefaultPrintSettings(fragment)
                mPrinterTabletView!!.setDefaultSettingSelected(msg.arg1, true)
            }
            else -> if (id == R.id.menu_id_action_search_button) {
                mPauseableHandler!!.pause()
                displayPrinterSearchFragment()
            } else if (id == R.id.menu_id_action_add_button) {
                mPauseableHandler!!.pause()
                displayAddPrinterFragment()
            } else if (id == R.id.menu_id_printer_search_settings_button) {
                mPauseableHandler!!.pause()
                displaySearchPrinterFragment()
            } else if (id == R.id.menu_id_action_button) {
                mPauseableHandler!!.pause()
                val activity = activity as MainActivity?
                activity!!.openDrawer(Gravity.LEFT)
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
        private const val KEY_PRINTERS_DIALOG = "printers_dialog"
        private const val MSG_POPULATE_PRINTERS_LIST = 0x0
    }

}