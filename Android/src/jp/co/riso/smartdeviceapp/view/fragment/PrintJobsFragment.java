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
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class PrintJobsFragment extends BaseFragment implements OnTouchListener, PrintJobsGroupListener, PrintJobsViewListener, ConfirmDialogListener, Callback {
    
    private static final String TAG = "PrintJobsFragment";
    private static final int MSG_SCROLL = 0;
    
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
    private ScrollView mScrollView;
    private int mScrollPosition;
    private Handler mHandler;
    
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
        mHandler = new Handler(this);
    }
    
    /** {@inheritDoc} */
    @Override
    public void initializeView(View view, Bundle savedInstanceState) {
        
        // mPrintJobsLoadIndicator = (ProgressBar) view.findViewById(R.id.printJobsLoadIndicator);
        mScrollView = (ScrollView) view.findViewById(R.id.printJobScrollView);
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
    public void onDestroyView() {
        super.onDestroyView();
        mScrollPosition = mScrollView.getScrollY();
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
        this.mPrintGroupToDelete = printJobsGroupView;
        this.mPrinterToDelete = printer;
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
        String title = getResources().getString(R.string.ids_lbl_delete_jobs_title);
        String message = getResources().getString(R.string.ids_lbl_delete_jobs_msg);
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
    }
    
    /** {@inheritDoc} */
    @Override
    public void setDeletePrintJob(PrintJob job) {
        mPrintJobToDelete = job;
    }
    
    // ================================================================================
    // INTERFACE - PrintJobsViewListener
    // ================================================================================
    
    /** {@inheritDoc} */
    @Override
    public void hideLoading() {
        if (!isTablet()) {
            getView().setBackgroundColor(getResources().getColor(R.color.theme_light_3));
            Message newMessage = Message.obtain(mHandler, MSG_SCROLL);
            newMessage.arg1 = mScrollPosition;
            mHandler.sendMessage(newMessage);
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
            mPrintGroupToDelete.onDeleteJobGroup();
            mPrintGroupToDelete = null;
            mPrinterToDelete = null;
            mConfirmDialog = null;
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void onCancel() {
        if (mPrintGroupToDelete != null) {
            mPrintGroupToDelete.onCancelDeleteGroup();
            mPrintGroupToDelete = null;
            mPrinterToDelete = null;
            mConfirmDialog = null;
        }
        
    }
    
    // ================================================================================
    // INTERFACE - Callback
    // ================================================================================
    
    /** {@inheritDoc} */
    @Override
    public boolean handleMessage(Message msg) {
        mScrollView.setScrollY(msg.arg1);
        return true;
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
                boolean printersChanged = false;
                
                if (mPrintersList != null) {
                    if (mPrintersList.size() != printers.size()) {
                        printersChanged = true;
                    } else {
                        for (int i = 0; i < printers.size(); i++){
                            if (printers.get(i).getId() != mPrintersList.get(i).getId()) {
                                printersChanged = true;
                                break;
                            }
                        }
                    }
                }
                if (mPrintJobsList == null || mPrintersList == null || pm.isRefreshFlag() || printersChanged) {
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
                mPrintJobsView.setData(mPrintJobsList, mPrintersList, PrintJobsFragment.this, PrintJobsFragment.this, mCollapsedPrinters, mPrintJobToDelete,
                        mPrinterToDelete);
            }
            // mPrintJobColumnView.setVisibility(View.INVISIBLE);
            
            // }
        }
    }
    
}
