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
import jp.co.riso.smartdeviceapp.R;
import jp.co.riso.smartdeviceapp.controller.jobs.PrintJobManager;
import jp.co.riso.smartdeviceapp.model.PrintJob;
import jp.co.riso.smartdeviceapp.model.Printer;
import jp.co.riso.smartdeviceapp.view.base.BaseFragment;
import jp.co.riso.smartdeviceapp.view.jobs.PrintJobsGroupView;
import jp.co.riso.smartdeviceapp.view.jobs.PrintJobsGroupView.PrintJobsGroupListener;
import jp.co.riso.smartdeviceapp.view.jobs.PrintJobsView;
import jp.co.riso.smartdeviceapp.view.jobs.PrintJobsView.PrintJobsViewListener;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PrintJobsFragment extends BaseFragment implements OnTouchListener, PrintJobsGroupListener, PrintJobsViewListener, ConfirmDialogListener {
    
    private static final String TAG = "PrintJobsFragment";
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
    
    public PrintJobsFragment() {
        super();
    }
    
    @Override
    public int getViewLayout() {
        return R.layout.fragment_printjobs;
    }
    
    @Override
    public void initializeFragment(Bundle savedInstanceState) {
        setRetainInstance(true);
    }
    
    @Override
    public void initializeView(View view, Bundle savedInstanceState) {
        
        // mPrintJobsLoadIndicator = (ProgressBar) view.findViewById(R.id.printJobsLoadIndicator);
        mPrintJobContainer = (LinearLayout) view.findViewById(R.id.printJobContainer);
        
        mPrintJobContainer.setOnTouchListener(this);
        
        mPrintJobsView = (PrintJobsView) view.findViewById(R.id.printJobsView);
        
        // mPrintJobsLoadIndicator.setVisibility(View.VISIBLE);
        mLoadPrintJobsTask = new LoadPrintJobsTask(getActivity(), mPrintJobs, mPrinters);
        mLoadPrintJobsTask.execute();
    }
    
    @Override
    public void initializeCustomActionBar(View view, Bundle savedInstanceState) {
        TextView textView = (TextView) view.findViewById(R.id.actionBarTitle);
        textView.setText(R.string.ids_lbl_print_job_history);
        addActionMenuButton(view);
    }
    
    // ================================================================================
    // INTERFACE - View.onTouchListener
    // ================================================================================
    
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
    
    @Override
    public void setPrinterToDelete(PrintJobsGroupView printJobsGroupView, Printer printer) {
        this.mPrintGroupToDelete = printJobsGroupView;
        this.mPrinterToDelete = printer;
    }
    
    @Override
    public void deletePrinterFromList(Printer printer) {
        mPrinters.remove(printer);
    }
    
    @Override
    public void deleteJobFromList(PrintJob printJob) {
        mPrintJobs.remove(printJob);
    }
    
    @Override
    public void showDeleteDialog() {
        String title = getResources().getString(R.string.ids_lbl_delete_jobs_title);
        String message = getResources().getString(R.string.ids_lbl_delete_jobs_msg);
        String confirmMsg = getResources().getString(R.string.ids_lbl_ok);
        String cancelMsg = getResources().getString(R.string.ids_lbl_cancel);
        
        ConfirmDialogFragment dialog = ConfirmDialogFragment.newInstance(title, message, confirmMsg, cancelMsg);
        dialog.setTargetFragment(this, 0);
        DialogUtils.displayDialog(getActivity(), TAG, dialog);
    }
    
    @Override
    public void setCollapsed(Printer printer, boolean isCollapsed) {
        if (isCollapsed) {
            mCollapsedPrinters.add(printer);
        } else {
            mCollapsedPrinters.remove(printer);
        }
    }
    
    @Override
    public void setDeletePrintJob(PrintJob job) {
        mPrintJobToDelete = job;
    }
    
    // ================================================================================
    // INTERFACE - PrintJobsViewListener
    // ================================================================================
    
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
    
    @Override
    public void onConfirm() {
        if (mPrintGroupToDelete != null) {
            mPrintGroupToDelete.onDeleteJobGroup();
            mPrintGroupToDelete = null;
            mPrinterToDelete = null;
        }
    }
    
    @Override
    public void onCancel() {
        if (mPrintGroupToDelete != null) {
            mPrintGroupToDelete.onCancelDeleteGroup();
            mPrintGroupToDelete = null;
            mPrinterToDelete = null;
        }
    }
    
    // ================================================================================
    // Internal Classes
    // ================================================================================
    
    private class LoadPrintJobsTask extends AsyncTask<Void, Void, Void> {
        private WeakReference<Context> mContextRef;
        private List<PrintJob> mPrintJobsList;
        private List<Printer> mPrintersList;
        
        public LoadPrintJobsTask(Context context, List<PrintJob> printJobs, List<Printer> printers) {
            mContextRef = new WeakReference<Context>(context);
            mPrintJobsList = printJobs == null ? null : new ArrayList<PrintJob>(printJobs);
            mPrintersList = printers == null ? null : new ArrayList<Printer>(printers);
        }
        
        @Override
        protected Void doInBackground(Void... arg0) {
            if (mContextRef != null && mContextRef.get() != null) {
                PrintJobManager pm = PrintJobManager.getInstance(mContextRef.get());
                if (mPrintJobsList == null || mPrintersList == null || pm.isRefreshFlag()) {
                    mPrintJobsList = pm.getPrintJobs();
                    mPrintersList = pm.getPrintersWithJobs();
                    pm.setRefreshFlag(false);
                }
            }
            return null;
        }
        
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            
            // if (mPrintJobsList.isEmpty() || isCancelled()) {
            // mPrintJobsLoadIndicator.setVisibility(View.GONE);
            // } else {
            
            mPrintJobs = new ArrayList<PrintJob>(mPrintJobsList);
            mPrinters = new ArrayList<Printer>(mPrintersList);
            
            if (mContextRef != null && mContextRef.get() != null && !mPrintJobsList.isEmpty() && !mPrintersList.isEmpty()) {
                mPrintJobsView.setData(mPrintJobsList, mPrintersList, PrintJobsFragment.this, PrintJobsFragment.this, mCollapsedPrinters, mPrintJobToDelete,
                        mPrinterToDelete);
            }
            // mPrintJobColumnView.setVisibility(View.INVISIBLE);
            
            // }
        }
    }
    
}
