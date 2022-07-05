/*
 * Copyright (c) 2022 RISO, Inc. All rights reserved.
 *
 * PrintJobsFragment.kt
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.smartdeviceapp.view.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.content.ContextCompat
import jp.co.riso.android.dialog.ConfirmDialogFragment
import jp.co.riso.android.dialog.ConfirmDialogFragment.ConfirmDialogListener
import jp.co.riso.android.dialog.DialogUtils
import jp.co.riso.smartdeviceapp.controller.jobs.PrintJobManager
import jp.co.riso.smartdeviceapp.model.PrintJob
import jp.co.riso.smartdeviceapp.model.Printer
import jp.co.riso.smartdeviceapp.view.base.BaseFragment
import jp.co.riso.smartdeviceapp.view.jobs.PrintJobsGroupView
import jp.co.riso.smartdeviceapp.view.jobs.PrintJobsGroupView.PrintJobsGroupListener
import jp.co.riso.smartdeviceapp.view.jobs.PrintJobsView
import jp.co.riso.smartdeviceapp.view.jobs.PrintJobsView.PrintJobsViewListener
import jp.co.riso.smartprint.R
import java.lang.ref.WeakReference

/**
 * @class PrintJobsFragment
 *
 * @brief Fragment class for displaying Print Job History screen.
 * @brief Creates a PrintJobsFragment instance.
 */
class PrintJobsFragment : BaseFragment(), OnTouchListener, PrintJobsGroupListener, PrintJobsViewListener,
    ConfirmDialogListener {
    private var _printJobsView: PrintJobsView? = null
    private var _printGroupToDelete: PrintJobsGroupView? = null
    private var _printJobContainer: LinearLayout? = null
    private var _loadPrintJobsTask: LoadPrintJobsTask? = null
    private var _printJobs: MutableList<PrintJob>? = null
    private var _printers: MutableList<Printer>? = null
    private val _collapsedPrinters: MutableList<Printer> = ArrayList()
    private var _printJobToDelete: PrintJob? = null
    private var _printerToDelete: Printer? = null
    private var _confirmDialog: ConfirmDialogFragment? = null
    private var _scrollView: ScrollView? = null
    private var _emptyJobsText: TextView? = null

    override val viewLayout: Int
        get() = R.layout.fragment_printjobs

    override fun initializeFragment(savedInstanceState: Bundle?) {
        if (isChromeBook) {
            // RM1167 temporary fix - Avoid rotation issues in Chrome
            retainInstance = true
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun initializeView(view: View, savedInstanceState: Bundle?) {
        _collapsedPrinters.clear()
        _printJobToDelete = null
        _printerToDelete = null
        _printJobContainer = view.findViewById(R.id.printJobContainer)
        _printJobsView = view.findViewById(R.id.printJobsView)
        _emptyJobsText = view.findViewById(R.id.emptyJobsText)
        _scrollView = view.findViewById(R.id.printJobScrollView)
        _printJobContainer!!.setOnTouchListener(this)
        _loadPrintJobsTask = LoadPrintJobsTask(activity, _printJobs, _printers)
        _loadPrintJobsTask!!.start()
    }

    override fun initializeCustomActionBar(view: View, savedInstanceState: Bundle?) {
        val textView = view.findViewById<TextView>(R.id.actionBarTitle)
        textView.setText(R.string.ids_lbl_print_job_history)
        addActionMenuButton(view)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (isTablet && _printJobsView != null) {
            _printJobsView!!.reset()
        }
    }
    // ================================================================================
    // Private Methods
    // ================================================================================
    /**
     * @brief Displays empty message and hides PrintJobsView.
     */
    private fun showEmptyText() {
        _emptyJobsText!!.visibility = View.VISIBLE
        _scrollView!!.visibility = View.GONE
        val view = view
        view?.setBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.theme_light_2))
    }

    // ================================================================================
    // INTERFACE - View.onTouchListener
    // ================================================================================
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View, e: MotionEvent): Boolean {
        if (v.id == R.id.printJobContainer) {
            _printJobsView!!.endDelete(true)
        }
        return true
    }

    // ================================================================================
    // INTERFACE - PrintJobsGroupViewListener
    // ================================================================================
    override fun setPrinterToDelete(printJobsGroupView: PrintJobsGroupView?, printer: Printer?) {
        _printGroupToDelete = printJobsGroupView
        _printerToDelete = printer
        _printJobsView!!.setPrinterToDelete(printer)
    }

    override fun deletePrinterFromList(printer: Printer?) {
        _printers!!.remove(printer)
        if (printer != null) {
            _printJobsView!!.deletePrinterFromList(printer)
        }
    }

    override fun deleteJobFromList(printJob: PrintJob?) {
        _printJobs!!.remove(printJob)
        if (printJob != null) {
            _printJobsView!!.deleteJobFromList(printJob)
        }
        if (_printJobs!!.isEmpty()) {
            showEmptyText()
        }
    }

    override fun showDeleteDialog(): Boolean {
        val title = resources.getString(R.string.ids_info_msg_delete_jobs_title)
        val message = resources.getString(R.string.ids_info_msg_delete_jobs)
        val confirmMsg = resources.getString(R.string.ids_lbl_ok)
        val cancelMsg = resources.getString(R.string.ids_lbl_cancel)
        return if (_confirmDialog != null && _confirmDialog!!.isShowing || _printGroupToDelete == null) {
            false
        } else {
            _confirmDialog =
                ConfirmDialogFragment.newInstance(title, message, confirmMsg, cancelMsg, TAG)
            setResultListenerConfirmDialog(
                requireActivity().supportFragmentManager,
                this,
                TAG
            )
            DialogUtils.displayDialog(requireActivity(), TAG, _confirmDialog!!)
            true
        }
    }

    override fun setCollapsed(printer: Printer?, isCollapsed: Boolean) {
        if (isCollapsed) {
            if (printer != null) {
                _collapsedPrinters.add(printer)
            }
        } else {
            _collapsedPrinters.remove(printer)
        }
        if (printer != null) {
            _printJobsView!!.setCollapsedPrinters(printer, isCollapsed)
        }
    }

    override fun setDeletePrintJob(printJobsGroupView: PrintJobsGroupView?, job: PrintJob?) {
        _printGroupToDelete = printJobsGroupView
        _printJobToDelete = job
        _printJobsView!!.setJobToDelete(job)
    }

    // ================================================================================
    // INTERFACE - PrintJobsViewListener
    // ================================================================================
    override fun onLoadFinished() {
        if (!isTablet) {
            val view = view
            view?.setBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.theme_light_3))
        }
    }

    // ================================================================================
    // INTERFACE - ConfirmationDialogListener
    // ================================================================================
    override fun onConfirm() {
        if (_printGroupToDelete != null) {
            _printGroupToDelete!!.focusNextPrintJob()
            if (_printerToDelete != null) {
                _printGroupToDelete!!.onDeleteJobGroup()
                setPrinterToDelete(null, null)
            } else if (_printJobToDelete != null) {
                _printGroupToDelete!!.onDeletePrintJob(_printJobToDelete!!)
                setDeletePrintJob(null, null)
            }
            _confirmDialog = null
        }
    }

    override fun onCancel() {
        if (_printGroupToDelete != null) {
            _printGroupToDelete!!.returnFocusToPrintJob()
            if (_printerToDelete != null) {
                _printGroupToDelete!!.onCancelDeleteGroup()
                setPrinterToDelete(null, null)
            } else if (_printJobToDelete != null) {
                _printJobsView!!.endDelete(true)
                setDeletePrintJob(null, null)
            }
            _confirmDialog = null
        }
    }
    // ================================================================================
    // Internal Classes
    // ================================================================================
    /**
     * @class LoadPrintJobsTask
     *
     * @brief Thread for Loading Print Job Tasks
     */
    private inner class LoadPrintJobsTask(
        context: Context?,
        printJobs: List<PrintJob>?,
        printers: List<Printer>?
    ) : Thread() {
        private val _contextRef: WeakReference<Context?>?
        private var _printJobsList: List<PrintJob>? = null
        private var _printersList: List<Printer>? = null
        override fun run() {
            if (_contextRef!!.get() != null) {
                val pm = PrintJobManager.getInstance(_contextRef.get()!!)
                val printers = pm!!.printersWithJobs
                // if initial data OR job is added OR printer w/jobs is deleted (no need to check if a printer is added since initially w/o print job)
                if (_printJobsList == null || _printersList == null || pm.isRefreshFlag || _printersList!!.size > printers.size) {
                    _printJobsList = pm.printJobs
                    _printersList = printers
                    pm.isRefreshFlag = false
                }
                (_contextRef.get() as Activity?)!!.runOnUiThread {
                    if (_printJobsList!!.isEmpty()) {
                        showEmptyText()
                    } else {
                        _printJobs = ArrayList(_printJobsList!!)
                        _printers = ArrayList(_printersList!!)
                        if (_contextRef.get() != null && _printJobsList!!.isNotEmpty() && _printersList!!.isNotEmpty()) {
                            _printJobsView!!.setData(
                                _printJobsList,
                                _printersList,
                                this@PrintJobsFragment,
                                this@PrintJobsFragment
                            )
                        }
                    }
                }
            }
        }

        /**
         * @brief Creates LoadPrintJobsTask instance.
         *
         * @param context Activity context
         * @param printJobs List of PrintJob objects
         * @param printers List of Printer objects
         */
        init {
            _contextRef = WeakReference(context)
            _printJobsList = printJobs?.let { ArrayList(it) }
            _printersList = printers?.let { ArrayList(it) }
        }
    }

    companion object {
        val TAG = PrintJobsFragment::class.java.name
    }
}