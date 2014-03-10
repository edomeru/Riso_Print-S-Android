package jp.co.riso.smartdeviceapp.view.custom;

import java.util.ArrayList;
import java.util.List;

import jp.co.riso.smartdeviceapp.R;
import jp.co.riso.smartdeviceapp.controller.OnSwipeTouchListener;
import jp.co.riso.smartdeviceapp.model.PrintJob;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class PrintJobsGroupView extends LinearLayout implements View.OnClickListener {
    
    private View printGroupView;
    private List<View> printJobViews = new ArrayList<View>();
    private List<PrintJob> printJobs = new ArrayList<PrintJob>();
    private boolean withMargin;
    private String printerName;
    
    public void setData(List<PrintJob> printJobs, boolean withMargin, String printerName) {
        this.printJobs = printJobs;
        this.withMargin = withMargin;
        this.printerName = printerName;
        reset();
    }
    
    Context c;
    boolean isCollapsed = false;
    private int viewWidth;
    private int viewHeight;
    
    public PrintJobsGroupView(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
        init(context);
        reset();
        
    }
    
    public PrintJobsGroupView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
        init(context);
        reset();
    }
    
    public PrintJobsGroupView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
        init(context);
        reset();
    }
    
    private void init(Context context) {
        c = context;
        
        // setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        
        if (!isInEditMode()) {
            setOrientation(VERTICAL);
            LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            if (withMargin)
                lp.setMargins(5, 5, 5, 5);
            setLayoutParams(lp);
            
        }
    }
    
    private void reset() {
        
        LayoutInflater factory = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        // printGroupView = factory.inflate(R.layout.list_group, null,false);
        if (!printJobs.isEmpty()) {
            printGroupView = factory.inflate(R.layout.view_printjobgroup, this, true);
            // printGroupView.setOnClickListener(this);
            RelativeLayout printJobGroupLayout = (RelativeLayout) printGroupView.findViewById(R.id.printJobsGroupLayout);
            printJobGroupLayout.setOnClickListener(this);
            TextView printJobGroupText = (TextView) printGroupView.findViewById(R.id.printJobGroupText);
            printJobGroupText.setText(printerName);
            Button printJobGroupDelete = (Button) printGroupView.findViewById(R.id.printJobGroupDelete);
            printJobGroupDelete.setOnClickListener(this);
            
        }
        
        PrintJob pj = null;
        //
        for (int i = 0; i < printJobs.size(); i++) {
            pj = printJobs.get(i);
            // // View tempView = factory.inflate(R.layout.list_item,null,false);
            View tempView = factory.inflate(R.layout.view_printjobdetail, this, false);
            TextView printJobName = (TextView) tempView.findViewById(R.id.printJobName);
            final Button printJobDeleteBtn = (Button) tempView.findViewById(R.id.printJobDeleteBtn);
            final TextView printJobDate = (TextView) tempView.findViewById(R.id.printJobDate);
            printJobDeleteBtn.setOnClickListener(this);
            tempView.setOnTouchListener(new OnSwipeTouchListener(c) {
                @Override
                public void onSwipe() {
                    Toast.makeText(c, "swipe", Toast.LENGTH_SHORT).show();
                    printJobDeleteBtn.setVisibility(VISIBLE);
                    printJobDate.setVisibility(INVISIBLE);
                }
            });
            printJobName.setText(pj.getName());
            printJobDate.setText(pj.getDate().toString());
            
            addView(tempView, i + 1);
            printJobViews.add(tempView);
            
        }
        
    }
    
    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        
        if (v.getId() == R.id.printJobGroupDelete) {
            Log.d("CESTEST", "delete printgroup");
            Toast.makeText(c, "delete group", Toast.LENGTH_SHORT).show();
        } else if (v.getId() == R.id.printJobsGroupLayout) {// printGroupView.getId()) {
            Toast.makeText(c, "collapsed", Toast.LENGTH_SHORT).show();
            Log.d("CESTEST", "printGroupView" + ((TextView) v.findViewById(R.id.printJobGroupExpand)).getText());
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
            
        } else {
            if (v.getId() == R.id.printJobDeleteBtn) {
                Log.d("CESTEST", "delete printJobView");
                Toast.makeText(c, "delete", Toast.LENGTH_SHORT).show();
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
        viewWidth = r - l;
        viewHeight = b - t;
        // Log.d("CESTEST", changed+"onLayout group " + " " + (viewWidth) + " " + (viewHeight));
        
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int xOld, int yOld) {
        super.onSizeChanged(w, h, xOld, yOld);

        
        setViewWidth(w);
        setViewHeight(h);

    }
    
    public int getViewWidth() {
        return viewWidth;
    }
    
    public void setViewWidth(int viewWidth) {
        this.viewWidth = viewWidth;
    }
    
    public int getViewHeight() {
        return viewHeight;
    }
    
    public void setViewHeight(int viewHeight) {
        this.viewHeight = viewHeight;
    }
}
