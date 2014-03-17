package jp.co.riso.smartdeviceapp.view.custom;

import java.util.ArrayList;
import java.util.List;

import jp.co.riso.smartdeviceapp.R;
import jp.co.riso.smartdeviceapp.model.PrintJob;
import jp.co.riso.smartdeviceapp.model.Printer;
import jp.co.riso.smartdeviceapp.view.custom.PrintJobsGroupView.PrintDeleteListener;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

public class PrintJobsColumnView extends LinearLayout {
    
    private Context mContext;
    private List<PrintJob> mPrintJobs = new ArrayList<PrintJob>();
    private List<Printer> mPrinterIds = new ArrayList<Printer>();
    private List<LinearLayout> mColumns = new ArrayList<LinearLayout>(3);
    private PrintDeleteListener mDelListener;
    
    private int mColNum = 0;
    private int mJobGroupCtr = 0;
    private int mJobCtr = 0;
    
    private Runnable mRunnable;
    private LoadingViewListener mLoadingListener;
    
    public PrintJobsColumnView(Context context, List<PrintJob> printJobs, List<Printer> printerIds, int colNum, PrintDeleteListener delListener,
            LoadingViewListener loadingListener) {
        this(context);
        this.mContext = context;
        this.mPrintJobs = printJobs;
        this.mPrinterIds = printerIds;
        this.mColNum = colNum;
        this.mDelListener = delListener;
        this.mLoadingListener = loadingListener;
        this.mRunnable = new Runnable() {
            public void run() {
                addToColumns();
                mJobGroupCtr++;
                requestLayout();
                if (mJobGroupCtr >= mPrinterIds.size()) {
                    mLoadingListener.hideLoading();
                    mPrintJobs.clear();
                    mPrinterIds.clear();
                    mColumns.clear();
                }
            }
        };
        
        init(context);
    }
    
    private PrintJobsColumnView(Context context) {
        super(context);
    }
    
    private void init(Context context) {
        
        if (!isInEditMode()) {
            LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            
            if (mColNum > 1) {
                lp.leftMargin = getResources().getDimensionPixelSize(R.dimen.printjob_column_margin_side);
                lp.rightMargin = getResources().getDimensionPixelSize(R.dimen.printjob_column_margin_side);
            }
            setLayoutParams(lp);
            
            LayoutInflater factory = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View viewgroup_printjobs = factory.inflate(R.layout.view_printjobs, this, true);
            
            mColumns.add((LinearLayout) viewgroup_printjobs.findViewById(R.id.column1));
            mColumns.add((LinearLayout) viewgroup_printjobs.findViewById(R.id.column2));
            mColumns.add((LinearLayout) viewgroup_printjobs.findViewById(R.id.column3));
            
            if (mColNum < 3)
                mColumns.get(2).setVisibility(GONE);
            if (mColNum < 2)
                mColumns.get(1).setVisibility(GONE);
            
        }
        
    }
    
    private void addToColumns() {
        
        List<PrintJob> jobs = new ArrayList<PrintJob>();
        Printer printer = mPrinterIds.get(mJobGroupCtr);
        int pid = printer.getPrinterId();
        // get printer's jobs list with printerid==pid
        // printJobs is ordered according to prn_id in query
        for (int i = mJobCtr; i < mPrintJobs.size(); i++) {
            
            PrintJob pj = mPrintJobs.get(i);
            int id = pj.getPrinterId();
            
            if (id == pid) {
                jobs.add(pj);
            }
            mJobCtr = i;
            
            //if current printer id is different from printer id of next print job in the list
            if (i == mPrintJobs.size() - 1 || pid != mPrintJobs.get(i + 1).getPrinterId()) {
                break;
            }
        }
        
        // use jobs list to add view to smallest column
        if (!jobs.isEmpty()) {
            addPrintJobsGroupView(jobs, getSmallestColumn(), printer);
            jobs.clear();
        }
        jobs = null;
        printer = null;
        
        // if # of columns == 1, no need to depend on change in column size
        if (mColNum == 1) {
            mJobGroupCtr++;
            if (mJobGroupCtr < mPrinterIds.size())
                addToColumns();
        }
        
    }
    
    private int getSmallestColumn() {
        // initially assign to 1st column
        int smallestColumn = 0;
        int tempHeight = mColumns.get(smallestColumn).getHeight();
        
        for (int i = 1; i < mColNum; i++) {
            if (mColumns.get(i).getHeight() < tempHeight) {
                tempHeight = mColumns.get(i).getHeight();
                smallestColumn = i;
            }
        }
        return smallestColumn;
    }
    
    private void addPrintJobsGroupView(List<PrintJob> jobsList, int column, Printer printer) {
        
        PrintJobsGroupView pjView;
        if (mColNum == 1)
            pjView = new PrintJobsGroupView(mContext, jobsList, false, printer, mDelListener);
        else
            pjView = new PrintJobsGroupView(mContext, jobsList, true, printer, mDelListener);
        
        mColumns.get(column).addView(pjView);
        
    }
    
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (mJobGroupCtr < mPrinterIds.size() && mJobCtr < mPrintJobs.size()) {
            // http://stackoverflow.com/questions/5852758/views-inside-a-custom-viewgroup-not-rendering-after-a-size-change
            post(mRunnable);
        }
    }
    
    public interface LoadingViewListener {
        public void hideLoading();
    }
    
}
