/*
 * Copyright (c) 2014 All rights reserved.
 *
 * PrintJobsFragment.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.smartdeviceapp.view.fragment;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jp.co.riso.smartdeviceapp.R;
import jp.co.riso.smartdeviceapp.controller.db.DatabaseManager;
import jp.co.riso.smartdeviceapp.controller.jobs.PrintJobManager;
import jp.co.riso.smartdeviceapp.model.PrintJob;
import jp.co.riso.smartdeviceapp.model.PrintJob.JobResult;
import jp.co.riso.smartdeviceapp.model.Printer;
import jp.co.riso.smartdeviceapp.view.base.BaseFragment;
import jp.co.riso.smartdeviceapp.view.custom.PrintJobsColumnView;
import jp.co.riso.smartdeviceapp.view.custom.PrintJobsColumnView.LoadingViewListener;
import jp.co.riso.smartdeviceapp.view.custom.PrintJobsGroupView;
import jp.co.riso.smartdeviceapp.view.custom.PrintJobsGroupView.PrintDeleteListener;
import android.content.ContentValues;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class PrintJobsFragment extends BaseFragment implements PrintDeleteListener, OnClickListener, LoadingViewListener {
    
    private PrintJobsColumnView mPrintJobColumnView;
    private List<PrintJob> mPrintJobs = new ArrayList<PrintJob>();
    private List<Printer> mPrinters = new ArrayList<Printer>();
    private PrintJobsGroupView mPrintGroupToDelete;
    private ProgressBar mPrintJobsLoadIndicator;
    private LinearLayout mPrintJobContainer;
    private View mPrintJobsView;
    
    public PrintJobsFragment() {
        super();
    }
    
    @Override
    public int getViewLayout() {
        return R.layout.fragment_printjobs;
    }
    
    @Override
    public void initializeFragment(Bundle savedInstanceState) {
        if (PrintJobManager.getInstance() == null)
            PrintJobManager.initializeInstance(getActivity());
        
    }
    
    @Override
    public void initializeView(View view, Bundle savedInstanceState) {
        mPrintJobsView = view;
        mPrintJobsLoadIndicator = (ProgressBar) view.findViewById(R.id.printJobsLoadIndicator);
        mPrintJobContainer = (LinearLayout) view.findViewById(R.id.printJobContainer);
       
    }
    
    @Override
    public void initializeCustomActionBar(View view, Bundle savedInstanceState) {
        TextView textView = (TextView) view.findViewById(R.id.actionBarTitle);
        textView.setText(R.string.ids_lbl_print_jobs);
        
        addActionMenuButton(view);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        
        (new LoadPrintJobsFromDB()).execute();
        
    }
    
    private void initializePids() {
        
        // for testing only
        
        DatabaseManager manager = new DatabaseManager(getActivity());
        ContentValues pvalues = new ContentValues();
        
        if (PrintJobManager.getPrintersWithJobs().isEmpty()) {
            
            pvalues.put("prn_name", "myprintername");
            pvalues.put("prn_port_setting", 0);
            
            manager.insert("Printer", "true", pvalues);
            
            pvalues.put("prn_name", "riso printer");
            pvalues.put("prn_port_setting", 0);
            manager.insert("Printer", "true", pvalues);
            
            pvalues.put("prn_name", "RISO");
            pvalues.put("prn_port_setting", 0);
            manager.insert("Printer", "true", pvalues);
            
            pvalues.put("prn_name", "this is a long printer jobs group name.");
            pvalues.put("prn_port_setting", 0);
            manager.insert("Printer", "true", pvalues);
            
            pvalues.put("prn_name", "printer5");
            pvalues.put("prn_port_setting", 0);
            manager.insert("Printer", "true", pvalues);
            
            pvalues.put("prn_name", "new printer");
            pvalues.put("prn_port_setting", 0);
            manager.insert("Printer", "true", pvalues);
            
            pvalues.put("prn_name", "7th printer");
            pvalues.put("prn_port_setting", 0);
            manager.insert("Printer", "true", pvalues);
            
        }
    }
    
    // for testing only
    private void initializePJs() {
        
        if (PrintJobManager.getPrintJobs().isEmpty()) {
            PrintJobManager.createPrintJob(1, "file1.pdf", new Date(), JobResult.SUCCESSFUL);
            PrintJobManager.createPrintJob(1, "test file.pdf", new Date(), JobResult.ERROR);
            PrintJobManager.createPrintJob(1, "filename.pdf", new Date(), JobResult.SUCCESSFUL);
            PrintJobManager.createPrintJob(1, "this is a long filename.pdf", new Date(), JobResult.ERROR);
            PrintJobManager.createPrintJob(2, "ALLCAPS.pdf", new Date(), JobResult.SUCCESSFUL);
            PrintJobManager.createPrintJob(2, "qwerty.pdf", new Date(), JobResult.ERROR);
            PrintJobManager.createPrintJob(2, "!@#$%^^&*.pdf", new Date(), JobResult.SUCCESSFUL);
            PrintJobManager.createPrintJob(3, "123456789 0987654321.pdf", new Date(), JobResult.ERROR);
            PrintJobManager.createPrintJob(3, "this is a long file name without a new line.pdf", new Date(), JobResult.SUCCESSFUL);
            PrintJobManager.createPrintJob(4, "this is a long file name \n with a newline.pdf", new Date(), JobResult.ERROR);
            PrintJobManager.createPrintJob(5, "riso file.pdf", new Date(), JobResult.SUCCESSFUL);
            PrintJobManager.createPrintJob(6, "filename_with_underscore.pdf", new Date(), JobResult.SUCCESSFUL);
            PrintJobManager.createPrintJob(6, "android.pdf", new Date(), JobResult.ERROR);
            PrintJobManager.createPrintJob(6, "filename?!.pdf", new Date(), JobResult.SUCCESSFUL);
            PrintJobManager.createPrintJob(6, "a loooooooooong filename.pdf", new Date(), JobResult.ERROR);
            PrintJobManager.createPrintJob(7, "ANOTHER ALLCAPS.pdf", new Date(), JobResult.SUCCESSFUL);
            PrintJobManager.createPrintJob(7, "asdfgqwerty.pdf", new Date(), JobResult.ERROR);  
        }
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        
    }
    
    @Override
    public void setPrintJobsGroupView(PrintJobsGroupView printJobsGroupView) {
        this.mPrintGroupToDelete = printJobsGroupView;
    }
    
    @Override
    public void onClick(View v) {
        if (mPrintGroupToDelete != null)
            mPrintGroupToDelete.clearDeleteButton();
        
        super.onClick(v);
        
    }
    
    @Override
    public void clearButton() {
        if (mPrintGroupToDelete != null)
            mPrintGroupToDelete.clearDeleteButton();
    }
    
    private class LoadPrintJobsFromDB extends AsyncTask<Void, Void, Void> {
        
        @Override
        protected Void doInBackground(Void... arg0) {
            // for testing only
            initializePids();
            initializePJs();
            // ///////////////
            
            if (mPrintJobs.isEmpty()) {
                
                mPrintJobs = PrintJobManager.getPrintJobs();
                
                mPrinters = PrintJobManager.getPrintersWithJobs();
                
            }
            return null;
        }
        
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            
            mPrintJobColumnView = new PrintJobsColumnView(PrintJobsFragment.this.getActivity(), mPrintJobs, mPrinters, isTablet() ? isTabletLand() ? 3 : 2 : 1,
                    PrintJobsFragment.this, PrintJobsFragment.this);
            mPrintJobColumnView.setVisibility(View.INVISIBLE);
            mPrintJobContainer.addView(mPrintJobColumnView, 0);
        }
        
    }
    
    @Override
    public void hideLoading() {
        mPrintJobColumnView.setVisibility(View.VISIBLE);
        
        mPrintJobsLoadIndicator.setVisibility(View.INVISIBLE);
        
        //set onClickListener (for delete button reset) only after loading of views     
        mPrintJobsView.setOnClickListener(this);
        mPrintJobContainer.setOnClickListener(this);
    }
    
}
