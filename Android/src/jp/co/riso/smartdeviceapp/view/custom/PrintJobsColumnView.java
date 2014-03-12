package jp.co.riso.smartdeviceapp.view.custom;

import java.util.ArrayList;
import java.util.List;

import jp.co.riso.smartdeviceapp.R;
import jp.co.riso.smartdeviceapp.model.PrintJob;
import jp.co.riso.smartdeviceapp.model.Printer;
import jp.co.riso.smartdeviceapp.view.custom.PrintJobsGroupView.PrintDeleteListener;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

public class PrintJobsColumnView extends LinearLayout {
    
    private static final String TAG = "PrintJobsColumnView";
    private Context context;
    private int placeJobGroupCtr = 0;
    private List<PrintJob> printJobs = new ArrayList<PrintJob>();
    private List<Printer> printerIds = new ArrayList<Printer>();
    private List<LinearLayout> columns = new ArrayList<LinearLayout>(3);

    private boolean initial;
    private int colNum = 0;
    private PrintDeleteListener delListener;

    
    public PrintJobsColumnView(Context context, List<PrintJob> printJobs, List<Printer> printerIds, int colNum, PrintDeleteListener delListener) {
        this(context);
        this.context = context;
        this.printJobs = printJobs;
        this.printerIds = printerIds;
        this.colNum = colNum;
        this.delListener = delListener;
        init(context);
    }
    
    private PrintJobsColumnView(Context context) {
        super(context);
    }
    
    private void init(Context context) {

        initial = true;
        if (!isInEditMode()) {
            LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            if (colNum > 1)
                lp.setMargins(20, 0, 20, 0);
            
            setLayoutParams(lp);
            
            LayoutInflater factory = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View viewgroup_printjobs = factory.inflate(R.layout.view_printjobs, this, true);
            
            columns.add((LinearLayout) viewgroup_printjobs.findViewById(R.id.column1));
            columns.add((LinearLayout) viewgroup_printjobs.findViewById(R.id.column2));
            columns.add((LinearLayout) viewgroup_printjobs.findViewById(R.id.column3));
            
            if (colNum < 3)
                columns.get(2).setVisibility(GONE);
            if (colNum < 2)
                columns.get(1).setVisibility(GONE);
            
        }
    }
    
    private void addToColumns() {
        List<PrintJob> jobs = new ArrayList<PrintJob>();
        PrintJob pj = null;
        int pid = -1;
        pid = printerIds.get(placeJobGroupCtr).getPrinterId();
        for (int j = 0; j < printJobs.size(); j++) {
            
            pj = printJobs.get(j);
            if (pj.getPrinterId() == pid) {
                jobs.add(pj);
                Log.d(TAG, "addToColumns add " + j + " " + placeJobGroupCtr + pid);
                
            }
        }
        if (!jobs.isEmpty()) {
            
            int smallestColumn = 0;
            int tempHeight = columns.get(0).getHeight();
            Log.d(TAG, "smallest " + tempHeight + columns.get(1).getHeight());
            for (int i = 1; i < colNum; i++) {
                Log.d(TAG, i + "i " + columns.get(i).getHeight());
                if (columns.get(i).getHeight() < tempHeight) {
                    tempHeight = columns.get(i).getHeight();
                    Log.d(TAG, i + "smallest " + tempHeight);
                    smallestColumn = i;
                }
            }
            addPrintJobsGroupView(jobs, smallestColumn, printerIds.get(placeJobGroupCtr));
            
            jobs.clear();
        }
    }
        
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        Log.d(TAG, changed + "onLayout column " + initial + columns.get(0).getHeight() + " " + (r - l) + " " + (b - t));
        if (placeJobGroupCtr < printerIds.size()) {
            
            addToColumns();
            placeJobGroupCtr++;
            
            // http://stackoverflow.com/questions/5852758/views-inside-a-custom-viewgroup-not-rendering-after-a-size-change
            
            post(new Runnable() {
                public void run() {
                    requestLayout();
                }
            });
            
        }
    }
   
    private void addPrintJobsGroupView(List<PrintJob> jobsList, int column, Printer printer) {
        
        PrintJobsGroupView pjView;
        if (colNum == 1)
            pjView = new PrintJobsGroupView(context, jobsList, false, printer, delListener);
        else
            pjView = new PrintJobsGroupView(context, jobsList, true, printer, delListener);
        
        columns.get(column).addView(pjView);
        
    }
    
}
