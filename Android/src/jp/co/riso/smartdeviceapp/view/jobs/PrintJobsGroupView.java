/*
 * Copyright (c) 2014 All rights reserved.
 *
 * PrintJobsGroupView.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.view.jobs;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jp.co.riso.android.dialog.ConfirmDialogFragment;
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
import android.content.DialogInterface;
import android.graphics.Rect;
import android.text.format.DateFormat;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class PrintJobsGroupView extends LinearLayout implements View.OnClickListener, DialogInterface.OnClickListener {
    
    private static final String TAG = "PrintJobsGroupView";
    private static final String C_SPACE = " ";
    private View mPrintGroupView;
    private List<View> mPrintJobViews = new ArrayList<View>();
    private List<PrintJob> mPrintJobs = new ArrayList<PrintJob>();
    private Printer mPrinter;
    private View mViewToDelete;
    private JobDeleteListener mDelListener;
    
    private boolean mIsMultColumn;
    private boolean mIsCollapsed = false;
    private String mTitle;
    private String mMessage;
    private String mErrorMessage;
    private String mConfirmMsg;
    private String mCancelMsg;
    
    public PrintJobsGroupView(Context context, List<PrintJob> printJobs, boolean isMultColumn, Printer printer, JobDeleteListener delListener) {
        this(context);
        
        this.mPrintJobs = printJobs;
        this.mIsMultColumn = isMultColumn;
        this.mPrinter = printer;
        this.mDelListener = delListener;
        init(context);
        
    }
    
    private PrintJobsGroupView(Context context) {
        super(context);
    }
    
    private void init(Context context) {
        
        if (!isInEditMode()) {
            mTitle = getResources().getString(R.string.ids_lbl_delete_jobs_title);
            mMessage = getResources().getString(R.string.ids_lbl_delete_jobs_msg);
            mConfirmMsg = getResources().getString(R.string.ids_lbl_ok);
            mCancelMsg = getResources().getString(R.string.ids_lbl_cancel);
            mErrorMessage = getResources().getString(R.string.ids_err_msg_delete_failed);
            
            LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            if (mIsMultColumn) {
                lp.leftMargin = getResources().getDimensionPixelSize(R.dimen.printjob_margin_side);
                lp.rightMargin = getResources().getDimensionPixelSize(R.dimen.printjob_margin_side);
                lp.topMargin = getResources().getDimensionPixelSize(R.dimen.printjob_margin_top);
            }
            setOrientation(VERTICAL);
            setLayoutParams(lp);
            
            createView(context);
        }
    }
    
    private void createView(Context context) {
        
        // create header
        if (!mPrintJobs.isEmpty()) {
            createHeader(context);
        }
        
        // add print jobs
        for (int i = 0; i < mPrintJobs.size(); i++) {
            createItem(context, i);
            
        }
        
        mPrintJobs.clear();
        mPrintJobs = null;
        
    }
    
    private void createHeader(Context context) {
        LayoutInflater factory = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        
        mPrintGroupView = factory.inflate(R.layout.printjobs_group, this, true);
        RelativeLayout printJobGroupLayout = (RelativeLayout) mPrintGroupView.findViewById(R.id.printJobsGroupLayout);
        TextView printJobGroupText = (TextView) mPrintGroupView.findViewById(R.id.printJobGroupText);
        Button printJobGroupDelete = (Button) mPrintGroupView.findViewById(R.id.printJobGroupDelete);
        
        printJobGroupText.setText(mPrinter.getName());
        
        printJobGroupDelete.setTag(mPrinter);
        printJobGroupLayout.setOnClickListener(this);
        printJobGroupDelete.setOnClickListener(this);
        
        AppUtils.changeChildrenFont((ViewGroup) mPrintGroupView, SmartDeviceApp.getAppFont());
        
        if (mIsMultColumn) {
            mPrintGroupView.findViewById(R.id.printJobDeleteSeparator).setVisibility(INVISIBLE);
        }
    }
    
    private void createItem(Context context, int index) {
        LayoutInflater factory = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View tempView = factory.inflate(R.layout.printjobs_item, this, false);
        TextView printJobName = (TextView) tempView.findViewById(R.id.printJobName);
        ImageView printJobError = (ImageView) tempView.findViewById(R.id.printJobError);
        ImageView printJobSuccess = (ImageView) tempView.findViewById(R.id.printJobSuccess);
        Button printJobDeleteBtn = (Button) tempView.findViewById(R.id.printJobDeleteBtn);
        TextView printJobDate = (TextView) tempView.findViewById(R.id.printJobDate);
        
        PrintJob pj = mPrintJobs.get(index);
        
        tempView.setTag(pj.getId());
        tempView.setOnTouchListener(new OnSwipeTouchListener(context, tempView));
        
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
        addView(tempView, index + 1);
        
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
        if (mIsCollapsed) {
            expandGroup(v);
            mIsCollapsed = false;
        } else {
            collapseGroup(v);
            mIsCollapsed = true;
        }
        
    }
    
    private void expandGroup(View v) {
        for (int i = 0; i < mPrintJobViews.size(); i++) {
            mPrintJobViews.get(i).setVisibility(VISIBLE);
        }
        
        v.findViewById(R.id.printJobGroupExpand).setVisibility(INVISIBLE);
        v.findViewById(R.id.printJobGroupCollapse).setVisibility(VISIBLE);
        if (!mIsMultColumn)
            this.findViewById(R.id.printJobGroupSeparator).setVisibility(GONE);
    }
    
    private void collapseGroup(View v) {
        for (int i = 0; i < mPrintJobViews.size(); i++) {
            mPrintJobViews.get(i).setVisibility(GONE);
        }
        
        v.findViewById(R.id.printJobGroupExpand).setVisibility(VISIBLE);
        v.findViewById(R.id.printJobGroupCollapse).setVisibility(INVISIBLE);
        if (!mIsMultColumn)
            this.findViewById(R.id.printJobGroupSeparator).setVisibility(VISIBLE);
    }
    
    // display delete print jobs dialog when clicked
    private void deleteJobGroup(View v) {
        
        ConfirmDialogFragment dialog = ConfirmDialogFragment.newInstance(mTitle, mMessage, mConfirmMsg, mCancelMsg);
        dialog.setListener(this);
        DialogUtils.displayDialog((Activity) getContext(), TAG, dialog);
    }
    
    private void deletePrintJobGroupView() {
        ((LinearLayout) mPrintGroupView.getParent()).removeView(mPrintGroupView);
    }
    
    // delete a print job when clicked
    private void deletePrintJob(View v) {
        int jobId = ((PrintJob) v.getTag()).getId();
        boolean isSuccess = PrintJobManager.deleteWithJobId(jobId);
        
        if (isSuccess) {
            deletePrintJobView(mPrintGroupView.findViewWithTag(jobId));
        } else {
            // show dialog
            InfoDialogFragment errordialog = InfoDialogFragment.newInstance(mTitle, mErrorMessage, mConfirmMsg);
            DialogUtils.displayDialog((Activity) getContext(), TAG, errordialog);
        }
    }
    
    private void deletePrintJobView(View v) {
        for (int i = 0; i < mPrintJobViews.size(); i++) {
            if (mPrintJobViews.get(i).equals(v)) {
                mPrintJobViews.remove(i);
            }
        }
        
        ((LinearLayout) mPrintGroupView).removeView(v);
        
        if (mPrintJobViews.size() == 0) {
            deletePrintJobGroupView();
        }
    }
    
    public void clearDeleteButton() {
        
        if (mViewToDelete != null) {
            mViewToDelete.findViewById(R.id.printJobDeleteBtn).setVisibility(INVISIBLE);
            mViewToDelete.findViewById(R.id.printJobDate).setVisibility(VISIBLE);
            mViewToDelete.setBackgroundColor(getResources().getColor(R.color.theme_light_3));
            mViewToDelete = null;
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
        mDelListener.clearButton();
    }
    
    // ================================================================================
    // INTERFACE - DialogInterface.OnClickListener
    // ================================================================================
    
    /** {@inheritDoc} */
    @Override
    public void onClick(DialogInterface dialog, int which) {
        
        boolean isSuccess = PrintJobManager.deleteWithPrinterId(mPrinter.getId());
        if (isSuccess) {
            deletePrintJobGroupView();
            mPrintJobViews.clear();
        } else {
            // show dialog
            InfoDialogFragment errordialog = InfoDialogFragment.newInstance(mTitle, mErrorMessage, mConfirmMsg);
            DialogUtils.displayDialog((Activity) getContext(), TAG, errordialog);
        }
    }
    
    // ================================================================================
    // Internal Classes
    // ================================================================================
    
    private class OnSwipeTouchListener extends SimpleOnGestureListener implements OnTouchListener {
        private static final int SWIPE_DISTANCE_THRESHOLD = 50;
        private final GestureDetector gestureDetector;
        private View viewTouched;
        
        public OnSwipeTouchListener(Context context, View view) {
            gestureDetector = new GestureDetector(context, this);
            this.viewTouched = view;
        }
        
        public void showDeleteButton(View view) {
            view.findViewById(R.id.printJobDeleteBtn).setVisibility(VISIBLE);
            view.findViewById(R.id.printJobDate).setVisibility(INVISIBLE);
            view.setBackgroundColor(getResources().getColor(R.color.theme_color_1));
            mDelListener.setPrintJobsGroupView(PrintJobsGroupView.this);
            mViewToDelete = view;
        }
        
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mDelListener.clearButton();
            return gestureDetector.onTouchEvent(event);
        }
        
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }
        
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            int coords[] = new int[2];
            
            viewTouched.getLocationOnScreen(coords);
            
            Rect rect = new Rect(coords[0], coords[1], coords[0] + viewTouched.getWidth(), coords[1] + viewTouched.getHeight());
            
            float x = Math.abs(e2.getX() - e1.getX());
            boolean containsE1 = rect.contains((int) e1.getRawX(), (int) e1.getRawY());
            boolean containsE2 = rect.contains((int) e2.getRawX(), (int) e2.getRawY());
            boolean swiped = x > SWIPE_DISTANCE_THRESHOLD;
            
            if (containsE1 && containsE2 && swiped) {
                showDeleteButton(viewTouched);
                return true;
            }
            return false;
        }
    }
    
    public interface JobDeleteListener {
        public void setPrintJobsGroupView(PrintJobsGroupView printJobsGroupView);
        
        public void clearButton();
    }
    
}
