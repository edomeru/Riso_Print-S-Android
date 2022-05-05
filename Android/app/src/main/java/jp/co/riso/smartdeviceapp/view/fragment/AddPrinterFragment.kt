/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * AddPrinterFragment.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.smartdeviceapp.view.fragment

import android.os.Bundle
import android.os.Looper
import android.os.Message
import android.text.InputFilter
import android.text.InputType
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import jp.co.riso.android.dialog.ConfirmDialogFragment
import jp.co.riso.android.dialog.ConfirmDialogFragment.ConfirmDialogListener
import jp.co.riso.android.dialog.DialogUtils
import jp.co.riso.android.dialog.InfoDialogFragment
import jp.co.riso.android.os.pauseablehandler.PauseableHandler
import jp.co.riso.android.os.pauseablehandler.PauseableHandlerCallback
import jp.co.riso.android.text.IpAddressFilter
import jp.co.riso.android.util.AppUtils.getScreenDimensions
import jp.co.riso.android.util.AppUtils.hideSoftKeyboard
import jp.co.riso.smartdeviceapp.SmartDeviceApp
import jp.co.riso.smartdeviceapp.common.JniUtils.validateIpAddress
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager.Companion.getInstance
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager.PrinterSearchCallback
import jp.co.riso.smartdeviceapp.model.Printer
import jp.co.riso.smartdeviceapp.view.MainActivity
import jp.co.riso.smartdeviceapp.view.base.BaseFragment
import jp.co.riso.smartprint.R

/**
 * @class AddPrinterFragment
 *
 * @brief Fragment for Add Printer Screen.
 */
class AddPrinterFragment : BaseFragment(), PrinterSearchCallback, OnEditorActionListener,
    ConfirmDialogListener, PauseableHandlerCallback {
    private var mAddPrinterView: ViewHolder? = null
    private var mPrinterManager: PrinterManager? = null
    private var mAdded = false
    private var mPauseableHandler: PauseableHandler? = null

    override val viewLayout: Int
        get() = R.layout.fragment_addprinter

    override fun initializeFragment(savedInstanceState: Bundle?) {
        retainInstance = true
        mAdded = false
        mPrinterManager = getInstance(SmartDeviceApp.appContext!!)
        mPrinterManager!!.setPrinterSearchCallback(this)
        mAddPrinterView = ViewHolder()
        if (mPauseableHandler == null) {
            mPauseableHandler = PauseableHandler(Looper.myLooper(), this)
        }
    }

    override fun initializeView(view: View, savedInstanceState: Bundle?) {
        mAddPrinterView!!.mIpAddress = view.findViewById(R.id.inputIpAddress)
        mAddPrinterView!!.mSaveButton = view.findViewById(R.id.img_save_button)
        mAddPrinterView!!.mProgressBar = view.findViewById(R.id.actionbar_progressbar)
        mAddPrinterView!!.mIpAddress!!.setBackgroundColor(
            ContextCompat.getColor(
                requireActivity(),
                R.color.theme_light_1
            )
        )
        mAddPrinterView!!.mIpAddress!!.setOnEditorActionListener(this)
        mAddPrinterView!!.mSaveButton!!.setOnClickListener(this)
        mAddPrinterView!!.mIpAddress!!.setFilters(arrayOf<InputFilter>(IpAddressFilter()))
        if (mPrinterManager!!.isSearching) {
            setViewToDisable(mAddPrinterView)
        }

        // RM#911 when display size is changed, layout can change from tablet to phone
        // if layout is phone, also check if fragment is currently open in the right drawer
        // if it is, do not expand to fit the screen to prevent clipping
        if (!isTablet && !isOnRightDrawer) {
            val screenSize = getScreenDimensions(activity)
            val rootView = view.findViewById<View>(R.id.rootView) ?: return
            val params = rootView.layoutParams
            params.width = Math.min(screenSize!!.x, screenSize.y)
        }
    }

    override fun initializeCustomActionBar(view: View, savedInstanceState: Bundle?) {
        val textView = view.findViewById<TextView>(R.id.actionBarTitle)
        textView.setText(R.string.ids_lbl_add_printer)

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

    override fun onPause() {
        super.onPause()
        mPauseableHandler!!.pause()
    }

    override fun onResume() {
        super.onResume()
        mPauseableHandler!!.resume()
    }

    override fun onKeyUp(keyCode: Int): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_ENTER -> {
                if (mAddPrinterView!!.mIpAddress!!.isFocused) {
                    startManualSearch()
                    return true
                }
                super.onKeyUp(keyCode)
            }
            else -> super.onKeyUp(keyCode)
        }
    }
    // ================================================================================
    // Private Methods
    // ================================================================================
    /**
     * @brief Search for printer device
     *
     * @param ipAddress Printer IP Address
     */
    private fun findPrinter(ipAddress: String) {
        mPrinterManager!!.searchPrinter(ipAddress)
    }

    /**
     * @brief Display success dialog during successful printer search
     */
    private fun dialogCb() {
        if (isTablet && activity != null && activity is MainActivity) {
            val activity = activity as MainActivity?
            if (!activity!!.isDrawerOpen(Gravity.RIGHT)) {
                return
            }
        } else if (isTablet) {
            return
        }
        if (isAdded) {
            val title = resources.getString(R.string.ids_lbl_add_printer)
            val msg = resources.getString(R.string.ids_info_msg_printer_add_successful)
            val info = ConfirmDialogFragment.newInstance(
                title,
                msg,
                resources.getString(R.string.ids_lbl_ok),
                null,
                KEY_ADD_PRINTER_DIALOG
            )
            if (activity != null && activity is MainActivity) {
                setResultListenerConfirmDialog(
                    requireActivity().supportFragmentManager,
                    this,
                    KEY_ADD_PRINTER_DIALOG)
                DialogUtils.displayDialog(activity as MainActivity, KEY_ADD_PRINTER_DIALOG, info)
            }
        }
    }

    /**
     * @brief Display error dialog during failed printer search
     *
     * @param err Error code
     */
    private fun dialogErrCb(err: Int) {
        if (isTablet) {
            if (activity != null && activity is MainActivity) {
                val activity = activity as MainActivity?
                if (!activity!!.isDrawerOpen(Gravity.RIGHT)) {
                    return
                }
            } else if (activity == null) {
                return
            }
        }
        val title = resources.getString(R.string.ids_lbl_add_printer)
        var errMsg: String? = null
        val info: DialogFragment
        when (err) {
            ERR_INVALID_IP_ADDRESS -> errMsg =
                resources.getString(R.string.ids_err_msg_invalid_ip_address)
            ERR_CAN_NOT_ADD_PRINTER -> errMsg =
                resources.getString(R.string.ids_err_msg_cannot_add_printer)
            ERR_PRINTER_ADDED_WARNING -> errMsg =
                resources.getString(R.string.ids_info_msg_warning_cannot_find_printer)
            ERR_DB_FAILURE -> errMsg = resources.getString(R.string.ids_err_msg_db_failure)
        }
        if (err == ERR_PRINTER_ADDED_WARNING) {
            info = ConfirmDialogFragment.newInstance(
                title,
                errMsg,
                resources.getString(R.string.ids_lbl_ok),
                null,
                KEY_ADD_PRINTER_DIALOG
            )
            setResultListenerConfirmDialog(
                requireActivity().supportFragmentManager,
                this,
                KEY_ADD_PRINTER_DIALOG)
        } else {
            info = InfoDialogFragment.newInstance(
                title,
                errMsg,
                resources.getString(R.string.ids_lbl_ok)
            )
        }
        DialogUtils.displayDialog(requireActivity(), KEY_ADD_PRINTER_DIALOG, info)
    }

    /**
     * @brief Close the Add Printer screen
     */
    private fun closeScreen() {
        if (isTablet) {
            if (activity != null && activity is MainActivity) {
                val activity = activity as MainActivity?
                activity!!.runOnUiThread { activity.closeDrawers() }
            }
        } else if (isAdded) {
            val fm = parentFragmentManager
            val ft = fm.beginTransaction()
            if (fm.backStackEntryCount > 0) {
                fm.popBackStack()
                ft.commit()
                fm.executePendingTransactions()
            }
        }

        // Check if Add Printer screen is not yet closed
        if (activity != null) {
            hideSoftKeyboard(requireActivity())
        }
    }

    /**
     * @brief Set the Add Printer Screen to disabled mode to prevent changes from user input
     *
     * @param viewHolder Add Printer Screen view holder
     */
    private fun setViewToDisable(viewHolder: ViewHolder?) {
        if (viewHolder == null) {
            return
        }
        viewHolder.mSaveButton!!.visibility = View.GONE
        viewHolder.mProgressBar!!.visibility = View.VISIBLE
        viewHolder.mIpAddress!!.setTextColor(
            ContextCompat.getColor(
                requireActivity(),
                R.color.theme_light_4
            )
        )

        // #RM908 for chromeOS, setFocusable(false) somehow causes virtual keyboard to reappear after printer is added
        // use alternative way to disable IP address field which does cause the same problem
        if (isChromeBook) {
            viewHolder.mIpAddress!!.inputType = InputType.TYPE_NULL
        } else {
            viewHolder.mIpAddress!!.isFocusable = false
        }
    }

    /**
     * @brief Set the Add Printer Screen to normal
     *
     * @param viewHolder Add Printer Screen view holder
     */
    private fun setViewToNormal(viewHolder: ViewHolder?) {
        if (viewHolder == null) {
            return
        }
        viewHolder.mSaveButton!!.visibility = View.VISIBLE
        viewHolder.mProgressBar!!.visibility = View.GONE
        viewHolder.mIpAddress!!.setTextColor(
            ContextCompat.getColor(
                requireActivity(),
                R.color.theme_dark_1
            )
        )
        viewHolder.mIpAddress!!.isFocusableInTouchMode = true
    }

    /**
     * @brief Start manual printer search
     */
    private fun startManualSearch() {
        var ipAddress: String?
        if (mAddPrinterView!!.mIpAddress!!.text != null) {
            ipAddress = mAddPrinterView?.mIpAddress?.text.toString()
            ipAddress = validateIpAddress(ipAddress)
        } else {
            ipAddress = null
        }
        if (ipAddress == null || ipAddress.contentEquals(BROADCAST_ADDRESS)) {
            dialogErrCb(ERR_INVALID_IP_ADDRESS)
            return
        }
        mAddPrinterView!!.mIpAddress!!.setText(ipAddress)
        if (mPrinterManager!!.isExists(ipAddress)) {
            dialogErrCb(ERR_CAN_NOT_ADD_PRINTER)
            return
        }
        if (!mPrinterManager!!.isSearching) {
            setViewToDisable(mAddPrinterView)
            findPrinter(mAddPrinterView!!.mIpAddress!!.text.toString())
        }
        hideSoftKeyboard(requireActivity())
    }

    // ================================================================================
    // INTERFACE - View.OnClickListener
    // ================================================================================
    override fun onClick(v: View) {
        super.onClick(v)
        val id = v.id
        if (id == R.id.menu_id_back_button) {
            closeScreen()
        } else if (id == R.id.img_save_button) {
            startManualSearch()
        }
    }

    // ================================================================================
    // INTERFACE - OnPrinterSearch
    // ================================================================================
    override fun onPrinterAdd(printer: Printer?) {
        if (mPrinterManager!!.isCancelled) {
            return
        }
        val newMessage: Message
        if (mPrinterManager!!.isExists(printer)) {
            newMessage = Message.obtain(mPauseableHandler, MSG_ERROR)
            newMessage.arg1 = ERR_INVALID_IP_ADDRESS
        } else if (mPrinterManager!!.savePrinterToDB(printer, true)) {
            mAdded = true
            newMessage = Message.obtain(mPauseableHandler, MSG_ADD_SUCCESS)
            newMessage.obj = printer
        } else {
            newMessage = Message.obtain(mPauseableHandler, MSG_ERROR)
            newMessage.arg1 = ERR_DB_FAILURE
        }
        mPauseableHandler!!.sendMessage(newMessage)
    }

    override fun onSearchEnd() {
        if (mPrinterManager!!.isCancelled) {
            return
        }
        val activity = activity as MainActivity?
        activity!!.runOnUiThread { setViewToNormal(mAddPrinterView) }
        if (!mAdded) {
            val newWarningMsg = Message.obtain(mPauseableHandler, MSG_ERROR)
            newWarningMsg.arg1 = ERR_PRINTER_ADDED_WARNING
            mPauseableHandler!!.sendMessage(newWarningMsg)
        }
    }

    // ================================================================================
    // INTERFACE - OnEditorActionListener
    // ================================================================================
    override fun onEditorAction(v: TextView, actionId: Int, event: KeyEvent?): Boolean {
        if (actionId and EditorInfo.IME_MASK_ACTION == EditorInfo.IME_ACTION_DONE) {
            startManualSearch()
            return true
        }
        return false
    }

    // ================================================================================
    // INTERFACE - ConfirmDialogListener
    // ================================================================================
    override fun onConfirm() {
        closeScreen()
    }

    override fun onCancel() {
        closeScreen()
    }
    // ================================================================================
    // INTERNAL Classes
    // ================================================================================
    /**
     * @class ViewHolder
     *
     * @brief Add Printer Screen view holder
     */
    class ViewHolder {
        var mIpAddress: EditText? = null
        var mProgressBar: View? = null
        var mSaveButton: View? = null
    }

    // ================================================================================
    // INTERFACE - PauseableHandlerCallback
    // ================================================================================
    override fun storeMessage(message: Message?): Boolean {
        return message!!.what == MSG_ERROR || message.what == MSG_ADD_SUCCESS
    }

    override fun processMessage(message: Message?) {
        if (message != null) {
            when (message.what) {
                MSG_ERROR -> dialogErrCb(message.arg1)
                MSG_ADD_SUCCESS -> dialogCb()
            }
        }
    }

    companion object {
        private const val KEY_ADD_PRINTER_DIALOG = "add_printer_dialog"
        private const val ERR_INVALID_IP_ADDRESS = -1
        private const val ERR_CAN_NOT_ADD_PRINTER = -2
        private const val ERR_PRINTER_ADDED_WARNING = -3
        private const val ERR_DB_FAILURE = -4
        private const val MSG_ERROR = 0
        private const val MSG_ADD_SUCCESS = 1
        private const val BROADCAST_ADDRESS = "255.255.255.255"
    }
}