/*
 * Copyright (c) 2022 RISO, Inc. All rights reserved.
 *
 * PrinterSearchFragment.kt
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.smartdeviceapp.view.fragment

import android.animation.ValueAnimator
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.os.Looper
import android.os.Message
import android.os.Parcelable
import android.view.View
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import eu.erikw.PullToRefreshListView
import jp.co.riso.android.dialog.ConfirmDialogFragment
import jp.co.riso.android.dialog.ConfirmDialogFragment.ConfirmDialogListener
import jp.co.riso.android.dialog.DialogUtils
import jp.co.riso.android.dialog.InfoDialogFragment
import jp.co.riso.android.os.pauseablehandler.PauseableHandler
import jp.co.riso.android.os.pauseablehandler.PauseableHandlerCallback
import jp.co.riso.android.util.NetUtils
import jp.co.riso.smartdeviceapp.SmartDeviceApp
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager.Companion.getInstance
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager.PrinterSearchCallback
import jp.co.riso.smartdeviceapp.model.Printer
import jp.co.riso.smartdeviceapp.view.MainActivity
import jp.co.riso.smartdeviceapp.view.base.BaseFragment
import jp.co.riso.smartdeviceapp.view.printers.PrinterSearchAdapter
import jp.co.riso.smartdeviceapp.view.printers.PrinterSearchAdapter.PrinterSearchAdapterInterface
import jp.co.riso.smartprint.R

/**
 * @class PrinterSearchFragment
 *
 * @brief Fragment for Printer Search Screen
 */
class PrinterSearchFragment : BaseFragment(), PullToRefreshListView.OnRefreshListener,
    PrinterSearchCallback, PrinterSearchAdapterInterface, ConfirmDialogListener,
    PauseableHandlerCallback {
    // ListView parameters
    private var _listView: PullToRefreshListView? = null
    private var _printer: ArrayList<Printer?>? = null
    private var _printerSearchAdapter: PrinterSearchAdapter? = null
    private var _printerManager: PrinterManager? = null
    private var _pauseableHandler: PauseableHandler? = null
    private var _emptySearchText: TextView? = null
    private var _noNetwork = false

    override val viewLayout: Int
        get() = R.layout.fragment_printersearch

    private inline fun <reified T : Parcelable> Bundle.parcelable(key: String): T? = when {
        SDK_INT >= Build.VERSION_CODES.TIRAMISU -> getParcelable(key, T::class.java)
        else -> @Suppress("DEPRECATION") getParcelable(key) as? T
    }

    private inline fun <reified T : Parcelable> Bundle.parcelableArrayList(key: String): ArrayList<T>? = when {
        SDK_INT >= Build.VERSION_CODES.TIRAMISU -> getParcelableArrayList(key, T::class.java)
        else -> @Suppress("DEPRECATION") getParcelableArrayList(key)
    }

    override fun initializeFragment(savedInstanceState: Bundle?) {
        _printer = if (savedInstanceState != null) {
            savedInstanceState.parcelable(KEY_SEARCHED_PRINTER_LIST)
        } else {
            ArrayList()
        }
        if (_pauseableHandler == null) {
            _pauseableHandler = PauseableHandler(Looper.myLooper(), this)
        }
        _printerSearchAdapter =
            PrinterSearchAdapter(activity, R.layout.printersearch_container_item, _printer)
        _printerSearchAdapter!!.setSearchAdapterInterface(this)
        _printerManager = getInstance(SmartDeviceApp.appContext!!)
        _printerManager!!.setPrinterSearchCallback(this)
    }

    override fun initializeView(view: View, savedInstanceState: Bundle?) {
        _listView = view.findViewById(R.id.printer_list)
        _listView!!.setBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.theme_light_3))
        _listView!!.adapter = _printerSearchAdapter
        _listView!!.setOnRefreshListener(this)
        _emptySearchText = view.findViewById(R.id.emptySearchText)
        val progressLayoutParams =
            _listView!!.findViewById<View>(R.id.ptr_id_spinner)!!.layoutParams as RelativeLayout.LayoutParams?
        progressLayoutParams!!.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE)
        val arrowLayoutParams =
            _listView!!.findViewById<View>(R.id.ptr_id_image)!!.layoutParams as RelativeLayout.LayoutParams?
        arrowLayoutParams!!.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE)
    }

    override fun initializeCustomActionBar(view: View, savedInstanceState: Bundle?) {
        val textView = view.findViewById<TextView>(R.id.actionBarTitle)
        textView.setText(R.string.ids_lbl_search_printers)
        if (isTablet) {
            val leftViewPadding = resources.getDimension(R.dimen.printers_subview_margin)
                .toInt()
            val leftTextPadding = resources.getDimension(R.dimen.home_title_padding)
                .toInt()
            view.setPadding(leftViewPadding, 0, 0, 0)
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (_printerManager!!.isSearching) {
            updateRefreshBar()
        }
        if (savedInstanceState == null) {
            onRefresh()
            updateRefreshBar()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelableArrayList(KEY_SEARCHED_PRINTER_LIST, _printer)
        super.onSaveInstanceState(outState)
    }

    override fun onPause() {
        super.onPause()
        _pauseableHandler!!.pause()
    }

    override fun onResume() {
        super.onResume()
        _pauseableHandler!!.resume()
    }
    // ================================================================================
    // Private Methods
    // ================================================================================
    /**
     * @brief Updates the status of the refresh bar.
     */
    private fun updateRefreshBar() {
        val newMessage = Message.obtain(_pauseableHandler, MSG_UPDATE_REFRESH_BAR)
        _pauseableHandler!!.sendMessage(newMessage)
    }

    /**
     * @brief Display error dialog during failed printer search
     */
    private fun dialogErrCb() {
        val title = resources.getString(R.string.ids_lbl_search_printers)
        val errMsg: String = resources.getString(R.string.ids_err_msg_network_error)
        val info: DialogFragment =
            InfoDialogFragment.newInstance(title, errMsg, resources.getString(R.string.ids_lbl_ok))
        DialogUtils.displayDialog(requireActivity(), KEY_PRINTER_ERR_DIALOG, info)
    }

    /**
     * @brief Closes the PrinterSearch Screen.
     */
    private fun closeScreen() {
        if (isTablet) {
            val activity = activity as MainActivity?
            activity!!.closeDrawers()
        } else {
            val fm = parentFragmentManager
            val ft = fm.beginTransaction()
            if (fm.backStackEntryCount > 0) {
                fm.popBackStack()
                ft.commit()
            }
        }
    }

    // ================================================================================
    // INTERFACE - onRefresh()
    // ================================================================================
    override fun onRefresh() {
        _printer!!.clear()
        _emptySearchText!!.visibility = View.GONE
        _noNetwork = false
        if (!NetUtils.isWifiAvailable) {
            _noNetwork = true
            dialogErrCb()
            updateRefreshBar()
            return
        }
        _printerManager!!.startPrinterSearch()
    }

    override fun onHeaderAdjusted(margin: Int) {
        if (_emptySearchText != null) {
            val params = _emptySearchText!!.layoutParams as FrameLayout.LayoutParams?
            params!!.topMargin = margin
            _emptySearchText!!.layoutParams = params
        }
    }

    override fun onBounceBackHeader(duration: Int) {
        // http://stackoverflow.com/questions/13881419/android-change-left-margin-using-animation
        val params = _emptySearchText!!.layoutParams as FrameLayout.LayoutParams?
        val animation = ValueAnimator.ofInt(params!!.topMargin, 0)
        animation.addUpdateListener { valueAnimator ->
            params.topMargin = (valueAnimator.animatedValue as Int?)!!
            _emptySearchText!!.requestLayout()
        }
        animation.duration = duration.toLong()
        animation.start()
    }

    // ================================================================================
    // INTERFACE - View.OnClickListener
    // ================================================================================
    override fun onClick(v: View) {
        _printerManager!!.cancelPrinterSearch()
        closeScreen()
    }

    // ================================================================================
    // INTERFACE - OnPrinterSearchCallback
    // ================================================================================
    override fun onPrinterAdd(printer: Printer?) {
        if (activity == null) {
            return
        }
        requireActivity().runOnUiThread(Runnable {
            if (!_printerManager!!.isSearching) {
                return@Runnable
            }
            for (i in _printer!!.indices) {
                if (_printer!![i]!!.ipAddress == printer!!.ipAddress) {
                    _printer!![i] = printer
                    return@Runnable
                }
            }
            _printer!!.add(printer)
            _printerSearchAdapter!!.notifyDataSetChanged()
        })
    }

    override fun onSearchEnd() {
        updateRefreshBar()
    }

    // ================================================================================
    // INTERFACE - PrinterSearchAdapterInterface
    // ================================================================================
    override fun onAddPrinter(printer: Printer?): Int {
        var ret = 0
        if (printer == null) {
            return -1
        }
        val info: DialogFragment
        val title = resources.getString(R.string.ids_lbl_search_printers)
        val msg: String
        if (!_printerManager!!.savePrinterToDB(printer, true)) {
            ret = -1
            msg = resources.getString(R.string.ids_err_msg_db_failure)
            info = InfoDialogFragment.newInstance(title, msg, resources.getString(R.string.ids_lbl_ok))
        } else {
            msg = resources.getString(R.string.ids_info_msg_printer_add_successful)
            info = ConfirmDialogFragment.newInstance(
                title,
                msg,
                resources.getString(R.string.ids_lbl_ok),
                null,
                KEY_SEARCHED_PRINTER_DIALOG
            )
            setResultListenerConfirmDialog(
                requireActivity().supportFragmentManager,
                this,
                KEY_SEARCHED_PRINTER_DIALOG
            )
        }
        DialogUtils.displayDialog(requireActivity(), KEY_SEARCHED_PRINTER_DIALOG, info)
        return ret
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
    // INTERFACE - PauseableHandlerCallback
    // ================================================================================
    override fun storeMessage(message: Message?): Boolean {
        return message!!.what == MSG_UPDATE_REFRESH_BAR
    }

    override fun processMessage(message: Message?) {
        when (message!!.what) {
            MSG_UPDATE_REFRESH_BAR -> if (_printerManager!!.isSearching) {
                _listView!!.setRefreshing()
            } else {
                _listView!!.onRefreshComplete()
                if (_printer!!.isEmpty() && !_noNetwork) {
                    _emptySearchText!!.visibility = View.VISIBLE
                }
            }
        }
    }

    companion object {
        const val KEY_PRINTER_ERR_DIALOG = "printer_err_dialog"
        const val KEY_SEARCHED_PRINTER_DIALOG = "searched_printer_dialog"
        private const val KEY_SEARCHED_PRINTER_LIST = "searched_printer_list"
        private const val MSG_UPDATE_REFRESH_BAR = 0x0
    }
}