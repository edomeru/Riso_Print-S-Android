package jp.co.riso.smartdeviceapp.view.jobs;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import jp.co.riso.smartdeviceapp.R;
import jp.co.riso.smartdeviceapp.model.PrintJob;
import jp.co.riso.smartdeviceapp.model.Printer;
import jp.co.riso.smartdeviceapp.view.jobs.PrintJobsGroupView.PrintDeleteListener;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

public class PrintJobsColumnView extends LinearLayout {
    List<PrintJobsGroupView> mprintJobsView = new ArrayList<PrintJobsGroupView>();
    private Context mContext;
    private List<PrintJob> mPrintJobs = new ArrayList<PrintJob>();
    private List<Printer> mPrinterIds = new ArrayList<Printer>();
    private List<LinearLayout> mColumns = new ArrayList<LinearLayout>(3);
    private PrintDeleteListener mDelListener;
    
    private int mColNum = 0;
    private int mJobGroupCtr = 0;
    private int mJobCtr = 0;
    
    private Runnable mRunnable;
    private WeakReference<LoadingViewListener> mLoadingListenerRef;
    private WeakReference<ReloadViewListener> mReloadListenerRef;
    
    private boolean mIsLoaded;
    
    public PrintJobsColumnView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }
    
    public PrintJobsColumnView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    
    public PrintJobsColumnView(Context context) {
        super(context);
        init(context);
    }
    
    public void setData(List<PrintJob> printJobs, List<Printer> printerIds, int colNum, PrintDeleteListener delListener, LoadingViewListener loadingListener,
            ReloadViewListener reloadListener) {
        this.mPrintJobs = printJobs;
        this.mPrinterIds = printerIds;
        this.mColNum = colNum;
        this.mDelListener = delListener;
        this.mLoadingListenerRef = new WeakReference<LoadingViewListener>(loadingListener);
        this.mReloadListenerRef = new WeakReference<ReloadViewListener>(reloadListener);
        updateColumns();
    }
    
    private void init(Context context) {
        this.mContext = context;
        if (!isInEditMode()) {
            
            LayoutInflater factory = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View viewgroup_printjobs = factory.inflate(R.layout.printjobs_column, this, true);
            
            mColumns.add((LinearLayout) viewgroup_printjobs.findViewById(R.id.column1));
            mColumns.add((LinearLayout) viewgroup_printjobs.findViewById(R.id.column2));
            mColumns.add((LinearLayout) viewgroup_printjobs.findViewById(R.id.column3));
            
            mRunnable = new AddViewRunnable();
        }
    }
    
    private void updateColumns() {
        
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        
        if (mColNum > 1) {
            lp.leftMargin = getResources().getDimensionPixelSize(R.dimen.printjob_column_margin_side);
            lp.rightMargin = getResources().getDimensionPixelSize(R.dimen.printjob_column_margin_side);
        }
        setLayoutParams(lp);
        
        if (mColNum < 3) {
            mColumns.get(2).setVisibility(GONE);
        }
        if (mColNum < 2) {
            mColumns.get(1).setVisibility(GONE);
        }
        
    }
    
    private void addToColumns() {
        
        List<PrintJob> jobs = new ArrayList<PrintJob>();
        Printer printer = mPrinterIds.get(mJobGroupCtr);
        int pid = printer.getId();
        // get printer's jobs list with printerid==pid
        // printJobs is ordered according to prn_id in query
        for (int i = mJobCtr; i < mPrintJobs.size(); i++) {
            
            PrintJob pj = mPrintJobs.get(i);
            int id = pj.getPrinterId();
            
            if (id == pid) {
                jobs.add(pj);
            }
            mJobCtr = i;
            
            // if current printer id is different from printer id of next print job in the list
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
            addToColumns();
            mJobGroupCtr++;
            post(mRunnable);
        } else {
            mIsLoaded = true;
        }
        
        if (mIsLoaded && mColNum > 0) {
            relayoutColumns();
        }
    }
    
    private void relayoutColumns() {
        
        if (checkIfNeedsRelayout()) {
            if (mReloadListenerRef != null && mReloadListenerRef.get() != null) {
                mIsLoaded = false;
                mJobGroupCtr = 0;
                mJobCtr = 0;
                for (int i = 0; i < mColNum; i++) {
                    mColumns.get(i).removeAllViews();
                    
                }
                
                mReloadListenerRef.get().reloadView();
            }
            
        }
    }
    
    private boolean checkIfNeedsRelayout() {
        boolean isColumnCleared = false;
        int childrenNum = 0;
        boolean isLeftCleared = false;
        
        for (int i = 0; i < mColNum; i++) {
            isColumnCleared |= (mColumns.get(i).getHeight() == 0);
            
            childrenNum += mColumns.get(i).getChildCount();
            if (i < mColNum - 1) {
                isLeftCleared |= (mColumns.get(i).getHeight() == 0);
            }
            
        }
        return isColumnCleared && (childrenNum >= mColNum || isLeftCleared);
    }
    
    public interface LoadingViewListener {
        public void hideLoading();
    }
    
    public interface ReloadViewListener {
        public void reloadView();
    }
    
    // http://stackoverflow.com/questions/5852758/views-inside-a-custom-viewgroup-not-rendering-after-a-size-change
    private class AddViewRunnable implements Runnable {
        
        @Override
        public void run() {
            requestLayout();
            if (mPrinterIds.size() > 0 && mJobGroupCtr >= mPrinterIds.size()) {
                if (mLoadingListenerRef != null && mLoadingListenerRef.get() != null) {
                    mLoadingListenerRef.get().hideLoading();
                }
                mPrintJobs.clear();
                mPrinterIds.clear();
            }
        }
        
    }
}
