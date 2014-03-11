/*
 * Copyright (c) 2014 All rights reserved.
 *
 * PrintJobsFragment.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.smartdeviceapp.view.fragment;

import java.util.Date;
import java.util.ArrayList;
import java.util.List;

import jp.co.riso.smartdeviceapp.R;
import jp.co.riso.smartdeviceapp.controller.PrintJobManager;
import jp.co.riso.smartdeviceapp.controller.db.DatabaseManager;
import jp.co.riso.smartdeviceapp.model.PrintJob;
import jp.co.riso.smartdeviceapp.model.PrintJob.JobResult;
import jp.co.riso.smartdeviceapp.model.Printer;
import jp.co.riso.smartdeviceapp.view.base.BaseFragment;
import jp.co.riso.smartdeviceapp.view.custom.PrintJobsColumnView;
import android.content.ContentValues;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PrintJobsFragment extends BaseFragment {
    
    private PrintJobsColumnView printJobColumnView;
    private List<PrintJob> printJobs = new ArrayList<PrintJob>();
    private List<Printer> printerIds = new ArrayList<Printer>();
    private LinearLayout rootView;
    
    @Override
    public int getViewLayout() {
        return R.layout.fragment_printjobs;
    }
    
    @Override
    public void initializeFragment(Bundle savedInstanceState) {
        
    }
    
    @Override
    public void initializeView(View view, Bundle savedInstanceState) {
        
        rootView = (LinearLayout) view.findViewById(R.id.rootView);
        rootView.removeAllViews();
        printJobColumnView = new PrintJobsColumnView(getActivity());
        
    }
    
    @Override
    public void initializeCustomActionBar(View view, Bundle savedInstanceState) {
        TextView textView = (TextView) view.findViewById(R.id.actionBarTitle);
        textView.setText(R.string.ids_lbl_print_jobs);
        
        addActionMenuButton(view);
    }
    
    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        
        printJobColumnView.setData(initializePJs(), initializePids(), isTablet() ? isTabletLand() ? 3 : 2 : 1);
        
        rootView.addView(printJobColumnView);
        
    }
    
    private List<Printer> initializePids() {
        // for testing only
        DatabaseManager manager = new DatabaseManager(getActivity());
        ContentValues pvalues = new ContentValues();
        pvalues.put("printer_id", 1);
        pvalues.put("prn_name", "myprintername");
        pvalues.put("prn_port_setting", 0);
        
        manager.insert("Printer", "true", pvalues);
        
        
        pvalues.put("printer_id", 2);
        pvalues.put("prn_name", "riso printer");
        pvalues.put("prn_port_setting", 0);
        
        manager.insert("Printer", "true", pvalues);
        
        pvalues.put("printer_id", 3);
        pvalues.put("prn_name", "RISO");
        pvalues.put("prn_port_setting", 0);
        
        manager.insert("Printer", "true", pvalues);
        
        pvalues.put("printer_id", 4);
        pvalues.put("prn_name", "printer");
        pvalues.put("prn_port_setting", 0);
        
        manager.insert("Printer", "true", pvalues);

        pvalues.put("printer_id", 5);
        pvalues.put("prn_name", "printer5");
        pvalues.put("prn_port_setting", 0);
        
        manager.insert("Printer", "true", pvalues);

        
        // ////////////////////////////
        
        printerIds.add(new Printer(1, "myprintername"));
        printerIds.add(new Printer(2, "riso printer"));
        printerIds.add(new Printer(3, "RISO"));
        printerIds.add(new Printer(4, "printer"));
        printerIds.add(new Printer(5, "printer5"));
        return printerIds;
    }
    
    private List<PrintJob> initializePJs() {
        PrintJobManager.initializeInstance(getActivity());
//        PrintJobManager.createPrintJob(1, "tst.pdf", "2014/3/1 10:20", 1);
//        PrintJobManager.createPrintJob(1, "tst3.pdf", "2014/13/1 10:20", 1);
//        PrintJobManager.createPrintJob(1, "test111.pdf", "2014/3/31 14:20", 1);
//        PrintJobManager.createPrintJob(1, "qqq.pdf", "2014/12/12 23:20", 1);
//        PrintJobManager.createPrintJob(2, "qwe.pdf", "2014/11/26 24:20", 1);
//        PrintJobManager.createPrintJob(2, "qwerty.pdf", "2014/4/5 10:20", 1);
//        
//        PrintJobManager.createPrintJob(2, "111.pdf", "2014/5/4 10:20", 1);
//        PrintJobManager.createPrintJob(3, "TEST qqq.pdf", "2014/2/1 10:20", 1);
//        PrintJobManager.createPrintJob(3, "ADAWEAWqwe.pdf", "2014/3/1 10:20", 1);
//        PrintJobManager.createPrintJob(3, "qqwerty.pdf", "2014/3/1 10:20", 1);
//        PrintJobManager.createPrintJob(4, "1111.pdf", "2014/3/1 10:20", 1);
//        PrintJobManager.createPrintJob(4, "1TEST qqq.pdf", "2014/3/1 10:20", 1);
//        PrintJobManager.createPrintJob(5, "1ADAWEAWqwe.pdf", "2014/3/1 10:20", 1);
//        PrintJobManager.createPrintJob(6, "1qqwerty.pdf", "2014/3/1 10:20", 1);
//        PrintJobManager.createPrintJob(6, "1ADAWEAWqwe.pdf", "2014/3/1 10:20", 1);
//        PrintJobManager.createPrintJob(7, "1qqwerty.pdf", PrintJobManager.convertToDateTime(new Date()), 1);
        
        PrintJobManager.createPrintJob(1, "tst.pdf",new Date(), JobResult.SUCCESSFUL);
        PrintJobManager.createPrintJob(1, "tst3.pdf", new Date(), JobResult.ERROR);
        PrintJobManager.createPrintJob(1, "test111.pdf", new Date(), JobResult.SUCCESSFUL);
        PrintJobManager.createPrintJob(1, "qqq.pdf", new Date(),JobResult.ERROR);
        PrintJobManager.createPrintJob(2, "qwe.pdf", new Date(), JobResult.SUCCESSFUL);
        PrintJobManager.createPrintJob(2, "qwerty.pdf", new Date(),JobResult.SUCCESSFUL);
        
        PrintJobManager.createPrintJob(2, "111.pdf", new Date(),JobResult.SUCCESSFUL);
        PrintJobManager.createPrintJob(3, "TEST qqq.pdf", new Date(), JobResult.ERROR);
        PrintJobManager.createPrintJob(3, "ADAWEAWqwe.pdf", new Date(), JobResult.SUCCESSFUL);
        PrintJobManager.createPrintJob(3, "qqwerty.pdf", new Date(), JobResult.ERROR);
        PrintJobManager.createPrintJob(4, "1111.pdf", new Date(), JobResult.SUCCESSFUL);

        
        
//        printJobs.add(new PrintJob(1, 1, "tst.pdf", "2014/3/1 10:20", 1));
//        printJobs.add(new PrintJob(2, 1, "tst3.pdf", "2014/3/1 10:20", 1));
//        printJobs.add(new PrintJob(3, 1, "test111.pdf", "2014/3/1 10:20", 1));
//        printJobs.add(new PrintJob(4, 1, "qqq.pdf", "2014/3/1 10:20", 1));
//        printJobs.add(new PrintJob(5, 2, "qwe.pdf", "2014/3/1 10:20", 1));
//        printJobs.add(new PrintJob(6, 2, "qwerty.pdf", "2014/3/1 10:20", 1));
//        
//        printJobs.add(new PrintJob(7, 2, "111.pdf", "2014/3/1 10:20", 1));
//        printJobs.add(new PrintJob(8, 3, "TEST qqq.pdf", "2014/3/1 10:20", 1));
//        printJobs.add(new PrintJob(9, 3, "ADAWEAWqwe.pdf", "2014/3/1 10:20", 1));
//        printJobs.add(new PrintJob(10, 3, "qqwerty.pdf", "2014/3/1 10:20", 1));
//        printJobs.add(new PrintJob(11, 4, "1111.pdf", "2014/3/1 10:20", 1));
//        printJobs.add(new PrintJob(12, 4, "1TEST qqq.pdf", "2014/3/1 10:20", 1));
//        printJobs.add(new PrintJob(13, 5, "1ADAWEAWqwe.pdf", "2014/3/1 10:20", 1));
//        printJobs.add(new PrintJob(14, 6, "1qqwerty.pdf", "2014/3/1 10:20", 1));
//        printJobs.add(new PrintJob(15, 6, "1ADAWEAWqwe.pdf", "2014/3/1 10:20", 1));
//        printJobs.add(new PrintJob(16, 7, "1qqwerty.pdf", "2014/3/1 10:20", 1));
        
        return PrintJobManager.getPrintJobs();
    }
}
