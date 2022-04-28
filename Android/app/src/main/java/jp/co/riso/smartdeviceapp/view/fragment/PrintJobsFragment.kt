/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * PrintJobsFragment.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.smartdeviceapp.view.fragment

import jp.co.riso.smartdeviceapp.view.base.BaseFragment
import android.view.View.OnTouchListener
import jp.co.riso.smartdeviceapp.view.jobs.PrintJobsGroupView.PrintJobsGroupListener
import jp.co.riso.smartdeviceapp.view.jobs.PrintJobsView.PrintJobsViewListener
import jp.co.riso.android.dialog.ConfirmDialogFragment.ConfirmDialogListener
import jp.co.riso.smartdeviceapp.view.jobs.PrintJobsView
import jp.co.riso.smartdeviceapp.view.jobs.PrintJobsGroupView
import android.widget.LinearLayout
import jp.co.riso.smartdeviceapp.view.fragment.PrintJobsFragment.LoadPrintJobsTask
import jp.co.riso.android.dialog.ConfirmDialogFragment
import android.widget.ScrollView
import android.widget.TextView
import jp.co.riso.smartprint.R
import android.os.Bundle
import androidx.core.content.ContextCompat
import android.annotation.SuppressLint
import android.view.MotionEvent
import jp.co.riso.android.dialog.DialogUtils
import jp.co.riso.smartdeviceapp.view.fragment.PrintJobsFragment
import jp.co.riso.smartdeviceapp.controller.jobs.PrintJobManager
import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.view.View
import jp.co.riso.smartdeviceapp.model.PrintJob
import jp.co.riso.smartdeviceapp.model.Printer
import java.lang.ref.WeakReference
import java.util.ArrayList

/**
 * @class PrintJobsFragment
 *
 * @brief Fragment class for displaying Print Job History screen.
 */
class PrintJobsFragment
/**
 * @brief Creates a PrintJobsFragment instance.
 */
    : BaseFragment(), OnTouchListener, PrintJobsGroupListener, PrintJobsViewListener,
    ConfirmDialogListener {
    private var mPrintJobsView: PrintJobsView? = null
    private var mPrintGroupToDelete: PrintJobsGroupView? = null
    private var mPrintJobContainer: LinearLayout? = null
    private var mLoadPrintJobsTask: LoadPrintJobsTask? = null
    private var mPrintJobs: MutableList<PrintJob?>? = null
    private var mPrinters: MutableList<Printer?>? = null
    private val mCollapsedPrinters: MutableList<Printer> = ArrayList()
    private var mPrintJobToDelete: PrintJob? = null
    private var mPrinterToDelete: Printer? = null
    private var mConfirmDialog: ConfirmDialogFragment? = null
    private var mScrollView: ScrollView? = null
    private var mEmptyJobsText: TextView? = null
    override fun getViewLayout(): Int {
        return R.layout.fragment_printjobs
    }

    override fun initializeFragment(savedInstanceState: Bundle?) {
        retainInstance = true
    }

    override fun initializeView(view: View, savedInstanceState: Bundle?) {
        mCollapsedPrinters.clear()
        mPrintJobToDelete = null
        mPrinterToDelete = null
        mPrintJobContainer = view.findViewById(R.id.printJobContainer)
        mPrintJobsView = view.findViewById(R.id.printJobsView)
        mEmptyJobsText = view.findViewById(R.id.emptyJobsText)
        mScrollView = view.findViewById(R.id.printJobScrollView)
        mPrintJobContainer!!.setOnTouchListener(this)
        mLoadPrintJobsTask = LoadPrintJobsTask(activity, mPrintJobs, mPrinters)
        mLoadPrintJobsTask!!.start()
    }

    override fun initializeCustomActionBar(view: View, savedInstanceState: Bundle?) {
        val textView = view.findViewById<TextView>(R.id.actionBarTitle)
        textView.setText(R.string.ids_lbl_print_job_history)
        addActionMenuButton(view)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (isTablet && mPrintJobsView != null) {
            mPrintJobsView!!.reset()
        }
    }
    // ================================================================================
    // Private Methods
    // ================================================================================
    /**
     * @brief Displays empty message and hides PrintJobsView.
     */
    private fun showEmptyText() {
        mEmptyJobsText!!.visibility = View.VISIBLE
        mScrollView!!.visibility = View.GONE
        val view = view
        view?.setBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.theme_light_2))
    }

    // ================================================================================
    // INTERFACE - View.onTouchListener
    // ================================================================================
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View, e: MotionEvent): Boolean {
        if (v.id == R.id.printJobContainer) {
            mPrintJobsView!!.endDelete(true)
        }
        return true
    }

    // ================================================================================
    // INTERFACE - PrintJobsGroupViewListener
    // ================================================================================
    override fun setPrinterToDelete(printJobsGroupView: PrintJobsGroupView?, printer: Printer?) {
        mPrintGroupToDelete = printJobsGroupView
        mPrinterToDelete = printer
        mPrintJobsView!!.setPrinterToDelete(printer)
    }

    override fun deletePrinterFromList(printer: Printer?) {
        mPrinters!!.remove(printer)
        if (printer != null) {
            mPrintJobsView!!.deletePrinterFromList(printer)
        }
    }

    override fun deleteJobFromList(printJob: PrintJob?) {
        mPrintJobs!!.remove(printJob)
        if (printJob != null) {
            mPrintJobsView!!.deleteJobFromList(printJob)
        }
        if (mPrintJobs!!.isEmpty()) {
            showEmptyText()
        }
    }

    override fun showDeleteDialog(): Boolean {
        val title = resources.getString(R.string.ids_info_msg_delete_jobs_title)
        val message = resources.getString(R.string.ids_info_msg_delete_jobs)
        val confirmMsg = resources.getString(R.string.ids_lbl_ok)
        val cancelMsg = resources.getString(R.string.ids_lbl_cancel)
        return if (mConfirmDialog != null && mConfirmDialog!!.isShowing || mPrintGroupToDelete == null) {
            false
        } else {
            mConfirmDialog =
                ConfirmDialogFragment.newInstance(title, message, confirmMsg, cancelMsg)
            mConfirmDialog!!.setTargetFragment(this, 0)
            DialogUtils.displayDialog(activity, TAG, mConfirmDialog)
            true
        }
    }

    override fun setCollapsed(printer: Printer?, isCollapsed: Boolean) {
        if (isCollapsed) {
            if (printer != null) {
                mCollapsedPrinters.add(printer)
            }
        } else {
            mCollapsedPrinters.remove(printer)
        }
        if (printer != null) {
            mPrintJobsView!!.setCollapsedPrinters(printer, isCollapsed)
        }
    }

    override fun setDeletePrintJob(printJobsGroupView: PrintJobsGroupView?, job: PrintJob?) {
        mPrintGroupToDelete = printJobsGroupView
        mPrintJobToDelete = job
        mPrintJobsView!!.setJobToDelete(job)
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
        if (mPrintGroupToDelete != null) {
            mPrintGroupToDelete!!.focusNextPrintJob()
            if (mPrinterToDelete != null) {
                mPrintGroupToDelete!!.onDeleteJobGroup()
                setPrinterToDelete(null, null)
            } else if (mPrintJobToDelete != null) {
                mPrintGroupToDelete!!.onDeletePrintJob(mPrintJobToDelete!!)
                setDeletePrintJob(null, null)
            }
            mConfirmDialog = null
        }
    }

    override fun onCancel() {
        if (mPrintGroupToDelete != null) {
            mPrintGroupToDelete!!.returnFocusToPrintJob()
            if (mPrinterToDelete != null) {
                mPrintGroupToDelete!!.onCancelDeleteGroup()
                setPrinterToDelete(null, null)
            } else if (mPrintJobToDelete != null) {
                mPrintJobsView!!.endDelete(true)
                setDeletePrintJob(null, null)
            }
            mConfirmDialog = null
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
        printJobs: List<PrintJob?>?,
        printers: List<Printer?>?
    ) : Thread() {
        private val mContextRef: WeakReference<Context?>?
        private var mPrintJobsList: List<PrintJob?>? = null
        private var mPrintersList: List<Printer?>? = null
        override fun run() {
            if (mContextRef != null && mContextRef.get() != null) {
                val pm = PrintJobManager.getInstance(mContextRef.get()!!)
                val printers = pm!!.printersWithJobs
                // if initial data OR job is added OR printer w/jobs is deleted (no need to check if a printer is added since initially w/o print job)
                if (mPrintJobsList == null || mPrintersList == null || pm.isRefreshFlag || mPrintersList!!.size > printers.size) {
                    mPrintJobsList = pm.printJobs
                    mPrintersList = printers
                    pm.isRefreshFlag = false
                }
                (mContextRef.get() as Activity?)!!.runOnUiThread {
                    if (mPrintJobsList!!.isEmpty()) {
                        showEmptyText()
                    } else {
                        mPrintJobs = ArrayList(mPrintJobsList)
                        mPrinters = ArrayList(mPrintersList)
                        if (mContextRef != null && mContextRef.get() != null && !mPrintJobsList!!.isEmpty() && !mPrintersList!!.isEmpty()) {
                            mPrintJobsView!!.setData(
                                mPrintJobsList as List<PrintJob>?,
                                mPrintersList as List<Printer>?,
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
            mContextRef = WeakReference(context)
            if (printJobs != null) {
                mPrintJobsList = ArrayList(printJobs)
            }
            if (printers != null) {
                mPrintersList = ArrayList(printers)
            }
        }
    }

    companion object {
        private val TAG = PrintJobsFragment::class.java.name
    }
}