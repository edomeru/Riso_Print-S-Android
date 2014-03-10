/*
 * Copyright (c) 2014 All rights reserved.
 *
 * PrintJobsFragment.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.smartdeviceapp.view.fragment;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import jp.co.riso.smartdeviceapp.R;
import jp.co.riso.smartdeviceapp.model.PrintJob;
import jp.co.riso.smartdeviceapp.model.Printer;
import jp.co.riso.smartdeviceapp.view.base.BaseFragment;
import jp.co.riso.smartdeviceapp.view.custom.PrintJobsColumnView;
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
        
        printerIds.add(new Printer(1, "1 printer"));
        printerIds.add(new Printer(2, "2 riso printer"));
        printerIds.add(new Printer(3, "3 RISO"));
        printerIds.add(new Printer(4, "4 riso"));
        printerIds.add(new Printer(5, "5 myprinter"));
        printerIds.add(new Printer(6, "6 printer"));
        printerIds.add(new Printer(7, "7 RISO printer"));
        return printerIds;
    }
    
    private List<PrintJob> initializePJs() {
        
        printJobs.add(new PrintJob(1, 1, "tst.pdf", new Date(2014, 03, 12), 1));
        printJobs.add(new PrintJob(2, 1, "tst3.pdf", new Date(2014, 04, 12), 1));
        printJobs.add(new PrintJob(3, 1, "test111.pdf", new Date(2014, 02, 01), 1));
        printJobs.add(new PrintJob(4, 1, "qqq.pdf", new Date(2014, 03, 12), 1));
        printJobs.add(new PrintJob(5, 2, "qwe.pdf", new Date(2014, 04, 12), 1));
        printJobs.add(new PrintJob(6, 2, "qwerty.pdf", new Date(2014, 02, 01), 1));
        
        printJobs.add(new PrintJob(7, 2, "111.pdf", new Date(2013, 12, 01), 1));
        printJobs.add(new PrintJob(8, 3, "TEST qqq.pdf", new Date(2013, 10, 12), 1));
        printJobs.add(new PrintJob(9, 3, "ADAWEAWqwe.pdf", new Date(2014, 04, 12), 1));
        printJobs.add(new PrintJob(10, 3, "qqwerty.pdf", new Date(2014, 02, 01), 1));
        printJobs.add(new PrintJob(11, 4, "1111.pdf", new Date(2013, 12, 01), 1));
        printJobs.add(new PrintJob(12, 4, "1TEST qqq.pdf", new Date(2013, 10, 12), 1));
        printJobs.add(new PrintJob(13, 5, "1ADAWEAWqwe.pdf", new Date(2014, 04, 12), 1));
        printJobs.add(new PrintJob(14, 6, "1qqwerty.pdf", new Date(2014, 02, 01), 1));
        printJobs.add(new PrintJob(15, 6, "1ADAWEAWqwe.pdf", new Date(2014, 04, 12), 1));
        printJobs.add(new PrintJob(16, 7, "1qqwerty.pdf", new Date(2014, 02, 01), 1));
        
        return printJobs;
    }
}
