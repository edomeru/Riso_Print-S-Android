/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * PrinterSearchFragment.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.smartdeviceapp.view.fragment

//import jp.co.riso.smartdeviceapp.view.printers.PrinterSearchAdapter.setSearchAdapterInterface
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager.Companion.getInstance
//import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager.setPrinterSearchCallback
//import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager.isSearching
//import jp.co.riso.android.os.pauseablehandler.PauseableHandler.pause
//import jp.co.riso.android.os.pauseablehandler.PauseableHandler.resume
//import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager.startPrinterSearch
//import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager.cancelPrinterSearch
//import jp.co.riso.smartdeviceapp.model.Printer.ipAddress
//import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager.savePrinterToDB
import jp.co.riso.smartdeviceapp.view.base.BaseFragment
import eu.erikw.PullToRefreshListView
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager.PrinterSearchCallback
import jp.co.riso.smartdeviceapp.view.printers.PrinterSearchAdapter.PrinterSearchAdapterInterface
import jp.co.riso.android.dialog.ConfirmDialogFragment.ConfirmDialogListener
import jp.co.riso.android.os.pauseablehandler.PauseableHandlerCallback
import jp.co.riso.smartdeviceapp.view.printers.PrinterSearchAdapter
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager
import jp.co.riso.android.os.pauseablehandler.PauseableHandler
import android.widget.TextView
import jp.co.riso.smartprint.R
import android.os.Bundle
import android.os.Looper
import jp.co.riso.smartdeviceapp.SmartDeviceApp
import androidx.core.content.ContextCompat
import android.widget.RelativeLayout
import jp.co.riso.android.dialog.InfoDialogFragment
import jp.co.riso.android.dialog.DialogUtils
import jp.co.riso.smartdeviceapp.view.MainActivity
import jp.co.riso.android.util.NetUtils
import android.widget.FrameLayout
import android.animation.ValueAnimator
import android.os.Message
import android.view.View
import androidx.fragment.app.DialogFragment
import jp.co.riso.android.dialog.ConfirmDialogFragment
import jp.co.riso.smartdeviceapp.model.Printer
import java.util.ArrayList

/**
 * @class PrinterSearchFragment
 *
 * @brief Fragment for Printer Search Screen
 */
class PrinterSearchFragment : BaseFragment(), PullToRefreshListView.OnRefreshListener,
    PrinterSearchCallback, PrinterSearchAdapterInterface, ConfirmDialogListener,
    PauseableHandlerCallback {
    // ListView parameters
    private var mListView: PullToRefreshListView? = null
    private var mPrinter: ArrayList<Printer?>? = null
    private var mPrinterSearchAdapter: PrinterSearchAdapter? = null
    private var mPrinterManager: PrinterManager? = null
    private var mPauseableHandler: PauseableHandler? = null
    private var mEmptySearchText: TextView? = null
    private var mNoNetwork = false

    override val viewLayout: Int
        get() = R.layout.fragment_printersearch

    override fun initializeFragment(savedInstanceState: Bundle?) {
        mPrinter = if (savedInstanceState != null) {
            savedInstanceState.getParcelableArrayList(KEY_SEARCHED_PRINTER_LIST)
        } else {
            ArrayList()
        }
        if (mPauseableHandler == null) {
            mPauseableHandler = PauseableHandler(Looper.myLooper(), this)
        }
        mPrinterSearchAdapter =
            PrinterSearchAdapter(activity, R.layout.printersearch_container_item, mPrinter)
        mPrinterSearchAdapter!!.setSearchAdapterInterface(this)
        mPrinterManager = getInstance(SmartDeviceApp.appContext!!)
        mPrinterManager!!.setPrinterSearchCallback(this)
    }

    override fun initializeView(view: View, savedInstanceState: Bundle?) {
        mListView = view.findViewById(R.id.printer_list)
        mListView?.setBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.theme_light_3))
        mListView?.setAdapter(mPrinterSearchAdapter)
        mListView?.setOnRefreshListener(this)
        mEmptySearchText = view.findViewById(R.id.emptySearchText)
        val progressLayoutParams =
            mListView?.findViewById<View>(R.id.ptr_id_spinner)?.layoutParams as RelativeLayout.LayoutParams
        progressLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE)
        val arrowLayoutParams =
            mListView?.findViewById<View>(R.id.ptr_id_image)?.layoutParams as RelativeLayout.LayoutParams
        arrowLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE)
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
        if (mPrinterManager!!.isSearching) {
            updateRefreshBar()
        }
        if (savedInstanceState == null) {
            onRefresh()
            updateRefreshBar()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelableArrayList(KEY_SEARCHED_PRINTER_LIST, mPrinter)
        super.onSaveInstanceState(outState)
    }

    override fun onPause() {
        super.onPause()
        mPauseableHandler!!.pause()
    }

    override fun onResume() {
        super.onResume()
        mPauseableHandler!!.resume()
    }
    // ================================================================================
    // Private Methods
    // ================================================================================
    /**
     * @brief Updates the status of the refresh bar.
     */
    private fun updateRefreshBar() {
        val newMessage = Message.obtain(mPauseableHandler, MSG_UPDATE_REFRESH_BAR)
        mPauseableHandler!!.sendMessage(newMessage)
    }

    /**
     * @brief Display error dialog during failed printer search
     */
    private fun dialogErrCb() {
        val title = resources.getString(R.string.ids_lbl_search_printers)
        val errMsg: String
        errMsg = resources.getString(R.string.ids_err_msg_network_error)
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
        mPrinter!!.clear()
        mEmptySearchText!!.visibility = View.GONE
        mNoNetwork = false
        if (!NetUtils.isWifiAvailable) {
            mNoNetwork = true
            dialogErrCb()
            updateRefreshBar()
            return
        }
        mPrinterManager!!.startPrinterSearch()
    }

    override fun onHeaderAdjusted(margin: Int) {
        if (mEmptySearchText != null) {
            val params = mEmptySearchText!!.layoutParams as FrameLayout.LayoutParams
            params.topMargin = margin
            mEmptySearchText!!.layoutParams = params
        }
    }

    override fun onBounceBackHeader(duration: Int) {
        // http://stackoverflow.com/questions/13881419/android-change-left-margin-using-animation
        val params = mEmptySearchText!!.layoutParams as FrameLayout.LayoutParams
        val animation = ValueAnimator.ofInt(params.topMargin, 0)
        animation.addUpdateListener { valueAnimator ->
            params.topMargin = (valueAnimator.animatedValue as Int)
            mEmptySearchText!!.requestLayout()
        }
        animation.duration = duration.toLong()
        animation.start()
    }

    // ================================================================================
    // INTERFACE - View.OnClickListener
    // ================================================================================
    override fun onClick(v: View) {
        // Back Button
        if (v.id == R.id.menu_id_back_button) {
            mPrinterManager!!.cancelPrinterSearch()
            closeScreen()
        }
    }

    // ================================================================================
    // INTERFACE - OnPrinterSearchCallback
    // ================================================================================
    override fun onPrinterAdd(printer: Printer?) {
        if (activity == null) {
            return
        }
        requireActivity().runOnUiThread(Runnable {
            if (!mPrinterManager!!.isSearching) {
                return@Runnable
            }
            for (i in mPrinter!!.indices) {
                if (mPrinter!![i]!!.ipAddress == printer!!.ipAddress) {
                    mPrinter!![i] = printer
                    return@Runnable
                }
            }
            mPrinter!!.add(printer)
            mPrinterSearchAdapter!!.notifyDataSetChanged()
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
        if (!mPrinterManager!!.savePrinterToDB(printer, true)) {
            ret = -1
            msg = resources.getString(R.string.ids_err_msg_db_failure)
            info =
                InfoDialogFragment.newInstance(title, msg, resources.getString(R.string.ids_lbl_ok))
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
            MSG_UPDATE_REFRESH_BAR -> if (mPrinterManager!!.isSearching) {
                mListView!!.setRefreshing()
            } else {
                mListView!!.onRefreshComplete()
                if (mPrinter!!.isEmpty() && !mNoNetwork) {
                    mEmptySearchText!!.visibility = View.VISIBLE
                }
            }
        }
    }

    companion object {
        private const val KEY_PRINTER_ERR_DIALOG = "printer_err_dialog"
        private const val KEY_SEARCHED_PRINTER_LIST = "searched_printer_list"
        private const val KEY_SEARCHED_PRINTER_DIALOG = "searched_printer_dialog"
        private const val MSG_UPDATE_REFRESH_BAR = 0x0
    }
}