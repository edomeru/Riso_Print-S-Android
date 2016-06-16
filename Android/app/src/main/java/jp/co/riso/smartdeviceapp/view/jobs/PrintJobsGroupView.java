/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * PrintJobsGroupView.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.view.jobs;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jp.co.riso.android.dialog.DialogUtils;
import jp.co.riso.android.dialog.InfoDialogFragment;
import jp.co.riso.android.util.AppUtils;
import jp.co.riso.smartdeviceapp.controller.jobs.PrintJobManager;
import jp.co.riso.smartdeviceapp.model.PrintJob;
import jp.co.riso.smartdeviceapp.model.PrintJob.JobResult;
import jp.co.riso.smartdeviceapp.model.Printer;
import jp.co.riso.smartprint.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * @class PrintJobsGroupView
 * 
 * @brief Custom view for a print jobs group (jobs under the same printer).
 */
public class PrintJobsGroupView extends LinearLayout implements View.OnClickListener, OnTouchListener, Callback {

    private static final String TAG = PrintJobsGroupView.class.getName();
    private static final String C_SPACE = " ";
    private static final int MSG_COLLAPSE = 0;
    private static final int MSG_EXPAND = 1;
    private static final int MSG_DELETEJOB = 2;
    private static final int MSG_DELETEGROUP = 3;
    private static float DURATION_MULTIPLIER = 0.2f;
    
    private View mPrintGroupView;
    private List<PrintJob> mPrintJobs;
    private Printer mPrinter;
    private View mViewToDelete;
    private PrintJobsGroupListener mGroupListener;
    private PrintJobsLayoutListener mLayoutListener;
    private RelativeLayout mPrintJobGroupLayout;
    private boolean mIsCollapsed = false;
    private String mTitle;
    private String mErrorMessage;
    private String mOkText;
    private Handler mHandler;
    private LinearLayout mJobsLayout;
    private int mRowHeight;
    private int mSeparatorHeight;
    
    /**
     * @brief Default Constructor
     * 
     * @param context Activity context
     */
    public PrintJobsGroupView(Context context) {
        super(context);
        init();
    }
    
    /**
     * @brief Default Constructor
     * 
     * @param context Activity context
     * @param attrs AttributeSet
     * @param defStyle Default Style
     */
    
    public PrintJobsGroupView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }
    
    /**
     * @brief Default Constructor
     * 
     * @param context Activity context
     * @param attrs AttributeSet
     */
    public PrintJobsGroupView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    /**
     * @brief Sets the data for the PrintJobsGroupView and creates view.
     * 
     * @param printJobs List of print jobs
     * @param printer Printer object of the print jobs
     * @param groupListener Print job group listener
     * @param layoutListener Layout listener
     */
    public void setData(List<PrintJob> printJobs, Printer printer, PrintJobsGroupListener groupListener, PrintJobsLayoutListener layoutListener) {
        this.mPrintJobs = new ArrayList<PrintJob>(printJobs);
        this.mPrinter = printer;
        this.mGroupListener = groupListener;
        this.mLayoutListener = layoutListener;
        createView();
    }
    
    /**
     * @brief Retrieves the expanded height of the PrintJobsGroupView
     * 
     * @return Expanded height of PrintJobsGroupView
     */
    public int getGroupHeight() {
        return ((mJobsLayout.getChildCount() + 1) * mRowHeight) + ((mJobsLayout.getChildCount() - 1) * mSeparatorHeight);
    }
    
    /**
     * @brief Restores the UI state of the jobs group
     * 
     * @param isCollapsed Collapsed state
     * @param printerToDelete Printer of the print jobs group to be deleted
     * @param jobToDelete Print job to be deleted
     */
    public void restoreState(boolean isCollapsed, Printer printerToDelete, PrintJob jobToDelete) {
        boolean isDeleteAllClicked = printerToDelete != null && printerToDelete.equals(mPrinter);
        boolean isDeleteShown = jobToDelete != null && mPrintJobs.contains(jobToDelete);
        
        if (isCollapsed) {
            animateCollapse(false);
            mIsCollapsed = isCollapsed;
        }
        
        if (isDeleteAllClicked) {
            mGroupListener.setPrinterToDelete(this, mPrinter);
            mPrintGroupView.findViewWithTag(printerToDelete).setSelected(true);
        }
        
        if (isDeleteShown) {
            mGroupListener.setDeletePrintJob(this, jobToDelete);
        }
    }
    
    /**
     * @brief Deletes Job Group in the database and view
     */
    public void onDeleteJobGroup() {
        PrintJobManager pm = PrintJobManager.getInstance(getContext());
        boolean isSuccess = pm.deleteWithPrinterId(mPrinter.getId());
        if (isSuccess) {
            animateDeleteGroup();
        } else {
            mPrintJobGroupLayout.findViewById(R.id.printJobGroupDelete).setSelected(false);
            // show dialog
            InfoDialogFragment errordialog = InfoDialogFragment.newInstance(mTitle, mErrorMessage, mOkText);
            DialogUtils.displayDialog((Activity) getContext(), TAG, errordialog);
        }
    }
    
    /**
     * @brief Deletes a print job in the database and view
     * 
     * @param job Print Job to be deleted
     */
    public void onDeletePrintJob(PrintJob job) {
        PrintJobManager pm = PrintJobManager.getInstance(getContext());
        boolean isSuccess = pm.deleteWithJobId(job.getId());
        if (isSuccess) {
            mGroupListener.deleteJobFromList(job);
            animateDeleteJob(mJobsLayout.findViewWithTag(job));
        } else {
            // show dialog
            InfoDialogFragment errordialog = InfoDialogFragment.newInstance(mTitle, mErrorMessage, mOkText);
            DialogUtils.displayDialog((Activity) getContext(), TAG, errordialog);
        }
        // clears delete state
        mLayoutListener.onDeleteJob();
    }
    
    /**
     * @brief Cancels delete Job Group
     */
    public void onCancelDeleteGroup() {
        mPrintJobGroupLayout.findViewById(R.id.printJobGroupDelete).setSelected(false);
    }
    
    /**
     * @brief Sets delete button state
     * 
     * @param v Print Job view to be deleted
     */
    public void setDeleteButton(View v) {
        mViewToDelete = v;
        mViewToDelete.setSelected(true);
        mGroupListener.setDeletePrintJob(this, (PrintJob) mViewToDelete.getTag());
    }
    
    /**
     * @brief Clears delete button state 
     */
    public void clearDeleteButton() {
        if (mViewToDelete != null) {
            mViewToDelete.setSelected(false);
            mViewToDelete = null;
            mGroupListener.setDeletePrintJob(null, null);
        }
    }
    
    /**
     * @brief Retrieves a print job row layout if a swipe is valid within the row. 
     * 
     * @param downPoint Point containing the coordinates of the ACTION_DOWN event
     * @param ev MotionEvent of the ACTION_MOVE containing the coordinates  
     * 
     * @return Swiped job view
     * @retval null Invalid swipe
     */
    public View getJobViewSwiped(Point downPoint, MotionEvent ev) {
        if (mIsCollapsed) {
            return null;
        }
        
        int coords[] = new int[2];
        for (int i = 0; i < mJobsLayout.getChildCount(); i++) {
            View view = mJobsLayout.getChildAt(i);
            
            if (view != null) {
                view.getLocationOnScreen(coords);
                
                Rect rect = new Rect(coords[0], coords[1], coords[0] + view.getWidth(), coords[1] + view.getHeight());
                
                boolean contains1 = rect.contains(downPoint.x, downPoint.y);
                boolean contains2 = rect.contains((int) ev.getRawX(), (int) ev.getRawY());
                
                if (contains1 && contains2) {
                    return view;
                }
            }
        }
        return null;
    }
    
    /**
     * @brief Gets animation duration based on the height of the view animated or screen height whichever is smaller 
     * 
     * @param originalHeight Height of the view to be animated 
     * 
     * @return Computed animation duration 
     */    
    public int getAnimationDuration(int originalHeight) {
        int newHeight = Math.min(originalHeight, AppUtils.getScreenDimensions((Activity) getContext()).y);
        return (int) (newHeight * DURATION_MULTIPLIER);
    }
    
    /**
     * @brief Initializes PrintJobsGroupView
     */
    private void init() {
        if (!isInEditMode()) {
            mTitle = getResources().getString(R.string.ids_info_msg_delete_jobs_title);
            mOkText = getResources().getString(R.string.ids_lbl_ok);
            mErrorMessage = getResources().getString(R.string.ids_err_msg_db_failure);
            setOrientation(VERTICAL);
            mRowHeight = getResources().getDimensionPixelSize(R.dimen.printjob_row_height);
            mSeparatorHeight = getResources().getDimensionPixelSize(R.dimen.separator_size);
            
            ((Activity) getContext()).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mHandler = new Handler(PrintJobsGroupView.this);
                }
            });
        }
    }
    
    /**
     * @brief Creates the view for print jobs group.
     */
    private void createView() {
        // create header
        if (!mPrintJobs.isEmpty()) {
            createHeader();
        }
        
        // add print jobs
        for (int i = 0; i < mPrintJobs.size(); i++) {
            createItem(i);
        }
        
        mPrintJobs.clear();
    }
    
    /**
     * @brief Creates view for the header.
     */
    private void createHeader() {
        LayoutInflater factory = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        
        mPrintGroupView = factory.inflate(R.layout.printjobs_group, this, true);
        mPrintJobGroupLayout = (RelativeLayout) mPrintGroupView.findViewById(R.id.printJobsGroupLayout);
        TextView printJobGroupText = (TextView) mPrintGroupView.findViewById(R.id.printJobGroupText);
        TextView printJobGroupSubText = (TextView) mPrintGroupView.findViewById(R.id.printJobGroupSubText);
        Button printJobGroupDelete = (Button) mPrintGroupView.findViewById(R.id.printJobGroupDelete);
        String printerName = mPrinter.getName();
        if (printerName == null || printerName.isEmpty()) {
            printerName = getContext().getResources().getString(R.string.ids_lbl_no_name);
        }
        printJobGroupText.setText(printerName);
        printJobGroupSubText.setText(mPrinter.getIpAddress());
        
        printJobGroupDelete.setTag(mPrinter);
        mPrintJobGroupLayout.setOnClickListener(this);
        printJobGroupDelete.setOnClickListener(this);
        
        // AppUtils.changeChildrenFont(mPrintJobGroupLayout, SmartDeviceApp.getAppFont());
        
        mJobsLayout = new LinearLayout(getContext());
        mJobsLayout.setOrientation(VERTICAL);
        addView(mJobsLayout, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        if (getResources().getBoolean(R.bool.is_tablet)) {
            mPrintGroupView.findViewById(R.id.printJobDeleteSeparator).setVisibility(INVISIBLE);
        }
    }
    
    /**
     * @brief Creates view for an item in the jobs list.
     * 
     * @param index Index of the print job item in the list to be created
     */
    private void createItem(int index) {
        LayoutInflater factory = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View tempView = factory.inflate(R.layout.printjobs_item, this, false);
        TextView printJobName = (TextView) tempView.findViewById(R.id.printJobName);
        ImageView printJobError = (ImageView) tempView.findViewById(R.id.printJobError);
        ImageView printJobSuccess = (ImageView) tempView.findViewById(R.id.printJobSuccess);
        Button printJobDeleteBtn = (Button) tempView.findViewById(R.id.printJobDeleteBtn);
        TextView printJobDate = (TextView) tempView.findViewById(R.id.printJobDate);
        
        PrintJob pj = mPrintJobs.get(index);
        tempView.setTag(pj);
        tempView.setOnTouchListener(this);
        printJobName.setText(pj.getName());
        printJobDate.setText(formatDate(pj.getDate()));
        printJobDeleteBtn.setOnClickListener(this);
        
        if (pj.getResult().equals(JobResult.ERROR)) {
            printJobError.setVisibility(VISIBLE);
            printJobSuccess.setVisibility(GONE);
        }
        
        mJobsLayout.addView(tempView);
        
        // AppUtils.changeChildrenFont((ViewGroup) tempView, SmartDeviceApp.getAppFont());
        
        if (index == mPrintJobs.size() - 1) {
            tempView.findViewById(R.id.printJobSeparator).setVisibility(GONE);
        }
    }
    
    /**
     * @brief Format date into string using locale format (date and time)
     * 
     * @param date Date to be formatted
     * 
     * @return Converted string format
     */
    private String formatDate(Date date) {
        String dateStr = DateFormat.getDateFormat(getContext()).format(date);
        String timeStr = DateFormat.getTimeFormat(getContext()).format(date);
        return dateStr + C_SPACE + timeStr;
    }
    
    /**
     * @brief Toggles collapse/expand of a group view when clicked.
     * 
     * @param v Header view of the group view to be collapse/expand
     */
    private void toggleGroupView(View v) {
        v.setClickable(false);
        if (mIsCollapsed) {
            animateExpand(true);
            mIsCollapsed = false;
        } else {
            animateCollapse(true);
            mIsCollapsed = true;
        }
        mGroupListener.setCollapsed(mPrinter, mIsCollapsed);
    }
    
    /**
     * @brief Animates expand of a print job group.
     * 
     * @param animate true if expand with animation 
     */
    private void animateExpand(boolean animate) {
        mJobsLayout.setVisibility(View.VISIBLE);
        int totalHeight = (mJobsLayout.getChildCount() * mRowHeight) + ((mJobsLayout.getChildCount() - 1) * mSeparatorHeight);
        
        if (animate) {
            for (int i = 0; i < mJobsLayout.getChildCount(); i++) {
                View child = mJobsLayout.getChildAt(i);
                TranslateAnimation animation = new TranslateAnimation(0, 0, -totalHeight, 0);
                animation.setDuration(getAnimationDuration(totalHeight));
                
                if (i == mJobsLayout.getChildCount() - 1) {
                    animation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                        }
                        
                        @Override
                        public void onAnimationRepeat(Animation animation) {
                        }
                        
                        @Override
                        public void onAnimationEnd(Animation animation) {
                            Message newMessage = Message.obtain(mHandler, MSG_EXPAND);
                            mHandler.sendMessage(newMessage);
                        }
                    });
                }
                child.clearAnimation();
                child.startAnimation(animation);
            }
            mLayoutListener.animateGroups(this, totalHeight, DURATION_MULTIPLIER, true);
        } else {
            expandGroupView();
        }
    }
    
    /**
     * @brief Animates collapse of a print job group.
     * 
     * @param animate true if collapse with animation
     */
    private void animateCollapse(boolean animate) {
        if (animate) {
            int totalHeight = (mJobsLayout.getChildCount() * mRowHeight) + ((mJobsLayout.getChildCount() - 1) * mSeparatorHeight);
            
            for (int i = 0; i < mJobsLayout.getChildCount(); i++) {
                View child = mJobsLayout.getChildAt(i);
                TranslateAnimation animation = new TranslateAnimation(0, 0, 0, -totalHeight);
                animation.setDuration(getAnimationDuration(totalHeight));
                animation.setFillAfter(true);
                
                if (i == mJobsLayout.getChildCount() - 1) {
                    
                    animation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                        }
                        
                        @Override
                        public void onAnimationRepeat(Animation animation) {
                        }
                        
                        @Override
                        public void onAnimationEnd(Animation animation) {
                            Message newMessage = Message.obtain(mHandler, MSG_COLLAPSE);
                            mHandler.sendMessage(newMessage);
                        }
                    });
                }
                child.clearAnimation();
                child.startAnimation(animation);
            }
            mLayoutListener.animateGroups(PrintJobsGroupView.this, totalHeight, DURATION_MULTIPLIER, false);
        } else {
            collapseGroupView();
        }
    }
    
    /**
     * @brief Animates deletion of a Print Job view.
     * 
     * @param v View of the print job row to be deleted
     */
    private void animateDeleteJob(View v) {
        int totalHeight = mRowHeight + mSeparatorHeight;
        
        if (mJobsLayout.getChildCount() == 1) {
            animateDeleteGroup();
        } else {
            
            final int jobToDelete = mJobsLayout.indexOfChild(v); //mPrintJobViews.indexOf(v);
            
            ScaleAnimation deleteAnim = new ScaleAnimation(1.0f, 1.0f, 1.0f, 0.0f);
            deleteAnim.setDuration((int) (totalHeight * DURATION_MULTIPLIER));
            
            if (jobToDelete == mJobsLayout.getChildCount() - 1) {
                deleteAnim.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }
                    
                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                    
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        Message newMessage = Message.obtain(mHandler, MSG_DELETEJOB);
                        newMessage.arg1 = jobToDelete;
                        mHandler.sendMessage(newMessage);
                    }
                });
            }
            
            v.clearAnimation();
            v.startAnimation(deleteAnim);
            
            for (int i = jobToDelete + 1; i < mJobsLayout.getChildCount(); i++) {
                View child = mJobsLayout.getChildAt(i);
                TranslateAnimation animation = new TranslateAnimation(0, 0, 0, -totalHeight);
                animation.setDuration((int) (totalHeight * DURATION_MULTIPLIER));
                
                if (i == mJobsLayout.getChildCount() - 1) {
                    
                    animation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                        }
                        
                        @Override
                        public void onAnimationRepeat(Animation animation) {
                        }
                        
                        @Override
                        public void onAnimationEnd(Animation animation) {
                            Message newMessage = Message.obtain(mHandler, MSG_DELETEJOB);
                            newMessage.arg1 = jobToDelete;
                            mHandler.sendMessage(newMessage);
                        }
                    });
                }
                child.clearAnimation();
                child.startAnimation(animation);
            }
            mLayoutListener.animateGroups(PrintJobsGroupView.this, totalHeight, DURATION_MULTIPLIER, false);
        }
        
    }
    
    /**
     * @brief Animates deletion of Print Jobs Group.
     */
    private void animateDeleteGroup() {
        int totalHeight = 0;
        
        if (!mIsCollapsed) {
            totalHeight = getGroupHeight();
        } else if (getResources().getBoolean(R.bool.is_tablet)){
            totalHeight = mRowHeight;
        } else {
            totalHeight = mRowHeight + mSeparatorHeight;
        }
        
        ScaleAnimation animation = new ScaleAnimation(1.0f, 1.0f, 1.0f, 0.0f);
        animation.setDuration(getAnimationDuration(totalHeight));
        animation.setFillAfter(true);
        
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }
            
            @Override
            public void onAnimationRepeat(Animation animation) {
            }
            
            @Override
            public void onAnimationEnd(Animation animation) {
                Message newMessage = Message.obtain(mHandler, MSG_DELETEGROUP);
                mHandler.sendMessage(newMessage);
            }
        });
        mPrintGroupView.clearAnimation();
        mPrintGroupView.startAnimation(animation);
        mLayoutListener.animateGroups(PrintJobsGroupView.this, totalHeight, DURATION_MULTIPLIER, false);
    }
    
    /**
     * @brief Expands view group.
     */
    private void expandGroupView() {
        mPrintJobGroupLayout.setSelected(false);
        if (!getResources().getBoolean(R.bool.is_tablet)) {
            this.findViewById(R.id.printJobGroupSeparator).setVisibility(GONE);
        }
        mPrintJobGroupLayout.setClickable(true);
    }
    
    /**
     * @brief Collapses view group.
     */
    private void collapseGroupView() {
        mJobsLayout.setVisibility(GONE);
        mPrintJobGroupLayout.setSelected(true);
        mPrintJobGroupLayout.findViewById(R.id.printJobGroupDelete).setSelected(false);
        if (!getResources().getBoolean(R.bool.is_tablet)) {
            this.findViewById(R.id.printJobGroupSeparator).setVisibility(VISIBLE);
        }
        mPrintJobGroupLayout.setClickable(true);
    }
    
    /**
     * @brief Displays delete print jobs dialog when the delete button is clicked.
     * 
     * @param v Delete view clicked
     */
    private void deleteJobGroup(View v) {
        if (mGroupListener.showDeleteDialog()) {
            mGroupListener.setPrinterToDelete(this, mPrinter);
            v.findViewById(R.id.printJobGroupDelete).setSelected(true);
        }
    }
    
    /**
     * @brief Deletes the print job group view.
     */
    private void deletePrintJobGroupView() {
        for (int i = 0; i < mJobsLayout.getChildCount(); i++) {
            mGroupListener.deleteJobFromList((PrintJob) mJobsLayout.getChildAt(i).getTag());
        }
        mGroupListener.deletePrinterFromList(mPrinter);
        mLayoutListener.deletePrintJobsGroup(this);
        ((LinearLayout) mPrintGroupView.getParent()).removeView(mPrintGroupView);
    }
    
    /**
     * @brief Deletes a print job row layout.
     * 
     * @param i Index of Print Job View to be deleted
     */
    private void deletePrintJobView(int i) {
        mJobsLayout.removeViewAt(i);
        if (mJobsLayout.getChildCount() == 0) {
            deletePrintJobGroupView();
        } else if (i == mJobsLayout.getChildCount()) { 
            // after deletion remove separator in last row
            int lastRow = i - 1;
            mJobsLayout.getChildAt(lastRow).findViewById(R.id.printJobSeparator).setVisibility(GONE);
        }
    }
    
    // ================================================================================
    // INTERFACE - View.OnClickListener
    // ================================================================================
    
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.printJobGroupDelete:
                deleteJobGroup(v);
                break;
            case R.id.printJobDeleteBtn:
                mGroupListener.showDeleteDialog();
                break;
            case R.id.printJobsGroupLayout:
                toggleGroupView(v);
                break;
        }
    }
    
    // ================================================================================
    // INTERFACE - View.onTouchListener
    // ================================================================================

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                v.setPressed(true);
                v.findViewById(R.id.printJobDeleteBtn).setPressed(false);
                return true;
            case MotionEvent.ACTION_MOVE:
                return true;
            case MotionEvent.ACTION_UP:
                v.setPressed(false);
                return true;
            case MotionEvent.ACTION_CANCEL:
                v.setPressed(false);
                return true;
        }
        return false;
    }
    
    // ================================================================================
    // INTERFACE - Callback
    // ================================================================================
    
    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_COLLAPSE:
                collapseGroupView();
                return true;
            case MSG_EXPAND:
                expandGroupView();
                return true;
            case MSG_DELETEJOB:
                deletePrintJobView(msg.arg1);
                return true;
            case MSG_DELETEGROUP:
                deletePrintJobGroupView();
                return true;
        }
        return false;
    }
    
    // ================================================================================
    // Internal Classes
    // ================================================================================
    
    /**
     * @interface PrintJobsGroupListener
     * 
     * @brief Interface for PrintJobsGroupView events such as collapse,expand and delete.
     */
    public interface PrintJobsGroupListener {
        /**
         * @brief Called when a print jobs group is deleted
         * 
         * @param printer Printer object of the print job group to be removed
         */
        public void deletePrinterFromList(Printer printer);
        
        /**
         * @brief Called when a print job is deleted
         * 
         * @param printJob PrintJob object to be removed
         */
        public void deleteJobFromList(PrintJob printJob);
        
        /**
         * @brief Called when a print job group is expanded/collapsed
         * 
         * @param printer Printer object of the print job group
         * @param isCollapsed Collapse state
         */
        public void setCollapsed(Printer printer, boolean isCollapsed);
        
        /**
         * @brief Callback for setting print job to be deleted
         * 
         * @param printJobsGroupView PrintJobsGroupView containing the PrintJob to be deleted
         * @param job PrintJob to be deleted
         */
        public void setDeletePrintJob(PrintJobsGroupView printJobsGroupView, PrintJob job);
        
        /**
         * @brief Callback for setting print job group to be deleted
         * 
         * @param printJobsGroupView PrintJobsGroupView of the job group to be deleted 
         * @param printer Printer of the print job group to be deleted
         */
        public void setPrinterToDelete(PrintJobsGroupView printJobsGroupView, Printer printer);
        
        /**
         * @brief Called when a delete button is clicked
         */
        public boolean showDeleteDialog();
    }
    
    /**
     * @interface PrintJobsGroupListener
     * 
     * @brief Interface for PrintJobsGroupView events such as animate and delete.
     */
    public interface PrintJobsLayoutListener {
        /**
         * @brief Called when a print job group is deleted
         * 
         * @param printJobsGroupView Print job group to be removed
         */
        public void deletePrintJobsGroup(PrintJobsGroupView printJobsGroupView);
        
        /**
         * @brief Called when animating a print job group
         * 
         * @param printJobsGroupView Print job group view to animate
         * @param totalHeight Total height of the view to animate
         * @param durationMultiplier Duration multiplier for the animation
         * @param down Direction of animation; if true views translates downwards, else upwards
         */
        public void animateGroups(PrintJobsGroupView printJobsGroupView, int totalHeight, float durationMultiplier, boolean down);
        
        /**
         * @brief Called when a print job is deleted
         */
        public void onDeleteJob();
    }
}
