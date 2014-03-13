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
import jp.co.riso.smartdeviceapp.view.custom.PrintJobsGroupView;
import jp.co.riso.smartdeviceapp.view.custom.PrintJobsGroupView.PrintDeleteListener;
import android.content.ContentValues;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PrintJobsFragment extends BaseFragment implements PrintDeleteListener, OnClickListener {
        
    private PrintJobsColumnView printJobColumnView;
    private List<PrintJob> printJobs = new ArrayList<PrintJob>();
    private List<Printer> printers = new ArrayList<Printer>();
    private LinearLayout rootView;
    private PrintJobsGroupView printGroupToDelete;
    
    public PrintJobsFragment() {
        super();
    }
    
    @Override
    public int getViewLayout() {
        return R.layout.fragment_printjobs;
    }
    
    @Override
    public void initializeFragment(Bundle savedInstanceState) {
        PrintJobManager.initializeInstance(getActivity());
    }
    
    @Override
    public void initializeView(View view, Bundle savedInstanceState) {
        
        rootView = (LinearLayout) view.findViewById(R.id.rootView);
        view.setOnClickListener(this);
        rootView.setOnClickListener(this);
        
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
        
        // for testing only
        initializePids();
        initializePJs();
        // ///////////////
        
        if (printJobs.isEmpty()) {
            
            printJobs = PrintJobManager.getPrintJobs();
            
            printers = PrintJobManager.getPrinters();
            
            printJobColumnView = new PrintJobsColumnView(getActivity(), printJobs, printers, isTablet() ? isTabletLand() ? 3 : 2 : 1, this);
            rootView.removeAllViews();
            rootView.addView(printJobColumnView);
            
        }
        
    }
    
    private void initializePids() {
        
        // for testing only
        
        DatabaseManager manager = new DatabaseManager(getActivity());
        ContentValues pvalues = new ContentValues();
        
        if (PrintJobManager.getPrinters().isEmpty()) {
            
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
        this.printGroupToDelete = printJobsGroupView;
    }
    
    @Override
    public void onClick(View v) {
        if (printGroupToDelete != null)
            printGroupToDelete.clearDeleteButton();
        
        super.onClick(v);
        
    }
    
    @Override
    public void clearButton() {
        if (printGroupToDelete != null)
            printGroupToDelete.clearDeleteButton();
    }
    
}
