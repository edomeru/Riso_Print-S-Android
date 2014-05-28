/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * PrintJobsFragment.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.smartdeviceapp.view.fragment;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import jp.co.riso.android.dialog.ConfirmDialogFragment;
import jp.co.riso.android.dialog.ConfirmDialogFragment.ConfirmDialogListener;
import jp.co.riso.android.dialog.DialogUtils;
import jp.co.riso.smartprint.R;
import jp.co.riso.smartdeviceapp.controller.jobs.PrintJobManager;
import jp.co.riso.smartdeviceapp.model.PrintJob;
import jp.co.riso.smartdeviceapp.model.Printer;
import jp.co.riso.smartdeviceapp.view.base.BaseFragment;
import jp.co.riso.smartdeviceapp.view.jobs.PrintJobsGroupView;
import jp.co.riso.smartdeviceapp.view.jobs.PrintJobsGroupView.PrintJobsGroupListener;
import jp.co.riso.smartdeviceapp.view.jobs.PrintJobsView;
import jp.co.riso.smartdeviceapp.view.jobs.PrintJobsView.PrintJobsViewListener;
import android.content.Context;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PrintJobsFragment extends BaseFragment implements OnTouchListener, PrintJobsGroupListener, PrintJobsViewListener, ConfirmDialogListener {
    
    private static final String TAG = PrintJobsFragment.class.getName();
    
    private PrintJobsView mPrintJobsView;
    private PrintJobsGroupView mPrintGroupToDelete;
    // TODO: use of loading indicator
    // private ProgressBar mPrintJobsLoadIndicator;
    private LinearLayout mPrintJobContainer;
    private LoadPrintJobsTask mLoadPrintJobsTask;
    private List<PrintJob> mPrintJobs;
    private List<Printer> mPrinters;
    private List<Printer> mCollapsedPrinters = new ArrayList<Printer>();
    private PrintJob mPrintJobToDelete;
    private Printer mPrinterToDelete;
    private ConfirmDialogFragment mConfirmDialog;
    
    /**
     * Instantiate PrintJobsFragment
     */
    public PrintJobsFragment() {
        super();
    }
    
    /** {@inheritDoc} */
    @Override
    public int getViewLayout() {
        return R.layout.fragment_printjobs;
    }
    
    /** {@inheritDoc} */
    @Override
    public void initializeFragment(Bundle savedInstanceState) {
        setRetainInstance(true);
    }
    
    /** {@inheritDoc} */
    @Override
    public void initializeView(View view, Bundle savedInstanceState) {
        mCollapsedPrinters.clear();
        mPrintJobToDelete = null;
        mPrinterToDelete = null;
        // mPrintJobsLoadIndicator = (ProgressBar) view.findViewById(R.id.printJobsLoadIndicator);
        mPrintJobContainer = (LinearLayout) view.findViewById(R.id.printJobContainer);
        mPrintJobsView = (PrintJobsView) view.findViewById(R.id.printJobsView);
        
        mPrintJobContainer.setOnTouchListener(this);
        
        // mPrintJobsLoadIndicator.setVisibility(View.VISIBLE);
        mLoadPrintJobsTask = new LoadPrintJobsTask(getActivity(), mPrintJobs, mPrinters);
        mLoadPrintJobsTask.execute();
    }
    
    /** {@inheritDoc} */
    @Override
    public void initializeCustomActionBar(View view, Bundle savedInstanceState) {
        TextView textView = (TextView) view.findViewById(R.id.actionBarTitle);
        textView.setText(R.string.ids_lbl_print_job_history);
        addActionMenuButton(view);
    }
    
    /** {@inheritDoc} */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        
        if (isTablet() && mPrintJobsView != null) {
            mPrintJobsView.reset();
        }
    }
    
    // ================================================================================
    // INTERFACE - View.onTouchListener
    // ================================================================================

    /** {@inheritDoc} */
    @Override
    public boolean onTouch(View v, MotionEvent e) {
        if (v.getId() == R.id.printJobContainer) {
            mPrintJobsView.endDelete(true);
        }
        return true;
    }
    
    // ================================================================================
    // INTERFACE - PrintJobsGroupViewListener
    // ================================================================================
    
    /** {@inheritDoc} */
    @Override
    public void setPrinterToDelete(PrintJobsGroupView printJobsGroupView, Printer printer) {
        mPrintGroupToDelete = printJobsGroupView;
        mPrinterToDelete = printer;
        mPrintJobsView.setPrinterToDelete(printer);
    }
    
    /** {@inheritDoc} */
    @Override
    public void deletePrinterFromList(Printer printer) {
        mPrinters.remove(printer);
    }
    
    /** {@inheritDoc} */
    @Override
    public void deleteJobFromList(PrintJob printJob) {
        mPrintJobs.remove(printJob);
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean showDeleteDialog() {
        String title = getResources().getString(R.string.ids_info_msg_delete_jobs_title);
        String message = getResources().getString(R.string.ids_info_msg_delete_jobs);
        String confirmMsg = getResources().getString(R.string.ids_lbl_ok);
        String cancelMsg = getResources().getString(R.string.ids_lbl_cancel);
        
        if (mConfirmDialog != null) {
            return false;
        } else {
            mConfirmDialog = ConfirmDialogFragment.newInstance(title, message, confirmMsg, cancelMsg);
            mConfirmDialog.setTargetFragment(this, 0);
            DialogUtils.displayDialog(getActivity(), TAG, mConfirmDialog);
            return true;
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void setCollapsed(Printer printer, boolean isCollapsed) {
        if (isCollapsed) {
            mCollapsedPrinters.add(printer);
        } else {
            mCollapsedPrinters.remove(printer);
        }
        mPrintJobsView.setCollapsedPrinters(printer, isCollapsed);
    }
    
    /** {@inheritDoc} */
    @Override
    public void setDeletePrintJob(PrintJobsGroupView printJobsGroupView, PrintJob job) {
        mPrintGroupToDelete = printJobsGroupView;
        mPrintJobToDelete = job;
        mPrintJobsView.setJobToDelete(job);
    }
    
    // ================================================================================
    // INTERFACE - PrintJobsViewListener
    // ================================================================================
    
    /** {@inheritDoc} */
    @Override
    public void hideLoading() {
        if (!isTablet()) {
            getView().setBackgroundColor(getResources().getColor(R.color.theme_light_3));
        }
        
        // mPrintJobsView.setVisibility(View.VISIBLE);
        // mPrintJobsLoadIndicator.setVisibility(View.GONE);
    }
    
    // ================================================================================
    // INTERFACE - ConfirmationDialogListener
    // ================================================================================
    
    /** {@inheritDoc} */
    @Override
    public void onConfirm() {
        if (mPrintGroupToDelete != null) {
            if (mPrinterToDelete != null) {
                mPrintGroupToDelete.onDeleteJobGroup();
                setPrinterToDelete(null, null);
            }
            else if (mPrintJobToDelete != null) {
                mPrintGroupToDelete.onDeletePrintJob(mPrintJobToDelete);
                setDeletePrintJob(null, null);
            }
            mConfirmDialog = null;
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void onCancel() {
        if (mPrintGroupToDelete != null) {
            if (mPrinterToDelete != null) {
                mPrintGroupToDelete.onCancelDeleteGroup();
                setPrinterToDelete(null, null);
            }
            else if (mPrintJobToDelete != null) {
                mPrintJobsView.endDelete(true);
                setDeletePrintJob(null, null);
            }
            mConfirmDialog = null;
        }
        
    }
    
    // ================================================================================
    // Internal Classes
    // ================================================================================
    
    /**
     * AsyncTask for Loading Print Job Tasks
     */
    private class LoadPrintJobsTask extends AsyncTask<Void, Void, Void> {
        private WeakReference<Context> mContextRef;
        private List<PrintJob> mPrintJobsList;
        private List<Printer> mPrintersList;
        
        public LoadPrintJobsTask(Context context, List<PrintJob> printJobs, List<Printer> printers) {
            mContextRef = new WeakReference<Context>(context);
            if (printJobs != null) {
                mPrintJobsList = new ArrayList<PrintJob>(printJobs);
            }
            if (printers != null) {
                mPrintersList = new ArrayList<Printer>(printers);
            }
        }
        
        /** {@inheritDoc} */
        @Override
        protected Void doInBackground(Void... arg0) {
            if (mContextRef != null && mContextRef.get() != null) {
                PrintJobManager pm = PrintJobManager.getInstance(mContextRef.get());
                List<Printer> printers = pm.getPrintersWithJobs();
                // if initial data OR job is added OR printer w/jobs is deleted (no need to check if a printer is added since initially w/o print job)
                if (mPrintJobsList == null || mPrintersList == null || pm.isRefreshFlag() || mPrintersList.size() > printers.size()) {
                    mPrintJobsList = pm.getPrintJobs();
                    mPrintersList = printers;
                    pm.setRefreshFlag(false);
                }
            }
            return null;
        }
        
        /** {@inheritDoc} */
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            
            // if (mPrintJobsList.isEmpty() || isCancelled()) {
            // mPrintJobsLoadIndicator.setVisibility(View.GONE);
            // } else {
            
            mPrintJobs = new ArrayList<PrintJob>(mPrintJobsList);
            mPrinters = new ArrayList<Printer>(mPrintersList);
            
            if (mContextRef != null && mContextRef.get() != null && !mPrintJobsList.isEmpty() && !mPrintersList.isEmpty()) {
                mPrintJobsView.setData(mPrintJobsList, mPrintersList, PrintJobsFragment.this, PrintJobsFragment.this);
            }
            // mPrintJobColumnView.setVisibility(View.INVISIBLE);
            
            // }
        }
    }
    
}
