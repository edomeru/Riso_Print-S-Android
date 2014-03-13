package jp.co.riso.smartdeviceapp.view.custom;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jp.co.riso.android.dialog.DialogUtils;
import jp.co.riso.smartdeviceapp.R;
import jp.co.riso.smartdeviceapp.controller.jobs.PrintJobManager;
import jp.co.riso.smartdeviceapp.model.PrintJob;
import jp.co.riso.smartdeviceapp.model.Printer;
import jp.co.riso.smartdeviceapp.view.dialog.PrintJobsDeleteDialog;
import jp.co.riso.smartdeviceapp.view.dialog.PrintJobsDeleteDialog.PrintJobsDeleteDialogListener;
import jp.co.riso.smartdeviceapp.view.dialog.PrintJobsDeleteErrorDialog;
import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.text.format.DateFormat;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class PrintJobsGroupView extends LinearLayout implements View.OnClickListener, PrintJobsDeleteDialogListener {
    
    private static final String TAG = "PrintJobsGroupView";
    private static final String C_SPACE = " ";
    private View mPrintGroupView;
    private List<View> mPrintJobViews = new ArrayList<View>();
    private List<PrintJob> mPrintJobs = new ArrayList<PrintJob>();
    private Printer mPrinter;
    private Button mDeleteBtn;
    private TextView mDateTxt;
    private Context mContext;
    private PrintDeleteListener mDelListener;
    
    private boolean mWithMargin;
    private boolean misCollapsed = false;
    
    public PrintJobsGroupView(Context context, List<PrintJob> printJobs, boolean withMargin, Printer printer, PrintDeleteListener delListener) {
        this(context);
        
        this.mContext = context;
        this.mPrintJobs = printJobs;
        this.mWithMargin = withMargin;
        this.mPrinter = printer;
        this.mDelListener = delListener;
        init(context);
        createView();
        
    }
    
    private PrintJobsGroupView(Context context) {
        super(context);
    }
    
    private void init(Context context) {
        
        if (!isInEditMode()) {
            setOrientation(VERTICAL);
            LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            if (mWithMargin){
                lp.leftMargin = getResources().getDimensionPixelSize(R.dimen.printjob_margin_side);
                lp.rightMargin = getResources().getDimensionPixelSize(R.dimen.printjob_margin_side);
                lp.topMargin = getResources().getDimensionPixelSize(R.dimen.printjob_margin_top);
            }

            setLayoutParams(lp);
            
        }
    }
    
    private void createView() {
        
        LayoutInflater factory = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        
        // create header
        if (!mPrintJobs.isEmpty()) {
            mPrintGroupView = factory.inflate(R.layout.view_printjobgroup, this, true);
            RelativeLayout printJobGroupLayout = (RelativeLayout) mPrintGroupView.findViewById(R.id.printJobsGroupLayout);
            TextView printJobGroupText = (TextView) mPrintGroupView.findViewById(R.id.printJobGroupText);
            Button printJobGroupDelete = (Button) mPrintGroupView.findViewById(R.id.printJobGroupDelete);
            
            printJobGroupText.setText(mPrinter.getPrinterName());
            
            printJobGroupDelete.setTag(mPrinter);
            printJobGroupLayout.setOnClickListener(this);
            printJobGroupDelete.setOnClickListener(this);
        }
        
        // add print jobs
        for (int i = 0; i < mPrintJobs.size(); i++) {
            View tempView = factory.inflate(R.layout.view_printjobdetail, this, false);
            TextView printJobName = (TextView) tempView.findViewById(R.id.printJobName);
            ImageView printJobError = (ImageView) tempView.findViewById(R.id.printJobError);
            ImageView printJobSuccess = (ImageView) tempView.findViewById(R.id.printJobSuccess);
            Button printJobDeleteBtn = (Button) tempView.findViewById(R.id.printJobDeleteBtn);
            TextView printJobDate = (TextView) tempView.findViewById(R.id.printJobDate);
            
            PrintJob pj = mPrintJobs.get(i);
            
            tempView.setTag(pj.getId());
            tempView.setOnTouchListener(new OnSwipeTouchListener(mContext, tempView));
            
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
            addView(tempView, i + 1);
        }
        
    }
    
    private String formatDate(Date date) {
        // SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/M/d HH:mm", Locale.getDefault());
        String dateStr = DateFormat.getDateFormat(mContext).format(date);
        String timeStr = DateFormat.getTimeFormat(mContext).format(date);
        return dateStr + C_SPACE + timeStr;
    }
    
    // toggle collapse/expand of a group view when clicked
    private void toggleGroupView(View v) {
        if (misCollapsed) {
            for (int i = 0; i < mPrintJobViews.size(); i++) {
                mPrintJobViews.get(i).setVisibility(VISIBLE);
            }
            misCollapsed = false;
            ((TextView) v.findViewById(R.id.printJobGroupExpand)).setText(R.string.ids_lbl_collapse);
            
        } else {
            for (int i = 0; i < mPrintJobViews.size(); i++) {
                mPrintJobViews.get(i).setVisibility(GONE);
            }
            misCollapsed = true;
            ((TextView) v.findViewById(R.id.printJobGroupExpand)).setText(R.string.ids_lbl_expand);
        }
        
    }
    
    // display delete print jobs dialog when clicked
    private void deleteJobGroup(View v) {
        int printerId = ((Printer) v.getTag()).getPrinterId();
        PrintJobsDeleteDialog dialog = PrintJobsDeleteDialog.newInstance(printerId);
        dialog.setListener(this);
        DialogUtils.displayDialog((Activity) mContext, TAG, dialog);
    }
    
    // delete a print job when clicked
    private void deletePrintJob(View v) {
        int jobId = ((PrintJob) v.getTag()).getId();
        View viewToRemove = mPrintGroupView.findViewWithTag(jobId);
        boolean isSuccess = PrintJobManager.deleteWithJobId(jobId);
        
        if (isSuccess) {
            for (int i = 0; i < mPrintJobViews.size(); i++) {
                if (mPrintJobViews.get(i).equals(viewToRemove)) {
                    mPrintJobViews.remove(i);
                    mPrintJobs.remove(i);
                }
            }
            
            ((LinearLayout) mPrintGroupView).removeView(viewToRemove);
            
            if (mPrintJobViews.size() == 0) {
                ((LinearLayout) mPrintGroupView.getParent()).removeView(mPrintGroupView);
            }
        } else {
            // show dialog
            PrintJobsDeleteErrorDialog dialog = PrintJobsDeleteErrorDialog.newInstance();
            DialogUtils.displayDialog((Activity) mContext, TAG, dialog);
        }
    }
    
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
    
    @Override
    public void onDelete(int printerId) {
        boolean isSuccess = PrintJobManager.deleteWithPrinterId(printerId);
        if (isSuccess) {
            ((LinearLayout) mPrintGroupView.getParent()).removeView(mPrintGroupView);
            mPrintJobViews.clear();
            mPrintJobs.clear();
        } else {
            // show dialog
            PrintJobsDeleteErrorDialog dialog = PrintJobsDeleteErrorDialog.newInstance();
            DialogUtils.displayDialog((Activity) mContext, TAG, dialog);
        }
    }
    
    public void clearDeleteButton() {
        
        if (mDeleteBtn != null) {
            mDeleteBtn.setVisibility(INVISIBLE);
            mDeleteBtn = null;
        }
        if (mDateTxt != null) {
            mDateTxt.setVisibility(VISIBLE);
            mDateTxt = null;
        }
    }
    
    private class OnSwipeTouchListener extends SimpleOnGestureListener implements OnTouchListener {
        private static final int SWIPE_DISTANCE_THRESHOLD = 50;
        private final GestureDetector gestureDetector;
        private View viewTouched;
        
        public OnSwipeTouchListener(Context context, View view) {
            gestureDetector = new GestureDetector(context, this);
            this.viewTouched = view;
        }
        
        public void showDeleteButton(View view) {
            mDeleteBtn = (Button) view.findViewById(R.id.printJobDeleteBtn);
            mDeleteBtn.setVisibility(VISIBLE);
            mDateTxt = (TextView) view.findViewById(R.id.printJobDate);
            mDateTxt.setVisibility(INVISIBLE);
            mDelListener.setPrintJobsGroupView(PrintJobsGroupView.this);
        }
        
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
    
    public interface PrintDeleteListener {
        public void setPrintJobsGroupView(PrintJobsGroupView printJobsGroupView);
        
        public void clearButton();
    }
}
