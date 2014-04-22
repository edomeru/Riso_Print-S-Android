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

import jp.co.riso.smartdeviceapp.R;
import jp.co.riso.smartdeviceapp.model.PrintJob;
import jp.co.riso.smartdeviceapp.model.Printer;
import jp.co.riso.smartdeviceapp.view.anim.DisplayDeleteAnimation;
import jp.co.riso.smartdeviceapp.view.jobs.PrintJobsGroupView.PrintJobsGroupListener;
import jp.co.riso.smartdeviceapp.view.jobs.PrintJobsGroupView.PrintJobsLayoutListener;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;

public class PrintJobsView extends LinearLayout implements PrintJobsLayoutListener {
    private static final int SWIPE_THRESHOLD = 50;
    private WeakReference<PrintJobsViewListener> mListenerRef;
    private List<PrintJob> mPrintJobs;
    private List<Printer> mPrinters;
    private List<Printer> mCollapsedPrinters;
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
    
    /**
     * Constructor
     */
    public PrintJobsView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }
    
    /**
     * Constructor
     */
    public PrintJobsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    /**
     * Constructor
     */
    public PrintJobsView(Context context) {
        super(context);
        init();
    }
    
    /**
     * Set Data
     * 
     * @param printJobs
     *            list of print jobs
     * @param printer
     *            list of printer objects
     * @param delListener
     *            PrintJobsGroup listener
     * @param listener
     *            PrintJobsView listener
     */
    public void setData(List<PrintJob> printJobs, List<Printer> printers, PrintJobsGroupListener delListener, PrintJobsViewListener listener,
            List<Printer> collapsedPrinters, PrintJob printJobToDelete, Printer printerToDelete) {
        this.mPrintJobs = new ArrayList<PrintJob>(printJobs);
        this.mPrinters = new ArrayList<Printer>(printers);
        this.mGroupListener = delListener;
        this.mListenerRef = new WeakReference<PrintJobsViewListener>(listener);
        this.mCollapsedPrinters = collapsedPrinters;
        this.mPrintJobToDelete = printJobToDelete;
        this.mPrinterToDelete = printerToDelete;
        
        reset();
    }
    
    /**
     * Begin delete mode
     * 
     * @param pj
     *            print jobs group view
     * @param view
     *            row layout view
     * @param animate
     *            animate delete mode
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
     * End delete mode
     * 
     * @param animate
     *            animate delete mode
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
     * Initialize PrintJobsView
     */
    private void init() {
        if (!isInEditMode()) {
            setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            setOrientation(HORIZONTAL);
            mDeleteAnimation = new DisplayDeleteAnimation();
            mRunnable = new AddViewRunnable();
        }
    }
    
    /**
     * Reset the PrintJobsView
     */
    private void reset() {
        mGroupViewCtr = 0;
        mInitialFlag = true;
        removeAllViews();
        mColumns.clear();
        mPrintGroupViews.clear();
        groupPrintJobs();
    }
    
    /**
     * Groups print jobs
     */
    private void groupPrintJobs() {
        int jobCtr = 0;
        List<PrintJob> jobs = new ArrayList<PrintJob>();
        for (int j = 0; j < mPrinters.size(); j++) {
            Printer printer = mPrinters.get(j);
            int pid = printer.getId();
            // get printer's jobs list with printerid==pid
            // printJobs is ordered according to prn_id in query
            for (int i = jobCtr; i < mPrintJobs.size(); i++) {
                
                PrintJob pj = mPrintJobs.get(i);
                int id = pj.getPrinterId();
                
                if (id == pid) {
                    jobs.add(pj);
                    // if current printer id is different from printer id of next print job in the list
                    if (i == mPrintJobs.size() - 1 || pid != mPrintJobs.get(i + 1).getPrinterId()) {
                        break;
                    }
                }
                jobCtr = i;
            }
            
            // use jobs list to add view to smallest column
            if (!jobs.isEmpty()) {
                createPrintJobsView(jobs, printer);
                jobs.clear();
            }
        }
    }
    
    /**
     * @return index of the smallest column
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
     * Creates print jobs view
     */
    private void createPrintJobsView(List<PrintJob> jobsList, Printer printer) {
        PrintJobsGroupView pjView = new PrintJobsGroupView(getContext());
        pjView.setData(jobsList, printer, mGroupListener, this);
        restoreUIstate(pjView, printer);
        
        mPrintGroupViews.add(pjView);
    }
    
    /**
     * Restores the UI state
     */
    private void restoreUIstate(PrintJobsGroupView pj, Printer printer) {
        boolean isCollapsed = mCollapsedPrinters.contains(printer);
        boolean isDeleteShown = mPrintJobToDelete != null && mPrintJobs.contains(mPrintJobToDelete);
        
        if (isDeleteShown) {
            View v = pj.findViewWithTag(mPrintJobToDelete);
            if (v != null) {
                beginDelete(pj, v, false);
            }
        }
        
        pj.restoreState(isCollapsed, mPrintJobToDelete, mPrinterToDelete);
    }
    
    /**
     * Add PrintJobsGroupView in columns
     */
    private void placeInColumns() {
        PrintJobsGroupView pjView = mPrintGroupViews.get(mGroupViewCtr);
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        if (getResources().getBoolean(R.bool.is_tablet)) {
            lp.leftMargin = getResources().getDimensionPixelSize(R.dimen.printjob_margin_side);
            lp.rightMargin = getResources().getDimensionPixelSize(R.dimen.printjob_margin_side);
            lp.topMargin = getResources().getDimensionPixelSize(R.dimen.printjob_margin_top);
        }
        pjView.setOrientation(VERTICAL);
        
        int col = getSmallestColumn();
        mColumns.get(col).addView(pjView, lp);
        mColumnsHeight[col] += pjView.getGroupHeight() + lp.topMargin; // update column height
    }
    
    /**
     * Add Views in columns
     */
    private void addViewsToColumns() {
        placeInColumns();
        mGroupViewCtr++;
        post(mRunnable);
    }
    
    /**
     * Re-layout columns
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
     * checks if there is a need to re-layout
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
     * creates columns
     */
    private void createColumns() {
        int colNum = getResources().getBoolean(R.bool.is_tablet) ? getResources().getBoolean(R.bool.is_tablet_land) ? 3 : 2 : 1;
        LayoutParams param = (LayoutParams) getLayoutParams();
        
        if (colNum > 1) {
            param.leftMargin = getResources().getDimensionPixelSize(R.dimen.printjob_column_margin_side);
            param.rightMargin = getResources().getDimensionPixelSize(R.dimen.printjob_column_margin_side);
        }
        
        mColumnsHeight = new int[colNum];
        
        for (int i = 0; i < colNum; i++) {
            LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1.0f);
            mColumns.add(new LinearLayout(getContext()));
            mColumns.get(i).setOrientation(VERTICAL);
            addView(mColumns.get(i), lp);
        }
    }
    
    /**
     * checks if a view is swiped
     */
    private boolean checkSwipe(MotionEvent ev) {
        int coords[] = new int[2];
        boolean dragged = Math.abs(mDownPoint.x - ev.getRawX()) > SWIPE_THRESHOLD;
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
                            beginDelete(mPrintGroupViews.get(i), view, true);
                            return true;
                        }
                    }
                }
            }
        }
        
        return false;
    }
    
    /**
     * process swipe
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
    
    /** {@inheritDoc} */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        
        if (mInitialFlag) {
            createColumns();
            mInitialFlag = false;
        }
        if (mGroupViewCtr < mPrintGroupViews.size()) {
            addViewsToColumns();
        } else if (mColumns.size() > 1 && mGroupViewCtr > mPrintGroupViews.size()) {
            // if multiple columns and after deletion of a PrintJobsGroupView
            relayoutColumns();
        }
    }
    
    /** {@inheritDoc} */
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
    
    /** {@inheritDoc} */
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return super.onTouchEvent(ev);
    }
    
    // ================================================================================
    // INTERFACE - PrintJobsGroupDeleteListener
    // ================================================================================
    
    /** {@inheritDoc} */
    @Override
    public void deletePrintJobsGroup(PrintJobsGroupView printJobsGroupView) {
        mPrintGroupViews.remove(printJobsGroupView);
    }
    
    /** {@inheritDoc} */
    @Override
    public void animateGroups(PrintJobsGroupView printJobsGroupView, int totalHeight, float durationMultiplier, boolean isCollapsed) {
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
            if (isCollapsed) {
                animation = new TranslateAnimation(0, 0, -totalHeight, 0);
                animation.setStartOffset((int) (getResources().getDimensionPixelSize(R.dimen.printjob_row_height) * durationMultiplier));
            } else {
                animation = new TranslateAnimation(0, 0, 0, -totalHeight);
            }
            animation.setDuration((int) (totalHeight * durationMultiplier));
            mColumns.get(column).getChildAt(i).clearAnimation();
            mColumns.get(column).getChildAt(i).startAnimation(animation);
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void onDeleteJob(){
        endDelete(false);
    }
    
    // ================================================================================
    // Internal Classes
    // ================================================================================
    
    /**
     * Add View
     */
    // http://stackoverflow.com/questions/5852758/views-inside-a-custom-viewgroup-not-rendering-after-a-size-change
    private class AddViewRunnable implements Runnable {
        @Override
        public void run() {
            requestLayout();
            if (mGroupViewCtr >= mPrintGroupViews.size()) {
                if (mListenerRef != null && mListenerRef.get() != null) {
                    mListenerRef.get().hideLoading();
                }
            }
        }
    }
    
    /**
     * PrintJobsView Listener
     */
    public interface PrintJobsViewListener {
        /**
         * Hide loading
         */
        public void hideLoading();
    }
}