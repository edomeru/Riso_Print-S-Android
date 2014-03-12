package jp.co.riso.smartdeviceapp.view.custom;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import jp.co.riso.android.dialog.DialogUtils;
import jp.co.riso.smartdeviceapp.R;
import jp.co.riso.smartdeviceapp.controller.PrintJobManager;
import jp.co.riso.smartdeviceapp.model.PrintJob;
import jp.co.riso.smartdeviceapp.model.Printer;
import jp.co.riso.smartdeviceapp.view.dialog.PrintJobsDeleteDialog;
import jp.co.riso.smartdeviceapp.view.dialog.PrintJobsDeleteDialog.PrintJobsDeleteDialogListener;
import jp.co.riso.smartdeviceapp.view.dialog.PrintJobsDeleteErrorDialog;
import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.util.Log;
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
    private View printGroupView;
    private List<View> printJobViews = new ArrayList<View>();
    private List<PrintJob> printJobs = new ArrayList<PrintJob>();
    private Printer printer;
    private Button deleteBtn;
    private TextView dateTxt;
    private Context context;
    private PrintDeleteListener delListener;
    
    private boolean withMargin;
    private boolean isCollapsed = false;
    
    public PrintJobsGroupView(Context context, List<PrintJob> printJobs, boolean withMargin, Printer printer, PrintDeleteListener delListener) {
        this(context);
        
        this.context = context;
        this.printJobs = printJobs;
        this.withMargin = withMargin;
        this.printer = printer;
        this.delListener = delListener;
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
            if (withMargin)
                lp.setMargins(2, 4, 2, 0);
            setLayoutParams(lp);
            
        }
    }
    
    private void createView() {
        
        LayoutInflater factory = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        
        // create header
        if (!printJobs.isEmpty()) {
            printGroupView = factory.inflate(R.layout.view_printjobgroup, this, true);
            RelativeLayout printJobGroupLayout = (RelativeLayout) printGroupView.findViewById(R.id.printJobsGroupLayout);
            TextView printJobGroupText = (TextView) printGroupView.findViewById(R.id.printJobGroupText);
            Button printJobGroupDelete = (Button) printGroupView.findViewById(R.id.printJobGroupDelete);
            
            printJobGroupText.setText(printer.getPrinterName());
            
            printJobGroupDelete.setTag(printer);
            printJobGroupLayout.setOnClickListener(this);
            printJobGroupDelete.setOnClickListener(this);
        }
        
        // add print jobs
        for (int i = 0; i < printJobs.size(); i++) {
            View tempView = factory.inflate(R.layout.view_printjobdetail, this, false);
            TextView printJobName = (TextView) tempView.findViewById(R.id.printJobName);
            ImageView printJobError = (ImageView) tempView.findViewById(R.id.printJobError);
            ImageView printJobSuccess = (ImageView) tempView.findViewById(R.id.printJobSuccess);
            Button printJobDeleteBtn = (Button) tempView.findViewById(R.id.printJobDeleteBtn);
            TextView printJobDate = (TextView) tempView.findViewById(R.id.printJobDate);
            
            PrintJob pj = printJobs.get(i);
            
            tempView.setTag(pj);
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
            
            printJobViews.add(tempView);
            addView(tempView, i + 1);
        }
        
    }
    
    private String formatDate(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/M/d HH:mm", Locale.getDefault());
        
        return dateFormat.format(date);
    }
    
    // toggle collapse/expand of a group view when clicked
    private void toggleGroupView(View v) {
        if (isCollapsed) {
            for (int i = 0; i < printJobViews.size(); i++) {
                printJobViews.get(i).setVisibility(VISIBLE);
            }
            isCollapsed = false;
            ((TextView) v.findViewById(R.id.printJobGroupExpand)).setText(R.string.ids_lbl_collapse);
            
        } else {
            for (int i = 0; i < printJobViews.size(); i++) {
                printJobViews.get(i).setVisibility(GONE);
            }
            isCollapsed = true;
            ((TextView) v.findViewById(R.id.printJobGroupExpand)).setText(R.string.ids_lbl_expand);
        }
        
    }
    
    // display delete print jobs dialog when clicked
    private void deleteJobGroup(View v) {
        int printerId = ((Printer) v.getTag()).getPrinterId();
        PrintJobsDeleteDialog dialog = PrintJobsDeleteDialog.newInstance(printerId);
        dialog.setListener(this);
        DialogUtils.displayDialog((Activity) context, TAG, dialog);
    }
    
    // delete a print job when clicked
    private void deletePrintJob(View v) {
        PrintJob job = (PrintJob) v.getTag();
        Log.d(TAG, "deletePrintJob" + printJobs.size());
        boolean isSuccess = PrintJobManager.deleteWithJobId(job.getId());
        if (isSuccess) {
            printGroupView.findViewWithTag(job).setVisibility(GONE);
            
            for (int i = 0; i < printJobViews.size(); i++) {
                if (printJobViews.get(i).equals(printGroupView.findViewWithTag(job))) {
                    printJobViews.remove(i);
                    printJobs.remove(i);
                    Log.d(TAG, "delete at " + i);
                }
            }
            if (printJobViews.size() == 0) {
                printGroupView.setVisibility(GONE);
            }
        } else {
            // show dialog
            PrintJobsDeleteErrorDialog dialog = PrintJobsDeleteErrorDialog.newInstance();
            DialogUtils.displayDialog((Activity) context, TAG, dialog);
        }
    }
    
    @Override
    public void onClick(View v) {
        
        if (v.getId() == R.id.printJobGroupDelete) {
            deleteJobGroup(v);
            
        } else if (v.getId() == R.id.printJobDeleteBtn) {
            deletePrintJob(v);
        } else if (v.getId() == R.id.printJobsGroupLayout) {// printGroupView.getId()) {
            toggleGroupView(v);
        }
        delListener.clearButton();
    }
    
    @Override
    public void onDelete(int printerId) {
        Log.d(TAG, "onDelete" + printJobs.size());
        boolean isSuccess = PrintJobManager.deleteWithPrinterId(printerId);
        // reset screen
        if (isSuccess) {
            printGroupView.setVisibility(GONE);
            printJobViews.clear();
            printJobs.clear();
        } else {
            // show dialog
            PrintJobsDeleteErrorDialog dialog = PrintJobsDeleteErrorDialog.newInstance();
            DialogUtils.displayDialog((Activity) context, TAG, dialog);
        }
    }
    
    public void clearDeleteButton() {
        
        if (deleteBtn != null) {
            deleteBtn.setVisibility(INVISIBLE);
            deleteBtn = null;
        }
        if (dateTxt != null) {
            dateTxt.setVisibility(VISIBLE);
            dateTxt = null;
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
            deleteBtn = (Button) view.findViewById(R.id.printJobDeleteBtn);
            deleteBtn.setVisibility(VISIBLE);
            dateTxt = (TextView) view.findViewById(R.id.printJobDate);
            dateTxt.setVisibility(INVISIBLE);
            delListener.setPrintJobsGroupView(PrintJobsGroupView.this);
        }
        
        public boolean onTouch(View v, MotionEvent event) {
            delListener.clearButton();
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
