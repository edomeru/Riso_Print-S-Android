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
import jp.co.riso.smartdeviceapp.R;
import jp.co.riso.smartdeviceapp.SmartDeviceApp;
import jp.co.riso.smartdeviceapp.controller.jobs.PrintJobManager;
import jp.co.riso.smartdeviceapp.model.PrintJob;
import jp.co.riso.smartdeviceapp.model.Printer;
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
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class PrintJobsGroupView extends LinearLayout implements View.OnClickListener, OnTouchListener, Callback {
    
    private static final String TAG = "PrintJobsGroupView";
    private static final String C_SPACE = " ";
    private static final int MSG_COLLAPSE = 0;
    private static final int MSG_EXPAND = 1;
    private static float DURATION_MULTIPLIER = 0.3f;
    
    private View mPrintGroupView;
    private List<View> mPrintJobViews = new ArrayList<View>();
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
    
    public PrintJobsGroupView(Context context) {
        super(context);
        init();
    }
    
    public PrintJobsGroupView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }
    
    public PrintJobsGroupView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public void setData(List<PrintJob> printJobs, Printer printer, PrintJobsGroupListener groupListener, PrintJobsLayoutListener layoutListener) {
        this.mPrintJobs = new ArrayList<PrintJob>(printJobs);
        this.mPrinter = printer;
        this.mGroupListener = groupListener;
        this.mLayoutListener = layoutListener;
        createView();
    }
    
    // get expanded height of PrintJobsGroupView
    public int getGroupHeight() {
        return (mPrintJobViews.size() + 1) * mRowHeight;
    }
    
    public void restoreState(boolean isCollapsed, PrintJob jobToDelete, Printer printerToDelete) {
        boolean isDeleteAllClicked = printerToDelete != null && printerToDelete.equals(mPrinter);
        
        if (isCollapsed) {
            animateCollapse(false);
            mIsCollapsed = isCollapsed;
        }
        
        if (isDeleteAllClicked) {
            mGroupListener.setPrinterToDelete(this, mPrinter);
            mPrintGroupView.findViewWithTag(printerToDelete).setSelected(true);
        }
    }
    
    public void onDeleteJobGroup() {
        PrintJobManager pm = PrintJobManager.getInstance(getContext());
        boolean isSuccess = pm.deleteWithPrinterId(mPrinter.getId());
        if (isSuccess) {
            deletePrintJobGroupView();
            mPrintJobViews.clear();
        } else {
            mPrintJobGroupLayout.findViewById(R.id.printJobGroupDelete).setSelected(false);
            // show dialog
            InfoDialogFragment errordialog = InfoDialogFragment.newInstance(mTitle, mErrorMessage, mOkText);
            DialogUtils.displayDialog((Activity) getContext(), TAG, errordialog);
        }
    }
    
    public void onCancelDeleteGroup() {
        mPrintJobGroupLayout.findViewById(R.id.printJobGroupDelete).setSelected(false);
    }
    
    public void setDeleteButton(View v) {
        mViewToDelete = v;
        mViewToDelete.setSelected(true);
        mGroupListener.setDeletePrintJob((PrintJob) mViewToDelete.getTag());
    }
    
    public void clearDeleteButton() {
        if (mViewToDelete != null) {
            mViewToDelete.setSelected(false);
            mViewToDelete = null;
            mGroupListener.setDeletePrintJob(null);
        }
    }
    
    public View getJobViewSwiped(Point downPoint, MotionEvent ev) {
        if (mIsCollapsed) {
            return null;
        }
        
        int coords[] = new int[2];
        for (int i = 0; i < mPrintJobViews.size(); i++) {
            View view = mPrintJobViews.get(i);
            
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
    
    private void init() {
        if (!isInEditMode()) {
            mTitle = getResources().getString(R.string.ids_lbl_delete_jobs_title);
            mOkText = getResources().getString(R.string.ids_lbl_ok);
            mErrorMessage = getResources().getString(R.string.ids_err_msg_delete_failed);
            setOrientation(VERTICAL);
            mHandler = new Handler(this);
        }
    }
    
    private void createView() {
        // create header
        if (!mPrintJobs.isEmpty()) {
            createHeader();
        }
        
        // add print jobs
        for (int i = 0; i < mPrintJobs.size(); i++) {
            createItem(i);
        }
        
        mRowHeight = getResources().getDimensionPixelSize(R.dimen.printjob_row_height);
        
    }
    
    private void createHeader() {
        LayoutInflater factory = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        
        mPrintGroupView = factory.inflate(R.layout.printjobs_group, this, true);
        mPrintJobGroupLayout = (RelativeLayout) mPrintGroupView.findViewById(R.id.printJobsGroupLayout);
        TextView printJobGroupText = (TextView) mPrintGroupView.findViewById(R.id.printJobGroupText);
        Button printJobGroupDelete = (Button) mPrintGroupView.findViewById(R.id.printJobGroupDelete);
        
        printJobGroupText.setText(mPrinter.getName());
        
        printJobGroupDelete.setTag(mPrinter);
        mPrintJobGroupLayout.setOnClickListener(this);
        printJobGroupDelete.setOnClickListener(this);
        
        AppUtils.changeChildrenFont(mPrintJobGroupLayout, SmartDeviceApp.getAppFont());
        
        mJobsLayout = new LinearLayout(getContext());
        mJobsLayout.setOrientation(VERTICAL);
        addView(mJobsLayout, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        if (getResources().getBoolean(R.bool.is_tablet)) {
            mPrintGroupView.findViewById(R.id.printJobDeleteSeparator).setVisibility(INVISIBLE);
        }
    }
    
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
        printJobDeleteBtn.setTag(pj);
        
        switch (pj.getResult()) {
            case SUCCESSFUL:
                printJobSuccess.setVisibility(VISIBLE);
                printJobError.setVisibility(INVISIBLE);
                break;
            case ERROR:
                printJobError.setVisibility(VISIBLE);
                printJobSuccess.setVisibility(INVISIBLE);
                break;
        }
        
        printJobName.setText(pj.getName());
        printJobDate.setText(formatDate(pj.getDate()));
        
        mPrintJobViews.add(tempView);
        mJobsLayout.addView(tempView);
        
        AppUtils.changeChildrenFont((ViewGroup) tempView, SmartDeviceApp.getAppFont());
        
        if (index == mPrintJobs.size() - 1) {
            tempView.findViewById(R.id.printJobSeparator).setVisibility(GONE);
        }
    }
    
    private String formatDate(Date date) {
        String dateStr = DateFormat.getDateFormat(getContext()).format(date);
        String timeStr = DateFormat.getTimeFormat(getContext()).format(date);
        return dateStr + C_SPACE + timeStr;
    }
    
    // toggle collapse/expand of a group view when clicked
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
    
    private void animateExpand(boolean animate) {
        mJobsLayout.setVisibility(View.VISIBLE);
        int totalHeight = mPrintJobViews.size() * mRowHeight;
        
        if (animate) {
            for (int i = 0; i < mPrintJobViews.size(); i++) {
                View child = mPrintJobViews.get(i);
                TranslateAnimation animation = new TranslateAnimation(0, 0, -totalHeight, 0);
                animation.setDuration((int) (totalHeight * DURATION_MULTIPLIER));
                
                if (i == mPrintJobViews.size() - 1) {
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
    
    private void animateCollapse(boolean animate) {
        if (animate) {
            int totalHeight = mPrintJobViews.size() * mRowHeight;
            for (int i = 0; i < mPrintJobViews.size(); i++) {
                View child = mPrintJobViews.get(i);
                TranslateAnimation animation = new TranslateAnimation(0, 0, 0, -totalHeight);
                animation.setDuration((int) (totalHeight * DURATION_MULTIPLIER));
                animation.setFillAfter(true);
                
                if (i == mPrintJobViews.size() - 1) {
                    
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
    
    private void expandGroupView() {
        mPrintJobGroupLayout.setSelected(false);
        if (!getResources().getBoolean(R.bool.is_tablet)) {
            this.findViewById(R.id.printJobGroupSeparator).setVisibility(GONE);
        }
        mPrintJobGroupLayout.setClickable(true);
    }
    
    private void collapseGroupView() {
        mJobsLayout.setVisibility(GONE);
        mPrintJobGroupLayout.setSelected(true);
        mPrintJobGroupLayout.findViewById(R.id.printJobGroupDelete).setSelected(false);
        if (!getResources().getBoolean(R.bool.is_tablet)) {
            this.findViewById(R.id.printJobGroupSeparator).setVisibility(VISIBLE);
        }
        mPrintJobGroupLayout.setClickable(true);
    }
    
    // display delete print jobs dialog when clicked
    private void deleteJobGroup(View v) {
        if (mGroupListener.showDeleteDialog()) {
            mGroupListener.setPrinterToDelete(this, mPrinter);
            v.findViewById(R.id.printJobGroupDelete).setSelected(true);
        }
    }
    
    private void deletePrintJobGroupView() {
        for (int i = 0; i < mPrintJobViews.size(); i++) {
            mGroupListener.deleteJobFromList((PrintJob) mPrintJobViews.get(i).getTag());
        }
        mGroupListener.deletePrinterFromList(mPrinter);
        mLayoutListener.deletePrintJobsGroup(this);
        ((LinearLayout) mPrintGroupView.getParent()).removeView(mPrintGroupView);
    }
    
    // delete a print job when clicked
    private void deletePrintJob(View v) {
        PrintJob job = ((PrintJob) v.getTag());
        PrintJobManager pm = PrintJobManager.getInstance(getContext());
        boolean isSuccess = pm.deleteWithJobId(job.getId());
        if (isSuccess) {
            mGroupListener.deleteJobFromList((PrintJob) v.getTag());
            deletePrintJobView(mPrintGroupView.findViewWithTag(job));
        } else {
            // show dialog
            InfoDialogFragment errordialog = InfoDialogFragment.newInstance(mTitle, mErrorMessage, mOkText);
            DialogUtils.displayDialog((Activity) getContext(), TAG, errordialog);
        }
        mLayoutListener.onDeleteJob();
    }
    
    private void deletePrintJobView(View v) {
        for (int i = 0; i < mPrintJobViews.size(); i++) {
            if (mPrintJobViews.get(i).equals(v)) {
                mPrintJobViews.remove(i);
                mPrintJobs.remove(i);
            }
        }
        mJobsLayout.removeView(v);
        
        if (mPrintJobViews.size() == 0) {
            deletePrintJobGroupView();
        }
    }
    
    // ================================================================================
    // INTERFACE - View.OnClickListener
    // ================================================================================
    /** {@inheritDoc} */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.printJobGroupDelete:
                deleteJobGroup(v);
                break;
            case R.id.printJobDeleteBtn:
                deletePrintJob(v);
                break;
            case R.id.printJobsGroupLayout:
                toggleGroupView(v);
                break;
        }
    }
    
    // ================================================================================
    // INTERFACE - View.onTouchListener
    // ================================================================================
    /** {@inheritDoc} */
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
    /** {@inheritDoc} */
    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_COLLAPSE:
                collapseGroupView();
                return true;
            case MSG_EXPAND:
                expandGroupView();
                return true;
        }
        return false;
    }
    
    // ================================================================================
    // Internal Classes
    // ================================================================================
    
    public interface PrintJobsGroupListener {
        public void deletePrinterFromList(Printer printer);
        
        public void deleteJobFromList(PrintJob printJob);
        
        public void setCollapsed(Printer printer, boolean isCollapsed);
        
        public void setDeletePrintJob(PrintJob job);
        
        public void setPrinterToDelete(PrintJobsGroupView printJobsGroupView, Printer printer);
        
        public boolean showDeleteDialog();
    }
    
    public interface PrintJobsLayoutListener {
        public void deletePrintJobsGroup(PrintJobsGroupView printJobsGroupView);
        
        public void animateGroups(PrintJobsGroupView printJobsGroupView, int totalHeight, float durationMultiplier, boolean isCollapsed);
        
        public void onDeleteJob();
    }
}
