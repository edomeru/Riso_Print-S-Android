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
import android.app.Activity;

import android.content.Context;

import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;

import android.view.View;
import android.view.GestureDetector.SimpleOnGestureListener;

import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class PrintJobsGroupView extends LinearLayout implements View.OnClickListener, PrintJobsDeleteDialogListener {
    
    private static final String TAG = "PrintJobsGroupView";
    private View printGroupView;
    private List<View> printJobViews = new ArrayList<View>();
    private List<PrintJob> printJobs = new ArrayList<PrintJob>();
    private boolean withMargin;
    private Printer printer;
    private Button deleteBtn;
    private TextView dateTxt;
    
    // public void setData(List<PrintJob> printJobs, boolean withMargin, Printer printer, View.OnClickListener
    // deleteListener) {
    // this.printJobs = printJobs;
    // this.withMargin = withMargin;
    // this.printer = printer;
    
    // reset();
    // }
    
    Context context;
    boolean isCollapsed = false;
    
    // private int viewWidth;
    // private int viewHeight;
    
    public PrintJobsGroupView(Context context, List<PrintJob> printJobs, boolean withMargin, Printer printer, View.OnClickListener deleteListener) {
        super(context);
        // TODO Auto-generated constructor stub
        
        this.printJobs = printJobs;
        this.withMargin = withMargin;
        this.printer = printer;
        
        // reset();
        
        init(context);
        reset();
        
    }
    
    // public PrintJobsGroupView(Context context) {
    // super(context);
    // // TODO Auto-generated constructor stub
    //
    // init(context);
    // reset();
    //
    // }
    // public PrintJobsGroupView(Context context, AttributeSet attrs, int defStyle) {
    // super(context, attrs, defStyle);
    // // TODO Auto-generated constructor stub
    // init(context);
    // reset();
    // }
    //
    // public PrintJobsGroupView(Context context, AttributeSet attrs) {
    // super(context, attrs);
    // // TODO Auto-generated constructor stub
    // init(context);
    // reset();
    // }
    
    private void init(Context context) {
        this.context = context;
        
        // setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        
        if (!isInEditMode()) {
            setOrientation(VERTICAL);
            LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            if (withMargin)
                lp.setMargins(2, 4, 2, 0);
            setLayoutParams(lp);
            
        }
    }
    
    private void reset() {
        
        LayoutInflater factory = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        // printGroupView = factory.inflate(R.layout.list_group, null,false);
        if (!printJobs.isEmpty()) {
            printGroupView = factory.inflate(R.layout.view_printjobgroup, this, true);
            // printGroupView.setOnClickListener(this);
            RelativeLayout printJobGroupLayout = (RelativeLayout) printGroupView.findViewById(R.id.printJobsGroupLayout);
            printJobGroupLayout.setOnClickListener(this);
            TextView printJobGroupText = (TextView) printGroupView.findViewById(R.id.printJobGroupText);
            printJobGroupText.setText(printer.getPrinterName());
            Button printJobGroupDelete = (Button) printGroupView.findViewById(R.id.printJobGroupDelete);
            printJobGroupDelete.setTag(printer);
            printJobGroupDelete.setOnClickListener(this);
            
        }
        
        PrintJob pj = null;
        //
        for (int i = 0; i < printJobs.size(); i++) {
            pj = printJobs.get(i);
            // // View tempView = factory.inflate(R.layout.list_item,null,false);
            View tempView = factory.inflate(R.layout.view_printjobdetail, this, false);
            TextView printJobName = (TextView) tempView.findViewById(R.id.printJobName);
            ImageView printJobError = (ImageView) tempView.findViewById(R.id.printJobError);
            ImageView printJobSuccess = (ImageView) tempView.findViewById(R.id.printJobSuccess);
            
            final Button printJobDeleteBtn = (Button) tempView.findViewById(R.id.printJobDeleteBtn);
            final TextView printJobDate = (TextView) tempView.findViewById(R.id.printJobDate);
            printJobDeleteBtn.setOnClickListener(this);
            printJobDeleteBtn.setTag(pj);
            tempView.setTag(pj);
            printJobDate.setTag("text tag");
            // tempView.setOnTouchListener(new OnSwipeTouchListener(context, tempView) {
            // @Override
            // public void onSwipe(View view) {
            // Toast.makeText(context, "swipe", Toast.LENGTH_SHORT).show();
            // view.findViewById(R.id.printJobDeleteBtn).setVisibility(VISIBLE);
            // view.findViewById(R.id.printJobDate).setVisibility(INVISIBLE);
            // }
            // });
            tempView.setOnTouchListener(new OnSwipeTouchListener(context, tempView));
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
            printJobDate.setText(convertDate(pj.getDate()));
            
            addView(tempView, i + 1);
            printJobViews.add(tempView);
            
        }
        
    }
    
    private String convertDate(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/M/d HH:mm", Locale.getDefault());
        
        return dateFormat.format(date);
    }
    
    @Override
    public void onClick(View v) {
        
        // TODO Auto-generated method stub
        
        // if (v.getId() == R.id.printJobGroupDelete) {
        // Log.d(TAG, "delete printgroup");
        // Toast.makeText(c, "delete group", Toast.LENGTH_SHORT).show();
        // PrintJobManager.deleteWithPrinterId(((Printer)v.getTag()).getPrinterId());
        // } else if (v.getId() == R.id.printJobsGroupLayout) {// printGroupView.getId()) {
        // Toast.makeText(c, "collapsed", Toast.LENGTH_SHORT).show();
        // Log.d(TAG, "printGroupView" + ((TextView) v.findViewById(R.id.printJobGroupExpand)).getText());
        // if (isCollapsed) {
        // for (int i = 0; i < printJobViews.size(); i++) {
        // printJobViews.get(i).setVisibility(VISIBLE);
        //
        // }
        // isCollapsed = false;
        // ((TextView) v.findViewById(R.id.printJobGroupExpand)).setText(R.string.ids_lbl_collapse);
        //
        // } else {
        // for (int i = 0; i < printJobViews.size(); i++) {
        // printJobViews.get(i).setVisibility(GONE);
        // }
        // isCollapsed = true;
        // ((TextView) v.findViewById(R.id.printJobGroupExpand)).setText(R.string.ids_lbl_expand);
        // }
        //
        // } else {
        // if (v.getId() == R.id.printJobDeleteBtn) {
        // Log.d(TAG, "delete printJobView");
        // Toast.makeText(c, "delete", Toast.LENGTH_SHORT).show();
        // }
        //
        // }
        
        // TODO Auto-generated method stub
        if (v.getId() == R.id.printJobGroupDelete) {
            Log.d(TAG, "delete printgroup");
            Toast.makeText(context, "delete group", Toast.LENGTH_SHORT).show();
            deleteJobGroup(((Printer) v.getTag()).getPrinterId());
        } else {
            if (v.getId() == R.id.printJobDeleteBtn) {
                Log.d(TAG, "delete printJobView");
                deletePrintJob((PrintJob) v.getTag());
                
                // v.findViewById(R.id.printJobStatus)
                // for (int i =0; i<getChildCount(); i++){
                // getChildAt(i).
                // }
            }
            
        }
        if (v.getId() == R.id.printJobsGroupLayout) {// printGroupView.getId()) {
            Toast.makeText(context, "collapsed", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "printGroupView" + ((TextView) v.findViewById(R.id.printJobGroupExpand)).getText());
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
    }
    
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) { // TODO Auto-generated method stub final
    
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
    
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        // TODO Auto-generated method stub
        super.onLayout(changed, l, t, r, b);
        // viewWidth = r - l;
        // viewHeight = b - t;
        // Log.d(TAG, changed+"onLayout group " + " " + (viewWidth) + " " + (viewHeight));
        
    }
    
    // @Override
    // protected void onSizeChanged(int w, int h, int xOld, int yOld) {
    // super.onSizeChanged(w, h, xOld, yOld);
    //
    // setViewWidth(w);
    // setViewHeight(h);
    //
    // }
    //
    // public int getViewWidth() {
    // return viewWidth;
    // }
    //
    // public void setViewWidth(int viewWidth) {
    // this.viewWidth = viewWidth;
    // }
    //
    // public int getViewHeight() {
    // return viewHeight;
    // }
    //
    // public void setViewHeight(int viewHeight) {
    // this.viewHeight = viewHeight;
    // }
    
    private void deleteJobGroup(int printerId) {
        // // TODO Auto-generated method stub
        // PrintJobManager.deleteWithPrinterId(printerId);
        // // reset screen
        // printGroupView.setVisibility(GONE);
        // printJobViews.clear();
        // // printJobs.clear();
        // already zero since printjob list is deleted from printJobsColumnView
        
        // showDialog();
        PrintJobsDeleteDialog dialog = PrintJobsDeleteDialog.newInstance(printerId);
        dialog.setListener(this);
        
        DialogUtils.displayDialog((Activity) context, "CES", dialog);
    }
    
    private void deletePrintJob(PrintJob job) {
        PrintJobManager.deleteWithJobId(job.getId());
        Toast.makeText(context, "delete " + job.getId() + job.getName(), Toast.LENGTH_SHORT).show();
        // reset screen
        Log.d(TAG, "delete " + job.getId() + " " + printGroupView.findViewWithTag(job));// .setVisibility(GONE);
        printGroupView.findViewWithTag(job).setVisibility(GONE);
        Log.d(TAG, "delete " + printGroupView.findViewWithTag("text tag"));
        Log.d(TAG, "delete " + printJobs.size() + " " + printJobViews.size());
        
        for (int i = 0; i < printJobViews.size(); i++) {
            if (printJobViews.get(i).equals(printGroupView.findViewWithTag(job))) {
                // if (printJobViews.get(i).getTag().equals(job)){
                printJobViews.remove(i);
                // printJobs.remove(i); //already zero since printjob list is deleted from printJobsColumnView
                Log.d(TAG, "delete at " + i);
            }
        }
        if (printJobViews.size() == 0) {
            printGroupView.setVisibility(GONE);
        }
        
    }
    
    // @Override
    // public void onClick(DialogInterface dialog, int which) {
    // // TODO Auto-generated method stub
    // switch (which) {
    // case Dialog.BUTTON_POSITIVE:
    // onDelete();
    // break;
    // case Dialog.BUTTON_NEGATIVE:
    // //dismiss
    // break;
    // default:
    // Log.d(TAG, "action not supported");
    // break;
    // }
    // dialog.dismiss();
    //
    // }
    //
    
    // private void showDialog(){
    // AlertDialog.Builder builder = new AlertDialog.Builder(context);
    // AlertDialog alert = null;
    // builder.setTitle("Delete Print Jobs");
    // builder.setMessage("Are you sure you want to delete these print jobs" + "?");
    // builder.setPositiveButton(R.string.ids_lbl_ok, this);
    //
    // builder.setNegativeButton(R.string.ids_lbl_cancel, this);
    //
    // alert = builder.create();
    // alert.setCanceledOnTouchOutside(false);
    // alert.show();
    // }
    
    @Override
    public void onDelete(int printerId) {
        PrintJobManager.deleteWithPrinterId(printerId);
        // reset screen
        printGroupView.setVisibility(GONE);
        printJobViews.clear();
        // printJobs.clear();
        // already zero since printjob list is deleted from printJobsColumnView
    }
    
    private class OnSwipeTouchListener extends SimpleOnGestureListener implements OnTouchListener {
        private static final int SWIPE_DISTANCE_THRESHOLD = 50;
        private final GestureDetector gestureDetector;
        private View view;
        
        public OnSwipeTouchListener(Context context, View view) {
            gestureDetector = new GestureDetector(context, this);
            this.view = view;
        }
        
        public void onSwipe(View view) {
            deleteBtn = (Button) view.findViewById(R.id.printJobDeleteBtn);
            deleteBtn.setVisibility(VISIBLE);
            dateTxt = (TextView) view.findViewById(R.id.printJobDate);
            dateTxt.setVisibility(INVISIBLE);
        }
        
        public boolean onTouch(View v, MotionEvent event) {
            return gestureDetector.onTouchEvent(event);
        }
        
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }
        
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float x = Math.abs(e2.getX() - e1.getX());
            float y = Math.abs(e2.getY() - e1.getY());
            if (x > y && x > SWIPE_DISTANCE_THRESHOLD) {
//                if (e1.getX() <= view.getLeft() 
//                        && e2.getX() >= view.getRight() 
//                        && e1.getY() <= view.getTop() 
//                        && e2.getY() >= view.getRight())
                Log.d("CESTEST", "xxyy" + e1.getX() + " " + e2.getX() +" "+ e1.getY() + " " + e2.getY());

                Log.d("CESTEST", "onfling" + view.getLeft() + " " + view.getRight() +" "+view.getTop() + " " + view.getBottom());
                    onSwipe(view);
                return true;
            }
            return false;
        }
    }
}
