/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * PrintJobsView.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.view.jobs;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import jp.co.riso.android.util.AppUtils;
import jp.co.riso.smartdeviceapp.model.PrintJob;
import jp.co.riso.smartdeviceapp.model.Printer;
import jp.co.riso.smartdeviceapp.view.anim.DisplayDeleteAnimation;
import jp.co.riso.smartdeviceapp.view.jobs.PrintJobsGroupView.PrintJobsGroupListener;
import jp.co.riso.smartdeviceapp.view.jobs.PrintJobsGroupView.PrintJobsLayoutListener;
import jp.co.riso.smartprint.R;
import androidx.fragment.app.FragmentActivity;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;

/**
 * @class PrintJobsView
 * 
 * @brief Custom view for the Print Job History view
 */
public class PrintJobsView extends LinearLayout implements PrintJobsLayoutListener {
    private static final int SWIPE_THRESHOLD = 50;
    private static final int MIN_COLUMNS = 2;
    
    private WeakReference<PrintJobsViewListener> mListenerRef;
    private List<PrintJob> mPrintJobs = new ArrayList<PrintJob>();
    private List<Printer> mPrinters = new ArrayList<Printer>();
    private List<Printer> mCollapsedPrinters = new ArrayList<Printer>();
    private List<LinearLayout> mColumns = new ArrayList<LinearLayout>();
    private List<PrintJobsGroupView> mPrintGroupViews = new ArrayList<PrintJobsGroupView>();
    private PrintJobsGroupView mPrintGroupWithDelete;
    private DisplayDeleteAnimation mDeleteAnimation;
    private PrintJobsGroupListener mGroupListener;
    private PrintJob mPrintJobToDelete;
    private Printer mPrinterToDelete;
    private Runnable mRunnable;
    private int mGroupViewCtr;
    private boolean mInitialFlag;
    private boolean mDeleteMode = false;
    private View mDeleteView = null;
    private Point mDownPoint;
    private int[] mColumnsHeight;
    private ViewCreationThread mThread;
    
    /**
     * @brief Default Constructor
     * 
     * @param context Activity context
     * @param attrs AttributeSet
     * @param defStyle Default Style
     */
    public PrintJobsView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }
    
    /**
     * @brief Default Constructor
     * 
     * @param context Activity context
     * @param attrs AttributeSet
     */
    public PrintJobsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    /**
     * @brief Default Constructor
     * 
     * @param context Activity context
     */
    public PrintJobsView(Context context) {
        super(context);
        init();
    }
    
    /**
     * @brief Sets Data for PrintJobsView and resets view according to this data.
     * 
     * @param printJobs List of print jobs
     * @param printers List of printer objects
     * @param groupListener PrintJobsGroupListener
     * @param viewListener PrintJobsViewListener
     */
    public void setData(List<PrintJob> printJobs, List<Printer> printers, PrintJobsGroupListener groupListener, PrintJobsViewListener viewListener) {
        this.mPrintJobs = new ArrayList<PrintJob>(printJobs);
        this.mPrinters = new ArrayList<Printer>(printers);
        this.mGroupListener = groupListener;
        this.mListenerRef = new WeakReference<PrintJobsViewListener>(viewListener);
        reset();
    }
    
    /**
     * @brief Begins delete mode, i.e. displays the delete button.
     * 
     * @param pj Print jobs group view containing the delete button to be displayed
     * @param view Job row layout view containing the delete button to be displayed
     * @param animate If true, delete button is displayed with animation 
     */
    public void beginDelete(PrintJobsGroupView pj, View view, boolean animate) {
        if (!mDeleteMode) {
            mDeleteMode = true;
            mDeleteAnimation.beginDeleteModeOnView(view, animate, R.id.printJobDeleteBtn, R.id.printJobDate);
            mDeleteView = view;
            pj.setDeleteButton(view);
            mPrintGroupWithDelete = pj;
        }
    }
    
    /**
     * @brief Ends delete mode, i.e. hides the delete button.
     * 
     * @param animate If true, delete button is hidden with animation 
     */
    public void endDelete(boolean animate) {
        if (mDeleteMode) {
            mDeleteMode = false;
            mDeleteAnimation.endDeleteMode(mDeleteView, animate, R.id.printJobDeleteBtn, R.id.printJobDate);
            mDeleteView = null;
            mPrintGroupWithDelete.clearDeleteButton();
        }
    }
    
    /**
     * @brief Resets the PrintJobsView.
     */
    public void reset() {
        mDeleteMode = false;
        mGroupViewCtr = 0;
        mPrintGroupViews.clear();

        mInitialFlag = true;
        removeAllViews();
        mColumns.clear();
        
        groupPrintJobs();
    }
    
    /**
     * @brief Sets print job to be deleted.
     * 
     * @param job PrintJob to be deleted
     */
    public void setJobToDelete(PrintJob job) {
        mPrintJobToDelete = job;
    }
    
    /**
     * @brief Set print job group to be deleted.
     * 
     * @param printer Printer of the job group to be deleted
     */
    public void setPrinterToDelete(Printer printer) {
        mPrinterToDelete = printer;
    }
    
    /**
     * @brief Set collapse state of the print job group.
     * 
     * @param printer Printer object of the print job group to be collapsed/expanded
     * @param isCollapsed Collapse state
     */
    public void setCollapsedPrinters(Printer printer, boolean isCollapsed) {
        if (isCollapsed) {
            mCollapsedPrinters.add(printer);
        } else {
            mCollapsedPrinters.remove(printer);
        }
    }
    
    /**
     * @brief Deletes PrintJob from list.
     * 
     * @param job PrintJob to be deleted
     */
    public void deleteJobFromList(PrintJob job) {
        mPrintJobs.remove(job);
    }
    
    /**
     * @brief Deletes printer from list
     * 
     * @param printer Printer to be deleted
     */
    public void deletePrinterFromList(Printer printer) {
        mPrinters.remove(printer);
    }
    
    /**
     * @brief Initializes PrintJobsView.
     */
    private void init() {
        mDeleteAnimation = new DisplayDeleteAnimation();
        mRunnable = new AddViewRunnable();
    }
   
    /**
     * @brief Creates a thread for grouping print jobs and creating views for each group
     */
    private void groupPrintJobs() {
        if (mThread != null && mThread.isAlive()) {
            mThread.interrupt();
            mThread.setIsRunning(false);
        }
        
        mThread = new ViewCreationThread();
        mThread.start();
    }
    
    /**
     * @brief Gets the column with the least height.
     * 
     * @return Index of the smallest column
     */
    private int getSmallestColumn() {
        // initially assign to 1st column
        int smallestColumn = 0;
        int tempHeight = mColumnsHeight[smallestColumn];
        
        for (int i = 1; i < mColumnsHeight.length; i++) {
            if (mColumnsHeight[i] < tempHeight) {
                tempHeight = mColumnsHeight[i];
                smallestColumn = i;
            }
        }
        return smallestColumn;
    }
    
    /**
     * @brief Creates PrintJobsGroupView using printer and print jobs.
     * 
     * @param jobsList List of the PrintJobs as the items of the print job group
     * @param printer Printer object as the header of the print job group
     */
    private PrintJobsGroupView createPrintJobsView(List<PrintJob> jobsList, Printer printer) {
        PrintJobsGroupView pjView = new PrintJobsGroupView(getContext());
        pjView.setData(jobsList, printer, mGroupListener, PrintJobsView.this);
        restoreUIstate(pjView, printer);
        return pjView;
    }
    
    /**
     * @brief Restores the UI state of a PrintJobsGroupView.
     * 
     * @param pj PrintJobsGroupView to be restored
     * @param printer Printer to be restored
     */
    private void restoreUIstate(PrintJobsGroupView pj, Printer printer) {
        boolean isCollapsed = mCollapsedPrinters.contains(printer);
        boolean isDeleteShown = mPrintJobToDelete != null && mPrintJobs.contains(mPrintJobToDelete);

        if (isDeleteShown) {
            View v = pj.findViewWithTag(mPrintJobToDelete);
            if (v != null) {
                beginDelete(pj, v, false);
                pj.restoreState(isCollapsed, mPrinterToDelete, mPrintJobToDelete);
            } else {
                pj.restoreState(isCollapsed, mPrinterToDelete, null);
            }
        } else {
            pj.restoreState(isCollapsed, mPrinterToDelete, null);
        }
    }
    
    /**
     * @brief Adds PrintJobsGroupView in columns.
     * 
     * @param pjView PrintJobsGroupView to be added
     */
    private void placeInColumns(PrintJobsGroupView pjView) {
        int padding = getResources().getDimensionPixelSize(R.dimen.printjob_padding_side);
        
        LayoutParams groupParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        if (getResources().getBoolean(R.bool.is_tablet)) {
            groupParams.topMargin = getResources().getDimensionPixelSize(R.dimen.printjob_margin_top);
            pjView.setPadding(padding, 0, padding, 0);
        }
        pjView.setOrientation(VERTICAL);
        
        int col = getSmallestColumn();
        mColumns.get(col).addView(pjView, groupParams);
        mColumnsHeight[col] += pjView.getGroupHeight() + groupParams.topMargin; // update column height
    }
    
    /**
     * @brief Adds PrintJobsGroupView in columns.
     */
    private void addViewsToColumns() {
        for (int i = 0; i < mPrintGroupViews.size(); i++) {
            placeInColumns(mPrintGroupViews.get(i));
        }
        
        mGroupViewCtr = mPrinters.size() - 1;
        post(mRunnable);
    }
    
    /**
     * @brief Re-layouts PrintJobsGroupViews in columns.
     */
    private void relayoutColumns() {
        if (checkIfNeedsRelayout()) {
            for (int i = 0; i < mColumns.size(); i++) {
                mColumns.get(i).removeAllViews();
                mColumnsHeight[i] = 0; // clear columns height
            }
            mGroupViewCtr = 0;
            addViewsToColumns();
        }
    }
    
    /**
     * @brief Checks if there is a need to re-layout after deletion of a PrintJobsGroupView.
     */
    private boolean checkIfNeedsRelayout() {
        boolean isColumnCleared = false;
        boolean isLeftCleared = false;
        boolean childNumExceeds = false;
        int childrenNum = 0;
        
        for (int i = 0; i < mColumns.size(); i++) {
            isColumnCleared |= (mColumns.get(i).getHeight() == 0);
            childrenNum += mColumns.get(i).getChildCount();
        }
        
        childNumExceeds = (childrenNum >= mColumns.size());
        
        if (!childNumExceeds) {
            for (int i = 0; i < childrenNum; i++) {
                isLeftCleared |= (mColumns.get(i).getHeight() == 0);
            }
        }
        
        return isColumnCleared && (childNumExceeds || isLeftCleared);
    }
    
    /**
     * @brief Creates column layouts as containers of PrintJobsGroupView.
     * 
     * @param width Size of width available for Print Jobs View
     */
    private void createColumns(int width) {
        int colNum = 1;
        int columnWidth = getContext().getResources().getDimensionPixelSize(R.dimen.printers_view_width);
        
        if (getResources().getBoolean(R.bool.is_tablet)) {
            // if tablet get number of columns based on whole width
            colNum = Math.max(width / columnWidth, MIN_COLUMNS);
            
            if (colNum == MIN_COLUMNS) {
                // adjust column width depending on whole width
                columnWidth = Math.min(width / MIN_COLUMNS, columnWidth);
            }
        }
        
        mColumnsHeight = new int[colNum];
        
        for (int i = 0; i < colNum; i++) {
            LayoutParams columnParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            mColumns.add(new LinearLayout(getContext()));
            mColumns.get(i).setOrientation(VERTICAL);
            
            // if tablet set column width
            if (colNum > 1) {
                columnParams.width = columnWidth;
            }
            
            addView(mColumns.get(i), columnParams);
        }
    }
    
    /**
     * @brief Checks for a valid swipe.
     * 
     * @param ev MotionEvent with action = ACTION_MOVE
     * 
     * @retval true if valid swipe
     * @retval false if not a valid swipe
     */
    private boolean checkSwipe(MotionEvent ev) {
        // if swipe to right end delete mode
        if ((ev.getRawX() - mDownPoint.x) > SWIPE_THRESHOLD) {
            endDelete(true);
            return false;
        }
        
        int coords[] = new int[2];
        boolean dragged = (mDownPoint.x - ev.getRawX()) > SWIPE_THRESHOLD;
        boolean contains1 = false;
        boolean contains2 = false;
        // check self, if valid swipe don't redisplay nor remove delete button
        if (mDeleteMode) {
            mDeleteView.getLocationOnScreen(coords);
            
            Rect rect = new Rect(coords[0], coords[1], coords[0] + mDeleteView.getWidth(), coords[1] + mDeleteView.getHeight());
            contains1 = rect.contains(mDownPoint.x, mDownPoint.y);
            contains2 = rect.contains((int) ev.getRawX(), (int) ev.getRawY());
            
            return (contains1 && contains2 && dragged);
        }
        
        for (int i = 0; i < mColumns.size(); i++) {
            LinearLayout column = mColumns.get(i);
            
            if (column != null) {
                column.getLocationOnScreen(coords);
                
                Rect rect = new Rect(coords[0], coords[1], coords[0] + column.getWidth(), coords[1] + column.getHeight());
                contains1 = rect.contains(mDownPoint.x, mDownPoint.y);
                contains2 = rect.contains((int) ev.getRawX(), (int) ev.getRawY());
                
                if (contains1 && contains2 && dragged) {
                    for (int j = 0; j < column.getChildCount(); j++) {
                        View view = ((PrintJobsGroupView) column.getChildAt(j)).getJobViewSwiped(mDownPoint, ev);
                        if (view != null) {
                            beginDelete((PrintJobsGroupView) column.getChildAt(j), view, true);
                            return true;
                        }
                    }
                }
            }
        }
        
        return false;
    }
    
    /**
     * @brief Process motion events to detect swipe.
     * 
     * @param ev MotionEvent 
     * 
     * @retval true if valid swipe
     * @retval false if not a valid swipe
     */
    private boolean processSwipe(MotionEvent ev) {
        boolean ret = false;
        int action = ev.getActionMasked();
        
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mDownPoint = new Point((int) ev.getRawX(), (int) ev.getRawY());
                break;
            case MotionEvent.ACTION_MOVE:
                ret = checkSwipe(ev);
                break;
        }
        
        return ret;
    }
    
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        
        if (mInitialFlag) {
            Point screenSize = AppUtils.getScreenDimensions((FragmentActivity) getContext());
            createColumns(screenSize.x);
            mInitialFlag = false;
        }
//        if (mGroupViewCtr < mPrintGroupViews.size()) {
//            addViewsToColumns();
//        } else 
        if (mColumns.size() > 1 && (mGroupViewCtr > mPrinters.size() - 1)) {
            // if multiple columns and after deletion of a PrintJobsGroupView
            relayoutColumns();
        }
    }
    
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int coords[] = new int[2];
        if (mDeleteMode) {
            
            if (mDeleteView != null) {
                View deleteButton = mDeleteView.findViewById(R.id.printJobDeleteBtn);
                if (deleteButton != null) {
                    deleteButton.getLocationOnScreen(coords);
                    
                    Rect rect = new Rect(coords[0], coords[1], coords[0] + deleteButton.getWidth(), coords[1] + deleteButton.getHeight());
                    // intercept if touched item is not the delete button
                    if (rect.contains((int) ev.getRawX(), (int) ev.getRawY())) {
                        if (ev.getActionMasked() == MotionEvent.ACTION_DOWN) {
                            mDownPoint = new Point((int) ev.getRawX(), (int) ev.getRawY());
                        }
                        return super.onInterceptTouchEvent(ev);
                    }
                }
                // intercept and clear delete button if ACTION_DOWN on different item
                if (ev.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    mDeleteView.getLocationOnScreen(coords);
                    
                    Rect rect = new Rect(coords[0], coords[1], coords[0] + mDeleteView.getWidth(), coords[1] + mDeleteView.getHeight());
                    if (!rect.contains((int) ev.getRawX(), (int) ev.getRawY())) {
                        endDelete(true);
                        return true;
                    }
                }
            }
            // check if swipe on same item
            boolean swipe = processSwipe(ev);
            
            if (swipe) {
                return true;
            }
            if (ev.getActionMasked() == MotionEvent.ACTION_UP || ev.getActionMasked() == MotionEvent.ACTION_CANCEL) {
                endDelete(true);
                return true;
            }
            return super.onInterceptTouchEvent(ev);
        } else {
            boolean swipe = processSwipe(ev);
            
            if (swipe) {
                return true;
            }
            return super.onInterceptTouchEvent(ev);
        }
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return super.onTouchEvent(ev);
    }
    
    // ================================================================================
    // INTERFACE - PrintJobsGroupDeleteListener
    // ================================================================================
    
    @Override
    public void deletePrintJobsGroup(PrintJobsGroupView printJobsGroupView) {
        mPrintGroupViews.remove(printJobsGroupView);
    }
    
    @Override
    public void animateGroups(PrintJobsGroupView printJobsGroupView, int totalHeight, float durationMultiplier, boolean down) {
        int idx = mPrintGroupViews.indexOf(printJobsGroupView);
        int column = 0;
        
        for (int i = 0; i < mColumns.size(); i++) {
            for (int j = 0; j < mColumns.get(i).getChildCount(); j++) {
                if (mColumns.get(i).getChildAt(j).equals(printJobsGroupView)) {
                    idx = j; // get index position in column of collapsed/expanded group
                    column = i; // get column of collapsed/expanded group
                    break;
                }
            }
        }
        
        TranslateAnimation animation = null;
        for (int i = idx + 1; i < mColumns.get(column).getChildCount(); i++) {
            if (down) {
                animation = new TranslateAnimation(0, 0, -totalHeight, 0);
                animation.setStartOffset((int) (getResources().getDimensionPixelSize(R.dimen.printjob_row_height) * durationMultiplier));
            } else {
                animation = new TranslateAnimation(0, 0, 0, -totalHeight);
            }
            animation.setDuration(printJobsGroupView.getAnimationDuration(totalHeight));
            mColumns.get(column).getChildAt(i).clearAnimation();
            mColumns.get(column).getChildAt(i).startAnimation(animation);
        }
    }
    
    @Override
    public void onDeleteJob() {
        endDelete(false);
    }
    
    // ================================================================================
    // Internal Classes
    // ================================================================================
    
    /**
     * @class AddViewRunnable 
     * 
     * @brief Requests layout after adding views during onLayout
     * 
     * Based on: http://stackoverflow.com/questions/5852758/views-inside-a-custom-viewgroup-not-rendering-after-a-size-change
     */
    private class AddViewRunnable implements Runnable {
        @Override
        public void run() {
            requestLayout();
        }
    }
    
    /**
     * @interface PrintJobsViewListener
     * 
     * @brief Interface for PrintJobsGroupView events
     */
    public interface PrintJobsViewListener {
        /**
         * @brief Called after loading initial views
         */
        public void onLoadFinished();
    }
    
    /**
     * @class ViewCreationThread
     * 
     * @brief Background Thread for creating PrintJobsGroupViews
     */
    private class ViewCreationThread extends Thread {
        private volatile boolean mIsRunning = true;
        
        public ViewCreationThread() {
            setPriority(Thread.MIN_PRIORITY);
        }
        
        public synchronized void setIsRunning(boolean b) {
            mIsRunning = b;
        }
        
        @Override
        public void run() {
            int jobCtr = 0;
            int start = 0;
            for (int j = 0; j < mPrinters.size(); j++) {
                if (!mIsRunning) {
                    return;  
                } 
 
                Printer printer = mPrinters.get(j);
                int pid = printer.getId();
                // get printer's jobs list with printerid==pid
                // printJobs is ordered according to prn_id in query
                for (int i = start; i < mPrintJobs.size(); i++) {
                    int id = mPrintJobs.get(i).getPrinterId();
                    jobCtr = i;
                    if (id == pid) {
                        // if current printer id is different from printer id of next print job in the list
                        if (i == mPrintJobs.size() - 1 || pid != mPrintJobs.get(i + 1).getPrinterId()) {
                            break;
                        }
                    }
                }
                final PrintJobsGroupView pjView = createPrintJobsView(mPrintJobs.subList(start, jobCtr + 1), printer);
                start = jobCtr + 1;
                if (!mIsRunning) {
                    return;  
                  } 
                
                mPrintGroupViews.add(pjView);
                mGroupViewCtr = j;
                PrintJobsView.this.post(new Runnable() {
                    @Override
                    public void run() {
                        placeInColumns(pjView);
                        if (mGroupViewCtr == mPrinters.size()-1) {
                            if (mListenerRef != null && mListenerRef.get() != null) {
                                mListenerRef.get().onLoadFinished();
                            }
                        }
                    }
                });
            }
        }
    }
}