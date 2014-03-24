/*
 * Copyright (c) 2014 All rights reserved.
 *
 * PrintJobsFragment.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.smartdeviceapp.view.fragment;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import jp.co.riso.smartdeviceapp.R;
import jp.co.riso.smartdeviceapp.controller.jobs.PrintJobManager;
import jp.co.riso.smartdeviceapp.model.PrintJob;
import jp.co.riso.smartdeviceapp.model.Printer;
import jp.co.riso.smartdeviceapp.view.base.BaseFragment;
import jp.co.riso.smartdeviceapp.view.jobs.PrintJobsColumnView;
import jp.co.riso.smartdeviceapp.view.jobs.PrintJobsColumnView.LoadingViewListener;
import jp.co.riso.smartdeviceapp.view.jobs.PrintJobsColumnView.ReloadViewListener;
import jp.co.riso.smartdeviceapp.view.jobs.PrintJobsGroupView;
import jp.co.riso.smartdeviceapp.view.jobs.PrintJobsGroupView.JobDeleteListener;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PrintJobsFragment extends BaseFragment implements JobDeleteListener, OnClickListener, LoadingViewListener, ReloadViewListener {
    
    private PrintJobsColumnView mPrintJobColumnView;
    private PrintJobsGroupView mPrintGroupToDelete;
    // TODO: use of loading indicator
    // private ProgressBar mPrintJobsLoadIndicator;
    private LinearLayout mPrintJobContainer;
    private LoadPrintJobsTask mLoadPrintJobsTask;
    private List<PrintJob> mPrintJobs;
    private List<Printer> mPrinters;
    
    public PrintJobsFragment() {
        super();
    }
    
    @Override
    public int getViewLayout() {
        return R.layout.fragment_printjobs;
    }
    
    @Override
    public void initializeFragment(Bundle savedInstanceState) {
    }
    
    @Override
    public void initializeView(View view, Bundle savedInstanceState) {
        setRetainInstance(true);
        
        // mPrintJobsLoadIndicator = (ProgressBar) view.findViewById(R.id.printJobsLoadIndicator);
        mPrintJobContainer = (LinearLayout) view.findViewById(R.id.printJobContainer);
        
        view.setOnClickListener(this);
        mPrintJobContainer.setOnClickListener(this);
        
        mPrintJobColumnView = (PrintJobsColumnView) view.findViewById(R.id.printJobsColumnView);
    }
    
    @Override
    public void initializeCustomActionBar(View view, Bundle savedInstanceState) {
        TextView textView = (TextView) view.findViewById(R.id.actionBarTitle);
        textView.setText(R.string.ids_lbl_print_job_history);
        addActionMenuButton(view);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        
        //        mPrintJobsLoadIndicator.setVisibility(View.VISIBLE);
        mLoadPrintJobsTask = new LoadPrintJobsTask(getActivity(), mPrintJobs, mPrinters);
        mLoadPrintJobsTask.execute();
    }
    
    // ================================================================================
    // INTERFACE - View.OnClickListener
    // ================================================================================
    
    /** {@inheritDoc} */
    @Override
    public void onClick(View v) {
        super.onClick(v);
        
        if (mPrintGroupToDelete != null) {
            mPrintGroupToDelete.clearDeleteButton();
        }
    }
    
    // ================================================================================
    // INTERFACE - JobDeleteListener
    // ================================================================================
    
    @Override
    public void setPrintJobsGroupView(PrintJobsGroupView printJobsGroupView) {
        this.mPrintGroupToDelete = printJobsGroupView;
    }
    
    @Override
    public void clearButton() {
        if (mPrintGroupToDelete != null) {
            mPrintGroupToDelete.clearDeleteButton();
        }
    }
    
    @Override
    public void deletePrinterFromList(Printer printer) {
        mPrinters.remove(printer);
    }
    
    @Override
    public void deleteJobFromList(PrintJob printJob) {
        mPrintJobs.remove(printJob);
    }
    
    // ================================================================================
    // INTERFACE - LoadingViewListener
    // ================================================================================
    
    @Override
    public void hideLoading() {
        if (!isTablet()) {
            getView().setBackgroundColor(getResources().getColor(R.color.theme_light_3));
        }
        
        // AppUtils.changeChildrenFont((ViewGroup) mPrintJobsView, SmartDeviceApp.getAppFont());
        
        mPrintJobColumnView.setVisibility(View.VISIBLE);
        
        //        mPrintJobsLoadIndicator.setVisibility(View.GONE);
        
    }
    
    // ================================================================================
    // INTERFACE - ReloadViewListener
    // ================================================================================
    
    @Override
    public void reloadView() {
        mLoadPrintJobsTask = new LoadPrintJobsTask(getActivity(), mPrintJobs, mPrinters);
        mLoadPrintJobsTask.execute();
    }
    
    // ================================================================================
    // Internal Classes
    // ================================================================================
    
    private class LoadPrintJobsTask extends AsyncTask<Void, Void, Void> {
        private WeakReference<Context> mContextRef;
        private List<PrintJob> mPrintJobsList = new ArrayList<PrintJob>();
        private List<Printer> mPrintersList = new ArrayList<Printer>();
        
        public LoadPrintJobsTask(Context context, List<PrintJob> printJobs, List<Printer> printers) {
            mContextRef = new WeakReference<Context>(context);
            mPrintJobsList = printJobs;
            mPrintersList = printers;
            
        }
        
        @Override
        protected Void doInBackground(Void... arg0) {
            if (mContextRef != null && mContextRef.get() != null) {
                PrintJobManager.getInstance(mContextRef.get());
                
                if (mPrintJobsList == null || mPrintersList == null) {
                    
                    mPrintJobsList = PrintJobManager.getPrintJobs();
                    
                    mPrintersList = PrintJobManager.getPrintersWithJobs();
                    
                }
            }
            return null;
        }
        
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            
            //            if (mPrintJobsList.isEmpty() || isCancelled()) {
            //                mPrintJobsLoadIndicator.setVisibility(View.GONE);
            //            } else {
            mPrintJobs = mPrintJobsList;
            mPrinters = mPrintersList;
            if (mContextRef != null && mContextRef.get() != null) {
                mPrintJobColumnView.setData(mPrintJobsList, mPrintersList, isTablet() ? isTabletLand() ? 3 : 2 : 1, PrintJobsFragment.this,
                        PrintJobsFragment.this, PrintJobsFragment.this);
            }
            //mPrintJobColumnView.setVisibility(View.INVISIBLE);
            
            //            }
            
        }
        
    }
    
}
