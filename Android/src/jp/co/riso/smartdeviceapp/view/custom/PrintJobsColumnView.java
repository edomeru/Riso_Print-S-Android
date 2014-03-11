package jp.co.riso.smartdeviceapp.view.custom;

import java.util.ArrayList;
import java.util.List;

import jp.co.riso.smartdeviceapp.R;
import jp.co.riso.smartdeviceapp.controller.PrintJobManager;
import jp.co.riso.smartdeviceapp.model.PrintJob;
import jp.co.riso.smartdeviceapp.model.Printer;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class PrintJobsColumnView extends LinearLayout implements View.OnClickListener {
    
    private Context c;
    private int placeJobGroupCtr = -1;
    private List<PrintJob> printJobs = new ArrayList<PrintJob>();
    private List<Printer> printerIds = new ArrayList<Printer>();
    private List<LinearLayout> columns = new ArrayList<LinearLayout>(3);
    private int width;
    private int height;
    private boolean initial;
    private int colNum = 0;
    
    public void setData(List<PrintJob> printJobs, List<Printer> printerIds, int colNum) {
        this.printJobs = printJobs;
        this.printerIds = printerIds;
        this.colNum = colNum;
        if (colNum < 3)
            columns.get(2).setVisibility(GONE);
        if (colNum < 2)
            columns.get(1).setVisibility(GONE);
        
        reset();
    }
    
    public PrintJobsColumnView(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
        init(context);
        reset();
    }
    
    public PrintJobsColumnView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
        init(context);
        reset();
    }
    
    public PrintJobsColumnView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
        init(context);
        reset();
    }
    
    private void init(Context context) {
        c = context;
        
        initial = true;
        if (!isInEditMode()) {
            
            setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            
            LayoutInflater factory = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View viewgroup_printjobs = factory.inflate(R.layout.view_printjobs, this, true);
            
            columns.add((LinearLayout) viewgroup_printjobs.findViewById(R.id.column1));
            columns.add((LinearLayout) viewgroup_printjobs.findViewById(R.id.column2));
            columns.add((LinearLayout) viewgroup_printjobs.findViewById(R.id.column3));
            
        }
    }
    
    private void reset() {
        if (printerIds.size() < 1 || placeJobGroupCtr == -1)
            return;
        
        Log.d("CESTEST", "reset " + placeJobGroupCtr + " " + printerIds.size());
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
                        Log.d("CESTEST", "reset add " + j + " " + i + pid);
                        
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
                    Log.d("CESTEST", "reset add " + j + " " + placeJobGroupCtr + pid);
                    
                }
            }
            if (!jobs.isEmpty()) {
                
                int smallestColumn = 0;
                int tempHeight = columns.get(0).getHeight();
                Log.d("CESTEST", "smallest " + tempHeight + columns.get(1).getHeight());
                for (int i = 1; i < colNum; i++) {
                    Log.d("CESTEST", i + "i " + columns.get(i).getHeight());
                    if (columns.get(i).getHeight() < tempHeight) {
                        tempHeight = columns.get(i).getHeight();
                        Log.d("CESTEST", i + "smallest " + tempHeight);
                        smallestColumn = i;
                    }
                }
                addPrintJobsGroupView(jobs, smallestColumn, printerIds.get(placeJobGroupCtr));
                
                jobs.clear();
            }
            // }else{
            // if (printerIds.size()>0) initial = false;
            //
            // }
            // int margin = 600;
            // Log.d("CESTEST", "layout"+ " " + getLeft() + getTop() + width + height + getHeight());
            
            // if (printerIds.size()>0) //layout(getLeft(), getTop(), width, height);
            // layout(getLeft(), getTop(), width, height-200);// fix get computed height of printjobsgroupview. how to
            // get the height?
        }
    }
    
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) { // TODO Auto-generated method stub final
    
        Log.d("CESTEST", "onMeasure" + widthMeasureSpec);
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
        Log.d("CESTEST", changed + "onLayout column " + initial + columns.get(0).getHeight() + " " + (r - l) + " " + (b - t));
        if (placeJobGroupCtr < printerIds.size() - 1) {
            
            placeJobGroupCtr++;
            Log.d("CESTEST", "onSizeChanged1 column " + placeJobGroupCtr + " " + columns.get(0).getHeight() + " " + width + " " + height);
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
        Log.d("CESTEST", printerIds.size() + "onSizeChanged column " + placeJobGroupCtr + " " + columns.get(0).getHeight() + " " + width + " " + height);
        // reset();
        // if (placeJobGroupCtr < printerIds.size()-1){
        //
        //
        // placeJobGroupCtr++;
        // // Log.d("CESTEST", "onSizeChanged1 column " + placeJobGroupCtr + " " + column1.getHeight() + " " + width +
        // " " +height);
        // Log.d("CESTEST", "onSizeChanged1 column " + placeJobGroupCtr + " " + columns.get(0).getHeight() + " " + width
        // + " " +height);
        // reset();
        // //http://stackoverflow.com/questions/5852758/views-inside-a-custom-viewgroup-not-rendering-after-a-size-change
        // post(new Runnable() {
        // public void run() {
        // requestLayout();
        // }
        // });
        // // placeJobGroupCtr++;
        
        // }
        
    }
    
    private void addPrintJobsGroupView(List<PrintJob> jobsList, int column, Printer printer) {
        PrintJobsGroupView pjView = new PrintJobsGroupView(getContext());
        if (column == 1)
            pjView.setData(jobsList, false, printer, this);
        else
            pjView.setData(jobsList, true, printer, this);
        
        columns.get(column).addView(pjView);
        
//        Log.d("CESTEST", pjView.getViewHeight() + "addVieww" + " " + column + " " + pjView.getHeight());
        
    }
    
    @Override
    public void onClick(View v) {
//        for (int i = 0; i < getChildCount(); i++) {
//            if (getChildAt(i).getClass().equals(PrintJobsGroupView.class)) {
//                Log.d("CESTEST", "OMG");
//                Toast.makeText(c, "OMG", Toast.LENGTH_SHORT).show();
//                PrintJobsGroupView a = (PrintJobsGroupView)getChildAt(i);
//
//            }
//            
//        }
//        
//        // TODO Auto-generated method stub
//        if (v.getId() == R.id.printJobGroupDelete) {
//            Log.d("CESTEST", "delete printgroup");
//            Toast.makeText(c, "delete group", Toast.LENGTH_SHORT).show();
//            PrintJobManager.deleteWithPrinterId(((Printer) v.getTag()).getPrinterId());
//            // reset screen
//        } else {
//            if (v.getId() == R.id.printJobDeleteBtn) {
//                Log.d("CESTEST", "delete printJobView");
//                
//                PrintJobManager.deleteWithJobId(((PrintJob) v.getTag()).getId());
//                Toast.makeText(c, "delete " + ((PrintJob) v.getTag()).getId() + ((PrintJob) v.getTag()).getName(), Toast.LENGTH_SHORT).show();
//                // reset screen
//            }
//            
//        }
    }
    
    public void toggleDeleteButton() {
        
    }
}
