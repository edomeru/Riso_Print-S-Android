package jp.co.riso.smartdeviceapp.view.custom;

import java.util.ArrayList;
import java.util.List;

import jp.co.riso.smartdeviceapp.R;
import jp.co.riso.smartdeviceapp.model.PrintJob;
import jp.co.riso.smartdeviceapp.model.Printer;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;


public class PrintJobsColumnView extends LinearLayout implements View.OnClickListener {
    
    private static final String TAG = "PrintJobsColumnView";
    private Context context;
    private int placeJobGroupCtr = -1;
    private List<PrintJob> printJobs = new ArrayList<PrintJob>();
    private List<Printer> printerIds = new ArrayList<Printer>();
    private List<LinearLayout> columns = new ArrayList<LinearLayout>(3);
    private int width;
    private int height;
    private boolean initial;
    private int colNum = 0;
    
    public PrintJobsColumnView(Context context, List<PrintJob> printJobs, List<Printer> printerIds, int colNum) {
        super(context);
        // TODO Auto-generated constructor stub
        this.printJobs = printJobs;
        this.printerIds = printerIds;
        this.colNum = colNum;
        
        init(context);
        
        reset();
    }
    
    // public void setData(List<PrintJob> printJobs, List<Printer> printerIds, int colNum) {
    // this.printJobs = printJobs;
    // this.printerIds = printerIds;
    // this.colNum = colNum;
    // if (colNum < 3)
    // columns.get(2).setVisibility(GONE);
    // if (colNum < 2)
    // columns.get(1).setVisibility(GONE);
    //
    // reset();
    // }
    //
    // public PrintJobsColumnView(Context context) {
    // super(context);
    // // TODO Auto-generated constructor stub
    // init(context);
    // reset();
    // }
    //
    // public PrintJobsColumnView(Context context, AttributeSet attrs, int defStyle) {
    // super(context, attrs, defStyle);
    // // TODO Auto-generated constructor stub
    // init(context);
    // reset();
    // }
    //
    // public PrintJobsColumnView(Context context, AttributeSet attrs) {
    // super(context, attrs);
    // // TODO Auto-generated constructor stub
    // init(context);
    // reset();
    // }
    
    private void init(Context context) {
        this.context = context;
        
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
    
    private void reset() {
        if (printerIds.size() < 1 || placeJobGroupCtr == -1)
            return;
        
        Log.d(TAG, "reset " + placeJobGroupCtr + " " + printerIds.size());
        List<PrintJob> jobs = new ArrayList<PrintJob>();
        PrintJob pj = null;
        int pid = -1;
        if (placeJobGroupCtr == 0) {
            placeJobGroupCtr = colNum - 1;
            for (int i = 0; i < colNum; i++) {
                pid = printerIds.get(i).getPrinterId();
                for (int j = 0; j < printJobs.size(); j++) {
                    
                    pj = printJobs.get(j);
                    if (pj.getPrinterId() == pid) {
                        jobs.add(pj);
                        Log.d(TAG, "reset add " + j + " " + i + pid);
                        
                    }
                }
                if (!jobs.isEmpty()) {
                    addPrintJobsGroupView(jobs, i, printerIds.get(i));
                    
                    jobs.clear();
                }
            }
        } else {
            pid = printerIds.get(placeJobGroupCtr).getPrinterId();
            for (int j = 0; j < printJobs.size(); j++) {
                
                pj = printJobs.get(j);
                if (pj.getPrinterId() == pid) {
                    jobs.add(pj);
                    Log.d(TAG, "reset add " + j + " " + placeJobGroupCtr + pid);
                    
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
    }
    
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) { // TODO Auto-generated method stub final
    
        Log.d(TAG, "onMeasure" + widthMeasureSpec);
        int computedWSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
        int computedHSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
        for (int i = 0; i < getChildCount(); i++) {
            View v = getChildAt(i);
            v.measure(computedWSpec, computedHSpec);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
    
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        Log.d(TAG, changed + "onLayout column " + initial + columns.get(0).getHeight() + " " + (r - l) + " " + (b - t));
        if (placeJobGroupCtr < printerIds.size() - 1) {
            
            placeJobGroupCtr++;
            Log.d(TAG, "onSizeChanged1 column " + placeJobGroupCtr + " " + columns.get(0).getHeight() + " " + width + " " + height);
            reset();
            // http://stackoverflow.com/questions/5852758/views-inside-a-custom-viewgroup-not-rendering-after-a-size-change
            post(new Runnable() {
                public void run() {
                    requestLayout();
                }
            });
        }
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);
        width = w;
        height = h;
        Log.d(TAG, printerIds.size() + "onSizeChanged column " + placeJobGroupCtr + " " + columns.get(0).getHeight() + " " + width + " " + height);

        
    }
    
    private void addPrintJobsGroupView(List<PrintJob> jobsList, int column, Printer printer) {
        // PrintJobsGroupView pjView = new PrintJobsGroupView(getContext());
        // if (colNum == 1)
        // pjView.setData(jobsList, false, printer, this);
        // else
        // pjView.setData(jobsList, true, printer, this);
        
        PrintJobsGroupView pjView;
        if (colNum == 1)
            pjView = new PrintJobsGroupView(context, jobsList, false, printer, this);
        else
            pjView = new PrintJobsGroupView(context, jobsList, true, printer, this);
        
        columns.get(column).addView(pjView);
        
        // Log.d(TAG, pjView.getViewHeight() + "addVieww" + " " + column + " " + pjView.getHeight());
        
    }
    
    @Override
    public void onClick(View v) {
  
    }
    
    public void toggleDeleteButton() {
        
    }
}
